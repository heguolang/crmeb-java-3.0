# -*- coding: utf-8 -*-
"""
生成 crmeb/sql/table_comments_20260923.sql —— 表备注补全/修正脚本。

取值优先级：
  1) OVERRIDE        人工确认的修正值（错注释 / 乱码 / 英文注释）
  2) 库中现有中文备注 非空、不含 '?'、含中文 → 保留
  3) Java 实体 @ApiModel(description)   —— 以代码为准
  4) MANUAL          人工补充（备份表 / Quartz 表 / 实体无描述的）

生成的 SQL 采用「条件化 PREPARE/EXECUTE」：
  · 表不存在 → 空操作（本地开发备份表不会让线上部署报错）
  · 备注已一致 → 空操作（幂等，连跑多遍结果不变）
"""
import os, re, sys

ROOT = r'D:\crmeb-java-3.0'
TMP = os.path.join(ROOT, 'local-dev', 'tmp')
OUT = os.path.join(ROOT, 'crmeb', 'sql', 'table_comments_20260923.sql')

# ---- 人工修正：库中注释写错 / 乱码 / 英文 ----
OVERRIDE = {
    'eb_ali_pay_info':      '支付宝订单表',
    'eb_agent_change_log':  '区域代理变更记录',
    'eb_stock_adjust_log':  '订货系统-订货商库存调整记录',
    'eb_stock_price_sku':   '订货系统-规格级拿货价',
    # 以下 6 张原注释过于笼统（同名不同义、看不出用途），按字段语义改写
    'eb_category':                  '分类表（商品 / 文章分类，type 区分）',
    'eb_system_store':              '门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）',
    'eb_template_message':          '微信订阅消息模板',
    'eb_product_day_record':        '商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）',
    'eb_shopping_product_day_record':'商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）',
    'eb_trading_day_record':        '商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）',
}

# ---- 人工补充：备份表 / Quartz 框架表 / 实体无描述的表 ----
MANUAL = {
    'bak_category_type1_20260916':
        '备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）',
    'bak_store_product_cate_20260916':
        '备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）',
    'eb_store_product_bak_20260923_115556':
        '本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）',
    'eb_system_config_bak_20260923_115556':
        '本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）',
    'eb_system_menu_bak_20260922':
        '本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）',
    'eb_system_menu_bak_20260923':
        '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）',
    'eb_system_menu_bak_20260923_115556':
        '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）',
    'eb_system_menu_bak_20260923_125223':
        '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）',
    'eb_activity_style':    '活动样式表（活动边框 / 活动背景装修）',
    'eb_user_token':        '会员登录令牌表（H5 / 小程序 / APP 各端 token）',
    # Quartz 定时任务框架自带表
    'qrtz_job_details':         'Quartz 定时任务-Job 定义（任务实现类与并发策略）',
    'qrtz_triggers':            'Quartz 定时任务-触发器（与 Job 的绑定关系）',
    'qrtz_cron_triggers':       'Quartz 定时任务-Cron 表达式触发器',
    'qrtz_simple_triggers':     'Quartz 定时任务-简单触发器（固定间隔 / 重复次数）',
    'qrtz_simprop_triggers':    'Quartz 定时任务-带属性的简单触发器',
    'qrtz_blob_triggers':       'Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）',
    'qrtz_fired_triggers':      'Quartz 定时任务-已触发的触发器实例（运行态）',
    'qrtz_paused_trigger_grps': 'Quartz 定时任务-已被暂停的触发器组',
    'qrtz_scheduler_state':     'Quartz 定时任务-调度器实例心跳状态（集群用）',
    'qrtz_locks':               'Quartz 定时任务-调度器悲观锁（集群用）',
}

CJK = re.compile(r'[\u4e00-\u9fff]')


def java_descriptions():
    m = {}
    for dp, dns, fs in os.walk(os.path.join(ROOT, 'crmeb')):
        if 'sql' in dns:
            dns.remove('sql')
        for f in fs:
            if not f.endswith('.java'):
                continue
            t = open(os.path.join(dp, f), encoding='utf-8', errors='replace').read()
            for tm in re.finditer(r'@TableName\("([a-z0-9_]+)"\)', t):
                seg = t[max(0, tm.start() - 400):tm.start() + 400]
                d = re.search(r'description\s*=\s*"([^"]+)"', seg)
                if d and tm.group(1) not in m:
                    m[tm.group(1)] = d.group(1)
    return m


