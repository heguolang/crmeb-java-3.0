-- ============================================================
-- 2+1 链动模式：完整增量升级脚本（自包含 / 幂等 / 可重复执行）
-- 生成日期：2026-10-11
--
-- 用途：已有库升级到「2+1 链动模式」最新形态。**不是全新建库脚本**。
-- 内容（按顺序）：
--    1. chain_2plus1_20261011.sql
--    2. chain_2plus1_phase1_patch_20261011.sql
--    3. chain_2plus1_phase2_20261011.sql
--    4. chain_product_20261011.sql
--    5. chain_help_unlock_20261011.sql
--    6. chain_admin_slim_20261011.sql
--    7. chain_menu_to_market_20261011.sql
--    8. chain_product_see_ratio_20261011.sql
--    9. chain_member_center_entry_20261011.sql
--   10. chain_commission_type_normalize_20261011.sql
--   11. chain_commission_status_align_20261011.sql
--   12. chain_mark_wording_fix_20261011.sql
--   13. chain_menu_finalize_20261011.sql
--
-- 说明：
--   · 全部语句幂等（CREATE TABLE IF NOT EXISTS / 查 information_schema 后再 ALTER /
--     INSERT ... WHERE NOT EXISTS），可反复执行。
--   · 风控模块已按需求方决策整体移除（不建表、不建配置、不建菜单）。
--   · 执行后需刷新配置缓存并重新登录后台。
--   · 本脚本只覆盖链动模块，不替代 oneclick/02_patches_all.sql。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ============================================================
-- ===== BEGIN: chain_2plus1_20261011.sql =====
-- ============================================================
-- 2+1 链动模块（增量）
-- 默认规则见代码 ChainConstants；后台可用 eb_system_config / eb_chain_reward_rule 调整
SET NAMES utf8mb4;

-- ---------- 用户冗余字段 ----------
SET @db := DATABASE();

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='chain_parent_id') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `chain_parent_id` int(11) NOT NULL DEFAULT 0 COMMENT ''链动安置上级UID'' AFTER `spread_uid`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='chain_invite_uid') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `chain_invite_uid` int(11) NOT NULL DEFAULT 0 COMMENT ''链动邀请人UID（直推奖归属）'' AFTER `chain_parent_id`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='chain_identity') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `chain_identity` tinyint(4) NOT NULL DEFAULT 0 COMMENT ''链动身份：0无 1代理 2老板'' AFTER `chain_invite_uid`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 业务表 ----------
CREATE TABLE IF NOT EXISTS `eb_chain_identity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户UID',
  `identity_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '1代理 2老板',
  `upgrade_time` datetime DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1有效 0失效',
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type_status` (`identity_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动身份记录';

CREATE TABLE IF NOT EXISTS `eb_chain_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `parent_id` int(11) NOT NULL DEFAULT 0 COMMENT '安置上级',
  `invite_uid` int(11) NOT NULL DEFAULT 0 COMMENT '邀请人',
  `level_depth` int(11) NOT NULL DEFAULT 0 COMMENT '相对根深度，可选',
  `is_help` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否帮扶对象',
  `path_snapshot` varchar(1000) DEFAULT NULL COMMENT '安置路径 /1/2/3/',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_invite` (`invite_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动上下级关系';

CREATE TABLE IF NOT EXISTS `eb_chain_reward_rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(32) NOT NULL COMMENT 'direct/see/peer/help',
  `gen_level` int(11) NOT NULL DEFAULT 1 COMMENT '见单代数等',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '固定金额',
  `ratio` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT '比例%',
  `status` tinyint(4) NOT NULL DEFAULT 1,
  `trigger_event` varchar(32) DEFAULT 'order' COMMENT 'order/upgrade',
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动奖励规则';

CREATE TABLE IF NOT EXISTS `eb_chain_commission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `source_uid` int(11) NOT NULL DEFAULT 0,
  `order_id` varchar(64) NOT NULL DEFAULT '',
  `reward_type` varchar(32) NOT NULL COMMENT 'direct/see/peer/help',
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1待结算 2冻结 3已结算 4失效 6待追回',
  `brokerage_record_id` int(11) NOT NULL DEFAULT 0,
  `mark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_reward_user` (`order_id`,`reward_type`,`user_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_source` (`source_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动佣金流水';

CREATE TABLE IF NOT EXISTS `eb_chain_slide_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `from_parent_id` int(11) NOT NULL DEFAULT 0,
  `to_parent_id` int(11) NOT NULL DEFAULT 0,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动滑落日志';

-- 注：原 eb_chain_help_record（帮扶记录）已废弃移除 —— 帮扶改为「帮扶基金冻结/解冻」，
-- 冻结池落在 eb_chain_commission.status=5，无需单独关系表。存量环境由
-- chain_2plus1_phase2_20261011.sql 末尾的 DROP TABLE IF EXISTS 清理。

CREATE TABLE IF NOT EXISTS `eb_chain_audit_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `operator` varchar(64) DEFAULT NULL,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `before_val` varchar(500) DEFAULT NULL,
  `after_val` varchar(500) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动关系审计日志';

-- ---------- 默认奖励规则 ----------
INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`remark`)
SELECT 'direct', 1, 100.00, 0.00, 1, 'order', '直推奖固定金额'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='direct' LIMIT 1);

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`remark`)
SELECT 'see', 1, 50.00, 0.00, 1, 'order', '见单奖-第1代老板'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='see' AND `gen_level`=1 LIMIT 1);

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`remark`)
SELECT 'see', 2, 30.00, 0.00, 1, 'order', '见单奖-第2代老板'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='see' AND `gen_level`=2 LIMIT 1);

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`remark`)
SELECT 'see', 3, 20.00, 0.00, 1, 'order', '见单奖-第3代老板'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='see' AND `gen_level`=3 LIMIT 1);

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`remark`)
SELECT 'peer', 1, 0.00, 15.00, 1, 'order', '平级奖占见单比例%'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='peer' LIMIT 1);

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`remark`)
SELECT 'help', 1, 200.00, 0.00, 1, 'upgrade', '帮扶奖一次性'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='help' LIMIT 1);

