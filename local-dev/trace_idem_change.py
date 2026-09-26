# -*- coding: utf-8 -*-
"""列级追踪：把指定表快照成临时表 → 跑一遍 ALL_IN_ONE → 用 SQL 逐列比对，输出变化的行与列。

用法: python local-dev/trace_idem_change.py
"""
import subprocess
import sys
import time

MYSQL = r"D:\env\mysql-8.0.29-winx64\bin\mysql.exe"
DB = "qianxu_java3"
SQL = r"D:\qianxu-java-3.0\qianxu\sql\oneclick\ALL_IN_ONE.sql"
LOG = r"D:\qianxu-java-3.0\local-dev\_trace_import.log"
TABLES = ["eb_system_menu", "eb_system_config", "eb_system_admin"]


def q(sql):
    p = subprocess.run([MYSQL, "-uroot", "-p123456", "-N", "-B",
                        "--default-character-set=utf8mb4", DB, "-e", sql], capture_output=True)
    err = p.stderr.decode("utf-8", "replace")
    if "ERROR" in err:
        raise RuntimeError(err.strip()[:400])
    return p.stdout.decode("utf-8", "replace")


def run_import():
    t0 = time.time()
    with open(SQL, "rb") as f, open(LOG, "wb") as log:
        p = subprocess.run([MYSQL, "-uroot", "-p123456", "--default-character-set=utf8mb4"],
                           stdin=f, stdout=log, stderr=subprocess.STDOUT)
    txt = open(LOG, encoding="utf-8", errors="replace").read()
    return p.returncode, txt.count("deploy done"), ("ERROR" in txt), time.time() - t0


def show(t):
    cols = q("SELECT column_name FROM information_schema.columns WHERE table_schema='%s' "
             "AND table_name='%s' ORDER BY ordinal_position;" % (DB, t)).split()
    key = cols[0]
    parts = ", ".join(
        "IF(NOT(a.`%s` <=> b.`%s`), CONCAT('%s: ', IFNULL(a.`%s`,'<NULL>'), ' -> ', "
        "IFNULL(b.`%s`,'<NULL>')), NULL)" % (c, c, c, c, c)
        for c in cols[1:])
    sql = ("SELECT a.`%s`, CONCAT_WS(' | ', %s) AS diff FROM `snap_%s` a "
           "JOIN `%s` b ON a.`%s` = b.`%s` WHERE NOT (CONCAT_WS('~', %s) <=> CONCAT_WS('~', %s));"
           % (key, parts, t, t, key, key,
              ", ".join("a.`%s`" % c for c in cols),
              ", ".join("b.`%s`" % c for c in cols)))
    rows = q(sql).strip()
    n = q("SELECT COUNT(*) FROM `snap_%s` a JOIN `%s` b ON a.`%s`=b.`%s`;" % (t, t, key, key)).strip()
    print("\n表 %s（比较 %s 行）" % (t, n))
    if not rows:
        print("   ✅ 零变化")
        return 0
    lines = rows.splitlines()
    print("   ❌ %d 行发生变化：" % len(lines))
    for ln in lines[:12]:
        print("   [%s] %s" % tuple(ln.split("\t", 1)) if "\t" in ln else "   " + ln)
    return len(lines)


def main():
    for t in TABLES:
        q("DROP TABLE IF EXISTS `snap_%s`;" % t)
        q("CREATE TABLE `snap_%s` AS SELECT * FROM `%s`;" % (t, t))
    print("已建快照表: %s" % ", ".join("snap_" + t for t in TABLES))

    rc, done, err, sec = run_import()
    print("重跑完成: 退出码=%s 完成标记=%s ERROR=%s 耗时=%.1fs" % (rc, done, err, sec))

    total = 0
    for t in TABLES:
        total += show(t)

    for t in TABLES:
        q("DROP TABLE IF EXISTS `snap_%s`;" % t)
    print("\n快照表已清理。")
    print("=" * 70)
    print("结论：%s" % ("严格幂等 ✓（零行零列变化）" if total == 0 else "存在 %d 行变化，见上" % total))
    return 0 if total == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
