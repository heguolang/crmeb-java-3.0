-- ============================================================
-- CRMEB Java 3.0 一键补丁合集（MySQL 5.7 / 宝塔兼容，幂等）
-- 修复: 去掉 ADD COLUMN IF NOT EXISTS、去掉 AFTER 依赖缺失列、去掉 DELIMITER
-- 域名: http://api.qianxutec.com
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== BEGIN: mariadb_any_value_compat.sql ==========
-- MariaDB 兼容补丁：CRMEB 部分 SQL 使用了 MySQL 5.7 的内置函数 ANY_VALUE()，
-- MariaDB 没有该函数，会报 "FUNCTION crmeb.ANY_VALUE does not exist"。
-- 本脚本创建同名兼容函数（恒等返回）。只需执行一次，随库持久保存。
-- 适用：MariaDB 10.x（本部署 10.6.28）
-- USE removed by oneclick
CREATE FUNCTION IF NOT EXISTS ANY_VALUE(x LONGTEXT) RETURNS LONGTEXT
DETERMINISTIC NO SQL
RETURN x;

-- ========== END: mariadb_any_value_compat.sql ==========

-- ========== BEGIN: add_missing_team_level_tables.sql ==========
-- ============================================================
-- 补齐：代码里有实体类、但 Crmeb_v3.0.sql 全量包中缺失的 5 张表
--
-- 背景：这 5 张表属于「团队等级 / 会员等级返佣」二次开发功能，
--       此前只在本地库手工创建过，未落任何脚本。第三方拉取仓库后
--       直接执行全量 SQL 会缺表，启动或访问相关功能即报错。
--
-- 执行顺序：必须【先】于 upgrade_team_level_direct.sql 执行
--           （该脚本是给 eb_system_team_level 追加列的，表不存在会失败）
-- 幂等：CREATE TABLE IF NOT EXISTS，可重复执行。
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 团队等级主表
-- ----------------------------
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

-- ----------------------------
-- 团队等级配置（极差比例 / 平级奖比例）
-- ----------------------------
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

-- ----------------------------
-- 会员等级返佣配置
-- ----------------------------
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

-- ----------------------------
-- 用户团队等级记录（业务运行时数据，脚本只建表不含数据）
-- ----------------------------
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

-- ----------------------------
-- 用户团队等级统计（自购/团队/直推累计金额，同上只建表）
-- ----------------------------
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

-- ========== END: add_missing_team_level_tables.sql ==========

-- ========== BEGIN: add_missing_columns.sql ==========
-- ============================================================
-- 补齐：代码实体中已使用、但 Crmeb_v3.0.sql 全量包中缺失的字段
--
-- 这些字段属于「会员等级升级条件 / 等级赠送积分 / 用户团队等级」二次开发，
-- 此前只在本地库手工 ALTER 过，未落脚本。第三方直接执行全量 SQL 会缺列，
-- 后台编辑会员等级或运行时会报 Unknown column。
--
-- 幂等：先查 information_schema 判断列是否存在，不存在才 ALTER，可重复执行。
-- ============================================================

SET @db = DATABASE();

-- ----------------------------
-- eb_system_user_level（会员等级）
-- ----------------------------
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

-- ----------------------------
-- eb_user_level（用户等级记录）
-- ----------------------------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user_level' AND COLUMN_NAME='give_integral') = 0,
    'ALTER TABLE `eb_user_level` ADD COLUMN `give_integral` int NOT NULL DEFAULT 0 COMMENT ''等级赠送积分''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------
-- eb_user（用户）
-- ----------------------------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='team_level') = 0,
    'ALTER TABLE `eb_user` ADD COLUMN `team_level` int NOT NULL DEFAULT 0 COMMENT ''团队等级ID（eb_system_team_level.id），0=无''',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========== END: add_missing_columns.sql ==========

-- ========== BEGIN: upgrade_team_level_direct.sql ==========
-- ============================================================
-- 团队等级条件关系改版（MySQL 5.7 / 宝塔兼容，幂等）
-- 兼容旧表（仅有 self/team_order_amount）与新表
-- 不加 AFTER：避免引用尚未存在的列导致 1054
-- ============================================================

SET @db = DATABASE();

-- 工具宏：列不存在则 ADD（无 AFTER）
-- direct_order_amount
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