-- ---------- 系统配置 ----------
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_func_status','0','链动功能开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_func_status');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_gift_product_ids','','链动代理礼包商品ID(逗号分隔)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_gift_product_ids');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_effective_timing','1','有效代理判定时机1支付2完成',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_effective_timing');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_credit_timing','1','链动佣金到账时机1支付2完成',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_credit_timing');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_freeze_days','1','链动佣金冻结天数',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_freeze_days');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_slide_enabled','1','链动滑落开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_slide_enabled');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_slide_capacity','2','链动直推位容量N',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_slide_capacity');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_slide_depth','2','链动滑落最大深度',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_slide_depth');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_see_generations','3','见单奖最大代数',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_see_generations');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_identity_downgrade','0','退款是否降级身份0否1是',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_identity_downgrade');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_skip_normal_brokerage','1','礼包订单跳过普通分销/团队奖',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_skip_normal_brokerage');

-- ---------- 后台菜单（挂在分销下，若不存在则跳过） ----------
SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '链动配置', NULL, 'admin:chain:config', '/distribution/chainConfig', 'C', 20, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainConfig' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '链动用户', NULL, 'admin:chain:users', '/distribution/chainUsers', 'C', 21, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainUsers' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '链动佣金', NULL, 'admin:chain:commission', '/distribution/chainCommission', 'C', 22, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainCommission' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '滑落日志', NULL, 'admin:chain:slide', '/distribution/chainSlide', 'C', 23, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainSlide' LIMIT 1);
-- ===== END: chain_2plus1_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_2plus1_phase1_patch_20261011.sql =====
-- ============================================================
-- 2+1 链动一期补丁：滑落方向/帮扶超时/规则文案/菜单
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_slide_mode','min_team','链动滑落方向min_team/left/right',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_slide_mode');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_timeout_days','0','帮扶超时天数0关闭',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_timeout_days');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_rules_content','','链动规则说明文案',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_rules_content');

SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '链动看板', NULL, 'admin:chain:config', '/distribution/chainDashboard', 'C', 19, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainDashboard' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '关系审计', NULL, 'admin:chain:users', '/distribution/chainAudit', 'C', 24, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainAudit' LIMIT 1);

-- 定时任务（若已有同名则跳过）：每天 2:30 释放超时帮扶
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`, `create_time`)
SELECT 'ChainHelpTimeoutTask', 'releaseTimeout', '', '0 30 2 * * ?', 0, '链动帮扶超时释放', 0, NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_schedule_job')
  AND NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name`='ChainHelpTimeoutTask' LIMIT 1);
-- ===== END: chain_2plus1_phase1_patch_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_2plus1_phase2_20261011.sql =====
-- ============================================================
-- 2+1 链动二期：通知 / 团队分红 / 看板配置
-- 注：风控模块已按需求方决策整体移除（不建表、不建配置、不建菜单）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eb_chain_notify_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `notify_type` varchar(32) NOT NULL COMMENT 'upgrade/commission/help/dividend/risk',
  `title` varchar(128) NOT NULL DEFAULT '',
  `content` varchar(500) NOT NULL DEFAULT '',
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动站内通知';

CREATE TABLE IF NOT EXISTS `eb_chain_dividend_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period` varchar(16) NOT NULL COMMENT '账期yyyy-MM',
  `user_id` int(11) NOT NULL,
  `team_sales` decimal(14,2) NOT NULL DEFAULT 0.00,
  `ratio` decimal(8,2) NOT NULL DEFAULT 0.00,
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `team_size` int(11) NOT NULL DEFAULT 0,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1已发放 0作废',
  `mark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_user` (`period`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动团队分红记录';

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_notify_status','1','链动站内通知开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_notify_status');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_dividend_status','0','团队分红开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_dividend_status');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_dividend_ratio','5','团队分红比例%',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_dividend_ratio');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_dividend_min_team','2','分红最低团队人数',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_dividend_min_team');

-- 通知场景（默认短信关闭，站内通知独立实现）
INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainUpgrade','链动','链动身份晋升通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainUpgrade');

INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainCommission','链动','链动佣金到账通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainCommission');

INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainHelp','链动','链动帮扶完成通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainHelp');

INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainDividend','链动','链动团队分红通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainDividend');

SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队分红', NULL, 'admin:chain:dividend', '/distribution/chainDividend', 'C', 26, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainDividend' LIMIT 1);

-- 清理历史遗留：风控配置 / 风控日志菜单 / 风控日志表 / 废弃的帮扶记录表
DELETE FROM `eb_system_config` WHERE `name` IN
  ('chain_risk_status','chain_risk_window_minutes','chain_risk_max_uid','chain_risk_max_phone','chain_risk_max_ip','chain_risk_max_device');
DELETE FROM `eb_system_menu` WHERE `component` = '/distribution/chainRisk';
DROP TABLE IF EXISTS `eb_chain_risk_log`;
DROP TABLE IF EXISTS `eb_chain_help_record`;

INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`, `create_time`)
SELECT 'ChainDividendTask', 'runMonthly', '', '0 15 3 1 * ?', 0, '链动团队分红(每月1日)', 0, NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_schedule_job')
  AND NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name`='ChainDividendTask' LIMIT 1);
