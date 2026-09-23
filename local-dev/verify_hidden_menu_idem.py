# -*- coding: utf-8 -*-
"""验证 ALL_IN_ONE.sql 的 hidden_menu 段幂等性。

口径：把该段从脚本里原样抽出来，在临时库 crmeb_java3 上连跑两次，
每次前后对 eb_system_menu 全表做快照（md5），要求 diff 恒为 0；
同时检查 hidden 两行的 id 是否漂移、eb_system_role_menu 是否新增孤儿。
"""
import hashlib
import re
import subprocess
import sys

MYSQL = r"D:\env\mysql-8.0.29-winx64\bin\mysql.exe"
DB = "crmeb_java3"
SQL_FILE = r"D:\crmeb-java-3.0\crmeb\sql\oneclick\ALL_IN_ONE.sql"
TMP = r"D:\crmeb-java-3.0\local-dev\_tmp_hidden_menu.sql"


def run(sql=None, stdin_file=None):
    cmd = [MYSQL, "-uroot", "-p123456", "-N", "-B",
           "--default-character-set=utf8mb4", DB]
    if sql is not None:
        cmd += ["-e", sql]
    p = subprocess.run(cmd, capture_output=True,
                       stdin=open(stdin_file, "rb") if stdin_file else None)
    out = p.stdout.decode("utf-8", "replace")
    err = p.stderr.decode("utf-8", "replace")
    err = "\n".join(l for l in err.splitlines() if "Using a password" not in l)
    return p.returncode, out, err


SNAP_SQL = ("SELECT id,pid,name,icon,perms,component,menu_type,sort,is_show,is_delte "
            "FROM eb_system_menu ORDER BY id;")


def snapshot():
    rc, out, err = run(SNAP_SQL)
    assert rc == 0, err
    h = hashlib.md5(out.encode("utf-8")).hexdigest()
    return h, out.count("\n") + (0 if out.endswith("\n") else 1)


def hidden_rows():
    rc, out, err = run("SELECT id,pid,name,icon,sort FROM eb_system_menu "
                       "WHERE component IN ('/hidden','/hidden/panel') ORDER BY id;")
    return out.strip()


def orphans():
    rc, out, err = run("SELECT COUNT(*) FROM eb_system_role_menu rm "
                       "WHERE NOT EXISTS (SELECT 1 FROM eb_system_menu m WHERE m.id=rm.menu_id);")
    return out.strip()


def main():
    src = open(SQL_FILE, encoding="utf-8").read()
    m = re.search(r"(-- =+ BEGIN: hidden_menu\.sql =+.*?-- =+ END: hidden_menu\.sql =+)",
                  src, re.S)
    if not m:
        print("!! 未找到 hidden_menu 段")
        return 1
    seg = m.group(1)
    open(TMP, "w", encoding="utf-8", newline="\n").write(seg + "\n")
    print("段长度 %d 字符，已抽出到 %s" % (len(seg), TMP))
    print("段内是否含 DELETE: %s" % ("DELETE" in seg.upper()))
    print("段内是否含 LAST_INSERT_ID: %s" % ("LAST_INSERT_ID" in seg.upper()))
    print("-" * 62)

    h0, n0 = snapshot()
    print("初始      菜单 md5=%s 行数=%d  hidden_id=[%s]  孤儿=%s"
          % (h0[:12], n0, hidden_rows().replace("\n", " | "), orphans()))

    ok = True
    for i in (1, 2):
        rc, out, err = run(stdin_file=TMP)
        h, n = snapshot()
        same = (h == h0 and n == n0)
        ok = ok and same
        print("第 %d 次跑   菜单 md5=%s 行数=%d  diff=%s  hidden_id=[%s]  孤儿=%s"
              % (i, h[:12], n, "0 行 ✓" if same else "有变化 ✗",
                 hidden_rows().replace("\n", " | "), orphans()))
        if err.strip():
            print("  stderr: %s" % err.strip()[:300])

    print("-" * 62)
    print("幂等结论：%s" % ("通过（连跑两次全表零变化）" if ok else "失败"))
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