-- 删除旧的全局条件关系列（若存在）
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'condition_relation') > 0,
    'ALTER TABLE `eb_system_team_level` DROP COLUMN `condition_relation`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 用户团队统计表补直推金额列（旧库无此字段会导致团队关联用户页 1054）
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

-- ========== END: upgrade_team_level_direct.sql ==========

-- ========== BEGIN: add_login_notice_config.sql ==========
-- ============================================================
-- 需求4：未登录用户进入首页提示「去登录」弹窗 + 后台开关
-- 位置：后台 -> 设置 -> 系统基础配置 (form_id = 148)
-- 幂等：可重复执行
-- ============================================================

-- 1. 新增两个配置项（开关 + 文案）
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'login_notice_switch', 'login_notice_switch', 148, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `eb_system_config`) t WHERE t.`name` = 'login_notice_switch');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'login_notice_text', 'login_notice_text', 148, '登录后即可享受完整服务，是否前往登录？', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `eb_system_config`) t WHERE t.`name` = 'login_notice_text');

-- 2. 在 form_id=148 的 content JSON 末尾 fields 数组中追加两个字段定义
--    el-switch   -> login_notice_switch
--    el-input    -> login_notice_text
UPDATE `eb_system_form_temp`
SET `content` = CONCAT(
      LEFT(`content`, CHAR_LENGTH(`content`) - 2),
      ',',
      '{"__config__":{"label":"未登录访问首页提示登录：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-switch","tagIcon":"switch","required":false,"tips":false,"tipsDesc":"","tipsIsLink":false,"tipsLink":"","layout":"colFormItem","span":24,"document":"https://element.eleme.cn/#/zh-CN/component/switch","formId":120,"renderKey":1762845400001,"defaultValue":"1"},"active-text":"开启","inactive-text":"关闭","active-color":"#13ce66","inactive-color":"#ff4949","active-value":"1","inactive-value":"0","disabled":false,"__vModel__":"login_notice_switch"},',
      '{"__config__":{"label":"提示登录文案：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-input","tagIcon":"input","required":false,"tips":false,"tipsDesc":"","tipsIsLink":false,"tipsLink":"","layout":"colFormItem","span":24,"document":"https://element.eleme.cn/#/zh-CN/component/input","formId":121,"renderKey":1762845400002},"__slot__":{"prepend":"","append":""},"placeholder":"请输入提示登录文案：","style":{"width":"50%"},"clearable":true,"prefix-icon":"","suffix-icon":"","maxlength":100,"show-word-limit":false,"readonly":false,"disabled":false,"__vModel__":"login_notice_text"}',
      ']}'
    )
WHERE `id` = 148
  AND `content` LIKE '%news_slides_limit%'
  AND `content` NOT LIKE '%login_notice_switch%';

-- 3. 校验
SELECT `id`, `name`, `title`, `form_id`, `value` FROM `eb_system_config` WHERE `name` IN ('login_notice_switch','login_notice_text');

-- ========== END: add_login_notice_config.sql ==========

-- ========== BEGIN: add_user_update_password_menu.sql ==========
-- ============================================================
-- 需求3：后台用户管理「更多」新增「修改登录密码」权限点
-- 父级：39 用户管理
-- 幂等：可重复执行
-- ============================================================

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 39, '修改登录密码', '', 'admin:user:update:password', '', 'A', 99999, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:user:update:password'
);

-- 校验
SELECT id, pid, name, perms, menu_type FROM eb_system_menu WHERE perms = 'admin:user:update:password';

-- ========== END: add_user_update_password_menu.sql ==========

-- ========== BEGIN: add_admin_log_menu_and_table.sql ==========
-- ============================================================
-- 需求6：管理员登录日志 + 操作日志（菜单/权限）
-- 幂等：可重复执行
-- ============================================================

-- ---------- 1. 管理员登录日志表 ----------
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

-- ---------- 2. 菜单：设置(12) 下新增「日志管理」分组 ----------
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 12, '日志管理', '', '', '/operation/logManager', 'M', 8, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t
  WHERE t.`component` = '/operation/logManager'
);

SET @log_parent := (SELECT id FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`component` = '/operation/logManager' LIMIT 1);

-- 管理员登录日志（菜单 C）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '管理员登录日志', '', 'admin:log:login:list', '/operation/logManager/adminLoginLog', 'C', 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:login:list'
);

