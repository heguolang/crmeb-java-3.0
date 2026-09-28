-- ============================================================
-- 全球分红功能 SQL（幂等，可重复执行）
-- 1. 分红等级表 eb_dividend_level：比例与升级条件独立维护（条件项与分销商等级同一套，
--    可参考分销商等级配置，但达标判定基于会员自身统计，与分销商等级本身无关）
-- 2. 分红结算记录表 eb_dividend_record / 明细表 eb_dividend_detail
-- 3. 分红配置 sys_dividend_*
-- 4. 菜单：大市场 → 全球分红(目录) → 分红设置/分红等级/分红记录
-- 5. 定时任务：DividendCheckTask 每分钟检查周期结算
-- ============================================================

-- 1. 分红等级表（升级条件字段与分销商等级同一套）
CREATE TABLE IF NOT EXISTS `eb_dividend_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '分红等级名称',
  `dividend_rate` decimal(6,3) NOT NULL DEFAULT 0.000 COMMENT '分红比例（%，按分红基数）',
  `direct_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-直推会员人数(人)',
  `direct_user_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '直推会员人数条件关系：1=与，2=或',
  `team_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-团队会员人数(人)',
  `team_user_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '团队会员人数条件关系：1=与，2=或',
  `direct_level_id` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-直推指定分销商等级ID，0=未启用',
  `direct_level_count` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-直推指定等级人数(人)',
  `direct_level_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '直推指定等级条件关系：1=与，2=或',
  `total_consume_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-累计商城总消费额(元)',
  `total_consume_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '累计商城总消费额条件关系：1=与，2=或',
  `total_recharge_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-总充值额(元)',
  `total_recharge_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '总充值额条件关系：1=与，2=或',
  `team_product_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-团队商品总消费额(元)',
  `team_product_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '团队商品总消费额条件关系：1=与，2=或',
  `direct_consume_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-直推商城消费总额(元)',
  `direct_consume_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '直推商城消费总额条件关系：1=与，2=或',
  `order_product_ids` varchar(500) DEFAULT '' COMMENT '升级条件-下单指定商品ID，逗号分隔，空=未启用',
  `order_product_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '下单指定商品条件关系：1=与，2=或',
  `order_product_mode` tinyint(1) NOT NULL DEFAULT 1 COMMENT '下单指定商品达成方式：1=任买一件即可，2=需全部购买',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全球分红等级表';

-- 2. 分红结算记录表（每期一条汇总，period_no 唯一键幂等防重发）
CREATE TABLE IF NOT EXISTS `eb_dividend_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period_no` varchar(32) NOT NULL DEFAULT '' COMMENT '期号：日D20260928/周W2026-W39/月M2026-09',
  `cycle_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '周期类型：1=日，2=周，3=月',
  `base_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '分红基数：1=订单总业绩，2=订单利润',
  `base_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '本期分红基数总额',
  `order_count` int(11) NOT NULL DEFAULT 0 COMMENT '本期参与统计订单数',
  `total_paid` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '本期分红总发放金额',
  `member_count` int(11) NOT NULL DEFAULT 0 COMMENT '本期参与分红会员人次',
  `mark` varchar(500) DEFAULT '' COMMENT '备注（每档均分明细说明）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '结算时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_no` (`period_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全球分红结算记录表';

CREATE TABLE IF NOT EXISTS `eb_dividend_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `record_id` int(11) NOT NULL DEFAULT 0 COMMENT '结算记录ID',
  `period_no` varchar(32) NOT NULL DEFAULT '' COMMENT '期号',
  `uid` int(11) NOT NULL DEFAULT 0 COMMENT '会员uid',
  `level_id` int(11) NOT NULL DEFAULT 0 COMMENT '分红等级ID',
  `level_name` varchar(50) DEFAULT '' COMMENT '分红等级名称',
  `rate` decimal(6,3) NOT NULL DEFAULT 0.000 COMMENT '分红比例（%）',
  `amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '分红金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_uid_level` (`period_no`, `uid`, `level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全球分红发放明细表';

-- 3. 分红配置
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'sys_dividend_status', '全球分红-开关', 0, '0', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'sys_dividend_status');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'sys_dividend_cycle', '全球分红-周期类型', 0, '1', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'sys_dividend_cycle');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'sys_dividend_base', '全球分红-分红基数', 0, '1', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'sys_dividend_base');

-- 4. 菜单：大市场(按 component 动态找) → 全球分红(目录) → 分红设置/分红等级/分红记录
-- 4.1 目录
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '全球分红', 's-claim', '', '/market/dividend', 'M', 2, 1, 0
FROM (SELECT id FROM `eb_system_menu` WHERE `component` = '/market' AND `pid` = 0 AND `is_delte` = 0 LIMIT 1) m
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend' AND `is_delte` = 0);

SET @dividend_dir := (SELECT id FROM `eb_system_menu` WHERE `component` = '/market/dividend' AND `is_delte` = 0 LIMIT 1);

-- 已部署环境补图标（幂等）
UPDATE `eb_system_menu` SET `icon` = 's-claim'
WHERE `component` = '/market/dividend' AND `is_delte` = 0 AND (`icon` IS NULL OR `icon` = '');

-- 4.2 三个子页面
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @dividend_dir, '分红设置', '', 'admin:dividend:info', '/market/dividend/setting', 'C', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend/setting' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @dividend_dir, '分红等级', '', 'admin:dividend:info', '/market/dividend/level', 'C', 2, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend/level' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @dividend_dir, '分红记录', '', 'admin:dividend:info', '/market/dividend/record', 'C', 3, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend/record' AND `is_delte` = 0);

-- 5. 定时任务（每分钟检查，命中周期时间点才结算；status 0=启用）
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`)
SELECT 'DividendCheckTask', 'dividendCheck', '', '0 */1 * * * ?', 0, '全球分红周期结算检查任务', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name` = 'DividendCheckTask' AND `is_delte` = 0);
