#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
重复逻辑检测：找出「方法体规范化后完全一致」且体量够大的方法（跨文件/同文件）。

规范化 = 去掉注释、字符串字面量、所有空白。
只报告体量 >= MIN_LINES 行的，避免 getter/setter、单行委托这类天然相似的方法刷屏。

用法： python local-dev/tools/deadcode_scan_dup.py
"""
import os
import re
import hashlib
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MIN_LINES = 8
MIN_CHARS = 160
SKIP_DIR = {'target', 'node_modules', '.git', 'dist', 'build'}

METH_RE = re.compile(
    r'(?m)^[ \t]*(?:public|protected|private)\s+(?:static\s+|final\s+|synchronized\s+)*'
    r'[\w<>\[\],.?\s]+?\s+(\w+)\s*\([^)]*\)\s*(?:throws\s[\w,\s.]+)?\{')


def strip_comments_strings(src):
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n':
                i += 1
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i + 1] == '/'):
                i += 1
            i += 2
        elif c in '"\'':
            # ⚠ 字符串字面量必须保留内容：两个方法可能只差在字面量上（业务逻辑不同），
            #   若一并剥离会产生误报（实测 ProductUtils.getTaobaoProductInfo/getTmallProductInfo 即如此）
            q, buf = c, [c]
            i += 1
            while i < n and src[i] != q:
                buf.append(src[i])
                i += 2 if src[i] == '\\' else 1
            buf.append(q)
            i += 1
            out.append(''.join(buf))
        else:
            out.append(c)
            i += 1
    return ''.join(out)


def main():
    java = []
    for base, dirs, files in os.walk(ROOT):
        dirs[:] = [d for d in dirs if d not in SKIP_DIR]
        for f in files:
            if f.endswith('.java'):
                java.append(os.path.join(base, f))

    groups = defaultdict(list)
    for p in java:
        src = strip_comments_strings(open(p, 'r', encoding='utf-8', errors='replace').read())
        rel = os.path.relpath(p, ROOT).replace('\\', '/')
        for m in METH_RE.finditer(src):
            i = src.find('{', m.end() - 1)
            depth, start = 0, i
            while i < len(src):
                if src[i] == '{':
                    depth += 1
                elif src[i] == '}':
                    depth -= 1
                    if depth == 0:
                        break
                i += 1
            body = src[start:i + 1]
            norm = re.sub(r'\s+', '', body)
            if norm.count('\n') == 0:
                pass
            lines = body.count('\n')
            if lines < MIN_LINES or len(norm) < MIN_CHARS:
                continue
            if m.group(1) in ('equals', 'hashCode', 'toString', 'main'):
                continue
            h = hashlib.md5(norm.encode()).hexdigest()
            groups[h].append((rel, m.group(1), lines))

    dup = {h: v for h, v in groups.items() if len(v) > 1}
    print('== 疑似重复逻辑（方法体完全一致，>=%d 行）: %d 组 ==' % (MIN_LINES, len(dup)))
    total = 0
    for h, v in sorted(dup.items(), key=lambda kv: -kv[1][0][2]):
        total += len(v) - 1
        print('  ▸ %d 处，约 %d 行：' % (len(v), v[0][2]))
        for rel, name, lines in v:
            print('      %s  →  %s()' % (rel, name))
    print('\n可归并冗余份数：%d' % total)


if __name__ == '__main__':
    main()