-- 管理员操作日志（菜单 C）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '管理员操作日志', '', 'admin:log:sensitive:list', '/operation/logManager/adminOperateLog', 'C', 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t
  WHERE t.`perms` = 'admin:log:sensitive:list' AND t.`pid` <> 0
);

-- ---------- 3. 清理历史脏数据 ----------
-- 初始 SQL 里 admin:log:sensitive:list 是 pid=0 的孤儿记录（id=569），
-- 上面已按 perms 新建了挂到「日志管理」下的正式菜单，这里删掉孤儿避免重复。
DELETE FROM `eb_system_menu` WHERE `perms` = 'admin:log:sensitive:list' AND `pid` = 0;

-- 登录日志页面的操作级权限点
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '登录日志详情', '', 'admin:log:login:info', '', 'A', 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:login:info'
);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '删除登录日志', '', 'admin:log:login:delete', '', 'A', 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:login:delete'
);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '删除操作日志', '', 'admin:log:sensitive:delete', '', 'A', 3, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:sensitive:delete'
);

-- ---------- 4. 校验 ----------
SELECT id, pid, name, perms, component, menu_type, sort, is_show
FROM eb_system_menu
WHERE component = '/operation/logManager'
   OR perms LIKE 'admin:log:%'
   OR pid = (SELECT id FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`component` = '/operation/logManager' LIMIT 1)
ORDER BY menu_type, sort, id;

-- ========== END: add_admin_log_menu_and_table.sql ==========

-- ========== BEGIN: update_copyright_company_name.sql ==========
-- ============================================================
-- 需求3：后台管理界面底部版权信息改为「黔序科技」
-- 说明：后台页脚组件 admin/src/components/copyright/index.vue
--       优先取接口返回的 companyName（即本配置项），
--       取不到时回退到组件内硬编码文案（本次一并同步修改）。
-- 幂等：可重复执行
-- ============================================================

-- 更新公司名称（配置键 copyright_company_name）
UPDATE `eb_system_config`
SET `value` = '黔序科技'
WHERE `name` = 'copyright_company_name';

-- 若该配置项不存在则补一条（归属到「版权信息」表单，与同组配置保持一致）
INSERT INTO `eb_system_config` (`name`, `title`, `value`, `form_id`, `status`)
SELECT 'copyright_company_name', 'copyright_company_name', '黔序科技', 0, 1
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_config`) t
  WHERE t.`name` = 'copyright_company_name'
);

-- ---------- 校验 ----------
SELECT id, name, value, form_id
FROM `eb_system_config`
WHERE `name` = 'copyright_company_name';

-- ========== END: update_copyright_company_name.sql ==========

-- ========== BEGIN: agent.sql ==========
-- ============================================================
-- 区域代理功能 数据库脚本（幂等，可重复执行）
-- 功能：省/市/区三级区域代理 + 按订单收货地址自动发放奖励
-- ============================================================

-- 1. 区域代理表 ------------------------------------------------
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

-- 2. 代理奖励明细表 --------------------------------------------
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

-- 3. 配置项 ----------------------------------------------------
-- agent_func_status   代理功能开关   1=开启 0=关闭
-- agent_apply_status  代理申请开关   1=开放申请 0=仅后台设置
-- agent_credit_timing 结算时机       1=订单付款成功 2=订单完成
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT t.name, t.title, 0, t.value, 0 FROM (
  SELECT 'agent_func_status'   AS name, '区域代理功能开关' AS title, '1' AS value
  UNION ALL SELECT 'agent_apply_status',   '区域代理申请开关', '1'
  UNION ALL SELECT 'agent_credit_timing',  '区域代理结算时机', '1'
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` c WHERE c.name = t.name);