-- ===== END: chain_2plus1_phase2_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_product_20261011.sql =====
-- ============================================================
-- 链动产品：指定礼包商品，并配置直推奖/见单奖/平级奖（优先于全局奖励规则）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eb_chain_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `direct_amount` decimal(12,2) NOT NULL DEFAULT 100.00 COMMENT '直推奖固定金额',
  `see_ratio` decimal(8,2) NOT NULL DEFAULT 10.00 COMMENT '见点奖比例%（订单实付 × 比例）',
  `see_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '见点奖固定金额（旧字段，兼容保留；see_ratio>0 时以比例为准）',
  `peer_ratio` decimal(8,2) NOT NULL DEFAULT 10.00 COMMENT '平级奖占见点比例%',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1启用礼包 0停用',
  `sort` int(11) NOT NULL DEFAULT 0,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动礼包产品及分佣配置';

-- 菜单：大市场 → 2+1链动 → 链动产品
SET @chainDirId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/chain' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, CONVERT(UNHEX('E993BEE58AA8E4BAA7E59381') USING utf8mb4), NULL, 'admin:chain:product', '/chain/product', 'C', 75, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/product' AND `is_delte` = 0 LIMIT 1);

UPDATE `eb_system_menu`
   SET `pid` = @chainDirId,
       `name` = CONVERT(UNHEX('E993BEE58AA8E4BAA7E59381') USING utf8mb4),
       `sort` = 75, `is_show` = 1, `perms` = 'admin:chain:product'
 WHERE `component` = '/chain/product' AND `is_delte` = 0 AND @chainDirId IS NOT NULL;

-- 超管角色授权
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
  FROM `eb_system_role` r
 CROSS JOIN `eb_system_menu` m
 WHERE r.`level` = 0 AND r.`status` = 1
   AND m.`is_delte` = 0
   AND m.`component` = '/chain/product'
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_role_menu` rm
          WHERE rm.`rid` = r.`id` AND rm.`menu_id` = m.`id`
       );
-- ===== END: chain_product_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_help_unlock_20261011.sql =====
-- ============================================================
-- 2+1 帮扶解锁（见单冻结）：升老板后，留下的 N 个代理各直推满 X 单礼包才解锁；
-- 未解锁见单按比例进帮扶基金，解锁后解冻且以后不再冻。
SET NAMES utf8mb4;

SET @db := DATABASE();

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='chain_help_unlocked') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `chain_help_unlocked` tinyint(4) NOT NULL DEFAULT 0 COMMENT ''老板帮扶是否解锁0否1是'' AFTER `chain_identity`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_unlock_orders','1','帮扶解锁：每位留下的代理需直推礼包单数X',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_unlock_orders');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_freeze_ratio','50','未解锁时见单奖冻结进帮扶基金比例%',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_freeze_ratio');

-- 对齐常见默认金额（仅当仍是初始化默认值时才改，避免覆盖商家已调价）
UPDATE `eb_chain_reward_rule` SET `amount`=100.00, `remark`='见单奖-第1代老板'
 WHERE `type`='see' AND `gen_level`=1 AND `amount`=50.00;

UPDATE `eb_chain_reward_rule` SET `ratio`=10.00, `remark`='平级奖占见单比例%'
 WHERE `type`='peer' AND `gen_level`=1 AND `ratio`=15.00;
-- ===== END: chain_help_unlock_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_admin_slim_20261011.sql =====
-- ============================================================
-- ============================================================
-- 2+1 链动：后台精简（配置项 + 菜单 + 奖励规则默认值）2026-10-11
--
-- 依据《2+1链动模式系统需求文档》口径，并落实决策：
--   · 风控（同人/设备/IP 校验、风控日志）整体移除
--   · 「滑落」改为「走人留人」，滑落相关配置项删除
--   · 见点奖收敛为 1 级（仅直属上级老板）
--   · 帮扶＝帮扶基金冻结/解冻，删除旧的「帮扶奖」与超时释放配置
--   · 团队分红保留
--
-- 幂等：全部使用 DELETE + UPDATE + INSERT ... WHERE NOT EXISTS。
-- 执行后请刷新后台配置缓存并重新登录：
--   POST /api/admin/system/config/clear/cache
-- ============================================================
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- ① 删除失效配置项
-- ------------------------------------------------------------
DELETE FROM `eb_system_config` WHERE `name` IN (
  -- 滑落机制（被走人留人取代）
  'chain_slide_enabled',
  'chain_slide_capacity',
  'chain_slide_depth',
  'chain_slide_mode',
  -- 见单奖多代（收敛为 1 级见点奖）
  'chain_see_generations',
  -- 帮扶超时释放 + 旧「每位留人代理直推 X 单」口径
  'chain_help_timeout_days',
  'chain_help_unlock_orders',
  -- 风控 6 项
  'chain_risk_status',
  'chain_risk_window_minutes',
  'chain_risk_max_uid',
  'chain_risk_max_phone',
  'chain_risk_max_ip',
  'chain_risk_max_device'
);

-- ------------------------------------------------------------
-- ② 新增配置项（文档默认值）
-- ------------------------------------------------------------

