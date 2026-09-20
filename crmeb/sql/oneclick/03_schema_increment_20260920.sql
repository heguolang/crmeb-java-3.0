-- +----------------------------------------------------------------------
-- | CRMEB Java 3.0 —— 订货 / 订货商 / 门店 / 区域代理 模块「结构增量」脚本
-- +----------------------------------------------------------------------
-- 用途：线上已有库（已存在业务数据）只补「表结构 / 字段」，不清数据、不导设置项。
-- 内容：本模块自研的全部自建表 + 在原版表上新增的字段。
-- 幂等：可重复执行。建表用 CREATE TABLE IF NOT EXISTS，加字段先查 information_schema。
-- 说明：菜单/权限/系统设置等「数据类」补丁不在此脚本内，需要时用 02_patches_all.sql。
-- 生成：由 build_schema_increment.py 从 02_patches_all.sql 自动抽取，生成日期 2026-09-20。
-- 执行：mysql -uroot -p密码 --default-character-set=utf8mb4 库名 < 03_schema_increment_20260920.sql
-- +----------------------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET @db = DATABASE();



-- ============================================================
-- 来源分节：add_missing_team_level_tables.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_system_team_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '团队等级名称',
  `grade` int NOT NULL DEFAULT '1' COMMENT '团队等级序号，数值越大等级越高',
  `self_order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '自购订单金额门槛(元)',
  `team_order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '团队订单金额门槛(元)',
  `direct_order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '直推订单金额门槛(元)',
  `self_team_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '自购与团队条件关系：1=与，2=或',
  `team_direct_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '团队与直推条件关系：1=与，2=或',
  `direct_level_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推金额与直推等级人数条件关系：1=与，2=或',
  `direct_level_id` int NOT NULL DEFAULT '0' COMMENT '直推等级人数-目标用户等级id(来源eb_system_user_level)，0=未启用',
  `direct_level_count` int NOT NULL DEFAULT '0' COMMENT '直推达到目标用户等级的人数门槛，0=未启用',
  `team_level_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推等级人数与团队级别人数条件关系：1=与，2=或',
  `team_level_id` int NOT NULL DEFAULT '0' COMMENT '团队级别人数-目标用户等级id(来源eb_system_user_level)，0=未启用',
  `team_level_count` int NOT NULL DEFAULT '0' COMMENT '团队中达到目标用户等级的人数门槛，0=未启用',
  `direct_order_trigger_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '直推订单统计时机：1=支付成功，2=订单完成',
  `self_order_trigger_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '自购订单统计时机：1=支付成功，2=订单完成',
  `team_order_trigger_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '团队订单统计时机：1=支付成功，2=订单完成',
  `description` varchar(500) DEFAULT NULL COMMENT '等级权益描述',
  `icon` varchar(255) NOT NULL DEFAULT '' COMMENT '等级图标',
  `is_show` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示：1=显示，0=隐藏',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_grade` (`grade`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='团队等级表';

