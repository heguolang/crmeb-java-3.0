# -*- coding: utf-8 -*-
"""
由 02_patches_all.sql 自动抽取「结构类」DDL，生成线上结构增量脚本。

用法（在本目录下执行）：
    python build_schema_increment.py [输出文件名]

默认输出：03_schema_increment_<今天日期>.sql

抽取规则
--------
1. 按 `-- ===== BEGIN: xxx.sql =====` / `-- ===== END: xxx.sql =====` 切分节，保持原顺序
   （相对顺序变了可能撞外键）。
2. 只抓两类块：
   - 行首 `CREATE TABLE IF NOT EXISTS ...` 的整块（以行尾 `;` 收口）；
   - `SET @s = IF(` 起、到 `DEALLOCATE PREPARE` 止的块，且 SQL 文本里含
     ALTER TABLE / CREATE TABLE / CREATE INDEX。
   纯 UPDATE / INSERT 的幂等块会被排除 —— 菜单、权限、系统设置等数据类补丁不进来。
3. 按「空白归一化后的文本」去重。
4. 头部补 SET FOREIGN_KEY_CHECKS = 0，尾部恢复 1，保证建表顺序不受外键约束影响。

验证方式：本机库连跑两遍，`mysqldump --no-data` 前后 diff 应无差异。
"""
import datetime
import io
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, '02_patches_all.sql')

DDL_HINT = ('ALTER TABLE', 'CREATE TABLE', 'CREATE INDEX')

HEADER = u"""-- +----------------------------------------------------------------------
-- | CRMEB Java 3.0 —— 订货 / 订货商 / 门店 / 区域代理 模块「结构增量」脚本
-- +----------------------------------------------------------------------
-- 用途：线上已有库（已存在业务数据）只补「表结构 / 字段」，不清数据、不导设置项。
-- 内容：本模块自研的全部自建表 + 在原版表上新增的字段。
-- 幂等：可重复执行。建表用 CREATE TABLE IF NOT EXISTS，加字段先查 information_schema。
-- 说明：菜单/权限/系统设置等「数据类」补丁不在此脚本内，需要时用 02_patches_all.sql。
-- 生成：由 build_schema_increment.py 从 02_patches_all.sql 自动抽取，生成日期 %(date)s。
-- 执行：mysql -uroot -p密码 --default-character-set=utf8mb4 库名 < %(name)s
-- +----------------------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();

"""

FOOTER = u"""
-- ============================================================
-- 执行结果自检
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
SELECT '结构增量脚本执行完成' AS result;
"""


def load_sections(lines):
    """返回 [(start_idx, end_idx, section_name)]，保持文件中的先后顺序。"""
    sections = []
    cur = None
    for i, ln in enumerate(lines):
        m = re.match(r'^-- ={5,} BEGIN: (.+?) ={5,}\s*$', ln)
        if m:
            cur = [i, None, m.group(1).strip()]
            sections.append(cur)
            continue
        m = re.match(r'^-- ={5,} END: (.+?) ={5,}\s*$', ln)
        if m and cur is not None and cur[1] is None:
            cur[1] = i
            cur = None
    for s in sections:
        if s[1] is None:
            s[1] = len(lines) - 1
    return [(a, b, n) for a, b, n in sections]


def extract_blocks(lines, sections):
    """从每个分节里抽出结构类 DDL 块，返回 [(section_name, block_text)]。"""
    out = []
    for start, end, name in sections:
        seg = lines[start:end + 1]
        idx, n = 0, len(seg)
        while idx < n:
            ln = seg[idx]

            # 整块 CREATE TABLE IF NOT EXISTS
            if re.match(r'^\s*CREATE TABLE IF NOT EXISTS', ln, re.I):
                buf = []
                while idx < n:
                    buf.append(seg[idx])
                    if seg[idx].rstrip().endswith(';'):
                        break
                    idx += 1
                out.append((name, '\n'.join(buf).strip()))
                idx += 1
                continue

            # SET @s = IF(...); PREPARE ... EXECUTE ... DEALLOCATE PREPARE stmt;
            if re.match(r'^\s*SET @s\s*=', ln, re.I):
                buf = [seg[idx]]
                idx += 1
                guard = 0
                while idx < n and guard < 60:
                    buf.append(seg[idx])
                    if re.search(r'DEALLOCATE\s+PREPARE', seg[idx], re.I):
                        break
                    idx += 1
                    guard += 1
                block = '\n'.join(buf)
                if any(h in block.upper() for h in DDL_HINT):
                    out.append((name, block.strip()))
                idx += 1
                continue

            idx += 1
    return out


def dedupe(blocks):
    seen, uniq = set(), []
    for name, blk in blocks:
        key = re.sub(r'\s+', ' ', blk).strip()
        if key in seen:
            continue
        seen.add(key)
        uniq.append((name, blk))
    return uniq


def main():
    out_name = sys.argv[1] if len(sys.argv) > 1 else \
        '03_schema_increment_%s.sql' % datetime.date.today().strftime('%Y%m%d')

    if not os.path.exists(SRC):
        sys.stderr.write(u'找不到 %s\n' % SRC)
        return 1

    text = io.open(SRC, encoding='utf-8').read()
    lines = text.split('\n')

    sections = load_sections(lines)
    uniq = dedupe(extract_blocks(lines, sections))

    parts = [HEADER % {'date': datetime.date.today().strftime('%Y-%m-%d'), 'name': out_name}]
    last = None
    for name, blk in uniq:
        if name != last:
            parts.append(u'\n-- ============================================================\n'
                         u'-- 来源分节：%s\n'
                         u'-- ============================================================\n' % name)
            last = name
        parts.append(blk + '\n')
    parts.append(FOOTER)

    dst = os.path.join(HERE, out_name)
    io.open(dst, 'w', encoding='utf-8').write('\n'.join(parts))

    tables = len([1 for _, b in uniq if b.lstrip().upper().startswith('CREATE TABLE')])
    alters = sum(b.upper().count('ALTER TABLE') for _, b in uniq)
    print(u'分节数: %d' % len(sections))
    print(u'DDL 块数(去重后): %d   建表: %d   字段变更: %d' % (len(uniq), tables, alters))
    print(u'输出: %s' % dst)
    return 0


if __name__ == '__main__':
    sys.exit(main())