-- 4. 后台菜单 --------------------------------------------------
-- 4.1 顶级目录「代理」
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT 0, '代理', 'clipboard', '', '/daili', 'M', 96, 1
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid`=0 AND `component`='/daili' AND `menu_type`='M');

-- 4.2 三个页面菜单
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT m.id, t.name, '', t.perms, t.component, 'C', t.sort, 1
FROM `eb_system_menu` m
JOIN (
  SELECT '代理管理' AS name, 'admin:agent:list'        AS perms, '/daili/agentList'   AS component, 1 AS sort
  UNION ALL SELECT '代理奖励明细', 'admin:agent:reward:list',  '/daili/agentReward', 2
  UNION ALL SELECT '代理设置',     'admin:agent:setting:list', '/daili/agentSetting', 3
) t
WHERE m.component='/daili' AND m.menu_type='M'
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.component = t.component AND x.menu_type='C');

-- 4.3 按钮权限（挂在对应页面下）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT p.id, t.name, '', t.perms, '', 'A', t.sort, 0
FROM `eb_system_menu` p
JOIN (
  SELECT '代理管理' AS page, '添加代理' AS name, 'admin:agent:save'    AS perms, 1 AS sort
  UNION ALL SELECT '代理管理', '修改代理',   'admin:agent:update', 2
  UNION ALL SELECT '代理管理', '审核代理',   'admin:agent:audit',  3
  UNION ALL SELECT '代理管理', '删除代理',   'admin:agent:delete', 4
  UNION ALL SELECT '代理管理', '省市区数据', 'admin:system:city:list:tree', 5
  UNION ALL SELECT '代理设置', '保存设置',   'admin:agent:setting:save', 1
) t
JOIN `eb_system_menu` c ON c.component = CASE t.page
    WHEN '代理管理' THEN '/daili/agentList'
    WHEN '代理设置' THEN '/daili/agentSetting' END
  AND c.menu_type='C'
WHERE p.id = c.id
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.perms = t.perms AND x.menu_type='A');

-- ========== END: agent.sql ==========

-- ========== BEGIN: stock.sql ==========
-- ============================================================
-- 订货系统 数据库脚本（幂等，可重复执行）
-- 功能：微商逐级拿货代理模式 —— 层级树/拿货价/订货订单（上级审核）
--       + 云仓库存 + 换货 + 奖励体系（差价/级差/平级）+ 提现
-- ============================================================

-- 1. 层级配置表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '层级名称（总代/一级代理/二级代理/普通代理）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（小=高层级，用于限制发展下级）',
  `discount` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '默认拿货折扣%（0=完全按价格表，无默认折扣）',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-代理层级配置';

INSERT INTO `eb_stock_level` (`name`, `sort`, `discount`)
SELECT t.name, t.sort, t.discount FROM (
  SELECT '总代' AS name, 10 AS sort, 80.00 AS discount
  UNION ALL SELECT '一级代理', 20, 85.00
  UNION ALL SELECT '二级代理', 30, 90.00
  UNION ALL SELECT '普通代理', 40, 95.00
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_stock_level` l WHERE l.name = t.name);

-- 2. 订货代理表（树形）-----------------------------------------
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

-- 3. 商品拿货价表 ----------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_price` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` int NOT NULL COMMENT '商品ID（eb_store_product.id）',
  `level_id` int NOT NULL COMMENT '层级ID',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '该层级拿货价',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_level` (`product_id`,`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-商品层级拿货价';

-- 4. 订货订单表 ------------------------------------------------
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

-- 5. 订货订单明细表 --------------------------------------------
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

-- 6. 换货单表 --------------------------------------------------
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

-- 7. 奖金明细表 ------------------------------------------------
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

-- 8. 级差阶梯表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_ladder` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `min_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩下限（含）',
  `max_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩上限（0=不限）',
  `rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-团队级差阶梯';