CREATE TABLE IF NOT EXISTS `eb_system_team_level_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `team_level_id` int NOT NULL COMMENT '团队等级ID，关联 eb_system_team_level.id',
  `team_brokerage_rate` int NOT NULL DEFAULT '0' COMMENT '团队极差比例(%)',
  `peer_award_rate` int NOT NULL DEFAULT '0' COMMENT '平级奖比例(%)',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_team_level_id` (`team_level_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='团队等级配置表';

CREATE TABLE IF NOT EXISTS `eb_system_user_level_brokerage` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `level_id` int NOT NULL COMMENT '会员等级ID，关联 eb_system_user_level.id',
  `self_brokerage_rate` int NOT NULL DEFAULT '0' COMMENT '自购返佣比例(%)',
  `brokerage_rate_one` int NOT NULL DEFAULT '0' COMMENT '一级返佣比例(%)',
  `brokerage_rate_two` int NOT NULL DEFAULT '0' COMMENT '二级返佣比例(%)',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_level_id` (`level_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='会员等级返佣配置表';

CREATE TABLE IF NOT EXISTS `eb_user_team_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '用户uid',
  `team_level_id` int NOT NULL DEFAULT '0' COMMENT '团队等级ID',
  `grade` int NOT NULL DEFAULT '0' COMMENT '团队等级序号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '0:禁止,1:正常',
  `mark` varchar(255) DEFAULT NULL COMMENT '备注',
  `remind` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已通知',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除,0=未删除,1=删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_uid` (`uid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户团队等级记录表';

CREATE TABLE IF NOT EXISTS `eb_user_team_level_stat` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '用户uid',
  `self_paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '自购已支付累计金额(元)',
  `self_complete_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '自购已完成累计金额(元)',
  `team_paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '团队已支付累计金额(元)',
  `team_complete_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '团队已完成累计金额(元)',
  `direct_paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '直推已支付累计金额(元)',
  `direct_complete_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '直推已完成累计金额(元)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_uid` (`uid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户团队等级统计表';


-- ============================================================
-- 来源分节：add_missing_columns.sql
-- ============================================================

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='upgrade_type') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `upgrade_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''升级条件类型：1=累计消费金额，2=累计订单数，3=两者同时满足'' AFTER `experience`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='consumption_trigger_type') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `consumption_trigger_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''消费金额统计时机：1=已付款，2=交易完成'' AFTER `upgrade_type`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='order_count_trigger_type') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `order_count_trigger_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''订单数统计时机：1=已付款，2=交易完成'' AFTER `consumption_trigger_type`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='upgrade_value') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `upgrade_value` int NOT NULL DEFAULT 0 COMMENT ''累计订单数升级门槛'' AFTER `order_count_trigger_type`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='give_integral') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `give_integral` int NOT NULL DEFAULT 0 COMMENT ''等级赠送积分（每单固定赠送，手输多少送多少）'' AFTER `upgrade_value`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='description') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `description` varchar(500) DEFAULT NULL COMMENT ''等级权益描述'' AFTER `give_integral`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user_level' AND COLUMN_NAME='give_integral') = 0,
    'ALTER TABLE `eb_user_level` ADD COLUMN `give_integral` int NOT NULL DEFAULT 0 COMMENT ''等级赠送积分''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='team_level') = 0,
    'ALTER TABLE `eb_user` ADD COLUMN `team_level` int NOT NULL DEFAULT 0 COMMENT ''团队等级ID（eb_system_team_level.id），0=无''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：upgrade_team_level_direct.sql
-- ============================================================

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_order_amount') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_order_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''直推订单金额门槛(元)''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_order_trigger_type') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_order_trigger_type` tinyint(1) NOT NULL DEFAULT 2 COMMENT ''直推订单统计时机：1=支付成功，2=订单完成''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'self_team_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `self_team_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''自购与团队条件关系：1=与，2=或''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_direct_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_direct_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''团队与直推条件关系：1=与，2=或''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_level_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_level_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''直推金额与直推等级人数条件关系：1=与，2=或''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_level_id') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_level_id` int NOT NULL DEFAULT 0 COMMENT ''直推等级人数-目标用户等级id，0=未启用''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_level_count') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_level_count` int NOT NULL DEFAULT 0 COMMENT ''直推达到目标用户等级的人数门槛，0=未启用''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_level_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_level_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''直推等级人数与团队级别人数条件关系：1=与，2=或''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_level_id') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_level_id` int NOT NULL DEFAULT 0 COMMENT ''团队级别人数-目标用户等级id，0=未启用''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_level_count') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_level_count` int NOT NULL DEFAULT 0 COMMENT ''团队中达到目标用户等级的人数门槛，0=未启用''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'condition_relation') > 0,
    'ALTER TABLE `eb_system_team_level` DROP COLUMN `condition_relation`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user_team_level_stat' AND COLUMN_NAME = 'direct_paid_amount') = 0,
    'ALTER TABLE `eb_user_team_level_stat` ADD COLUMN `direct_paid_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''直推已支付累计金额(元)''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user_team_level_stat' AND COLUMN_NAME = 'direct_complete_amount') = 0,
    'ALTER TABLE `eb_user_team_level_stat` ADD COLUMN `direct_complete_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''直推已完成累计金额(元)''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：add_admin_log_menu_and_table.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_admin_login_log` (
  `id`            int          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id`      int          NOT NULL DEFAULT 0 COMMENT '管理员id（登录失败时为0）',
  `admin_account` varchar(32)  NOT NULL DEFAULT '' COMMENT '登录账号',
  `ip`            varchar(50)           DEFAULT '' COMMENT '登录IP',
  `location`      varchar(100)          DEFAULT '' COMMENT '登录地点',
  `browser`       varchar(100)          DEFAULT '' COMMENT '浏览器',
  `os`            varchar(100)          DEFAULT '' COMMENT '操作系统',
  `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态 1成功 0失败',
  `msg`           varchar(255)          DEFAULT '' COMMENT '提示信息',
  `create_time`   timestamp    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_account` (`admin_account`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员登录日志表';


-- ============================================================
-- 来源分节：agent.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_agent` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '代理级别：1=省级 2=市级 3=区级',
  `province` varchar(64) NOT NULL DEFAULT '' COMMENT '省',
  `city` varchar(64) NOT NULL DEFAULT '' COMMENT '市',
  `district` varchar(64) NOT NULL DEFAULT '' COMMENT '区/县',
  `region_name` varchar(192) NOT NULL DEFAULT '' COMMENT '区域全称（省市区拼接）',
  `match_key` varchar(64) NOT NULL DEFAULT '' COMMENT '订单地址匹配关键词（省名/市名/区名）',
  `ratio` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待审核 1=已通过 2=已拒绝',
  `apply_mark` varchar(255) NOT NULL DEFAULT '' COMMENT '申请说明/后台备注',
  `check_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='区域代理';

CREATE TABLE IF NOT EXISTS `eb_agent_reward` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agent_id` int NOT NULL COMMENT '代理ID（eb_agent.id）',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `order_id` varchar(32) NOT NULL COMMENT '订单号',
  `order_pay_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单实付金额',
  `ratio` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `reward_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '奖励金额',
  `region_name` varchar(192) NOT NULL DEFAULT '' COMMENT '命中的代理区域',
  `record_id` int NOT NULL DEFAULT '0' COMMENT '关联佣金记录ID（eb_user_brokerage_record.id）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=待入账 2=已入账 3=已失效',
  `credit_time` datetime DEFAULT NULL COMMENT '入账时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_agent` (`order_id`,`agent_id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='区域代理奖励明细';


