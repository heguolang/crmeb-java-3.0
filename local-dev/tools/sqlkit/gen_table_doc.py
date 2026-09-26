# -*- coding: utf-8 -*-
"""由 local-dev/tmp/table_comments.tsv 生成数据库表清单文档。

分类规则：按表名前缀/精确名单归入业务模块，未命中归入「其它」。
新增表若未被规则覆盖会自动落到「其它」，不会丢表。
"""
import os

SRC = 'local-dev/tmp/table_comments.tsv'
OUT = 'local-dev/db_table_reference.md'

# 模块顺序 + 匹配规则（前缀 or 精确名）
MODULES = [
    ('系统设置与权限', ['eb_system_config', 'eb_system_menu', 'eb_system_role', 'eb_system_role_menu',
                   'eb_system_admin', 'eb_system_attachment', 'eb_system_notification', 'eb_system_city',
                   'eb_system_form_temp', 'eb_system_group', 'eb_system_group_data', 'eb_group_config']),
    ('会员与用户', ['eb_user', 'eb_user_address', 'eb_user_bill', 'eb_user_brokerage_record',
                'eb_user_experience_record', 'eb_user_extract', 'eb_user_group',
                'eb_user_integral_record', 'eb_user_level', 'eb_user_recharge', 'eb_user_sign',
                'eb_user_tag', 'eb_user_token', 'eb_user_visit_record', 'eb_user_distributor_level',
                'eb_user_distributor_level_stat']),
    ('等级体系（会员 / 团队 / 分销商 / 订货商）',
     ['eb_system_user_level', 'eb_system_user_level_brokerage', 'eb_level_stat_order_log',
      'eb_system_team_level', 'eb_system_team_level_config', 'eb_user_team_level',
      'eb_user_team_level_stat', 'eb_distributor_level']),
    ('商品', ['eb_store_product', 'eb_store_cart', 'eb_category']),
    ('订单与交易', ['eb_store_order', 'eb_store_pink', 'eb_store_combination']),
    ('营销活动', ['eb_store_coupon', 'eb_store_bargain', 'eb_store_seckill']),
    ('订货系统（微商逐级拿货）', ['eb_stock_', 'eb_stock_price', 'eb_stock_level']),
    ('区域代理', ['eb_agent']),
    ('内容与装修', ['eb_page_', 'eb_theme', 'eb_article', 'eb_activity_style']),
    ('微信与支付', ['eb_wechat_', 'eb_ali_pay_', 'eb_pay_component_', 'eb_template_message']),
    ('门店与配送', ['eb_system_store', 'eb_store_verify_record', 'eb_shipping_templates', 'eb_express']),
    ('短信', ['eb_sms_']),
    ('统计报表', ['eb_product_day_record', 'eb_shopping_product_day_record', 'eb_trading_day_record']),
    ('定时任务', ['qrtz_', 'eb_schedule_job']),
    ('日志与审计', ['eb_admin_login_log', 'eb_exception_log', 'eb_sensitive_method_log']),
    ('本地备份表（线上不存在）', ['bak_']),
]


def match(name):
    """返回模块名；未命中返回 None。

    优先级：精确名 > 最长前缀。
    只用「模块顺序 + startswith」会把 eb_user_team_level 错分到会员模块
    （'eb_user' 前缀先命中），所以必须让精确名和更长前缀优先。
    """
    if '_bak_' in name:
        return '本地备份表（线上不存在）'
    # 1) 精确名
    for mod, pats in MODULES:
        if name in pats:
            return mod
    # 2) 最长前缀
    best, best_len = None, -1
    for mod, pats in MODULES:
        for p in pats:
            if name.startswith(p) and len(p) > best_len:
                best, best_len = mod, len(p)
    return best


def main():
    rows = []
    for line in open(SRC, encoding='utf-8'):
        c = line.rstrip('\n').split('\t')
        if len(c) < 3:
            continue
        rows.append((c[0], c[1], c[2]))

    buckets = {}
    order = [m for m, _ in MODULES] + ['其它']
    for t, cmt, n in rows:
        buckets.setdefault(match(t) or '其它', []).append((t, cmt, n))

    out = []
    out.append('# QIANXU Java 3.0 数据库表清单')
    out.append('')
    out.append('> 库名 `qianxu`（线上 `crmeb_java3`），共 %d 张基础表，全部已带中文备注。' % len(rows))
    out.append('> 表备注由 `qianxu/sql/table_comments_20260923.sql` 幂等维护，已并入 `qianxu/sql/oneclick/02_patches_all.sql`。')
    out.append('')
    # 目录
    out.append('## 模块索引')
    out.append('')
    out.append('| 模块 | 表数 |')
    out.append('| --- | --- |')
    for m in order:
        if m in buckets:
            out.append('| %s | %d |' % (m, len(buckets[m])))
    out.append('| **合计** | **%d** |' % len(rows))
    out.append('')
    # 明细
    for m in order:
        if m not in buckets:
            continue
        out.append('## %s' % m)
        out.append('')
        out.append('| 表名 | 说明 |') 
        out.append('| --- | --- |')
        for t, cmt, n in sorted(buckets[m]):
            out.append('| `%s` | %s |' % (t, cmt.replace('|', '\\|') if cmt else ''))
        out.append('')

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, 'w', encoding='utf-8', newline='\n') as f:
        f.write('\n'.join(out) + '\n')
    print('写出', OUT, os.path.getsize(OUT), '字节')
    for m in order:
        if m in buckets:
            print('  %-30s %3d' % (m, len(buckets[m])))
    total = sum(len(v) for v in buckets.values())
    print('  合计 %d / 源 %d' % (total, len(rows)))


if __name__ == '__main__':
    main()
