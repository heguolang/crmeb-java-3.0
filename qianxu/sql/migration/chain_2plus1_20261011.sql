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
