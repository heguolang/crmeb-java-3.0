-- =============================================================
--  订货商板块改造（1）：升级条件 / 平级奖 / 向上找货 / 开关 / 五级默认数据
--  执行：mysql -uroot -p123456 crmeb < crmeb/sql/stock_upgrade_conditions.sql
-- =============================================================
USE crmeb;

-- 1. 级别表新增：四项升级条件 + 条件组合方式 + 平级奖比例
ALTER TABLE eb_stock_level
  ADD COLUMN IF NOT EXISTS cond_self_buy       TINYINT(1)      NOT NULL DEFAULT 0    COMMENT '启用条件：自购消费满额自动升级' AFTER discount,
  ADD COLUMN IF NOT EXISTS self_buy_amount     DECIMAL(12,2)   NOT NULL DEFAULT 0    COMMENT '自购消费门槛（元）' AFTER cond_self_buy,
  ADD COLUMN IF NOT EXISTS cond_direct         TINYINT(1)      NOT NULL DEFAULT 0    COMMENT '启用条件：直推订单总业绩' AFTER self_buy_amount,
  ADD COLUMN IF NOT EXISTS direct_order_amount DECIMAL(12,2)   NOT NULL DEFAULT 0    COMMENT '直推订单总业绩门槛（元）' AFTER cond_direct,
  ADD COLUMN IF NOT EXISTS cond_team           TINYINT(1)      NOT NULL DEFAULT 0    COMMENT '启用条件：团队伞下业绩' AFTER direct_order_amount,
  ADD COLUMN IF NOT EXISTS team_amount         DECIMAL(12,2)   NOT NULL DEFAULT 0    COMMENT '团队伞下业绩门槛（元）' AFTER cond_team,
  ADD COLUMN IF NOT EXISTS cond_product        TINYINT(1)      NOT NULL DEFAULT 0    COMMENT '启用条件：购买指定产品升级' AFTER team_amount,
  ADD COLUMN IF NOT EXISTS upgrade_product_ids VARCHAR(500)    NOT NULL DEFAULT ''   COMMENT '指定升级产品ID，英文逗号分隔' AFTER cond_product,
  ADD COLUMN IF NOT EXISTS condition_logic     TINYINT(1)      NOT NULL DEFAULT 0    COMMENT '条件组合：0=满足任一(或) 1=全部满足(与)' AFTER upgrade_product_ids,
  ADD COLUMN IF NOT EXISTS peer_rate           DECIMAL(5,2)    NOT NULL DEFAULT 0    COMMENT '平级奖比例(%)' AFTER condition_logic;

-- 2. 订单表新增：向上查找上级库存
ALTER TABLE eb_stock_order
  ADD COLUMN IF NOT EXISTS up_search_time DATETIME NULL            COMMENT '向上查找上级库存的时间' AFTER update_time,
  ADD COLUMN IF NOT EXISTS up_search_num  INT      NOT NULL DEFAULT 0 COMMENT '已向上查找次数' AFTER up_search_time;

-- 3. 后台开关配置（幂等）
INSERT INTO eb_system_config(name, title, form_id, value, status, create_time, update_time)
SELECT 'stock_parent_deliver', '订货订单由上级发货', 0, '0', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_parent_deliver'));

INSERT INTO eb_system_config(name, title, form_id, value, status, create_time, update_time)
SELECT 'stock_up_search_hours', '上级无库存自动向上查找等待时长(小时)', 0, '12', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_up_search_hours'));

-- 4. 五级订货商默认数据（sort 越小层级越高）
--    由低到高：区级 80% / 市级 70% / 省级 60% / 全国 50% / 分公司 40%
UPDATE eb_stock_level SET name = '分公司订货商', sort = 10, discount = 40.00,
       cond_self_buy = 1, self_buy_amount = 100000.00,
       cond_direct = 1, direct_order_amount = 500000.00,
       cond_team = 1, team_amount = 3000000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 1.00
 WHERE id = 1;

UPDATE eb_stock_level SET name = '全国订货商', sort = 20, discount = 50.00,
       cond_self_buy = 1, self_buy_amount = 50000.00,
       cond_direct = 1, direct_order_amount = 200000.00,
       cond_team = 1, team_amount = 1000000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 2.00
 WHERE id = 2;

UPDATE eb_stock_level SET name = '省级订货商', sort = 30, discount = 60.00,
       cond_self_buy = 1, self_buy_amount = 20000.00,
       cond_direct = 1, direct_order_amount = 80000.00,
       cond_team = 1, team_amount = 300000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 3.00
 WHERE id = 3;

UPDATE eb_stock_level SET name = '市级订货商', sort = 40, discount = 70.00,
       cond_self_buy = 1, self_buy_amount = 5000.00,
       cond_direct = 1, direct_order_amount = 20000.00,
       cond_team = 1, team_amount = 80000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 4.00
 WHERE id = 4;

INSERT INTO eb_stock_level(name, sort, discount, cond_self_buy, self_buy_amount,
                           cond_direct, direct_order_amount, cond_team, team_amount,
                           cond_product, upgrade_product_ids, condition_logic, peer_rate, is_del)
SELECT '区级订货商', 50, 80.00, 1, 1000.00, 1, 5000.00, 1, 20000.00, 0, '', 0, 5.00, 0
WHERE NOT EXISTS (SELECT 1 FROM eb_stock_level WHERE name = '区级订货商');