-- 基础：激活门槛价
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_active_threshold','399','链动激活门槛价(元，订单实付需达标)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_active_threshold');
-- 基础：售后保护期
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_after_sale_days','7','售后保护期(天，满N天无退款才计入链动；0=支付即生效)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_after_sale_days');
-- 身份：代理显示名
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_level_agent_name','代理','链动级别名称：代理',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_level_agent_name');
-- 身份：老板显示名
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_level_boss_name','老板','链动级别名称：老板',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_level_boss_name');
-- 身份：直推位容量 N
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_direct_capacity','2','直推位容量N(直推满N名有效代理晋升老板)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_direct_capacity');
-- 身份：走人留人开关
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_leave_team_enabled','1','走人留人机制开关(1开 0关)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_leave_team_enabled');
-- 奖励：单笔分润封顶
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_max_brokerage_ratio','50','单笔订单链动分润上限(占订单实付%)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_max_brokerage_ratio');
-- 帮扶：解冻条件 N
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_unlock_n','10','帮扶解冻条件N(留人座位各自团队有效订单数)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_unlock_n');
-- 帮扶：解冻条件 M
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_unlock_m','20','帮扶解冻条件M(老板本人团队有效订单数)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_unlock_m');
-- 帮扶：解冻方式
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_release_mode','1','帮扶基金解冻方式(1一次性 2按订单完成比例逐笔释放)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_release_mode');

-- ------------------------------------------------------------
-- ③ 修正既有配置项的取值/文案（改成文档默认口径）
-- ------------------------------------------------------------
-- 帮扶冻结比例 50% → 30%
UPDATE `eb_system_config`
SET `value` = '30',
    `title` = '帮扶基金冻结比例%(每笔见点/平级奖冻结进帮扶池)'
WHERE `name` = 'chain_help_freeze_ratio'
  AND (`value` = '50' OR `value` = '' OR `value` IS NULL);

-- 文案对齐（其余保持用户已填值不变）
UPDATE `eb_system_config` SET `title`='链动功能开关'          WHERE `name`='chain_func_status';
UPDATE `eb_system_config` SET `title`='链动礼包商品ID(逗号分隔，由「链动产品」同步维护)' WHERE `name`='chain_gift_product_ids';
UPDATE `eb_system_config` SET `title`='有效订单判定时机(1支付即有效 2订单完成才有效)'   WHERE `name`='chain_effective_timing';
UPDATE `eb_system_config` SET `title`='佣金到账时机(1支付到账 2完成到账)'              WHERE `name`='chain_credit_timing';
UPDATE `eb_system_config` SET `title`='佣金冻结天数(0=不冻结)'                         WHERE `name`='chain_freeze_days';
UPDATE `eb_system_config` SET `title`='退款是否降级身份(0否 1是)'                      WHERE `name`='chain_identity_downgrade';
UPDATE `eb_system_config` SET `title`='礼包订单跳过普通分销/团队奖(1跳过，避免叠佣)'    WHERE `name`='chain_skip_normal_brokerage';
UPDATE `eb_system_config` SET `title`='链动规则说明文案(留空则用系统按配置自动生成)'    WHERE `name`='chain_rules_content';
UPDATE `eb_system_config` SET `title`='链动站内通知开关'                                WHERE `name`='chain_notify_status';
UPDATE `eb_system_config` SET `title`='团队分红开关'                                    WHERE `name`='chain_dividend_status';
UPDATE `eb_system_config` SET `title`='团队分红比例%'                                   WHERE `name`='chain_dividend_ratio';
UPDATE `eb_system_config` SET `title`='分红最低团队人数'                                WHERE `name`='chain_dividend_min_team';

-- ------------------------------------------------------------
-- ④ 菜单精简：9 项 → 7 项
--   删：滑落日志 / 关系审计（合并为「关系变更」）、风控日志
--   链动看板 / 链动配置 / 链动产品 / 链动用户 / 链动佣金 / 关系变更 / 团队分红
-- ------------------------------------------------------------
SET @chainMenuId := (SELECT id FROM `eb_system_menu` WHERE `component` = '/chain' AND `menu_type` = 'M' LIMIT 1);

-- 先清权限关联，再删菜单
-- ⚠️ 不要删 '/chain/relation'（那是合并后的「关系变更」目标菜单；
--    删掉再插会让菜单 id 每次执行都漂移，破坏幂等）
DELETE FROM `eb_system_role_menu`
WHERE `menu_id` IN (SELECT id FROM (
  SELECT id FROM `eb_system_menu`
  WHERE `component` IN ('/chain/risk', '/chain/slide', '/chain/audit')
) t);

DELETE FROM `eb_system_menu`
WHERE `component` IN ('/chain/risk', '/chain/slide', '/chain/audit');

-- 新建「关系变更」（含走人留人/滑落轨迹 + 管理员调整审计）
INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chainMenuId,'关系变更',NULL,'admin:chain:slide','/chain/relation','C',40,1,0,NOW(),NOW()
FROM DUAL
WHERE @chainMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/relation' LIMIT 1);

-- 既有菜单补齐/校正（sort 降序）
UPDATE `eb_system_menu` SET `sort`=80, `perms`='admin:chain:config'  WHERE `component`='/chain/dashboard';
UPDATE `eb_system_menu` SET `sort`=75, `perms`='admin:chain:product' WHERE `component`='/chain/product';
UPDATE `eb_system_menu` SET `sort`=70, `perms`='admin:chain:config'  WHERE `component`='/chain/config';
UPDATE `eb_system_menu` SET `sort`=60, `perms`='admin:chain:users'   WHERE `component`='/chain/users';
UPDATE `eb_system_menu` SET `sort`=50, `perms`='admin:chain:commission' WHERE `component`='/chain/commission';
UPDATE `eb_system_menu` SET `sort`=10, `perms`='admin:chain:dividend'   WHERE `component`='/chain/dividend';

-- ------------------------------------------------------------
-- ⑤ 奖励规则：收敛为文档口径三条
--   直推奖 固定 100 元 / 见点奖 实付 × 10% / 平级奖 下级见点 × 10%
--   删除旧的帮扶奖(help)与多代见单(see 2/3 代)
-- ------------------------------------------------------------
DELETE FROM `eb_chain_reward_rule` WHERE `type` = 'help';
DELETE FROM `eb_chain_reward_rule` WHERE `type` = 'see' AND `gen_level` > 1;
DELETE FROM `eb_chain_reward_rule` WHERE `type` = 'direct' AND `gen_level` > 1;