INSERT INTO `eb_stock_ladder` (`min_amount`, `max_amount`, `rate`, `sort`)
SELECT t.min_amount, t.max_amount, t.rate, t.sort FROM (
  SELECT 0 AS min_amount, 10000 AS max_amount, 2.00 AS rate, 1 AS sort
  UNION ALL SELECT 10000, 50000, 5.00, 2
  UNION ALL SELECT 50000, 0, 8.00, 3
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_stock_ladder`);

-- 9. 提现表 ----------------------------------------------------
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

-- 10. 库存变动日志表 -------------------------------------------
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

-- 11. 消息通知表 -----------------------------------------------
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

-- 12. 配置项 ---------------------------------------------------
-- stock_order_audit        订单上级审核开关   1=必须上级审核 0=直接流转总部
-- stock_diff_reward_status 差价奖励开关       1=开启 0=关闭
-- stock_exchange_diff      换货单参与差价奖励 1=参与 0=不参与
-- stock_ladder_status      级差奖励开关       1=开启 0=关闭
-- stock_ladder_cycle       级差结算周期       1=按订单结算 2=按月统计结算
-- stock_peer_status        平级奖励开关       1=开启 0=关闭
-- stock_peer_rate          平级奖励比例（%）  默认5
-- stock_peer_generations   平级奖励代数       默认1（只拿直接平推同级）
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT t.name, t.title, 0, t.value, 0 FROM (
  SELECT 'stock_order_audit'       AS name, '订货订单上级审核开关' AS title, '1' AS value
  UNION ALL SELECT 'stock_diff_reward_status', '订货差价奖励开关', '1'
  UNION ALL SELECT 'stock_exchange_diff',      '换货单参与差价奖励', '0'
  UNION ALL SELECT 'stock_ladder_status',      '订货级差奖励开关', '1'
  UNION ALL SELECT 'stock_ladder_cycle',       '订货级差结算周期', '1'
  UNION ALL SELECT 'stock_peer_status',        '订货平级奖励开关', '1'
  UNION ALL SELECT 'stock_peer_rate',          '订货平级奖励比例', '5'
  UNION ALL SELECT 'stock_peer_generations',   '订货平级奖励代数', '1'
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` c WHERE c.name = t.name);

-- 13. 后台菜单 -------------------------------------------------
-- 13.1 顶级目录「订货」
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT 0, '订货', 'shopping-cart', '', '/stock', 'M', 95, 1
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid`=0 AND `component`='/stock' AND `menu_type`='M');

-- 13.2 页面菜单
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT m.id, t.name, '', t.perms, t.component, 'C', t.sort, 1
FROM `eb_system_menu` m
JOIN (
  SELECT '订货代理' AS name, 'admin:stock:agent:list'      AS perms, '/stock/agent'    AS component, 1 AS sort
  UNION ALL SELECT '商品与库存', 'admin:stock:product:list', '/stock/product', 2
  UNION ALL SELECT '订货订单',   'admin:stock:order:list',   '/stock/order',   3
  UNION ALL SELECT '换货管理',   'admin:stock:exchange:list','/stock/exchange',4
  UNION ALL SELECT '奖金明细',   'admin:stock:reward:list',  '/stock/reward',  5
  UNION ALL SELECT '奖励规则',   'admin:stock:setting:list', '/stock/setting', 6
  UNION ALL SELECT '数据报表',   'admin:stock:report:list',  '/stock/report',  7
) t
WHERE m.component='/stock' AND m.menu_type='M'
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.component = t.component AND x.menu_type='C');

-- 13.3 按钮权限
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT p.id, t.name, '', t.perms, '', 'A', t.sort, 0
FROM `eb_system_menu` p
JOIN (
  SELECT '订货代理' AS page, '新增代理' AS name, 'admin:stock:agent:save'      AS perms, 1 AS sort
  UNION ALL SELECT '订货代理', '修改代理',   'admin:stock:agent:update',    2
  UNION ALL SELECT '订货代理', '删除代理',   'admin:stock:agent:delete',    3
  UNION ALL SELECT '商品与库存', '设置拿货价', 'admin:stock:price:save',    1
  UNION ALL SELECT '商品与库存', '调整库存',   'admin:stock:log:adjust',    2
  UNION ALL SELECT '订货订单', '审核订单',   'admin:stock:order:audit',     1
  UNION ALL SELECT '订货订单', '确认收款',   'admin:stock:order:pay',       2
  UNION ALL SELECT '订货订单', '订单发货',   'admin:stock:order:send',      3
  UNION ALL SELECT '换货管理', '换货审核',   'admin:stock:exchange:audit',  1
  UNION ALL SELECT '换货管理', '换货入库',   'admin:stock:exchange:back',   2
  UNION ALL SELECT '换货管理', '换货发货',   'admin:stock:exchange:send',   3
  UNION ALL SELECT '奖励规则', '保存规则',   'admin:stock:setting:save',    1
  UNION ALL SELECT '数据报表', '导出报表',   'admin:stock:report:export',   1
) t
JOIN `eb_system_menu` c ON c.component = CASE t.page
    WHEN '订货代理' THEN '/stock/agent'
    WHEN '商品与库存' THEN '/stock/product'
    WHEN '订货订单' THEN '/stock/order'
    WHEN '换货管理' THEN '/stock/exchange'
    WHEN '奖励规则' THEN '/stock/setting'
    WHEN '数据报表' THEN '/stock/report' END
  AND c.menu_type='C'
WHERE p.id = c.id
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.perms = t.perms AND x.menu_type='A');

-- ========== END: stock.sql ==========

-- ========== BEGIN: stock_upgrade_conditions.sql ==========
-- =============================================================
-- 订货商升级条件 / 平级奖 / 向上找货（MySQL 5.7 兼容，幂等）
-- 不用 ADD COLUMN IF NOT EXISTS（仅 MariaDB 支持）
-- =============================================================

SET @db = DATABASE();

-- eb_stock_level 升级条件列
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

-- eb_stock_order 向上查找
SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='up_search_time')=0,
  'ALTER TABLE eb_stock_order ADD COLUMN up_search_time DATETIME NULL COMMENT ''向上查找上级库存的时间''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order' AND COLUMN_NAME='up_search_num')=0,
  'ALTER TABLE eb_stock_order ADD COLUMN up_search_num INT NOT NULL DEFAULT 0 COMMENT ''已向上查找次数''', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 后台开关配置（幂等）
INSERT INTO eb_system_config(name, title, form_id, value, status, create_time, update_time)
SELECT 'stock_parent_deliver', '订货订单由上级发货', 0, '0', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_parent_deliver');

INSERT INTO eb_system_config(name, title, form_id, value, status, create_time, update_time)
SELECT 'stock_up_search_hours', '上级无库存自动向上查找等待时长(小时)', 0, '12', 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_up_search_hours');

-- 五级订货商默认数据（仅更新已存在行；新区级插入）
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
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_stock_level WHERE name = '区级订货商');

-- ========== END: stock_upgrade_conditions.sql ==========

-- ========== BEGIN: stock_changelog_product_rel.sql ==========
-- ============================================================
-- 订货商模块 2026-09-18：变更记录表 + 商品加入制 + 菜单调整
-- ============================================================

-- 1. 订货商变更记录表
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

-- 2. 参与订货的商品关联表（加入制）
CREATE TABLE IF NOT EXISTS `eb_stock_product_rel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品ID(eb_store_product.id)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参与订货的商品关联';

