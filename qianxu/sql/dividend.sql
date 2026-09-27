-- ============================================================
-- 全球分红功能 SQL（幂等，可重复执行）
-- 1. 分红级别 = 分销商等级（复用升级条件），eb_distributor_level 加分红比例列
-- 2. 分红结算记录表 eb_dividend_record / 明细表 eb_dividend_detail
-- 3. 分红配置 sys_dividend_*
-- 4. 菜单：大市场 → 全球分红
-- 5. 定时任务：DividendCheckTask 每分钟检查周期结算
-- ============================================================

-- 1. 分销商等级加分红比例列（%，按分红基数计算）
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_distributor_level' AND COLUMN_NAME = 'dividend_rate');
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `eb_distributor_level` ADD COLUMN `dividend_rate` decimal(6,3) NOT NULL DEFAULT 0.000 COMMENT ''全球分红比例（%，按分红基数，0=不参与分红）'' AFTER `order_product_mode`',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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
  `level_id` int(11) NOT NULL DEFAULT 0 COMMENT '分红级别ID（分销商等级ID）',
  `level_name` varchar(50) DEFAULT '' COMMENT '分红级别名称',
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

-- 4. 菜单：大市场(712) → 全球分红
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 712, '全球分红', '', 'admin:dividend:info', '/market/dividend', 'C', 2, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend' AND `is_delte` = 0);

-- 5. 定时任务（每分钟检查，命中周期时间点才结算；status 0=启用）
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`)
SELECT 'DividendCheckTask', 'dividendCheck', '', '0 */1 * * * ?', 0, '全球分红周期结算检查任务', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name` = 'DividendCheckTask' AND `is_delte` = 0);
