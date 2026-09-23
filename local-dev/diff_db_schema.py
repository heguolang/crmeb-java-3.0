# -*- coding: utf-8 -*-
"""对 ALL_IN_ONE 建出的库与基线库（本地 crmeb = 线上快照）做结构级 diff。

四级：
  ① 表清单
  ② 列定义（名/类型/可空/默认/注释）
  ③ 索引定义
  ④ 关键表数据条数（菜单等）
用法: python local-dev/diff_db_schema.py <目标库> [基线库]
"""
import subprocess
import sys

MYSQL = r"D:\env\mysql-8.0.29-winx64\bin\mysql.exe"


def q(db, sql):
    p = subprocess.run([MYSQL, "-uroot", "-p123456", "-N", "-B",
                        "--default-character-set=utf8mb4", db, "-e", sql],
                       capture_output=True)
    err = p.stderr.decode("utf-8", "replace")
    assert "ERROR" not in err, err
    return p.stdout.decode("utf-8", "replace")


def tables(db):
    return set(x for x in q(db, "SELECT table_name FROM information_schema.tables "
                                "WHERE table_schema='%s' AND table_type='BASE TABLE';" % db).split())


def cols(db):
    return set(q(db, "SELECT CONCAT_WS('\t', TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, "
                        "IS_NULLABLE, IFNULL(COLUMN_DEFAULT,'<null>'), IFNULL(COLUMN_COMMENT,'')) "
                        "FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='%s';" % db).splitlines())


def idx(db):
    return set(q(db, "SELECT CONCAT_WS('\t', TABLE_NAME, INDEX_NAME, NON_UNIQUE, SEQ_IN_INDEX, "
                        "IFNULL(COLUMN_NAME,'<expr>')) "
                        "FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='%s';" % db).splitlines())


def counts(db, tbls):
    rows = []
    for t in sorted(tbls):
        n = q(db, "SELECT COUNT(*) FROM `%s`;" % t).strip()
        rows.append("%s\t%s" % (t, n))
    return rows


def main():
    tgt = sys.argv[1] if len(sys.argv) > 1 else "crmeb_java3"
    base = sys.argv[2] if len(sys.argv) > 2 else "crmeb"

    tb_t, tb_b = tables(tgt), tables(base)
    print("=" * 74)
    print("① 表清单：  目标 %s = %d 张   基线 %s = %d 张" % (tgt, len(tb_t), base, len(tb_b)))
    only_b = sorted(tb_b - tb_t)
    only_t = sorted(tb_t - tb_b)
    print("   基线有、目标缺 (%d): %s" % (len(only_b), only_b or "无"))
    print("   目标有、基线缺 (%d): %s" % (len(only_t), only_t or "无"))

    print("-" * 74)
    c_t, c_b = cols(tgt), cols(base)
    d1 = sorted(c_b - c_t)
    d2 = sorted(c_t - c_b)
    print("② 列定义：  基线有目标缺 %d 条 / 目标有基线缺 %d 条" % (len(d1), len(d2)))
    if d1:
        from collections import defaultdict
        g = defaultdict(list)
        for x in d1:
            p = x.split("\t")
            g[p[0]].append(".".join(p[1:3]))
        for k in sorted(g):
            print("   [缺列] %-42s %s" % (k, ", ".join(g[k])))
    if d2:
        for x in d2[:20]:
            print("   [多列] %s" % x.replace("\t", " | "))

    print("-" * 74)
    i_t, i_b = idx(tgt), idx(base)
    di = sorted(i_b - i_t)
    print("③ 索引：    基线有目标缺 %d 条" % len(di))
    from collections import defaultdict
    g = defaultdict(list)
    for x in di:
        p = x.split("\t")
        if len(p) < 5:
            print("   [缺索引] 解析异常行: %r" % x)
            continue
        g[p[0]].append("%s(%s)" % (p[1], p[4]))
    for k in sorted(g):
        print("   [缺索引] %-40s %s" % (k, ", ".join(g[k])))

    print("-" * 74)
    rf_t = set(q(tgt, "SELECT CONCAT(table_name,'=',ROW_FORMAT) FROM information_schema.tables "
                      "WHERE table_schema='%s';" % tgt).splitlines())
    rf_b = set(q(base, "SELECT CONCAT(table_name,'=',ROW_FORMAT) FROM information_schema.tables "
                       "WHERE table_schema='%s';" % base).splitlines())
    rfd = sorted(rf_b - rf_t)
    print("⑤ 行格式：  基线有目标缺 %d 条" % len(rfd))
    for x in rfd:
        print("   [行格式] %s" % x)
    if not rfd:
        nd = sorted(x for x in rf_b if not x.endswith("=Dynamic"))
        print("   （全部一致；基线非 Dynamic 的 %d 张：%s）"
              % (len(nd), ", ".join(x.split("=")[0] for x in nd)))

    print("-" * 74)
    common = sorted(tb_t & tb_b)
    ct, cb = counts(tgt, common), counts(base, common)
    diff = [(a, b) for a, b in zip(ct, cb) if a != b]
    print("④ 关键表行数：共有表 %d 张，行数不一致 %d 张" % (len(common), len(diff)))
    for a, b in diff:
        ta, na = a.split("\t")
        _, nb = b.split("\t")
        print("   %-42s 目标=%-7s 基线=%s" % (ta, na, nb))
    print("=" * 74)


if __name__ == "__main__":
    main()