-- ============================================================
-- 来源分节：stock.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_stock_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '层级名称（总代/一级代理/二级代理/普通代理）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（小=高层级，用于限制发展下级）',
  `discount` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '默认拿货折扣%（0=完全按价格表，无默认折扣）',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-代理层级配置';

CREATE TABLE IF NOT EXISTS `eb_stock_agent` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `level_id` int NOT NULL COMMENT '层级ID（eb_stock_level.id）',
  `parent_id` int NOT NULL DEFAULT '0' COMMENT '上级代理ID（0=上级为总部/平台）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_level` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-订货代理（树形层级）';

CREATE TABLE IF NOT EXISTS `eb_stock_price` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` int NOT NULL COMMENT '商品ID（eb_store_product.id）',
  `level_id` int NOT NULL COMMENT '层级ID',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '该层级拿货价',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_level` (`product_id`,`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-商品层级拿货价';

CREATE TABLE IF NOT EXISTS `eb_stock_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) NOT NULL COMMENT '订货单号',
  `uid` int NOT NULL COMMENT '下单代理用户UID',
  `agent_id` int NOT NULL COMMENT '下单代理ID（eb_stock_agent.id）',
  `parent_agent_id` int NOT NULL DEFAULT '0' COMMENT '直接上级代理ID（0=上级为总部）',
  `level_name` varchar(32) NOT NULL DEFAULT '' COMMENT '下单时层级名称（冗余）',
  `total_num` int NOT NULL DEFAULT '0' COMMENT '商品总数量',
  `total_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总额（按下单者拿货价）',
  `pay_type` tinyint NOT NULL DEFAULT '2' COMMENT '付款方式：1=微信线上支付 2=后台记账欠款',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '付款状态：0=未付款 1=已付款',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待上级审核 1=待付款 2=待发货 3=待收货 4=已完成 -1=上级驳回',
  `reject_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
  `audit_time` datetime DEFAULT NULL COMMENT '上级审核时间',
  `pay_time` datetime DEFAULT NULL COMMENT '付款时间',
  `send_time` datetime DEFAULT NULL COMMENT '总部发货时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `express_name` varchar(64) NOT NULL DEFAULT '' COMMENT '快递公司',
  `express_num` varchar(64) NOT NULL DEFAULT '' COMMENT '快递单号',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '订单备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_uid` (`uid`),
  KEY `idx_parent` (`parent_agent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-订货订单';

CREATE TABLE IF NOT EXISTS `eb_stock_order_product` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` int NOT NULL COMMENT '订货订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) NOT NULL DEFAULT '' COMMENT '商品名称（冗余）',
  `image` varchar(256) NOT NULL DEFAULT '' COMMENT '商品图（冗余）',
  `num` int NOT NULL DEFAULT '1' COMMENT '数量',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '下单者拿货价（单价）',
  `parent_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '上级拿货价（单价，0=上级为总部无差价）',
  `total_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '小计 = num * price',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-订货订单明细';

CREATE TABLE IF NOT EXISTS `eb_stock_exchange` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exchange_no` varchar(32) NOT NULL COMMENT '换货单号',
  `uid` int NOT NULL COMMENT '申请代理用户UID',
  `agent_id` int NOT NULL COMMENT '申请代理ID',
  `parent_agent_id` int NOT NULL DEFAULT '0' COMMENT '直接上级代理ID',
  `order_id` int NOT NULL COMMENT '关联订货订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) NOT NULL DEFAULT '' COMMENT '商品名称（冗余）',
  `num` int NOT NULL DEFAULT '1' COMMENT '换货数量',
  `reason` varchar(255) NOT NULL DEFAULT '' COMMENT '换货原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待上级审核 1=待总部审核 2=待旧品退回 3=待发新品 4=已完成 -1=驳回',
  `reject_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
  `back_express_name` varchar(64) NOT NULL DEFAULT '' COMMENT '旧品退回快递公司',
  `back_express_num` varchar(64) NOT NULL DEFAULT '' COMMENT '旧品退回快递单号',
  `new_express_name` varchar(64) NOT NULL DEFAULT '' COMMENT '新品发出快递公司',
  `new_express_num` varchar(64) NOT NULL DEFAULT '' COMMENT '新品发出快递单号',
  `audit_time` datetime DEFAULT NULL COMMENT '上级审核时间',
  `hq_audit_time` datetime DEFAULT NULL COMMENT '总部审核时间',
  `back_time` datetime DEFAULT NULL COMMENT '旧品核验入库时间（回补库存）',
  `send_time` datetime DEFAULT NULL COMMENT '新品发出时间（扣库存）',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exchange_no` (`exchange_no`),
  KEY `idx_uid` (`uid`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-换货单';

CREATE TABLE IF NOT EXISTS `eb_stock_reward` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '得奖代理用户UID',
  `type` tinyint NOT NULL COMMENT '奖励类型：1=差价奖励 2=团队级差奖励 3=平级奖励',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '业绩来源：1=订货单 2=换货单',
  `order_no` varchar(32) NOT NULL DEFAULT '' COMMENT '关联单号（订货单号/换货单号）',
  `link_uid` int NOT NULL DEFAULT '0' COMMENT '产生业绩的下级用户UID',
  `base_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '计算基数（业绩金额/差价基数）',
  `rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '比例（%）',
  `reward_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '奖励金额',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '说明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=已入账 -1=已失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_order` (`order_no`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-奖金明细';

CREATE TABLE IF NOT EXISTS `eb_stock_ladder` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `min_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩下限（含）',
  `max_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩上限（0=不限）',
  `rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-团队级差阶梯';

CREATE TABLE IF NOT EXISTS `eb_stock_withdraw` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `withdraw_no` varchar(32) NOT NULL COMMENT '提现单号',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '提现金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待审核 1=已打款 -1=驳回',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '申请备注（收款方式等）',
  `audit_mark` varchar(255) NOT NULL DEFAULT '' COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-奖金提现';

CREATE TABLE IF NOT EXISTS `eb_stock_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` int NOT NULL COMMENT '商品ID',
  `type` tinyint NOT NULL COMMENT '类型：1=审核通过扣库存 2=驳回回补 3=换货新品扣 4=换货旧品回补 5=手动调整',
  `change_num` int NOT NULL DEFAULT '0' COMMENT '变动数量（±）',
  `before_stock` int NOT NULL DEFAULT '0' COMMENT '变动前库存',
  `after_stock` int NOT NULL DEFAULT '0' COMMENT '变动后库存',
  `link_no` varchar(32) NOT NULL DEFAULT '' COMMENT '关联单号',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-云仓库存变动日志';

CREATE TABLE IF NOT EXISTS `eb_stock_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '接收用户UID',
  `type` tinyint NOT NULL COMMENT '类型：1=订单审核 2=发货通知 3=奖金到账 4=提现审核',
  `title` varchar(64) NOT NULL DEFAULT '' COMMENT '标题',
  `content` varchar(500) NOT NULL DEFAULT '' COMMENT '内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0=未读 1=已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-消息通知';


-- ============================================================
-- 来源分节：stock_upgrade_conditions.sql
-- ============================================================

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='cond_self_buy')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN cond_self_buy TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''启用条件：自购消费满额自动升级''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='self_buy_amount')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN self_buy_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''自购消费门槛（元）''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='cond_direct')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN cond_direct TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''启用条件：直推订单总业绩''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='direct_order_amount')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN direct_order_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''直推订单总业绩门槛（元）''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='cond_team')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN cond_team TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''启用条件：团队伞下业绩''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='team_amount')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN team_amount DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''团队伞下业绩门槛（元）''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='cond_product')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN cond_product TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''启用条件：购买指定产品升级''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='upgrade_product_ids')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN upgrade_product_ids VARCHAR(500) NOT NULL DEFAULT '''' COMMENT ''指定升级产品ID，英文逗号分隔''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='condition_logic')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN condition_logic TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''条件组合：0=满足任一(或) 1=全部满足(与)''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_level' AND COLUMN_NAME='peer_rate')=0,
  'ALTER TABLE eb_stock_level ADD COLUMN peer_rate DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT ''平级奖比例(%)''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='up_search_time')=0,
  'ALTER TABLE eb_stock_order ADD COLUMN up_search_time DATETIME NULL COMMENT ''向上查找上级库存的时间''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='up_search_num')=0,
  'ALTER TABLE eb_stock_order ADD COLUMN up_search_num INT NOT NULL DEFAULT 0 COMMENT ''已向上查找次数''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：stock_changelog_product_rel.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_stock_change_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) DEFAULT NULL COMMENT '订货商ID(eb_stock_agent.id)',
  `uid` int(11) DEFAULT NULL COMMENT '用户UID',
  `type` tinyint(4) DEFAULT NULL COMMENT '变更类型：1=新增 2=层级变更 3=上级变更 4=状态变更 5=删除',
  `old_value` varchar(255) DEFAULT NULL COMMENT '变更前',
  `new_value` varchar(255) DEFAULT NULL COMMENT '变更后',
  `mark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货商变更记录';

CREATE TABLE IF NOT EXISTS `eb_stock_product_rel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品ID(eb_store_product.id)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参与订货的商品关联';


-- ============================================================
-- 来源分节：merchant_store.sql
-- ============================================================

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='self_pickup')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `self_pickup` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否支持到店自提''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='delivery')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `delivery` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否支持上门配送''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='delivery_radius')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `delivery_radius` decimal(10,2) NOT NULL DEFAULT 5.00 COMMENT ''配送服务半径(公里)''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='verify_fee')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `verify_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''门店核销服务费''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='pickup_fee')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `pickup_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''到店自提服务费''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='delivery_fee')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `delivery_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''上门配送服务费''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='leader_uid')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `leader_uid` int(11) NOT NULL DEFAULT 0 COMMENT ''门店负责人用户UID''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_store' AND COLUMN_NAME='leader_name')=0,
  'ALTER TABLE `eb_system_store` ADD COLUMN `leader_name` varchar(64) DEFAULT '''' COMMENT ''门店负责人昵称''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='is_store')=0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `is_store` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否支持门店服务''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='store_self_pickup')=0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `store_self_pickup` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''门店是否支持自提''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='store_delivery')=0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `store_delivery` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''门店是否支持配送''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `eb_store_verify_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `store_id` int(11) NOT NULL COMMENT '门店ID',
  `store_name` varchar(128) DEFAULT '' COMMENT '门店名称',
  `order_id` int(11) DEFAULT NULL COMMENT '订单ID',
  `order_no` varchar(32) DEFAULT '' COMMENT '订单号',
  `verify_code` varchar(32) DEFAULT '' COMMENT '核销码',
  `product_info` varchar(1024) DEFAULT '' COMMENT '核销商品概要',
  `verify_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '核销方式：1=核销码',
  `service_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '本次核销服务费',
  `pay_price` decimal(10,2) DEFAULT NULL COMMENT '订单支付金额',
  `order_status` tinyint(4) DEFAULT NULL COMMENT '核销后订单状态',
  `verify_uid` int(11) DEFAULT NULL COMMENT '核销操作人UID',
  `verify_name` varchar(64) DEFAULT '' COMMENT '核销操作人昵称',
  `verify_source` tinyint(4) NOT NULL DEFAULT 1 COMMENT '核销来源：1=门店端 2=后台',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店核销记录';


