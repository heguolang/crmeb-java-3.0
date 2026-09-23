# -*- coding: utf-8 -*-
"""扫描 ALL_IN_ONE.sql 里所有「非幂等」写模式，并给出对应 02 的写法对照。

判据：段内出现 `DELETE FROM <表>` 且同段内存在针对同表的 INSERT —— 即 DELETE+INSERT 重建模式。
这类写法重复执行会：① 自增 id 漂移 ② create_time 被刷新 ③ 既有引用/授权变孤儿。
"""
import re
import sys

ROOT = r"D:\crmeb-java-3.0\crmeb\sql\oneclick"
SEG_RE = re.compile(r"-- =+ BEGIN: (?P<name>\S+\.sql) =+(?P<body>.*?)-- =+ END: (?P=name) =+", re.S)


def load(p):
    return open(p, encoding="utf-8").read()


def segs(text):
    return [(m.group("name"), m.group("body"), text[:m.start()].count("\n") + 1)
            for m in SEG_RE.finditer(text)]


def deletions(body):
    """返回 [(表名, 行号偏移, 语句)]"""
    out = []
    for i, ln in enumerate(body.splitlines()):
        s = ln.strip()
        m = re.match(r"(?i)DELETE\s+(?:[a-z0-9_]+\.)?(?:FROM\s+)?`?(\w+)`?", s)
        if m:
            out.append((m.group(1), i, s[:120]))
    return out


def main():
    a = load(ROOT + r"\ALL_IN_ONE.sql")
    p2 = load(ROOT + r"\02_patches_all.sql")
    s2 = {n: b for n, b, _ in segs(p2)}
    s2del = {n: deletions(b) for n, b in s2.items()}

    print("=" * 78)
    print("ALL_IN_ONE.sql 中「含 DELETE」的段")
    print("=" * 78)
    hits = 0
    for name, body, line in segs(a):
        dels = deletions(body)
        if not dels:
            continue
        tables = sorted(set(t for t, _, _ in dels))
        print("\n段 %s  (文件第 %d 行起)  DELETE 语句 %d 条，涉及表: %s"
              % (name, line, len(dels), ", ".join(tables)))
        for t, off, stmt in dels[:6]:
            print("     %s" % stmt)
        d2 = s2del.get(name)
        if d2 is None:
            print("     → 02 里没有同名段")
        else:
            t2 = sorted(set(t for t, _, _ in d2))
            print("     → 02 同名段 DELETE 语句 %d 条，涉及表: %s" % (len(d2), ", ".join(t2) or "无"))
            only_a = set(tables) - set(t2)
            if only_a:
                print("     ⚠️ ALL_IN_ONE 比 02 多删的表（可疑）: %s" % ", ".join(sorted(only_a)))
        hits += 1
    print("\n" + "=" * 78)
    print("共 %d 个段含 DELETE 语句" % hits)
    return 0


if __name__ == "__main__":
    sys.exit(main())