-- 3. 菜单：订货代理 → 订货商管理
UPDATE `eb_system_menu` SET `name` = '订货商管理' WHERE `id` = 660;

-- 4. 菜单：删除提现管理（含提现审核按钮；不依赖固定 id）
UPDATE `eb_system_menu`
SET `is_delte` = 1, `is_show` = 0
WHERE `component` = '/stock/withdraw'
   OR `perms` LIKE 'admin:stock:withdraw%'
   OR (`name` = '提现管理' AND (`perms` LIKE 'admin:stock:%' OR `component` LIKE '/stock/%'));

UPDATE `eb_system_menu` c
INNER JOIN `eb_system_menu` p ON c.`pid` = p.`id`
SET c.`is_delte` = 1, c.`is_show` = 0
WHERE p.`component` = '/stock/withdraw'
   OR p.`perms` = 'admin:stock:withdraw:list'
   OR (p.`name` = '提现管理' AND p.`perms` LIKE 'admin:stock:%');

-- 5. 菜单：新增订货商变更记录（挂订货一级菜单 652 下，排订货商管理之后）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 652, '订货商变更记录', '', 'admin:stock:agent:list', '/stock/changelog', 'C', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/stock/changelog' AND `is_delte` = 0);

-- ========== END: stock_changelog_product_rel.sql ==========

-- ========== BEGIN: stock_ladder_period_reward.sql ==========
-- 阶梯业绩奖励改造（2026-09-18）
-- 级差模式 → 阶梯模式：团队业绩落入阶梯区间，周期性（月度/季度/年度）一次性结算
-- 每档奖励二选一：reward(固定金额) > 0 直接发固定金额，否则按 团队业绩 × rate(比例%) 发放
-- stock_ladder_cycle 语义变更：1=月度(默认) 2=季度 3=年度

-- 幂等加列
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_ladder' AND COLUMN_NAME = 'reward'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE eb_stock_ladder ADD COLUMN reward DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''阶梯固定奖励金额（元）'' AFTER max_amount',
  'SELECT ''eb_stock_ladder.reward already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE eb_system_config SET title = '阶梯业绩结算周期（1=月度 2=季度 3=年度）' WHERE name = 'stock_ladder_cycle';
