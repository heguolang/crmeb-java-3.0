#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
前端死代码扫描（admin/src 与 app/）：从入口出发做 import 图可达性分析。

保守策略（宁可漏报，不可误删）
  1) 入口 = main.js / permission.js / 全局注册文件；用 BFS 走 import/require/dynamic import。
  2) 解析不了的特殊写法（require.context、字符串拼接路径、模板字符串）→ 若命中，
     则把该目录下全部文件标记为「疑似动态引用」，一律不报。
  3) 报出来的文件还要再过一道文本兜底：basename 在别处出现过就不报。

用法： python local-dev/tools/deadcode_scan_front.py <src_root>
"""
import os
import re
import sys
from collections import deque

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
EXTS = ['.js', '.vue', '.ts', '.json']

IMP_RE = re.compile(
    r"""(?:import\s+(?:[\w*{},\s]+\s+from\s+)?|import\(|require\(|from\s+)"""
    r"""['"]([^'"]+)['"]""")
CTX_RE = re.compile(r'require\.context\(\s*[\'"]([^\'"]+)[\'"]')
TEMPLATE_IMP_RE = re.compile(r'''(?:import\(|require\()\s*[`'"]\s*[^`'"]*\$\{''')


def collect(root):
    out = []
    for base, dirs, files in os.walk(root):
        dirs[:] = [d for d in dirs
                   if d not in {'node_modules', 'dist', 'unpackage', '.git', 'build'}]
        for f in files:
            if os.path.splitext(f)[1] in EXTS:
                out.append(os.path.join(base, f))
    return out


def resolve(spec, cur, root):
    """把 import 说明符解析成真实文件路径"""
    if spec.startswith('@/'):
        base = os.path.join(root, spec[2:])
    elif spec.startswith('.'):
        base = os.path.normpath(os.path.join(os.path.dirname(cur), spec))
    else:
        return None                      # 第三方包，忽略
    cands = [base] + [base + e for e in EXTS] + \
            [os.path.join(base, 'index' + e) for e in EXTS]
    for c in cands:
        if os.path.isfile(c):
            return os.path.normpath(c)
    return None


def main():
    root = sys.argv[1]
    root = os.path.normpath(os.path.join(ROOT, root))
    files = collect(root)
    if not files:
        print('no files under %s' % root)
        return
    text = {}
    for f in files:
        with open(f, 'r', encoding='utf-8', errors='replace') as fh:
            text[f] = fh.read()

    # 动态引用写法的兜底
    dyn_dirs = set()
    for f, t in text.items():
        for m in CTX_RE.finditer(t):
            dyn_dirs.add(os.path.normpath(os.path.join(os.path.dirname(f), m.group(1))))
        if TEMPLATE_IMP_RE.search(t):
            dyn_dirs.add(os.path.dirname(f))
    if dyn_dirs:
        print('# ⚠ 检测到动态引用写法，以下目录内文件一律不判为死代码:')
        for d in sorted(dyn_dirs):
            print('   ', os.path.relpath(d, ROOT).replace('\\', '/'))

    def in_dyn(p):
        return any(os.path.normpath(p).startswith(d + os.sep) for d in dyn_dirs)

    graph = {}
    for f, t in text.items():
        deps = set()
        for spec in IMP_RE.findall(t):
            r = resolve(spec, f, root)
            if r:
                deps.add(r)
        graph[f] = deps

    entries = [f for f in files if os.path.basename(f) in ('main.js', 'permission.js')]
    seen, q = set(entries), deque(entries)
    while q:
        cur = q.popleft()
        for nxt in graph.get(cur, ()):
            if nxt not in seen:
                seen.add(nxt)
                q.append(nxt)

    # 文本兜底：basename 或去扩展名的相对路径在别处出现过就认为可能被动态引用
    base_index = {}
    for f in files:
        stem = os.path.splitext(os.path.basename(f))[0]
        base_index.setdefault(stem, []).append(f)

    def textually_referenced(f):
        stem = os.path.splitext(os.path.basename(f))[0]
        if len(base_index.get(stem, [])) > 1:
            return True                                  # 同名文件，不敢判
        if len(stem) < 4:
            return True                                  # 名字太短，怕误判
        pat = re.compile(r'[\'"`/]' + re.escape(stem) + r'[\'"/.]')
        for g, t in text.items():
            if g != f and pat.search(t):
                return True
        return False

    dead = []
    for f in files:
        if f in seen or in_dyn(f):
            continue
        if textually_referenced(f):
            continue
        dead.append(os.path.relpath(f, ROOT).replace('\\', '/'))

    print('\n== 入口: %s' % ', '.join(os.path.relpath(e, ROOT) for e in entries))
    print('== 文件总数 %d，可达 %d' % (len(files), len(seen)))
    print('== 不可达（可删候选）: %d ==' % len(dead))
    for d in sorted(dead):
        print('  ' + d)


if __name__ == '__main__':
    main()
