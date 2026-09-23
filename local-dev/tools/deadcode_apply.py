#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
按 deadcode_scan.py 产出的 JSON 执行删除：
  * dead_types            -> 整文件删除
  * dead_private_members  -> 花括号配平后删除该成员（含其上的注解/javadoc）
  * 顺带清理因删除而变成「未使用」的 import

用法：
  python local-dev/tools/deadcode_apply.py /path/dead.json            # dry-run，只打印
  python local-dev/tools/deadcode_apply.py /path/dead.json --apply    # 真正删除
"""
import os
import re
import sys
import json

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
APPLY = '--apply' in sys.argv

ANN_LINE = re.compile(r'^[ \t]*@[\w.]+')


def find_decl_start(src, kw_pos):
    """从 private 关键字位置往前，吃掉紧邻的注解行、javadoc/行注释与空白"""
    line_start = src.rfind('\n', 0, kw_pos) + 1
    pos = line_start
    while True:
        # 上一条完整的行
        prev_end = pos - 1
        if prev_end <= 0:
            break
        prev_start = src.rfind('\n', 0, prev_end) + 1
        prev = src[prev_start:prev_end]
        s = prev.strip()
        if not s:
            pos = prev_start
            continue
        if ANN_LINE.match(prev):
            pos = prev_start
            continue
        if s.endswith('*/'):                      # javadoc / 块注释
            depth = 0
            i = prev_end
            while i > 0:
                i -= 1
                if src.startswith('*/', i):
                    depth += 1
                elif src.startswith('/*', i):
                    depth -= 1
                    if depth == 0:
                        break
            pos = src.rfind('\n', 0, i) + 1
            continue
        if s.startswith('//'):
            pos = prev_start
            continue
        break
    return pos


def field_span(src, kw_pos):
    """字段：从声明起点到第一个顶层分号"""
    start = find_decl_start(src, kw_pos)
    i, depth = kw_pos, 0
    while i < len(src):
        c = src[i]
        if c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
        elif c == ';' and depth == 0:
            end = i + 1
            while end < len(src) and src[end] in ' \t':
                end += 1
            if end < len(src) and src[end] == '\n':
                end += 1
            return start, end
        i += 1
    return None


def method_span(src, kw_pos):
    """方法：从声明起点到方法体的闭合花括号"""
    start = find_decl_start(src, kw_pos)
    i = kw_pos
    while i < len(src) and src[i] != '{':
        if src[i] == ';':                      # 抽象/接口方法
            return start, i + 1
        i += 1
    depth = 0
    while i < len(src):
        if src[i] == '{':
            depth += 1
        elif src[i] == '}':
            depth -= 1
            if depth == 0:
                end = i + 1
                while end < len(src) and src[end] in ' \t':
                    end += 1
                if end < len(src) and src[end] == '\n':
                    end += 1
                return start, end
        i += 1
    return None


def drop_unused_imports(src):
    """删掉因本次删除而不再被使用的 import（仅处理具名 import，* 跳过）"""
    out_lines, removed = [], []
    lines = src.split('\n')
    for ln in lines:
        m = re.match(r'\s*import\s+(static\s+)?([\w.]+)\s*;', ln)
        if m and not m.group(2).endswith('*'):
            simple = m.group(2).split('.')[-1]
            body = '\n'.join(l for l in lines if l is not ln)
            if not re.search(r'\b' + re.escape(simple) + r'\b', body):
                removed.append(simple)
                continue
        out_lines.append(ln)
    return '\n'.join(out_lines), removed


def main():
    data = json.load(open(sys.argv[1], encoding='utf-8'))

    print('### 1) 删除整个文件（%d 个）' % len(data['dead_types']))
    for r in data['dead_types']:
        p = os.path.join(ROOT, r['file'])
        print('  - %s' % r['file'])
        if APPLY and os.path.isfile(p):
            os.remove(p)
            # 若同名同目录还有关联文件（如 xml），提示人工确认
            xml = os.path.splitext(p)[0] + 'Mapper.xml'
            if os.path.isfile(xml):
                print('    ⚠ 存在同名 Mapper XML，需人工确认: %s' % xml)

    print('\n### 2) 删除 private 成员（%d 个）' % len(data['dead_private_members']))
    by_file = {}
    for r in data['dead_private_members']:
        by_file.setdefault(r['file'], []).append(r)

    total_removed = 0
    for rel, items in sorted(by_file.items()):
        p = os.path.join(ROOT, rel)
        if not os.path.isfile(p):
            continue
        src = open(p, 'r', encoding='utf-8').read()
        spans, desc = [], []
        for it in items:
            kind = it['sort']
            pat = (r'(?m)^[ \t]*private\s+(?:static\s+|final\s+|synchronized\s+|transient\s+|volatile\s+)*'
                   + (r'(?!class|interface|enum)' if kind == 'private method' else '')
                   + r'[\w<>\[\],.?\s]*?\b' + re.escape(it['member']) + r'\b')
            m = re.search(pat, src)
            if not m:
                print('  ! 未定位到 %s.%s' % (rel, it['member']))
                continue
            sp = method_span(src, m.start()) if kind == 'private method' else field_span(src, m.start())
            if not sp or sp[1] <= sp[0]:
                print('  ! 跨度解析失败 %s.%s' % (rel, it['member']))
                continue
            spans.append(sp)
            desc.append((kind, it['member'], src[sp[0]:sp[1]]))

        if not spans:
            continue
        spans.sort()
        for s, e in reversed(spans):
            src = src[:s] + src[e:]
        src, dropped = drop_unused_imports(src)
        total_removed += len(spans)
        print('  · %s  (删除 %d 处)' % (rel, len(spans)))
        for kind, name, snippet in desc:
            first = snippet.strip().split('\n')[0][:88]
            print('      [%s] %s  →  %s' % (kind.replace('private ', ''), name, first))
        if dropped:
            print('      顺带清理未用 import: %s' % ', '.join(dropped))
        if APPLY:
            with open(p, 'w', encoding='utf-8', newline='') as fh:
                fh.write(src)

    print('\n合计：类 %d 个文件，private 成员 %d 处。%s'
          % (len(data['dead_types']), total_removed,
             '已写入磁盘。' if APPLY else '这是 dry-run，未改动任何文件。'))


if __name__ == '__main__':
    main()