UPDATE eb_system_config SET title = '阶梯业绩奖励开关' WHERE name = 'stock_ladder_status';

-- ========== END: stock_ladder_period_reward.sql ==========

-- ========== BEGIN: merchant_store.sql ==========
-- ============================================================
-- 门店模块（MySQL 5.7 / 宝塔兼容，幂等，无 DELIMITER）
-- ============================================================

SET @db = DATABASE();

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

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 0, '门店', '', '', '/merchantStore', 'M', 96, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore' AND `pid` = 0 AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '门店管理', '', 'admin:merchant:store:list', '/merchantStore/list', 'C', 1, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore/list' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '核销记录', '', 'admin:merchant:verify:list', '/merchantStore/verify', 'C', 2, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore/verify' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '保存门店', '', 'admin:merchant:store:save', NULL, 'M', 3, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '修改门店', '', 'admin:merchant:store:update', NULL, 'M', 4, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '删除门店', '', 'admin:merchant:store:delete', NULL, 'M', 5, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:delete' AND `is_delte` = 0);

-- ========== END: merchant_store.sql ==========

-- ========== BEGIN: merchant_store_menu_buttons.sql ==========
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '保存门店', '', 'admin:merchant:store:save', NULL, 'M', 3, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:save' AND is_delte=0) t);
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '修改门店', '', 'admin:merchant:store:update', NULL, 'M', 4, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:update' AND is_delte=0) t);
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '删除门店', '', 'admin:merchant:store:delete', NULL, 'M', 5, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:delete' AND is_delte=0) t);

-- ========== END: merchant_store_menu_buttons.sql ==========

-- ========== BEGIN: extract_setting.sql ==========
-- ============================================================
-- 提现设置：菜单 + 配置项默认值
-- ============================================================

-- 1. 菜单：与「申请提现」同级增加「提现设置」
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.pid, '提现设置', '', 'admin:finance:extract:setting:get', '/financial/commission/setting', 'C', 2, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/financial/commission/template' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/financial/commission/setting' AND `is_delte` = 0);

-- 按钮权限：保存
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '提现设置保存', '', 'admin:finance:extract:setting:save', NULL, 'A', 1, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/financial/commission/setting' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:finance:extract:setting:save' AND `is_delte` = 0);

-- 2. 配置默认值
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_switch','佣金提现开关','0','1','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_switch') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_multiple','提现倍数','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_multiple') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_fee_type','手续费类型','0','ratio','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_fee_type') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_fee','手续费','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_fee') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_weekdays','可提现星期','0','1,2,3,4,5,6,7','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_weekdays') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_time_start','可提现开始小时','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_time_start') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_time_end','可提现结束小时','0','24','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_time_end') t);

-- ========== END: extract_setting.sql ==========

-- ========== BEGIN: fix_corrupted_config_values.sql ==========
-- ============================================================
-- 修复：部分开关类配置被写成了带引号的字符串（如 value = '1' 而非 1）
--
-- 成因：原始全量包 Crmeb_v3.0.sql 中这 8 行数据就带了多余引号，
--       且 created == updated（从未有人在后台改过），属于源数据缺陷。
--       代码里的判断是 getValueByKey(...).equals("1")，
--       带引号的值匹配不上，会导致支付方式等开关「看似开启实际不生效」。
--
-- 注意：新的全量 SQL 已修正源数据，全新安装无需执行本脚本。
--       仅用于修复按旧包安装过的存量数据库。
-- 幂等：只处理首尾同时为单引号的值，可重复执行。
-- ============================================================

SET NAMES utf8mb4;

UPDATE `eb_system_config`
SET `value` = TRIM(BOTH char(39) FROM `value`)
WHERE `value` LIKE CONCAT(char(39), '%', char(39));

-- 修复后应为：pay_weixin_open=1 / open_upgrade=0 等干净的 0|1

-- ========== END: fix_corrupted_config_values.sql ==========

-- ========== BEGIN: update_http_domain.sql ==========
-- 生产环境改为 HTTP（幂等，可重复执行）
-- 图片域名、后台/会员端接口统一走 api 站点
UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');

-- ========== END: update_http_domain.sql ==========
SET FOREIGN_KEY_CHECKS = 1;
SELECT 'CRMEB oneclick patches done' AS result;