UPDATE `eb_chain_reward_rule`
SET `amount` = 100.00, `ratio` = 0.00, `status` = 1, `trigger_event` = 'order_effective'
WHERE `type` = 'direct' AND `gen_level` = 1;

UPDATE `eb_chain_reward_rule`
SET `amount` = 0.00, `ratio` = 10.00, `status` = 1, `trigger_event` = 'order_effective'
WHERE `type` = 'see' AND `gen_level` = 1;

UPDATE `eb_chain_reward_rule`
SET `amount` = 0.00, `ratio` = 10.00, `status` = 1, `trigger_event` = 'order_effective'
WHERE `type` = 'peer' AND `gen_level` = 1;

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`create_time`,`update_time`)
SELECT 'direct',1,100.00,0.00,1,'order_effective',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='direct' AND `gen_level`=1);
INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`create_time`,`update_time`)
SELECT 'see',1,0.00,10.00,1,'order_effective',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='see' AND `gen_level`=1);
INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`create_time`,`update_time`)
SELECT 'peer',1,0.00,10.00,1,'order_effective',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='peer' AND `gen_level`=1);

-- ------------------------------------------------------------
-- ⑥ 校验
-- ------------------------------------------------------------
SELECT '--- 链动配置项 ---' AS info;
SELECT `name`, `value`, `title` FROM `eb_system_config` WHERE `name` LIKE 'chain%' ORDER BY `name`;
SELECT '--- 链动菜单（应为 7 项，M 目录除外）---' AS info;
SELECT id, pid, name, perms, component, sort FROM `eb_system_menu`
WHERE component LIKE '/chain%' ORDER BY menu_type DESC, sort DESC;
SELECT '--- 奖励规则（应为 3 条）---' AS info;
SELECT id, type, gen_level, amount, ratio, status FROM `eb_chain_reward_rule` ORDER BY type, gen_level;
-- ===== END: chain_admin_slim_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_menu_to_market_20261011.sql =====
-- ============================================================
-- ============================================================
-- 2+1链动菜单：从「分销商」独立到「大市场」下
--
-- 目标结构：
--   大市场                  ← 一级（优先复用已有「大市场」或 /yunying）
--     └─ 2+1链动 (/chain)  ← 二级目录
--          ├─ 链动看板 /chain/dashboard
--          ├─ 链动配置 /chain/config
--          ├─ 链动用户 /chain/users
--          ├─ 链动佣金 /chain/commission
--          ├─ 滑落日志 /chain/slide
--          ├─ 关系审计 /chain/audit
--          ├─ 风控日志 /chain/risk
--          └─ 团队分红 /chain/dividend
--
-- 幂等：可重复执行。
-- ============================================================
SET NAMES utf8mb4;

-- 1) 定位/创建「大市场」一级目录
SET @marketId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `is_delte` = 0 AND `pid` = 0 AND `menu_type` = 'M'
     AND (`name` = '大市场' OR `component` = '/yunying')
   ORDER BY CASE WHEN `name` = '大市场' THEN 0 ELSE 1 END, `id`
   LIMIT 1
);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '大市场', 's-goods', NULL, '/yunying', 'M', 106, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @marketId IS NULL;

SET @marketId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `is_delte` = 0 AND `pid` = 0 AND `menu_type` = 'M'
     AND (`name` = '大市场' OR `component` = '/yunying')
   ORDER BY CASE WHEN `name` = '大市场' THEN 0 ELSE 1 END, `id`
   LIMIT 1
);

-- 统一显示名（不改别人已有的 component，避免和生产不一致）
UPDATE `eb_system_menu`
   SET `name` = '大市场', `is_show` = 1
 WHERE `id` = @marketId;

-- 2) 2+1链动二级目录
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @marketId, '2+1链动', NULL, NULL, '/chain', 'M', 110, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @marketId IS NOT NULL
   AND NOT EXISTS (
       SELECT 1 FROM `eb_system_menu`
        WHERE `component` = '/chain' AND `is_delte` = 0
       );

UPDATE `eb_system_menu`
   SET `pid` = @marketId, `name` = '2+1链动', `is_show` = 1, `sort` = 110, `menu_type` = 'M'
 WHERE `component` = '/chain' AND `is_delte` = 0 AND @marketId IS NOT NULL;

SET @chainDirId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/chain' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

