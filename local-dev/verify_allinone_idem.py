# -*- coding: utf-8 -*-
"""ALL_IN_ONE 整包幂等验证：在已建好的库上**再跑一遍整个脚本**，比对前后是否零变化。

覆盖三个层面：
  ① 结构计数（表 / 列 / 索引）
  ② 关键表的全表内容 md5（菜单、配置、角色授权、组合数据、主题、等级）
  ③ 结构定义集合 md5（列定义、索引定义、行格式）
用法: python local-dev/verify_allinone_idem.py
"""
import hashlib
import subprocess
import sys
import time

MYSQL = r"D:\env\mysql-8.0.29-winx64\bin\mysql.exe"
DB = "qianxu_java3"
SQL = r"D:\qianxu-java-3.0\qianxu\sql\oneclick\ALL_IN_ONE.sql"
LOG = r"D:\qianxu-java-3.0\local-dev\_idem_import.log"

# 内容快照表（含 id、按 id 排序，保证可比）
CONTENT_TABLES = [
    "eb_system_menu", "eb_system_config", "eb_system_role_menu",
    "eb_system_group", "eb_system_group_data", "eb_theme",
    "eb_user_level", "eb_stock_level", "eb_system_admin",
    "eb_store_product_group", "eb_distributor_level",
]


def q(sql, db=DB):
    p = subprocess.run([MYSQL, "-uroot", "-p123456", "-N", "-B",
                        "--default-character-set=utf8mb4", db, "-e", sql],
                       capture_output=True)
    err = p.stderr.decode("utf-8", "replace")
    if "ERROR" in err:
        return "!!ERROR: " + err.strip()[:200]
    return p.stdout.decode("utf-8", "replace")


def md5(s):
    return hashlib.md5(s.encode("utf-8")).hexdigest()[:16]


def snapshot():
    snap = {}
    snap["表数"] = q("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%s';" % DB).strip()
    snap["列数"] = q("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='%s';" % DB).strip()
    snap["索引数"] = q("SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='%s';" % DB).strip()
    snap["列定义md5"] = md5(q("SELECT CONCAT_WS('|',TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,"
                              "IFNULL(COLUMN_DEFAULT,'~'),IFNULL(COLUMN_COMMENT,'')) "
                              "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='%s' "
                              "ORDER BY TABLE_NAME,COLUMN_NAME;" % DB))
    snap["索引定义md5"] = md5(q("SELECT CONCAT_WS('|',TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,"
                               "IFNULL(COLUMN_NAME,'~')) FROM information_schema.STATISTICS "
                               "WHERE TABLE_SCHEMA='%s' ORDER BY TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX;" % DB))
    snap["行格式md5"] = md5(q("SELECT CONCAT_WS('|',table_name,ROW_FORMAT) FROM information_schema.tables "
                             "WHERE table_schema='%s' ORDER BY table_name;" % DB))
    for t in CONTENT_TABLES:
        # ⚠️ 必须把值里的 CR/LF/TAB 转义掉再用：eb_system_config.value 里含 \n，
        #    未转义的 SELECT * 输出会拆行，使同内容的 md5 不稳定（踩过，误报"非幂等"）。
        cols = q("SELECT column_name FROM information_schema.columns WHERE table_schema='%s' "
                 "AND table_name='%s' ORDER BY ordinal_position;" % (DB, t)).split()
        sel = ", ".join("REPLACE(REPLACE(REPLACE(IFNULL(`%s`,'~NULL~'), '\\r','~R~'), "
                        "'\\n','~N~'), '\\t','~T~')" % c for c in cols)
        rows = q("SELECT CONCAT_WS('#~#', %s) FROM `%s` ORDER BY 1;" % (sel, t))
        n = q("SELECT COUNT(*) FROM `%s`;" % t).strip()
        snap[t] = "%s 行 / md5=%s" % (n, md5(rows))
    return snap


def run_import():
    t0 = time.time()
    with open(SQL, "rb") as f, open(LOG, "wb") as log:
        p = subprocess.run([MYSQL, "-uroot", "-p123456", "--default-character-set=utf8mb4"],
                           stdin=f, stdout=log, stderr=subprocess.STDOUT)
    txt = open(LOG, encoding="utf-8", errors="replace").read()
    return p.returncode, txt.count("deploy done"), ("ERROR" in txt), time.time() - t0


def main():
    print("=" * 74)
    print("① 跑前快照")
    s1 = snapshot()
    for k, v in s1.items():
        print("   %-24s %s" % (k, v))

    print("-" * 74)
    print("② 在同一库上重跑整个 ALL_IN_ONE.sql ...")
    rc, done, haserr, sec = run_import()
    print("   退出码=%s  完成标记=%s  ERROR=%s  耗时=%.1fs" % (rc, done, haserr, sec))

    print("-" * 74)
    print("③ 跑后快照与比对")
    s2 = snapshot()
    diffs = [(k, s1[k], s2[k]) for k in s1 if s1[k] != s2[k]]
    for k, v in s2.items():
        flag = "  <-- 变了" if s1[k] != v else ""
        print("   %-24s %s%s" % (k, v, flag))
    print("-" * 74)
    if diffs:
        print("❌ 幂等验证失败，%d 项发生变化：" % len(diffs))
        for k, a, b in diffs:
            print("   %s\n     前: %s\n     后: %s" % (k, a, b))
        return 1
    if rc != 0 or done != 1 or haserr:
        print("❌ 第二遍执行本身不干净（退出码/完成标记/ERROR 有异常）")
        return 1
    print("✅ 幂等验证通过：第二遍整包执行后，结构 + 11 张关键表内容全部零变化")
    return 0


if __name__ == "__main__":
    sys.exit(main())