def q(s):
    """单引号字面量（供 SQL 文件里的目标备注使用）"""
    return "'" + s.replace('\\', '\\\\').replace("'", "''") + "'"


jd = java_descriptions()
rows = [l.rstrip('\n').split('\t') for l in open(os.path.join(TMP, 'tables.tsv'), encoding='utf-8')
        if l.strip()]
rows = [(r[0], r[1] if len(r) > 1 else '') for r in rows]

final, src_stat = {}, {'keep': 0, 'override': 0, 'java': 0, 'manual': 0}
for name, dbc in rows:
    if name in OVERRIDE:
        final[name] = OVERRIDE[name]
        src_stat['override'] += 1
    elif dbc and '?' not in dbc and CJK.search(dbc):
        final[name] = dbc
        src_stat['keep'] += 1
    elif name in jd and CJK.search(jd[name]):
        final[name] = jd[name]
        src_stat['java'] += 1
    elif name in MANUAL:
        final[name] = MANUAL[name]
        src_stat['manual'] += 1
    elif dbc:
        final[name] = dbc          # 英文/无中文但非乱码 → 保留，人工再定
        src_stat['keep'] += 1
    else:
        print('!! 无备注可用:', name)
        sys.exit(1)

for k in MANUAL:
    if k not in final:
        print('!! MANUAL 里有本地不存在的表:', k)

# ---------- 生成 SQL ----------
missing_before = [n for n, c in rows if not c]
fixed = [n for n, c in rows if c and (n in OVERRIDE or '?' in c)]

hdr = f"""-- ============================================================
-- CRMEB Java 3.0 表备注补全 / 修正（2026-09-23）
--
-- 背景：库里有一部分表没有 COMMENT，在客户端里看不出这张表是做什么的；
--       另有少数表的注释写错、乱码或残留英文。
-- 范围：本地库 {len(rows)} 张基础表的全部表级 COMMENT。
--   · 原本无备注 {len(missing_before)} 张 —— 本次补齐
--   · 原备注有误 {len(fixed)} 张 —— 本次修正：{', '.join(fixed)}
--   · 其余 {src_stat['keep']} 张沿用库里现有中文备注
--   · 新增表说明取自 Java 实体 @ApiModel(description) 共 {src_stat['java']} 张，
--     以保证「代码里的叫法」与「库里的说明」一致
--
-- 幂等：逐表用 information_schema 比对当前备注，**只有不一致才真正 ALTER**；
--       备注已一致时该条退化为 SET 空操作，连跑多少遍结果都一样。
-- 安全：表不存在时同样退化为空操作（本地开发期的 *_bak_* 备份表线上并不存在，
--       不会让 ./deploy.sh patch 因 "Table doesn't exist" 中断）。
--       全部操作只改表元数据 COMMENT，不触碰任何数据行，MySQL 8 下不重建表。
-- ============================================================
SET NAMES utf8mb4;

"""

blocks = []
for name, _ in rows:
    cmt = final[name]
    blocks.append(
        "-- %s —— %s\n"
        "SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE(%s))\n"
        "                      FROM information_schema.TABLES\n"
        "                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '%s'\n"
        "                       AND IFNULL(TABLE_COMMENT, '') <> %s), 'SET @tc_noop := 1');\n"
        "PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;\n"
        % (name, cmt, q(cmt), name, q(cmt)))

tail = """
-- 自检：仍无备注的表应当只有「线上不存在的本地备份表」
SELECT COUNT(*) AS 库里无备注的表数
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
   AND IFNULL(TABLE_COMMENT, '') = '';

SELECT 'CRMEB table_comments done' AS result;
"""

with open(OUT, 'w', encoding='utf-8', newline='\n') as f:
    f.write(hdr + '\n'.join(blocks) + tail)

print('表数 %d：沿用中文备注 %d / 人工修正 %d / 取自 Java 实体 %d / 人工补充 %d'
      % (len(rows), src_stat['keep'], src_stat['override'], src_stat['java'], src_stat['manual']))
print('本次补齐 %d 张、修正 %d 张' % (len(missing_before), len(fixed)))
print('输出:', OUT, os.path.getsize(OUT), '字节')