-- 3) 旧 /distribution/chain* → /chain/*，并挂到 2+1链动下
UPDATE `eb_system_menu`
   SET `component` = '/chain/dashboard', `pid` = @chainDirId, `name` = '链动看板', `sort` = 80, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainDashboard', '/chain/dashboard');

UPDATE `eb_system_menu`
   SET `component` = '/chain/config', `pid` = @chainDirId, `name` = '链动配置', `sort` = 70, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainConfig', '/chain/config');

UPDATE `eb_system_menu`
   SET `component` = '/chain/users', `pid` = @chainDirId, `name` = '链动用户', `sort` = 60, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainUsers', '/chain/users');

UPDATE `eb_system_menu`
   SET `component` = '/chain/commission', `pid` = @chainDirId, `name` = '链动佣金', `sort` = 50, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainCommission', '/chain/commission');

UPDATE `eb_system_menu`
   SET `component` = '/chain/slide', `pid` = @chainDirId, `name` = '滑落日志', `sort` = 40, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainSlide', '/chain/slide');

UPDATE `eb_system_menu`
   SET `component` = '/chain/audit', `pid` = @chainDirId, `name` = '关系审计', `sort` = 30, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainAudit', '/chain/audit');

UPDATE `eb_system_menu`
   SET `component` = '/chain/risk', `pid` = @chainDirId, `name` = '风控日志', `sort` = 20, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainRisk', '/chain/risk');

UPDATE `eb_system_menu`
   SET `component` = '/chain/dividend', `pid` = @chainDirId, `name` = '团队分红', `sort` = 10, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainDividend', '/chain/dividend');

-- 4) 全新环境补齐叶子（已存在则跳过）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动看板', NULL, 'admin:chain:config', '/chain/dashboard', 'C', 80, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dashboard' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动配置', NULL, 'admin:chain:config', '/chain/config', 'C', 70, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/config' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动用户', NULL, 'admin:chain:users', '/chain/users', 'C', 60, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/users' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动佣金', NULL, 'admin:chain:commission', '/chain/commission', 'C', 50, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/commission' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '滑落日志', NULL, 'admin:chain:slide', '/chain/slide', 'C', 40, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/slide' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '关系审计', NULL, 'admin:chain:users', '/chain/audit', 'C', 30, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/audit' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '风控日志', NULL, 'admin:chain:risk', '/chain/risk', 'C', 20, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/risk' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '团队分红', NULL, 'admin:chain:dividend', '/chain/dividend', 'C', 10, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dividend' AND `is_delte` = 0);

-- 5) 分销下若还有旧链动路径残留，隐藏
UPDATE `eb_system_menu`
   SET `is_show` = 0
 WHERE `is_delte` = 0
   AND `component` LIKE '/distribution/chain%';
-- ===== END: chain_menu_to_market_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_product_see_ratio_20261011.sql =====
-- ============================================================
-- ============================================================
-- 链动产品：见点奖由「固定金额」改为「比例%」2026-10-11
--
-- 背景：文档口径为「见点奖 = 订单实付 × 10%」，为与全局奖励规则一致，
--       商品级覆盖也改为按比例。旧字段 see_amount 保留（兼容），
--       代码中 see_ratio>0 时优先按比例计算。
--
-- 幂等：用 information_schema 判断列是否存在后再 ALTER。
-- ============================================================
SET NAMES utf8mb4;

SET @colExists := (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
   WHERE `TABLE_SCHEMA` = DATABASE()
     AND `TABLE_NAME` = 'eb_chain_product'
     AND `COLUMN_NAME` = 'see_ratio'
);

SET @ddl := IF(@colExists = 0,
  'ALTER TABLE `eb_chain_product` ADD COLUMN `see_ratio` decimal(8,2) NOT NULL DEFAULT 10.00 COMMENT ''见点奖比例%（订单实付 × 比例）'' AFTER `direct_amount`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 旧字段见单奖固定金额语义修正：已有数据若为 100 而比例未设，统一按比例 10% 口径
UPDATE `eb_chain_product` SET `see_ratio` = 10.00 WHERE `see_ratio` IS NULL OR `see_ratio` = 0;

-- 校验
SELECT id, product_id, direct_amount, see_ratio, see_amount, peer_ratio, status
  FROM `eb_chain_product` ORDER BY id;
-- ===== END: chain_product_see_ratio_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_member_center_entry_20261011.sql =====
-- ============================================================
-- ============================================================
-- 链动中心：会员中心装修入口 + 会员端页面链接登记（2026-10-11）
--
-- 背景：
--   会员中心（/pages/user/index.vue）是纯装修器驱动的页面，「代理中心」也是先在
--   装修器「导航组」里被勾中才出现在会员中心。本脚本按同一机制把「链动中心」
--   加进「导航组」的可选预设列表（show=0，需在装修器里勾选才展示），
--   与会员端页面 /pages/promoter/chain_center/index 打通。
--
-- 前置：
--   1) 会员端已发布新版本（含 chain_center / chain_commission / chain_team 三个页面）
--   2) 后台 admin 已发布（linkaddress/linkData.json 已登记「链动中心」等 4 个链接）
-- 幂等：
--   WHERE 同时判断「含锚点」且「尚未含 chain_center/index」，重复执行不会追加多条。
-- ============================================================
SET NAMES utf8mb4;

-- ① 自定义装修数据（用户保存过的个人中心装修）
UPDATE `eb_theme`
SET `user_data` = REPLACE(
        `user_data`,
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]}',
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_center/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动收益","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_commission/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动团队","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_team/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动规则","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_rules/index","tips":"请输入链接"}]}'
    )
WHERE `user_data` LIKE '%"value":"代理中心"%'
  AND `user_data` NOT LIKE '%chain_center/index%';

-- ② 主题默认装修数据（未自定义时回落到这份）
UPDATE `eb_theme`
SET `user_default_data` = REPLACE(
        `user_default_data`,
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]}',
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_center/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动收益","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_commission/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动团队","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_team/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动规则","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_rules/index","tips":"请输入链接"}]}'
    )
WHERE `user_default_data` LIKE '%"value":"代理中心"%'
  AND `user_default_data` NOT LIKE '%chain_center/index%';

-- ③ 校验：应各返回 1 行（命中说明已写入）
SELECT id, title,
       `user_data`         LIKE '%chain_center/index%' AS user_data_ok,
       `user_default_data` LIKE '%chain_center/index%' AS user_default_ok
FROM `eb_theme`
WHERE `user_data` IS NOT NULL OR `user_default_data` IS NOT NULL;
-- ===== END: chain_member_center_entry_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_commission_type_normalize_20261011.sql =====
-- ============================================================
-- ============================================================
-- 链动流水：奖励类型归一化 2026-10-11
--
-- 背景：旧实现把代数后缀写进了 reward_type（see1/see2/see3、peer1/peer2、
--       see_help1/see_help2），而文档口径已收敛为「见点奖 1 级、平级奖 1 级」，
--       新代码只写 see / peer / see_help / peer_help。
--       后缀残留会让后台与会员端的类型筛选漏掉历史记录，故统一去掉后缀。
--
-- 只改字符串、不动金额与状态，不产生资金影响。幂等。
-- ============================================================
SET NAMES utf8mb4;

UPDATE `eb_chain_commission` SET `reward_type` = 'see_help'  WHERE `reward_type` REGEXP '^see_help[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'peer_help' WHERE `reward_type` REGEXP '^peer_help[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'see'       WHERE `reward_type` REGEXP '^see[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'peer'      WHERE `reward_type` REGEXP '^peer[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'direct'    WHERE `reward_type` REGEXP '^direct[0-9]+$';

-- 校验
SELECT `reward_type`, `status`, COUNT(*) AS c, SUM(`amount`) AS amt
  FROM `eb_chain_commission` GROUP BY `reward_type`, `status` ORDER BY `reward_type`, `status`;
-- ===== END: chain_commission_type_normalize_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_commission_status_align_20261011.sql =====
-- ============================================================
-- ============================================================
-- 链动流水：回填佣金钱包流水关联 + 状态对齐 2026-10-11
--
-- 背景：旧实现生成 eb_chain_commission 时既恒写 status=1（待结算），
--       又没有存 brokerage_record_id，导致后台「链动佣金」列表状态失真、
--       会员端「冻结中」把已到账佣金也算进去。
--
-- 做法：用 (uid + 订单号 + 金额 + 佣金等级) 精确匹配 eb_user_brokerage_record，
--       回填 brokerage_record_id，并把状态对齐为钱包流水的真实状态：
--         2 冻结中 / 3 已结算 / 4 已失效
--       帮扶基金行（status=5，brokerage_record_id=0）无钱包流水，保持不变。
--
-- 幂等；只改关联字段与状态，不动金额。执行后：
--   SELECT status, COUNT(*), SUM(amount) FROM eb_chain_commission GROUP BY status;
-- ============================================================
SET NAMES utf8mb4;

-- 注意：eb_chain_commission 与 eb_user_brokerage_record 排序规则不同
--       （utf8mb4_0900_ai_ci vs utf8mb4_general_ci），跨表比较需显式 COLLATE。
UPDATE `eb_chain_commission` c
  JOIN (
        SELECT c2.`id` AS cid,
               MIN(r.`id`)     AS rid,
               MIN(r.`status`) AS rstatus
          FROM `eb_chain_commission` c2
          JOIN `eb_user_brokerage_record` r
            ON r.`uid`     = c2.`user_id`
           AND r.`link_id` = c2.`order_id` COLLATE utf8mb4_general_ci
           AND r.`price`   = c2.`amount`
           AND r.`brokerage_level` = CASE c2.`reward_type`
                 WHEN 'direct' THEN 20
                 WHEN 'see'    THEN 21
                 WHEN 'peer'   THEN 22
                 WHEN 'help'   THEN 23
                 ELSE NULL END
         WHERE c2.`brokerage_record_id` = 0
           AND c2.`status` <> 5
         GROUP BY c2.`id`
       ) m ON m.cid = c.`id`
   SET c.`brokerage_record_id` = m.rid,
       c.`status` = CASE m.rstatus
                      WHEN 3 THEN 3
                      WHEN 2 THEN 2
                      WHEN 4 THEN 4
                      ELSE c.`status` END,
       c.`update_time` = NOW();

-- 校验
SELECT '--- 链动流水状态分布 ---' AS info;
SELECT `status`, COUNT(*) AS c, SUM(`amount`) AS amt
  FROM `eb_chain_commission` GROUP BY `status` ORDER BY `status`;
SELECT '--- 未回填关联的行（帮扶基金行属正常）---' AS info;
SELECT `status`, COUNT(*) AS c FROM `eb_chain_commission`
 WHERE `brokerage_record_id` = 0 GROUP BY `status`;
-- ===== END: chain_commission_status_align_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_mark_wording_fix_20261011.sql =====
-- ============================================================
-- ============================================================
-- 链动文案统一：历史数据里的「见单奖」→「见点奖」2026-10-11
--
-- 背景：旧实现把奖励写成「见单奖第1代」「见单帮扶基金第2代」，
--       文档口径统一叫「见点奖」。新代码已写「见点奖」，此处只回补历史数据。
-- 幂等：REPLACE 只对命中的行生效，重复执行无副作用。
-- ============================================================
SET NAMES utf8mb4;

-- 1) 链动流水 remark
UPDATE `eb_chain_commission`
   SET `mark` = REPLACE(`mark`, '见单帮扶基金', '见点奖帮扶基金')
 WHERE `mark` LIKE '%见单帮扶基金%';

UPDATE `eb_chain_commission`
   SET `mark` = REPLACE(`mark`, '见单奖', '见点奖')
 WHERE `mark` LIKE '%见单奖%';

-- 2) 奖励规则 remark（后台「链动设置 - 奖励规则」展示用）
UPDATE `eb_chain_reward_rule`
   SET `remark` = '见点奖-第1代老板'
 WHERE `type` = 'see' AND `remark` LIKE '%见单奖%';

SELECT 'chain mark wording fix done' AS msg;
-- ===== END: chain_mark_wording_fix_20261011.sql =====

-- ============================================================
-- ===== BEGIN: chain_menu_finalize_20261011.sql =====
-- ============================================================
-- ============================================================
-- 链动后台菜单：收敛到最终形态（幂等 / 可重复执行）2026-10-11
--
-- 最终形态：挂在「大市场」→「2+1链动」目录下，共 7 项
--   链动看板 /chain/dashboard
--   链动产品 /chain/product
--   链动配置 /chain/config
--   链动用户 /chain/users
--   链动佣金 /chain/commission
--   关系变更 /chain/relation     ← 原「滑落日志」+「关系审计」合并
--   团队分红 /chain/dividend
--
-- 本段负责三件事，保证反复执行后结果一致：
--   ① 删除已废弃组件（滑落日志 / 关系审计 / 风控日志，以及早期挂在分销商下的同名菜单）
--   ② 同一 component 去重（保留最小 id）
--   ③ 补齐缺失项并回挂到「2+1链动」目录
-- 放在整个升级脚本的最后执行。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- ① 删除已废弃组件 ----------
-- 注：不要用 FIND_IN_SET + 用户变量（变量与列的 collation 不一致会报 1267）
DELETE FROM `eb_system_role_menu`
 WHERE `menu_id` IN (SELECT `id` FROM `eb_system_menu`
                      WHERE `component` IN (
                        '/chain/slide','/chain/audit','/chain/risk',
                        '/distribution/chainSlide','/distribution/chainAudit','/distribution/chainRisk',
                        '/distribution/chainDashboard','/distribution/chainProduct','/distribution/chainConfig',
                        '/distribution/chainUsers','/distribution/chainCommission','/distribution/chainDividend'));

DELETE FROM `eb_system_menu`
 WHERE `component` IN (
   '/chain/slide','/chain/audit','/chain/risk',
   '/distribution/chainSlide','/distribution/chainAudit','/distribution/chainRisk',
   '/distribution/chainDashboard','/distribution/chainProduct','/distribution/chainConfig',
   '/distribution/chainUsers','/distribution/chainCommission','/distribution/chainDividend');

-- ---------- ② 同一 component 去重（保留最小 id） ----------
DROP TEMPORARY TABLE IF EXISTS `tmp_chain_keep`;
CREATE TEMPORARY TABLE `tmp_chain_keep` (
  `component` varchar(255) NOT NULL,
  `keep_id` int NOT NULL,
  PRIMARY KEY (`component`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
INSERT INTO `tmp_chain_keep` (`component`, `keep_id`)
SELECT `component`, MIN(`id`) FROM `eb_system_menu`
 WHERE `component` LIKE '/chain/%'
 GROUP BY `component` HAVING COUNT(*) > 1;

DELETE rm FROM `eb_system_role_menu` rm
  JOIN `eb_system_menu` m ON m.`id` = rm.`menu_id`
  JOIN `tmp_chain_keep` k ON k.`component` = m.`component`
 WHERE m.`id` <> k.`keep_id`;

DELETE m FROM `eb_system_menu` m
  JOIN `tmp_chain_keep` k ON k.`component` = m.`component`
 WHERE m.`id` <> k.`keep_id`;

DROP TEMPORARY TABLE IF EXISTS `tmp_chain_keep`;

-- ---------- ③ 目录归位：/chain 挂在「大市场」下（找不到则退回「分销商」） ----------
SET @chain_market_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/market' ORDER BY `id` LIMIT 1);
SET @chain_fallback_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/distribution' ORDER BY `id` LIMIT 1);
SET @chain_parent_id := IFNULL(@chain_market_id, IFNULL(@chain_fallback_id, 0));
SET @chain_dir_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/chain' ORDER BY `id` LIMIT 1);

UPDATE `eb_system_menu`
   SET `name` = '2+1链动', `pid` = @chain_parent_id, `menu_type` = 'M', `sort` = 110,
       `is_show` = 1, `is_delte` = 0, `update_time` = NOW()
 WHERE `id` = @chain_dir_id AND @chain_dir_id IS NOT NULL;

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_parent_id, '2+1链动', 'clipboard', NULL, '/chain', 'M', 110, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @chain_dir_id IS NULL AND @chain_parent_id > 0;

SET @chain_dir_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/chain' ORDER BY `id` LIMIT 1);

-- ---------- ④ 补齐 7 个叶子项 ----------
INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动看板', NULL, 'admin:chain:config', '/chain/dashboard', 'C', 80, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dashboard');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动产品', NULL, 'admin:chain:product', '/chain/product', 'C', 75, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/product');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动配置', NULL, 'admin:chain:config', '/chain/config', 'C', 70, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/config');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动用户', NULL, 'admin:chain:users', '/chain/users', 'C', 60, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/users');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动佣金', NULL, 'admin:chain:commission', '/chain/commission', 'C', 50, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/commission');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '关系变更', NULL, 'admin:chain:slide', '/chain/relation', 'C', 40, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/relation');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '团队分红', NULL, 'admin:chain:dividend', '/chain/dividend', 'C', 10, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dividend');

-- ---------- ⑤ 授权补偿：凡已授权「大市场」的角色，补授链动目录与其叶子 ----------
INSERT INTO `eb_system_role_menu` (`rid`,`menu_id`)
SELECT DISTINCT rm.`rid`, m.`id`
  FROM `eb_system_role_menu` rm
  JOIN `eb_system_menu` m ON m.`component` = '/chain' OR m.`component` LIKE '/chain/%'
 WHERE rm.`menu_id` = @chain_parent_id
   AND NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` x
                    WHERE x.`rid` = rm.`rid` AND x.`menu_id` = m.`id`);

SELECT 'chain menu finalize done' AS msg;
-- ===== END: chain_menu_finalize_20261011.sql =====

SET FOREIGN_KEY_CHECKS = 1;
SELECT 'CHAIN 2+1 deploy done' AS msg;