-- ============================================================
-- 来源分节：stock_rework_phase1.sql
-- ============================================================

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='address_id') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `address_id` int(11) DEFAULT NULL COMMENT ''下单时选用的用户地址ID''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='real_name') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `real_name` varchar(64) DEFAULT '''' COMMENT ''收货人姓名（下单地址快照）''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='phone') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `phone` varchar(32) DEFAULT '''' COMMENT ''收货人电话（下单地址快照）''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='user_address') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `user_address` varchar(500) DEFAULT '''' COMMENT ''收货地址（下单地址快照）''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='stock_type') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `stock_type` tinyint(1) DEFAULT 1 COMMENT ''库存类型：1=实体 2=虚拟''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='order_type') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `order_type` tinyint(1) DEFAULT 1 COMMENT ''订单类型：1=采购 2=虚拟提货 3=换货''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='cancel_time') = 0,
    'ALTER TABLE `eb_stock_order` ADD COLUMN `cancel_time` datetime DEFAULT NULL COMMENT ''取消时间''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order_product' AND COLUMN_NAME='sku_key') = 0,
    'ALTER TABLE `eb_stock_order_product` ADD COLUMN `sku_key` varchar(64) DEFAULT '''' COMMENT ''规格标识（空=商品级）''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：stock_rework_phase2.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_stock_virtual_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int(11) NOT NULL COMMENT '会员UID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `product_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称（冗余）',
  `image` varchar(512) NOT NULL DEFAULT '' COMMENT '商品图（冗余）',
  `sku_key` varchar(64) NOT NULL DEFAULT '' COMMENT '规格标识（预留，空=商品级）',
  `num` int(11) NOT NULL DEFAULT 0 COMMENT '累计入账数量',
  `remain_num` int(11) NOT NULL DEFAULT 0 COMMENT '剩余可提货数量',
  `source_order_no` varchar(32) NOT NULL DEFAULT '' COMMENT '最近一次入账来源订货单号',
  `parent_agent_id` int(11) NOT NULL DEFAULT 0 COMMENT '入账时订单上级代理快照（提货单沿用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-会员虚拟库存';


-- ============================================================
-- 来源分节：stock_rework_phase3.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS eb_stock_price_sku (
  id int(11) NOT NULL AUTO_INCREMENT,
  level_id int(11) NOT NULL DEFAULT 0,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  price decimal(10,2) NOT NULL DEFAULT 0.00,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_level_product_sku (level_id, product_id, sku_key),
  KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS eb_stock_exchange_config (
  id int(11) NOT NULL AUTO_INCREMENT,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  enable tinyint(1) NOT NULL DEFAULT 1,
  min_target_price decimal(10,2) NOT NULL DEFAULT 0.00,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_sku (product_id, sku_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='sku_key') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `sku_key` varchar(120) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='exchange_type') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `exchange_type` tinyint(1) NOT NULL DEFAULT 2',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='target_product_id') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `target_product_id` int(11) NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='target_sku_key') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `target_sku_key` varchar(120) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='target_product_name') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `target_product_name` varchar(255) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='target_price') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `target_price` decimal(10,2) NOT NULL DEFAULT 0.00',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='origin_price') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `origin_price` decimal(10,2) NOT NULL DEFAULT 0.00',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='diff_price') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `diff_price` decimal(10,2) NOT NULL DEFAULT 0.00',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='diff_pay_status') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `diff_pay_status` tinyint(1) NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='diff_pay_type') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `diff_pay_type` varchar(20) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='diff_pay_time') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `diff_pay_time` datetime DEFAULT NULL',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='real_name') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `real_name` varchar(64) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='phone') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `phone` varchar(20) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='user_address') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `user_address` varchar(255) NOT NULL DEFAULT ''''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='address_id') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `address_id` int(11) NOT NULL DEFAULT 0',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：stock_offline_sale.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS eb_stock_offline_sale (
  id int(11) NOT NULL AUTO_INCREMENT,
  uid int(11) NOT NULL DEFAULT 0,
  agent_id int(11) NOT NULL DEFAULT 0,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  product_name varchar(255) NOT NULL DEFAULT '',
  image varchar(512) NOT NULL DEFAULT '',
  num int(11) NOT NULL DEFAULT 0,
  mark varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_del tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_uid (uid),
  KEY idx_agent_product (agent_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 来源分节：stock_exchange_target.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS eb_stock_exchange_target (
  id int(11) NOT NULL AUTO_INCREMENT,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  target_product_id int(11) NOT NULL DEFAULT 0,
  target_sku_key varchar(120) NOT NULL DEFAULT '',
  target_product_name varchar(255) NOT NULL DEFAULT '',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_src_target (product_id, sku_key, target_product_id, target_sku_key),
  KEY idx_src (product_id, sku_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================================
-- 来源分节：stock_product_stock_type.sql
-- ============================================================

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_product_rel' AND COLUMN_NAME='support_virtual') = 0,
    'ALTER TABLE `eb_stock_product_rel` ADD COLUMN `support_virtual` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''支持虚拟库存 1=是 0=否''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_product_rel' AND COLUMN_NAME='support_physical') = 0,
    'ALTER TABLE `eb_stock_product_rel` ADD COLUMN `support_physical` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''支持实体库存 1=是 0=否''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：stock_issue_batch_20260918.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS `eb_stock_adjust_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) NOT NULL DEFAULT 0 COMMENT 'stock agent id',
  `uid` int(11) NOT NULL DEFAULT 0 COMMENT 'user id',
  `product_id` int(11) NOT NULL DEFAULT 0,
  `sku_key` varchar(128) NOT NULL DEFAULT '' COMMENT 'sku key, empty = whole product',
  `stock_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=physical 2=virtual',
  `num` int(11) NOT NULL DEFAULT 0 COMMENT 'positive=add negative=deduct',
  `mark` varchar(255) NOT NULL DEFAULT '',
  `link_uid` int(11) NOT NULL DEFAULT 0 COMMENT 'related sub user uid, virtual transfer buyer; 0=none',
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='stock agent inventory adjust log';

CREATE TABLE IF NOT EXISTS `eb_agent_change_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) NOT NULL DEFAULT 0,
  `uid` int(11) NOT NULL DEFAULT 0,
  `type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=add 2=level change 3=status change 4=delete 5=ratio change',
  `old_value` varchar(255) NOT NULL DEFAULT '',
  `new_value` varchar(255) NOT NULL DEFAULT '',
  `mark` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='reseller agent change log';


-- ============================================================
-- 来源分节：stock_adjust_log_link_uid.sql
-- ============================================================

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_adjust_log' AND COLUMN_NAME='link_uid') = 0,
    'ALTER TABLE `eb_stock_adjust_log` ADD COLUMN `link_uid` int NOT NULL DEFAULT 0 COMMENT ''关联下级UID：虚拟库存转卖的采购人（下单会员UID），0=无'' AFTER `mark`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 来源分节：stock_exchange_target_type.sql
-- ============================================================

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='target_stock_type') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `target_stock_type` tinyint NULL DEFAULT 1 COMMENT ''换入库存类型：1=实体 2=虚拟（虚拟换货时会员可选）；NULL/1=实体'' AFTER `target_sku_key`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- ============================================================
-- 执行结果自检
-- ============================================================
SET FOREIGN_KEY_CHECKS = 1;
SELECT '结构增量脚本执行完成' AS result;
