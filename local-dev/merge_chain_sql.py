# -*- coding: utf-8 -*-
"""把链动模块的多个增量补丁合并成一份自包含、幂等的升级脚本。"""
import io
import os

BASE = 'D:/WorkBuddy/qianxu-java5.0/qianxu/sql/migration'
DST = 'D:/WorkBuddy/qianxu-java5.0/qianxu/sql/upgrade_20261011_chain_2plus1.sql'

ORDER = [
    'chain_2plus1_20261011.sql',
    'chain_2plus1_phase1_patch_20261011.sql',
    'chain_2plus1_phase2_20261011.sql',
    'chain_product_20261011.sql',
    'chain_help_unlock_20261011.sql',
    'chain_admin_slim_20261011.sql',
    'chain_menu_to_market_20261011.sql',
    'chain_product_see_ratio_20261011.sql',
    'chain_member_center_entry_20261011.sql',
    'chain_commission_type_normalize_20261011.sql',
    'chain_commission_status_align_20261011.sql',
    'chain_mark_wording_fix_20261011.sql',
    # 必须放在最后：统一收敛菜单（删废弃 + 去重 + 补齐），保证脚本可反复执行
    'chain_menu_finalize_20261011.sql',
]

HEADER = """-- ============================================================
-- 2+1 链动模式：完整增量升级脚本（自包含 / 幂等 / 可重复执行）
-- 生成日期：2026-10-11
--
-- 用途：已有库升级到「2+1 链动模式」最新形态。**不是全新建库脚本**。
-- 内容（按顺序）：
"""

for _i, _f in enumerate(ORDER, 1):
    HEADER += '--   %2d. %s\n' % (_i, _f)

HEADER += """--
-- 说明：
--   · 全部语句幂等（CREATE TABLE IF NOT EXISTS / 查 information_schema 后再 ALTER /
--     INSERT ... WHERE NOT EXISTS），可反复执行。
--   · 风控模块已按需求方决策整体移除（不建表、不建配置、不建菜单）。
--   · 执行后需刷新配置缓存并重新登录后台。
--   · 本脚本只覆盖链动模块，不替代 oneclick/02_patches_all.sql。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

"""


def main():
    parts = [HEADER]
    for name in ORDER:
        path = BASE + '/' + name
        text = io.open(path, encoding='utf-8').read()
        parts.append('\n-- ============================================================\n')
        parts.append('-- ===== BEGIN: %s =====\n' % name)
        parts.append('-- ============================================================\n')
        parts.append(text.rstrip('\n') + '\n')
        parts.append('-- ===== END: %s =====\n' % name)
    parts.append('\nSET FOREIGN_KEY_CHECKS = 1;\n')
    parts.append("SELECT 'CHAIN 2+1 deploy done' AS msg;\n")
    io.open(DST, 'w', encoding='utf-8', newline='\n').write(''.join(parts))
    print('written', DST, os.path.getsize(DST), 'bytes')


if __name__ == '__main__':
    main()
