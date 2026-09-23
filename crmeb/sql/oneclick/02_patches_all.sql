-- ============================================================
-- CRMEB Java 3.0 增量补丁包（MySQL 5.7 兼容，全部幂等，可重复执行）
-- 说明：由 crmeb/sql/ 下的独立增量脚本按序合并而成，只补表结构 / 配置 / 菜单，不动业务数据
-- 用法：已有库执行  cd crmeb/sql/oneclick && ./deploy.sh patch
-- 注意：新增独立脚本后需同步追加到本文件末尾的 SET FOREIGN_KEY_CHECKS = 1; 之前
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='consumption_trigger_type') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `consumption_trigger_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''消费金额统计时机：1=已付款，2=交易完成'' AFTER `upgrade_type`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='order_count_trigger_type') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `order_count_trigger_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''订单数统计时机：1=已付款，2=交易完成'' AFTER `consumption_trigger_type`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='upgrade_value') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `upgrade_value` int NOT NULL DEFAULT 0 COMMENT ''累计订单数升级门槛'' AFTER `order_count_trigger_type`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='give_integral') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `give_integral` int NOT NULL DEFAULT 0 COMMENT ''等级赠送积分（每单固定赠送，手输多少送多少）'' AFTER `upgrade_value`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_system_user_level' AND COLUMN_NAME='description') = 0,
    'ALTER TABLE `eb_system_user_level` ADD COLUMN `description` varchar(500) DEFAULT NULL COMMENT ''等级权益描述'' AFTER `give_integral`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------
-- eb_user_level（用户等级记录）
-- ----------------------------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user_level' AND COLUMN_NAME='give_integral') = 0,
    'ALTER TABLE `eb_user_level` ADD COLUMN `give_integral` int NOT NULL DEFAULT 0 COMMENT ''等级赠送积分''',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------
-- eb_user（用户）
-- ----------------------------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='team_level') = 0,
    'ALTER TABLE `eb_user` ADD COLUMN `team_level` int NOT NULL DEFAULT 0 COMMENT ''团队等级ID（eb_system_team_level.id），0=无''',
    'DO 0');
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
--    注意：__config__ 必须带 "regList":[]，否则后端 SystemFormTempServiceImpl#checkRule
--    会因 regList 为 null 抛 NPE（表现为后台系统设置点保存直接报 Error）
UPDATE `eb_system_form_temp`
SET `content` = CONCAT(
      LEFT(`content`, CHAR_LENGTH(`content`) - 2),
      ',',
      '{"__config__":{"label":"未登录访问首页提示登录：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-switch","tagIcon":"switch","required":false,"tips":false,"tipsDesc":"","tipsIsLink":false,"tipsLink":"","layout":"colFormItem","span":24,"document":"https://element.eleme.cn/#/zh-CN/component/switch","formId":120,"regList":[],"renderKey":1762845400001,"defaultValue":"1"},"active-text":"开启","inactive-text":"关闭","active-color":"#13ce66","inactive-color":"#ff4949","active-value":"1","inactive-value":"0","disabled":false,"__vModel__":"login_notice_switch"},',
      '{"__config__":{"label":"提示登录文案：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-input","tagIcon":"input","required":false,"tips":false,"tipsDesc":"","tipsIsLink":false,"tipsLink":"","layout":"colFormItem","span":24,"document":"https://element.eleme.cn/#/zh-CN/component/input","formId":121,"regList":[],"renderKey":1762845400002},"__slot__":{"prepend":"","append":""},"placeholder":"请输入提示登录文案：","style":{"width":"50%"},"clearable":true,"prefix-icon":"","suffix-icon":"","maxlength":100,"show-word-limit":false,"readonly":false,"disabled":false,"__vModel__":"login_notice_text"}',
      ']}'
    )
WHERE `id` = 148
  AND `content` LIKE '%news_slides_limit%'
  AND `content` NOT LIKE '%login_notice_switch%';

-- 2b. 修复已按旧版脚本打过补丁的库（旧版追加的字段缺 regList，保存时后端 NPE）
--    幂等：补入 regList 后 LIKE 条件不再命中
UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"formId":120,"renderKey"', '"formId":120,"regList":[],"renderKey"')
WHERE `id` = 148 AND `content` LIKE '%"formId":120,"renderKey"%';

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"formId":121,"renderKey"', '"formId":121,"regList":[],"renderKey"')
WHERE `id` = 148 AND `content` LIKE '%"formId":121,"renderKey"%';

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
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component`='/daili' AND `menu_type`='M' AND `is_delte`=0);

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

-- ⚠️ 安全约束（2026-09-23 线上加固）：占位层级**只在层级表为空的全新库**播种。
--    原写法是「按 name 补插」，在已有自定义层级的线上库里会凭空多出
--    一级代理/二级代理/普通代理 等行；这些行带拿货折扣与升级条件，
--    会进入「订货商自动升级」判定（StockServiceImpl 按 cond_self_buy 等比较门槛），
--    可能把会员自动升成不该有的等级。已有数据的库一律不动。
SET @stock_level_seed := (SELECT COUNT(*) = 0 FROM `eb_stock_level`);

INSERT INTO `eb_stock_level` (`name`, `sort`, `discount`)
SELECT t.name, t.sort, t.discount FROM (
  SELECT '总代' AS name, 10 AS sort, 80.00 AS discount
  UNION ALL SELECT '一级代理', 20, 85.00
  UNION ALL SELECT '二级代理', 30, 90.00
  UNION ALL SELECT '普通代理', 40, 95.00
) t
WHERE @stock_level_seed = 1;

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
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component`='/stock' AND `menu_type`='M' AND `is_delte`=0);

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

-- 五级订货商默认数据
-- ⚠️ 安全约束（2026-09-23 线上加固）：只在「该行仍是 stock.sql 播下的初始占位层级」时才升级命名，
--    必须同时命中 id + 原名称 + 原排序 + 原折扣 四个条件。
--    原因：线上 id 1~4 很可能已经被业务人员改成了别的层级（本机库现为 8/19/23），
--    原来的 `WHERE id = 1..4` 会直接覆盖线上的拿货折扣、平级比例和升级门槛，改变拿货价与升降级判定。
UPDATE eb_stock_level SET name = '分公司订货商', sort = 10, discount = 40.00,
       cond_self_buy = 1, self_buy_amount = 100000.00,
       cond_direct = 1, direct_order_amount = 500000.00,
       cond_team = 1, team_amount = 3000000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 1.00
 WHERE id = 1 AND name = '总代' AND sort = 10 AND discount = 80.00;

UPDATE eb_stock_level SET name = '全国订货商', sort = 20, discount = 50.00,
       cond_self_buy = 1, self_buy_amount = 50000.00,
       cond_direct = 1, direct_order_amount = 200000.00,
       cond_team = 1, team_amount = 1000000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 2.00
 WHERE id = 2 AND name = '一级代理' AND sort = 20 AND discount = 85.00;

UPDATE eb_stock_level SET name = '省级订货商', sort = 30, discount = 60.00,
       cond_self_buy = 1, self_buy_amount = 20000.00,
       cond_direct = 1, direct_order_amount = 80000.00,
       cond_team = 1, team_amount = 300000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 3.00
 WHERE id = 3 AND name = '二级代理' AND sort = 30 AND discount = 90.00;

UPDATE eb_stock_level SET name = '市级订货商', sort = 40, discount = 70.00,
       cond_self_buy = 1, self_buy_amount = 5000.00,
       cond_direct = 1, direct_order_amount = 20000.00,
       cond_team = 1, team_amount = 80000.00,
       cond_product = 0, upgrade_product_ids = '',
       condition_logic = 0, peer_rate = 4.00
 WHERE id = 4 AND name = '普通代理' AND sort = 40 AND discount = 95.00;

-- ⚠️ 安全约束（2026-09-23 线上加固）：补最后一级必须建立在「五级方案已就位」之上 ——
--    只有已存在市级订货商（说明上面的默认数据已生效）时才插入。
--    原写法只判 name != '区级订货商'，会在已有自定义层级的线上库里凭空插入一个
--    cond_self_buy=1/1000 元 的等级，进而被自动升级逻辑命中，改变线上拿货价。
INSERT INTO eb_stock_level(name, sort, discount, cond_self_buy, self_buy_amount,
                           cond_direct, direct_order_amount, cond_team, team_amount,
                           cond_product, upgrade_product_ids, condition_logic, peer_rate, is_del)
SELECT '区级订货商', 50, 80.00, 1, 1000.00, 1, 5000.00, 1, 20000.00, 0, '', 0, 5.00, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM eb_stock_level) x WHERE x.`name` = '区级订货商')
  AND     EXISTS (SELECT 1 FROM (SELECT `name` FROM eb_stock_level) y WHERE y.`name` = '市级订货商');
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
-- 按 component 定位 + 兼保 id：线上 id 与本机可能不同，单靠 id 会改错菜单
UPDATE `eb_system_menu` SET `name` = '订货商管理'
 WHERE `id` = 660 AND `component` = '/stock/agent' AND `is_delte` = 0;

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
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore' AND `is_delte` = 0);

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
SELECT m.id, '保存门店', '', 'admin:merchant:store:save', NULL, 'A', 3, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '修改门店', '', 'admin:merchant:store:update', NULL, 'A', 4, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '删除门店', '', 'admin:merchant:store:delete', NULL, 'A', 5, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:delete' AND `is_delte` = 0);
-- ========== END: merchant_store.sql ==========


-- ========== BEGIN: merchant_store_menu_buttons.sql ==========
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '保存门店', '', 'admin:merchant:store:save', NULL, 'A', 3, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:save' AND is_delte=0) t);
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '修改门店', '', 'admin:merchant:store:update', NULL, 'A', 4, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:update' AND is_delte=0) t);
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '删除门店', '', 'admin:merchant:store:delete', NULL, 'A', 5, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:delete' AND is_delte=0) t);

-- 若历史数据写成了 M，纠正为 A
UPDATE eb_system_menu SET menu_type='A', is_show=0
WHERE perms IN ('admin:merchant:store:save','admin:merchant:store:update','admin:merchant:store:delete')
  AND menu_type='M';
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
--
-- ⚠️ 注意（2026-09-22 补充）：只清数据不够，还会复发！
--   代码 Constants.CONFIG_FORM_SWITCH_OPEN 原值为 "'1'"（带引号），
--   与源数据配套；把数据清成干净的 1 后，该常量必须同步改为 "1"，
--   否则 equals 匹配不上 → 支付页再次出现「暂无支付方式」。
--   2026-09-22 已把常量改为 "1"（随代码提交），本脚本保留用于清洗存量脏数据。
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


-- ========== BEGIN: stock_rework_phase1.sql ==========
-- =============================================================
-- 订货商模块一期改造（2026-09-18）
-- 需求1：下单即付款 + 收货地址 + 无库存自动上浮（等待匹配状态）
-- 状态机新增：10=等待匹配上级  -2=已取消
-- 说明：收货地址复用系统原有用户地址簿 eb_user_address，
--       下单时按 addressId 引用并把收货人/电话/地址快照进订货单。
-- 幂等：可重复执行（列存在时跳过）
-- =============================================================

-- 幂等辅助过程：列不存在才 ADD COLUMN（MySQL 5.7 无 ADD COLUMN IF NOT EXISTS）
DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN t varchar(64), IN c varchar(64), IN ddl text)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) THEN
    SET @s = CONCAT('ALTER TABLE ', t, ' ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;

-- 1. eb_stock_order 加列（逐列判断，注意 AFTER 的先后依赖顺序）
CALL add_col_if_missing('eb_stock_order', 'address_id',
  '`address_id` int(11) DEFAULT NULL COMMENT ''下单时选用的用户地址ID'' AFTER `mark`');
CALL add_col_if_missing('eb_stock_order', 'real_name',
  '`real_name` varchar(64) DEFAULT '''' COMMENT ''收货人姓名（下单地址快照）'' AFTER `address_id`');
CALL add_col_if_missing('eb_stock_order', 'phone',
  '`phone` varchar(32) DEFAULT '''' COMMENT ''收货人电话（下单地址快照）'' AFTER `real_name`');
CALL add_col_if_missing('eb_stock_order', 'user_address',
  '`user_address` varchar(500) DEFAULT '''' COMMENT ''收货地址（下单地址快照）'' AFTER `phone`');
CALL add_col_if_missing('eb_stock_order', 'stock_type',
  '`stock_type` tinyint(1) DEFAULT 1 COMMENT ''库存类型：1=实体 2=虚拟（二期启用虚拟）''');
CALL add_col_if_missing('eb_stock_order', 'order_type',
  '`order_type` tinyint(1) DEFAULT 1 COMMENT ''订单类型：1=采购 2=虚拟提货 3=换货（一期固定1）''');
CALL add_col_if_missing('eb_stock_order', 'cancel_time',
  '`cancel_time` datetime DEFAULT NULL COMMENT ''取消时间'' AFTER `finish_time`');

-- 2. eb_stock_order_product 加列（规格地基，一期先记录，取价商品级）
CALL add_col_if_missing('eb_stock_order_product', 'sku_key',
  '`sku_key` varchar(64) DEFAULT '''' COMMENT ''规格标识（空=商品级）'' AFTER `product_id`');

-- 3. 配置项
-- stock_wait_pay_hours  订货单待付款超时自动取消时长（小时），默认24
-- stock_virtual_audit   虚拟库存单付款后审核开关（二期启用）0=付款即完成 1=需审核
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT t.name, t.title, 0, t.value, 0 FROM (
  SELECT 'stock_wait_pay_hours' AS name, '订货单待付款超时时长（小时）' AS title, '24' AS value
  UNION ALL SELECT 'stock_virtual_audit', '虚拟库存单付款后审核开关', '0'
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` c WHERE c.name = t.name);
-- ========== END: stock_rework_phase1.sql ==========


-- ========== BEGIN: stock_rework_phase2.sql ==========
-- =============================================================
-- 订货商模块二期改造（2026-09-18）
-- 需求2：虚拟库存 + 提货
--   下单可选虚拟库存：付款成功后虚拟库存入账（会员中心可见），不发货；
--   提货 = 用虚拟库存换实物：生成 order_type=2 提货单，总部发货扣云仓。
-- 幂等：可重复执行
-- =============================================================

-- 1. 虚拟库存表（每个用户每商品一条，入账累加 remain_num）
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
-- ========== END: stock_rework_phase2.sql ==========


-- ========== BEGIN: stock_rework_phase3.sql ==========
-- ============================================================
-- Stock module phase 3: SKU support + full exchange capability
-- Idempotent: safe to run repeatedly
-- ============================================================

DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN t varchar(64), IN c varchar(64), IN ddl text)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) THEN
    SET @s = CONCAT('ALTER TABLE ', t, ' ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;

-- 1. SKU level purchase price
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

-- 2. Exchange switch per SKU
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

-- 3. Exchange order extra columns
CALL add_col_if_missing('eb_stock_exchange','sku_key','sku_key varchar(120) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','exchange_type','exchange_type tinyint(1) NOT NULL DEFAULT 2');
CALL add_col_if_missing('eb_stock_exchange','target_product_id','target_product_id int(11) NOT NULL DEFAULT 0');
CALL add_col_if_missing('eb_stock_exchange','target_sku_key','target_sku_key varchar(120) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','target_product_name','target_product_name varchar(255) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','target_price','target_price decimal(10,2) NOT NULL DEFAULT 0.00');
CALL add_col_if_missing('eb_stock_exchange','origin_price','origin_price decimal(10,2) NOT NULL DEFAULT 0.00');
CALL add_col_if_missing('eb_stock_exchange','diff_price','diff_price decimal(10,2) NOT NULL DEFAULT 0.00');
CALL add_col_if_missing('eb_stock_exchange','diff_pay_status','diff_pay_status tinyint(1) NOT NULL DEFAULT 0');
CALL add_col_if_missing('eb_stock_exchange','diff_pay_type','diff_pay_type varchar(20) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','diff_pay_time','diff_pay_time datetime DEFAULT NULL');
CALL add_col_if_missing('eb_stock_exchange','real_name','real_name varchar(64) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','phone','phone varchar(20) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','user_address','user_address varchar(255) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','address_id','address_id int(11) NOT NULL DEFAULT 0');

-- 4. Configs
INSERT INTO eb_system_config (name, value, status, create_time, update_time)
SELECT 'stock_exchange_single', '1', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) x WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_exchange_single'));

INSERT INTO eb_system_config (name, value, status, create_time, update_time)
SELECT 'stock_exchange_diff_parent_rate', '100', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) x WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_exchange_diff_parent_rate'));

DROP PROCEDURE IF EXISTS add_col_if_missing;
-- ========== END: stock_rework_phase3.sql ==========


-- ========== BEGIN: stock_offline_sale.sql ==========
-- ============================================================
-- Stock module: offline sale (线下销售) records
-- Idempotent: safe to run repeatedly
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
-- ========== END: stock_offline_sale.sql ==========


-- ========== BEGIN: stock_exchange_target.sql ==========
-- ============================================================
-- Stock module: exchange allowed-target list (source sku -> target product/sku)
-- Idempotent
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
-- ========== END: stock_exchange_target.sql ==========


-- ========== BEGIN: stock_product_stock_type.sql ==========
-- Stock module: per-product virtual/physical stock support (idempotent)
DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN t varchar(64), IN c varchar(64), IN ddl text)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) THEN
    SET @s = CONCAT('ALTER TABLE ', t, ' ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;

CALL add_col_if_missing('eb_stock_product_rel','support_virtual','support_virtual tinyint(1) NOT NULL DEFAULT 1');
CALL add_col_if_missing('eb_stock_product_rel','support_physical','support_physical tinyint(1) NOT NULL DEFAULT 1');

DROP PROCEDURE IF EXISTS add_col_if_missing;
SELECT id, product_id, support_virtual, support_physical FROM eb_stock_product_rel;
-- ========== END: stock_product_stock_type.sql ==========


-- ========== BEGIN: stock_issue_batch_20260918.sql ==========
-- 2026-09-18 issue batch (doc: system issue collection)

-- 1) stock adjust log (issue 9: adjust virtual / physical stock)
CREATE TABLE IF NOT EXISTS `eb_stock_adjust_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) NOT NULL DEFAULT 0 COMMENT 'stock agent id',
  `uid` int(11) NOT NULL DEFAULT 0 COMMENT 'user id',
  `product_id` int(11) NOT NULL DEFAULT 0,
  `sku_key` varchar(128) NOT NULL DEFAULT '' COMMENT 'sku key, empty = whole product',
  `stock_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=physical 2=virtual',
  `num` int(11) NOT NULL DEFAULT 0 COMMENT 'positive=add negative=deduct',
  `mark` varchar(255) NOT NULL DEFAULT '',
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='stock agent inventory adjust log';

-- 2) agent change log for reseller agents (issue 2)
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

-- 3) menu: rename 664? no -> 654 reward rule to stock setting (issue 13)
-- 按 component 定位 + 兼保 id：线上 id 与本机可能不同，单靠 id 会改错菜单
UPDATE `eb_system_menu` SET `name` = '订货商设置'
 WHERE `id` = 654 AND `component` = '/stock/setting' AND `is_delte` = 0;

-- 4) menu: new "level display" page under stock, below stock setting (issue 13)
INSERT INTO `eb_system_menu` (`id`, `pid`, `name`, `component`, `perms`, `menu_type`, `sort`, `icon`, `is_show`, `is_delte`, `create_time`)
SELECT 691, 652, '层级显示', '/stock/level', 'admin:stock:level:list', 'C', 5, '', 1, 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `id` = 691));

INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT 1, 691 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` WHERE `rid` = 1 AND `menu_id` = 691);

-- 5) menu: reseller agent change log (issue 2)
INSERT INTO `eb_system_menu` (`id`, `pid`, `name`, `component`, `perms`, `menu_type`, `sort`, `icon`, `is_show`, `is_delte`, `create_time`)
SELECT 692, 641, '代理商变更记录', '/daili/changeLog', 'admin:agent:changelog:list', 'C', 4, '', 1, 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `id` = 692));

INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT 1, 692 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` WHERE `rid` = 1 AND `menu_id` = 692);

-- 6) new config: agent apply regions (issue 8)
INSERT INTO `eb_system_config` (`name`, `title`, `value`, `status`, `create_time`)
SELECT 'agent_apply_regions', '会员端可申请的代理区域', '1,2,3', 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'agent_apply_regions');
-- ========== END: stock_issue_batch_20260918.sql ==========


-- ========== BEGIN: hidden_super_admin.sql ==========
-- 隐藏超级管理员账号 + 隐藏面板模块开关（幂等可重复执行）
-- 账号: qxtec / 密码: qx9264 (DES加密, key=账号, 与登录校验一致)
-- 账号存在则只校正字段，不存在才插入——避免每跑一次就换一个自增 id。
UPDATE eb_system_admin
   SET pwd = 'bDVAF6/lBMg=', real_name = '系统运维', roles = '1',
       level = 1, status = 1, is_del = 0, is_sms = 0, update_time = NOW()
 WHERE account = 'qxtec'
   AND NOT (pwd <=> 'bDVAF6/lBMg=' AND real_name <=> '系统运维' AND roles <=> '1'
            AND level <=> 1 AND status <=> 1 AND is_del <=> 0 AND is_sms <=> 0);
INSERT INTO eb_system_admin (account, pwd, real_name, roles, last_ip, login_count, level, status, is_del, is_sms, create_time, update_time)
SELECT 'qxtec', 'bDVAF6/lBMg=', '系统运维', '1', '', 0, 1, 1, 0, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT account FROM eb_system_admin) x WHERE x.account = 'qxtec');

-- 隐藏面板功能开关（status=0 为有效配置, value 1=开启 0=关闭）
-- 原来的写法是 DELETE + INSERT，每跑一次就换一批自增 id、update_time 也跟着变；
-- 改成 UPDATE + 缺失才补插，重复执行不再产生任何变化。
UPDATE eb_system_config
   SET title = '隐藏面板-团队奖开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_team_reward'
   AND NOT (title <=> '隐藏面板-团队奖开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-订货商开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_stock'
   AND NOT (title <=> '隐藏面板-订货商开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-门店开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_store'
   AND NOT (title <=> '隐藏面板-门店开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-区域代理开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_daili'
   AND NOT (title <=> '隐藏面板-区域代理开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-分销开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_spread'
   AND NOT (title <=> '隐藏面板-分销开关' AND value <=> '1' AND status <=> 0);
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time)
SELECT t.name, t.title, 0, '1', 0, NOW(), NOW() FROM (
  SELECT 'sys_switch_team_reward' AS name, '隐藏面板-团队奖开关' AS title
  UNION ALL SELECT 'sys_switch_stock',    '隐藏面板-订货商开关'
  UNION ALL SELECT 'sys_switch_store',    '隐藏面板-门店开关'
  UNION ALL SELECT 'sys_switch_daili',    '隐藏面板-区域代理开关'
  UNION ALL SELECT 'sys_switch_spread',   '隐藏面板-分销开关'
) t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM eb_system_config) x WHERE x.name = t.name);
-- ========== END: hidden_super_admin.sql ==========


-- ========== BEGIN: hidden_menu.sql ==========
-- ============================================================================
-- 系统运维菜单（仅 qxtec 登录时由后端下发，其他管理员不可见）
-- 幂等：只补插、不删除。旧写法是「DELETE 两行 + INSERT 两行」，每执行一次补丁就
--       换一批自增 id，既让菜单 id 漂移，又让 eb_system_role_menu 里的既有授权
--       全部指向已删除的旧 id，每次都留下 2 条孤儿授权。
-- 注意：名称 / 图标 / 排序的「最终显示值」由 menu_sync_20260923.sql 负责收敛，
--       本脚本只在菜单缺失时按下列初始值补建，不覆盖已有行的显示属性。
-- ============================================================================

-- 1) 顶级目录 /hidden（缺失才建）
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '系统', 'warning', '', '/hidden', 'M', 55, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/hidden');

SET @hidden_pid := (SELECT `id` FROM `eb_system_menu`
                     WHERE `component` = '/hidden' ORDER BY `id` LIMIT 1);

-- 2) 子菜单 /hidden/panel（缺失才建）
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @hidden_pid, '运维面板', '', '', '/hidden/panel', 'C', 0, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @hidden_pid IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/hidden/panel');

-- 3) 修正子菜单归属（只有真的挂错父级时才写，避免无谓刷新 update_time）
UPDATE `eb_system_menu`
   SET `pid` = @hidden_pid
 WHERE `component` = '/hidden/panel'
   AND @hidden_pid IS NOT NULL
   AND NOT (`pid` <=> @hidden_pid);
-- ========== END: hidden_menu.sql ==========


-- ========== BEGIN: hidden_marketing_switch.sql ==========
-- 隐藏面板营销开关（幂等可重复执行）
-- 积分/秒杀/砍价/拼团/优惠券（status=0 为有效配置, value 1=开启 0=关闭）
-- 原来的写法是 DELETE + INSERT，每跑一次就换一批自增 id、update_time 也跟着变；
-- 改成 UPDATE + 缺失才补插，重复执行不再产生任何变化。
UPDATE eb_system_config
   SET title = '隐藏面板-积分开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_integral'
   AND NOT (title <=> '隐藏面板-积分开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-秒杀开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_seckill'
   AND NOT (title <=> '隐藏面板-秒杀开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-砍价开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_bargain'
   AND NOT (title <=> '隐藏面板-砍价开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-拼团开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_combination'
   AND NOT (title <=> '隐藏面板-拼团开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-优惠券开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_coupon'
   AND NOT (title <=> '隐藏面板-优惠券开关' AND value <=> '1' AND status <=> 0);
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time)
SELECT t.name, t.title, 0, '1', 0, NOW(), NOW() FROM (
  SELECT 'sys_switch_integral' AS name, '隐藏面板-积分开关' AS title
  UNION ALL SELECT 'sys_switch_seckill',    '隐藏面板-秒杀开关'
  UNION ALL SELECT 'sys_switch_bargain',    '隐藏面板-砍价开关'
  UNION ALL SELECT 'sys_switch_combination','隐藏面板-拼团开关'
  UNION ALL SELECT 'sys_switch_coupon',     '隐藏面板-优惠券开关'
) t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM eb_system_config) x WHERE x.name = t.name);
-- ========== END: hidden_marketing_switch.sql ==========


-- ========== BEGIN: local_default_settings.sql ==========
-- 本地默认设置快照（配置项 + 装修页）—— ⛔ 已停用，不参与线上部署
-- 生成时间: 2026-09-19 12:46:39 ／ 停用时间: 2026-09-23
--
-- 停用原因：原本用 REPLACE INTO 按主键整表覆盖，会把【线上真实配置】一并冲掉——
--   pay_weixin_app_*（微信支付商户号/密钥）、APP_PRIVATE_KEY / ALIPAY_PUBLIC_KEY、
--   sms_account / sms_token、txAccessKey / qnAccessKey / jdAccessKey 存储密钥、
--   store_brokerage_*（分销比例）等，执行后线上支付、短信、上传会立即不可用。
-- 替代方案：线上配置改由 crmeb/sql/system_settings_20260923.sql 幂等收敛
--   （只改「与厂商基库不同的差异项」，凭证类一律不同步）。
-- 如需查看停用前的原始快照：
--   git show <停用前最近一次提交>:crmeb/sql/local_default_settings.sql
SET NAMES utf8mb4;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ⛔ 已停用：同上，会整页覆盖线上装修页（eb_page_diy）内容；如需查看原始快照见文件头。


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
-- ========== END: local_default_settings.sql ==========


-- ========== BEGIN: update_http_domain.sql ==========
-- 生产环境改为 HTTP（幂等，可重复执行）
-- 接口域名 / 图片域名 / 移动商城接口统一走 api 站点
-- 加「值不同才写」守卫：否则每执行一次都会刷 update_time，重复跑不幂等。
UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url')
  AND NOT (`value` <=> 'http://api.qianxutec.com');

-- ⚠️ site_url 不能和上面混在一起改：
--    它的语义是「移动端站点域名」，被微信 H5 支付当作 h5_info.wap_url 使用
--    （见 OrderPayServiceImpl#getUnifiedorderVo），必须指向 H5 部署域名，
--    否则微信 H5 支付的 wap_url 与后台配置的支付域名不一致。
UPDATE `eb_system_config` SET `value` = 'http://app.qianxutec.com', `update_time` = NOW()
WHERE `name` = 'site_url'
  AND NOT (`value` <=> 'http://app.qianxutec.com');
-- ========== END: update_http_domain.sql ==========


-- ========== BEGIN: stock_adjust_log_link_uid.sql ==========
-- 订货系统 - 库存调整流水补记「向下级（购货人）抵扣的上级 UID」
-- 背景：上级代下级下单时，库存调整流水只写了流水归属人自己的 uid，
--       换货差价流转场景下无法判断这笔流水对应哪个购货人，故补列并回填历史流水。
-- 做法：information_schema 判断列不存在才 ALTER；回填只处理 link_uid 为空或 0 的历史数据。
-- ============================================================
SET @db = DATABASE();

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_adjust_log' AND COLUMN_NAME='link_uid') = 0,
    'ALTER TABLE `eb_stock_adjust_log` ADD COLUMN `link_uid` int NOT NULL DEFAULT 0 COMMENT ''向下级UID：换货差价流转时记录购货人的上级UID，0=无'' AFTER `mark`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史流水回填：从备注中解析订货单号（如 SK17898828422122548），
-- 关联 eb_stock_order 取正常下单人作为 link_uid
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.TABLES
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_order') > 0,
    'UPDATE `eb_stock_adjust_log` l JOIN `eb_stock_order` o ON l.`mark` LIKE CONCAT(''%'', o.`order_no`, ''%'') SET l.`link_uid` = o.`uid` WHERE l.`stock_type` = 2 AND l.`num` < 0 AND (l.`link_uid` IS NULL OR l.`link_uid` = 0)',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- ========== END: stock_adjust_log_link_uid.sql ==========

-- ========== BEGIN: stock_exchange_target_type.sql ==========
-- 订货系统 - 换货目标类型（区分换货差价 / 换货退货两条流转）
-- 说明：换货差价走上级审核、上级代发并记账给申请人；
--       换货退货需要先退货，走退货物流后再由上级审核。
--       NULL / 1 视为兼容历史数据。
-- 做法：information_schema 判断列不存在才 ALTER。
-- ============================================================
SET @db = DATABASE();

SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_stock_exchange' AND COLUMN_NAME='target_stock_type') = 0,
    'ALTER TABLE `eb_stock_exchange` ADD COLUMN `target_stock_type` tinyint NULL DEFAULT 1 COMMENT ''换货目标类型：1=换货差价，2=换货退货；NULL/1=兼容'' AFTER `target_sku_key`',
    'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- ========== END: stock_exchange_target_type.sql ==========

-- ========== BEGIN: exchange_hq_audit_20260921.sql ==========
-- 订货 - 换货是否需要总部复核开关（1=需要，确认后流转；0=不需要，直接上级审核）
-- 上级端确认后仍然可继续审核。
-- 注意：status 必须为 0 —— CRMEB 约定 0=启用，getByName 以 status=false 过滤，填 1 会永远读不到。
-- ============================================================
INSERT INTO eb_system_config (name, value, status, create_time, update_time)
SELECT 'stock_exchange_hq_audit', '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) x WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_exchange_hq_audit'));
-- ========== END: exchange_hq_audit_20260921.sql ==========

-- ========== BEGIN: exchange_return_address_20260921.sql ==========
-- 订货 - 换货退货收件地址（由上级填写给申请人，后台设置页可维护）
-- 注意：status 必须为 0 —— CRMEB 约定 0=启用，getByName 以 status=false 过滤，填 1 会永远读不到。
-- ============================================================
INSERT INTO eb_system_config (name, value, status, create_time, update_time)
SELECT 'stock_exchange_return_address', '', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) x WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_exchange_return_address'));
-- ========== END: exchange_return_address_20260921.sql ==========

-- ========== BEGIN: fix_virtual_in_log_20260921.sql ==========
-- ----------------------------------------------------------------------
-- 补历史「虚拟库存入账」流水（2026-09-21）
-- 背景：虚拟采购单入账（creditVirtualStock）此前只加 eb_stock_virtual_stock.remain_num，
--       从不写 eb_stock_adjust_log，导致会员端「库存记录-虚拟库存」只有提货/转卖出库，
--       看不到采购入库。代码已修（入账时补写流水），本脚本为修复前已入账的历史单补数。
--
-- 幂等：同一订单已有入账流水（stock_type=2 且 num>0 且 mark 含该单号）则跳过，可重复执行。
-- 口径：只补「已完成(status=4)」的虚拟采购单（order_type=1 且 stock_type=2），
--       这些单在付款/审核通过时即已入账；未完成单本就不该有入账流水。
-- ----------------------------------------------------------------------

INSERT INTO eb_stock_adjust_log
    (agent_id, uid, link_uid, product_id, sku_key, stock_type, num, mark, is_del, create_time)
SELECT * FROM (
    SELECT o.agent_id,
           o.uid,
           0 AS link_uid,
           p.product_id,
           IFNULL(p.sku_key, '') AS sku_key,
           2 AS stock_type,
           p.num,
           CONCAT('虚拟库存入账（订单 ', o.order_no, '），本次增加 ', p.num) AS mark,
           0 AS is_del,
           o.create_time
      FROM eb_stock_order o
      JOIN eb_stock_order_product p ON p.order_id = o.id
     WHERE o.is_del = 0
       AND o.order_type = 1
       AND o.stock_type = 2
       AND o.status = 4
       AND p.num > 0
       AND NOT EXISTS (
             SELECT 1 FROM eb_stock_adjust_log a
              WHERE a.agent_id = o.agent_id
                AND a.stock_type = 2
                AND a.num > 0
                AND a.is_del = 0
                AND a.mark LIKE CONCAT('%', o.order_no, '%')
           )
) t;
-- ========== END: fix_virtual_in_log_20260921.sql ==========


-- ========== BEGIN: menu_restructure_20260922.sql ==========
-- ============================================================
-- 菜单结构调整（2026-09-22）
--
-- 背景：顶部一级菜单过于拥挤，重新组织菜单层级，便于后续管理。
--
-- 内容：
--   1) 新建「运营」一级目录（component=/yunying），把 分销 / 代理 / 团队 /
--      订货 / 门店 这 5 个原一级菜单连同其全部下级功能迁移到它下面；
--   2) 原「运营」(component=/dashboard) 更名为「首页」；
--   3) 「运营」下 5 个子菜单改名 + 重排；
--   4) 「区域代理」子菜单改名 + 重排；
--   5) 「订货商」子菜单改名 + 重排，并隐藏「订货商级别设置」入口
-- 注：本文件不再手写  = NOW()。该列声明了 ON UPDATE CURRENT_TIMESTAMP，
--     行内容有变化时会自动刷新；手写会让「跑两遍 diff 为空」的幂等判据失效。
--      （该页功能已并入「订货商设置」页面，见前端对应改动）。
--
-- 幂等：全部按 component 定位，不依赖自增 id，可重复执行。
-- 只改 name / sort / pid / is_show，不动 component 与 perms，
-- 因此不会影响任何页面路由与权限判断。
-- ============================================================
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1) 新建「运营」一级目录（已存在则跳过）
--    ⚠️ 判存在只看 component + 未删除，**不要再加 pid = 0**：
--       只要 /yunying 的 pid 因任何原因（人工调整、其它脚本）不再是 0，
--       带上 pid = 0 的守卫就会失配，于是每跑一次补丁就多插一条重复的「运营」。
-- ------------------------------------------------------------
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '运营', 's-operation', NULL, '/yunying', 'M', 190, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM `eb_system_menu`
        WHERE `component` = '/yunying' AND `is_delte` = 0
       );

SET @yy_id := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/yunying' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

-- 把 5 个模块迁移到「运营」下（仅移动当前仍在一级的，保证幂等）
UPDATE `eb_system_menu`
   SET `pid` = @yy_id
 WHERE `pid` = 0
   AND `is_delte` = 0
   AND @yy_id IS NOT NULL
   AND `component` IN ('/distribution', '/daili', '/tuandui', '/stock', '/merchantStore');

-- ------------------------------------------------------------
-- 2) 原「运营」更名为「首页」
-- ------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `name` = '首页'
 WHERE `pid` = 0 AND `component` = '/dashboard' AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 3) 「运营」下 5 个子菜单：改名 + 重排
--    显示顺序（自上而下）：分销商 → 团队奖 → 订货商 → 区域代理 → 门店
--    同级按 sort DESC 排列，故 sort 依次递减
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `name` = '分销商',   `sort` = 105 WHERE `component` = '/distribution'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '团队奖',   `sort` = 104 WHERE `component` = '/tuandui'       AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商',   `sort` = 103 WHERE `component` = '/stock'         AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '区域代理', `sort` = 102 WHERE `component` = '/daili'         AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '门店',     `sort` = 101 WHERE `component` = '/merchantStore' AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 4) 「区域代理」子菜单：改名 + 重排
--    顺序：代理商管理 → 区域代理设置 → 代理奖励明细 → 代理商变更记录
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `name` = '代理商管理',   `sort` = 4 WHERE `component` = '/daili/agentList'    AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '区域代理设置', `sort` = 3 WHERE `component` = '/daili/agentSetting' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 2 WHERE `component` = '/daili/agentReward' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 1 WHERE `component` = '/daili/changeLog'   AND `is_delte` = 0;
-- 兼容旧路由名（部分环境该页 component 为 /daili/changelog）
UPDATE `eb_system_menu` SET `sort` = 1 WHERE `component` = '/daili/changelog'   AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 5) 「订货商」子菜单：改名 + 重排 + 隐藏级别设置入口
--    顺序：数据报表 → 订货商管理 → 订货商设置 → 商品与库存
--          → 订货商资金记录 → 换货管理 → 订货商订单 → 订货商变更记录
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `sort` = 10 WHERE `component` = '/stock/report'   AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 9 WHERE `component` = '/stock/agent'    AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 8 WHERE `component` = '/stock/setting'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 7 WHERE `component` = '/stock/product'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商资金记录', `sort` = 6 WHERE `component` = '/stock/reward'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 5 WHERE `component` = '/stock/exchange' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商订单',     `sort` = 4 WHERE `component` = '/stock/order'   AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 3 WHERE `component` = '/stock/changelog' AND `is_delte` = 0;

-- 「订货商级别设置」入口隐藏（功能已并入「订货商设置」页，权限与记录保留）
-- 注：/stock/withdraw（提现管理）原本已隐藏，此处不处理
UPDATE `eb_system_menu`
   SET `is_show` = 0
 WHERE `is_delte` = 0
   AND (`component` = '/stock/level' OR (`name` = '订货商级别设置' AND `perms` LIKE 'admin:stock:level%'));

-- ------------------------------------------------------------
-- 校验：一级菜单 与「运营」下的子菜单
-- ------------------------------------------------------------
SELECT '--- 一级菜单（按显示顺序）---' AS info;
SELECT `id`, `name`, `component`, `sort`
  FROM `eb_system_menu`
 WHERE `pid` = 0 AND `is_show` = 1 AND `is_delte` = 0
 ORDER BY `sort` DESC;

SELECT '--- 「运营」下的子菜单 ---' AS info;
SELECT c.`id`, c.`name`, c.`component`, c.`sort`
  FROM `eb_system_menu` c
  JOIN `eb_system_menu` p ON c.`pid` = p.`id`
 WHERE p.`component` = '/yunying' AND c.`is_show` = 1 AND c.`is_delte` = 0
 ORDER BY c.`sort` DESC;

SELECT '--- 「订货商」下的子菜单 ---' AS info;
SELECT c.`id`, c.`name`, c.`component`, c.`sort`
  FROM `eb_system_menu` c
  JOIN `eb_system_menu` p ON c.`pid` = p.`id`
 WHERE p.`component` = '/stock' AND c.`is_show` = 1 AND c.`is_delte` = 0
 ORDER BY c.`sort` DESC;
-- ========== END: menu_restructure_20260922.sql ==========


-- ========== BEGIN: store_product_group_20260922.sql ==========
-- ============================================================
-- 商品分组（2026-09-22）
--
-- 功能：商城商品可加入多个「商品分组」，按角色/会员分组/等级控制可见性。
--       只管商城展示与购买，不影响订货中心。
--
-- 幂等：表 IF NOT EXISTS；菜单/配置 NOT EXISTS；角色授权 NOT EXISTS。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 1. 分组主表 ----------
CREATE TABLE IF NOT EXISTS `eb_store_product_group` (
  `id`              int          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`            varchar(64)  NOT NULL DEFAULT '' COMMENT '分组名称',
  `permission_type` varchar(32)  NOT NULL DEFAULT 'all' COMMENT '权限：all全部/promoter分销商/agent代理商/stock_agent订货商',
  `user_group_ids`  varchar(255) NOT NULL DEFAULT '' COMMENT '会员分组id，逗号分隔，空=不限',
  `user_level_ids`  varchar(255) NOT NULL DEFAULT '' COMMENT '会员等级id，逗号分隔，空=所有等级',
  `level_only`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否仅限所选等级：0否 1是',
  `min_buy`         int          NOT NULL DEFAULT 1 COMMENT '起卖数',
  `limit_one`       tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否限购一件：0否 1是',
  `layout`          varchar(16)  NOT NULL DEFAULT 'double' COMMENT '布局：double双列/single单列',
  `style`           tinyint      NOT NULL DEFAULT 1 COMMENT '内容样式 1/2/3',
  `badge`           varchar(255) NOT NULL DEFAULT '' COMMENT '图片角标',
  `title_multi`     tinyint(1)   NOT NULL DEFAULT 0 COMMENT '标题多行显示',
  `sort`            int          NOT NULL DEFAULT 0 COMMENT '排序，越大越靠前',
  `status`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1开启',
  `is_del`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_del` (`status`, `is_del`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品分组';

-- ---------- 2. 分组-商品关联 ----------
CREATE TABLE IF NOT EXISTS `eb_store_product_group_rel` (
  `id`          int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id`    int NOT NULL DEFAULT 0 COMMENT '分组id',
  `product_id`  int NOT NULL DEFAULT 0 COMMENT '商品id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_product` (`group_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分组关联';

-- ---------- 3. 全局配置 ----------
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'product_group_other_visible', '设置分组权限后其它会员能否看到分组中商品', 0, '0', 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'product_group_other_visible');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'product_group_deny_tip_enable', '自定义不符合商品分组权限条件的提示语开关', 0, '0', 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'product_group_deny_tip_enable');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'product_group_deny_tip', '不符合商品分组权限条件的提示语', 0, '您暂无权限查看该商品', 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'product_group_deny_tip');

-- ---------- 4. 菜单（挂到商品一级目录下） ----------
SET @store_pid := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `pid` = 0 AND (`component` = '/store' OR `name` = '商品') AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @store_pid, '商品分组', '', 'admin:store:product:group:list', '/store/productGroup', 'C', 85, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @store_pid IS NOT NULL
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_menu`
          WHERE `component` = '/store/productGroup' AND `is_delte` = 0
       );

SET @pg_menu_id := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/store/productGroup' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组新增', '', 'admin:store:product:group:save', '', 'A', 1, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组编辑', '', 'admin:store:product:group:update', '', 'A', 2, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组删除', '', 'admin:store:product:group:delete', '', 'A', 3, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:delete' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组详情', '', 'admin:store:product:group:info', '', 'A', 4, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:info' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组配置', '', 'admin:store:product:group:config', '', 'A', 5, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:config' AND `is_delte` = 0);

-- 【已下线，不再创建】「添加分组」入口 /store/productGroup/edit
--   背景：该入口后来被新的「商品分组」页面取代 ——
--   product_group_level_source.sql 会把它 is_show 置 0 隐藏，
--   fix_duplicate_data_20260923.sql 再按 name+perms 物理删除。
--   若本脚本继续「缺失就补插」，就会与上面两步形成
--   「插入 → 隐藏 → 删除 → 再插入」的死循环：
--     · 每次执行补丁都多插一条菜单并多授一条角色权限；
--     · 菜单随即被删，授权便成为指向不存在菜单 id 的孤儿数据。
--   故此处改为不再创建。历史上已存在的行由 fix_duplicate_data_20260923.sql 清理。

-- ---------- 5. 超管角色授权 ----------
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
  FROM `eb_system_role` r
 CROSS JOIN `eb_system_menu` m
 WHERE r.`level` = 0 AND r.`status` = 1
   AND m.`is_delte` = 0
   AND (m.`perms` LIKE 'admin:store:product:group:%'
        OR m.`component` = '/store/productGroup')
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_role_menu` rm
          WHERE rm.`rid` = r.`id` AND rm.`menu_id` = m.`id`
       );
-- ========== END: store_product_group_20260922.sql ==========


-- ========== BEGIN: product_commission_config_20260922.sql ==========
-- ============================================================
-- 商品级佣金设置（2026-09-22）
--
-- 单个商品可单独配置分销/代理/订货/门店佣金；
-- 字段为空表示取全局/等级配置；显式 0 表示该商品无此项奖励。
-- 幂等：列不存在才添加。
-- ============================================================
SET NAMES utf8mb4;

-- MySQL 5.7 兼容：用 information_schema 判断后 ADD COLUMN
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'commission_config'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `commission_config` text NULL COMMENT ''商品级佣金配置JSON：空字段取全局，0表示该商品无此项'' AFTER `is_sub`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- ========== END: product_commission_config_20260922.sql ==========


-- ========== BEGIN: distributor_level_20260923.sql ==========
-- =============================================================
-- 分销商等级（原「会员返佣配置」重构）
--   1) 新建独立的分销等级表 eb_distributor_level，不再依赖 eb_system_user_level
--   2) 按现有会员等级与返佣配置预置初始数据，保证平滑过渡
--   3) 菜单「会员返佣配置」改名为「分销商等级」，同步前端路由
-- MySQL 5.7 兼容，全部幂等，可重复执行
-- =============================================================

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. 分销商等级表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_distributor_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '分销等级名称',
  `grade` int NOT NULL DEFAULT '1' COMMENT '等级权重，数值越大等级越高',
  `self_brokerage_rate` int NOT NULL DEFAULT '0' COMMENT '自购返佣比例(%)',
  `brokerage_rate_one` int NOT NULL DEFAULT '0' COMMENT '一级返佣比例(%)',
  `brokerage_rate_two` int NOT NULL DEFAULT '0' COMMENT '二级返佣比例(%)',
  `direct_user_count` int NOT NULL DEFAULT '0' COMMENT '升级条件-直推会员人数(人)',
  `direct_user_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推会员人数条件关系：1=与，2=或',
  `team_user_count` int NOT NULL DEFAULT '0' COMMENT '升级条件-团队会员人数(人)',
  `team_user_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '团队会员人数条件关系：1=与，2=或',
  `direct_level_id` int NOT NULL DEFAULT '0' COMMENT '升级条件-直推指定等级ID，0=未启用',
  `direct_level_count` int NOT NULL DEFAULT '0' COMMENT '升级条件-直推指定等级人数(人)',
  `direct_level_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推指定等级条件关系：1=与，2=或',
  `total_consume_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-累计商城总消费额(元)',
  `total_consume_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '累计商城总消费额条件关系：1=与，2=或',
  `total_recharge_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-总充值额(元)',
  `total_recharge_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '总充值额条件关系：1=与，2=或',
  `team_product_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-团队商品总消费额(元)',
  `team_product_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '团队商品总消费额条件关系：1=与，2=或',
  `direct_consume_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-直推商城消费总额(元)',
  `direct_consume_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推商城消费总额条件关系：1=与，2=或',
  `direct_user_consume_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-直推会员商城消费总额(元)',
  `direct_user_consume_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推会员商城消费总额条件关系：1=与，2=或',
  `is_show` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示：1=显示，0=隐藏',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_grade` (`grade`) USING BTREE,
  KEY `idx_is_del` (`is_del`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='分销商等级表';

-- -------------------------------------------------------------
-- 2. 预置初始数据：按现有会员等级 + 其返佣配置迁移（幂等，按名称去重）
--    返佣配置取该等级在 eb_system_user_level_brokerage 里最新的一条
--    （不区分是否软删，保证老配置的数值不丢）
-- -------------------------------------------------------------
INSERT INTO `eb_distributor_level`
  (`name`, `grade`, `self_brokerage_rate`, `brokerage_rate_one`, `brokerage_rate_two`,
   `is_show`, `is_del`, `create_time`, `update_time`)
SELECT
  ul.`name`,
  ul.`grade`,
  COALESCE(b.`self_brokerage_rate`, 0),
  COALESCE(b.`brokerage_rate_one`, 0),
  COALESCE(b.`brokerage_rate_two`, 0),
  1, 0, NOW(), NOW()
FROM `eb_system_user_level` ul
LEFT JOIN (
  SELECT level_id, MAX(id) AS max_id
    FROM `eb_system_user_level_brokerage`
   GROUP BY level_id
) bm ON bm.`level_id` = ul.`id`
LEFT JOIN `eb_system_user_level_brokerage` b ON b.`id` = bm.`max_id`
WHERE ul.`is_del` = 0
  AND NOT EXISTS (
        SELECT 1 FROM `eb_distributor_level` dl WHERE dl.`name` = ul.`name` AND dl.`is_del` = 0
      );

-- -------------------------------------------------------------
-- 3. 菜单改名 + 路由同步（按 perms 幂等定位，不用自增 id）
--    perms 保持不变，避免影响角色授权与后端鉴权
-- -------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `name` = '分销商等级',
       `component` = '/distribution/distributorLevel'
 WHERE `perms` = 'admin:system:user:level:brokerage:list';

SELECT 'distributor_level patches done' AS result;
-- ========== END: distributor_level_20260923.sql ==========

-- ========== BEGIN: distributor_level_upgrade_20260923.sql ==========
-- ============================================================
-- 分销商等级：接入统计 + 自动升级判定
-- 同时补：等级统计幂等流水表（团队等级/分销商等级共用）
-- 幂等：可重复执行
-- ============================================================

-- 1) eb_user 增加分销商等级落点字段
SET @exist := (SELECT COUNT(1) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'distributor_level_id');
SET @sql := IF(@exist = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `distributor_level_id` int(11) NOT NULL DEFAULT 0 COMMENT ''分销商等级ID，0=无'' AFTER `team_level`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 分销商等级统计表（持久化累加，支付成功口径，退款回退）
CREATE TABLE IF NOT EXISTS `eb_user_distributor_level_stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户ID',
  `total_consume_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '累计商城总消费额（本人）',
  `direct_consume_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '直推商城消费总额（一级下级订单）',
  `direct_user_consume_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '直推会员商城消费总额（一级下级且 level>0）',
  `team_product_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '团队商品总消费额（整条推荐链下级订单）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商等级统计表';

-- 3) 分销商等级变更记录表
CREATE TABLE IF NOT EXISTS `eb_user_distributor_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户ID',
  `level_id` int(11) NOT NULL DEFAULT 0 COMMENT '分销商等级ID',
  `grade` int(11) NOT NULL DEFAULT 0 COMMENT '等级权重',
  `mark` varchar(255) DEFAULT '' COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁止',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商等级变更记录表';

-- 4) 等级统计幂等流水表（防止同一订单重复累加/重复回退）
--    module: TEAM=团队等级 DISTRIBUTOR=分销商等级
--    scene : PAID=支付成功 COMPLETE=订单完成 REFUND=退款回退
CREATE TABLE IF NOT EXISTS `eb_level_stat_order_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `module` varchar(32) NOT NULL COMMENT '模块：TEAM / DISTRIBUTOR',
  `scene` varchar(32) NOT NULL COMMENT '场景：PAID / COMPLETE / REFUND',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_module_scene` (`order_no`, `module`, `scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级统计幂等流水表';

-- 5) 配置项（不存在才插入，已存在不覆盖）
INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_enabled' AS n, '1' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_enabled');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_brokerage_enabled' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_brokerage_enabled');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'team_level_cycle_reset' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'team_level_cycle_reset');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_cycle_reset' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_cycle_reset');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_max_depth' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_max_depth');

-- 6) 历史数据回填：把已支付订单的金额补进分销商统计表（幂等，只补不存在的 uid 行）
--    只初始化行，具体金额由「全量重算」接口/任务补齐，避免此处全表扫描拖慢部署
INSERT INTO `eb_user_distributor_level_stat` (`uid`, `total_consume_amount`, `direct_consume_amount`,
                                              `direct_user_consume_amount`, `team_product_amount`)
SELECT u.`uid`, 0, 0, 0, 0
FROM `eb_user` u
WHERE NOT EXISTS (SELECT 1 FROM `eb_user_distributor_level_stat` s WHERE s.`uid` = u.`uid`);

-- 7) 校验
SELECT 'eb_user.distributor_level_id' AS item,
       (SELECT COUNT(1) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'distributor_level_id') AS ok;
SELECT `name`, `value` FROM `eb_system_config`
WHERE `name` IN ('distributor_level_enabled','distributor_level_brokerage_enabled',
                 'team_level_cycle_reset','distributor_level_cycle_reset','distributor_level_max_depth');
-- ========== END: distributor_level_upgrade_20260923.sql ==========













-- ========== BEGIN: product_group_level_source.sql ==========
-- ==================================================================
-- 商品分组：等级来源按权限拆分（会员 / 分销商 / 订货商-代理）
--            + 商品菜单调整（隐藏「添加分组」、商品分组排到商品分类下方）
--
-- 幂等，可重复执行。执行后需清 Redis 配置缓存：
--   redis-cli -a 123456 -n 7  HSET config_list <key> <val>   (仅当改了 eb_system_config 时)
--   菜单为实时查库，无需重启；但前端需重新登录/刷新以拉取新菜单。
-- ==================================================================
-- 注意：不写 USE `库名` —— 目标库由连接/命令行决定（本地 crmeb、线上 crmeb_java3）。
--       写死库名会让线上 deploy.sh 打到错库或 ERROR 1049 中断后续语句。

-- ----------------------------------------------------------------
-- 1) eb_store_product_group 新增两个等级列（幂等）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'distributor_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `distributor_level_ids` varchar(255) NOT NULL DEFAULT ''
      COMMENT '分销商等级id，逗号分隔' AFTER `user_level_ids`;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'stock_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `stock_level_ids` varchar(255) NOT NULL DEFAULT ''
      COMMENT '订货商/代理商等级id（eb_stock_level），逗号分隔' AFTER `distributor_level_ids`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_col_if_missing`;

-- ----------------------------------------------------------------
-- 2) 商品菜单：隐藏左侧「添加分组」（页面内已有新建按钮，避免重复）
--    按 component 定位，不用自增 id
-- ----------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `is_show` = 0
 WHERE `is_delte` = 0
   AND `menu_type` = 'C'
   AND `component` = '/store/productGroup/edit';

-- ----------------------------------------------------------------
-- 3) 商品下菜单排序：商品管理 > 商品分类 > 商品分组 > 商品规格 > 商品评论 > 保障服务
--    （同级按 sort DESC 排列，值越大越靠上）
-- ----------------------------------------------------------------
UPDATE `eb_system_menu` SET `sort` = 6 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/index';
UPDATE `eb_system_menu` SET `sort` = 5 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/sort';
UPDATE `eb_system_menu` SET `sort` = 4 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/productGroup';
UPDATE `eb_system_menu` SET `sort` = 3 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/attr';
UPDATE `eb_system_menu` SET `sort` = 2 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/comment';
UPDATE `eb_system_menu` SET `sort` = 1 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/guarantee';

SELECT 'product_group_level_source done' AS result;
-- ========== END: product_group_level_source.sql ==========


-- ========== BEGIN: product_group_theme_20260923.sql ==========
-- ==================================================================
-- 商品分组：绑定「装修页」（eb_theme，page_type=micro）
--
-- 目的：让商品分组拥有自己的可装修落地页。
--   * 每个分组懒创建一个 eb_theme 记录（page_type=micro，内容存 home_data），
--     通过 eb_store_product_group.theme_id 绑定；
--   * 后台分组编辑页内嵌装修器，装修结果写入该 theme 记录；
--   * H5 分组落地页按 theme_id 渲染装修内容 + 展示分组商品。
--
-- 幂等，可重复执行。
-- ==================================================================
-- 注意：不写 USE `库名` —— 目标库由连接/命令行决定（本地 crmeb、线上 crmeb_java3）。
--       写死库名会让线上 deploy.sh 打到错库或 ERROR 1049 中断后续语句。

-- ----------------------------------------------------------------
-- 1) eb_store_product_group 新增 theme_id（幂等）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_theme_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_theme_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'theme_id') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `theme_id` int NOT NULL DEFAULT 0
      COMMENT '绑定的装修页ID（eb_theme.id，page_type=micro），0=未创建' AFTER `badge`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_theme_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_theme_col_if_missing`;

SELECT 'product_group_theme done' AS result;
-- ========== END: product_group_theme_20260923.sql ==========

-- ========== BEGIN: product_group_permission_20260923.sql ==========
-- ==================================================================
-- 商品分组：权限口径调整（区域代理等级 + 社群团队等级）
--
-- 背景：
--   permission_type 原为 all / promoter / agent / stock_agent，
--   其中 agent（原「仅代理商」）复用了 eb_stock_level 的等级列表，
--   而「代理商」在系统里实际是独立模块「区域代理」（eb_agent），
--   其等级是固定枚举：1=省级(省代) 2=市级(市代) 3=区级(区代)。
--
-- 本次调整：
--   1) agent 语义明确为「仅区域代理」，等级改用固定枚举 1/2/3，
--      落到新列 agent_level_ids（不再用 eb_stock_level）；
--   2) 新增权限类型 team「仅社群团队」，等级来源为
--      后台「运营 - 团队等级配置」→ eb_system_team_level.id，
--      落到新列 team_level_ids。
--
-- 字段语义：
--   * agent_level_ids：区域代理等级 1=省代 2=市代 3=区代，逗号分隔，空=不限等级
--   * team_level_ids ：社群团队等级（eb_system_team_level.id），逗号分隔，空=不限等级
--
-- 注意：agent 类型不再读 stock_level_ids（值域不同，1/2/3 vs eb_stock_level.id），
--      变更前已存在的 agent 分组需在后台重新选择等级。
--
-- 幂等，可重复执行。
-- ==================================================================
-- 注意：不写 USE `库名` —— 目标库由连接/命令行决定（本地 crmeb、线上 crmeb_java3）。
--       写死库名会让线上 deploy.sh 打到错库或 ERROR 1049 中断后续语句。

-- ----------------------------------------------------------------
-- 1) 新增 agent_level_ids（区域代理等级）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_agent_level_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_agent_level_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'agent_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `agent_level_ids` varchar(64) NOT NULL DEFAULT ''
      COMMENT '区域代理等级（eb_agent.level：1=省代 2=市代 3=区代），逗号分隔' AFTER `distributor_level_ids`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_agent_level_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_agent_level_col_if_missing`;

-- ----------------------------------------------------------------
-- 2) 新增 team_level_ids（社群团队等级）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_team_level_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_team_level_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'team_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `team_level_ids` varchar(255) NOT NULL DEFAULT ''
      COMMENT '社群团队等级id（eb_system_team_level.id），逗号分隔' AFTER `stock_level_ids`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_team_level_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_team_level_col_if_missing`;

-- ----------------------------------------------------------------
-- 3) 刷新 permission_type / stock_level_ids 列注释（幂等）
-- ----------------------------------------------------------------
ALTER TABLE `eb_store_product_group`
  MODIFY COLUMN `permission_type` varchar(32) NOT NULL DEFAULT 'all'
  COMMENT '权限：all全部会员/promoter仅分销商/agent仅区域代理/stock_agent仅订货商/team仅社群团队';

ALTER TABLE `eb_store_product_group`
  MODIFY COLUMN `stock_level_ids` varchar(255) NOT NULL DEFAULT ''
  COMMENT '订货商等级id（eb_stock_level），逗号分隔';

SELECT 'product_group_permission done' AS result;
-- ========== END: product_group_permission_20260923.sql ==========

-- ========== BEGIN: fix_duplicate_data_20260923.sql ==========
-- ============================================================================
-- CRMEB 数据清理补丁（2026-09-23）
-- 清理三类历史脏数据，**幂等**，可重复执行；本地与线上同一份脚本。
--   1) eb_system_config 同 name 重复行（补丁 DELETE+INSERT 片段未生效时累积）
--   2) eb_system_menu 残留 / 重复 / 错挂菜单项
--   3) eb_store_product.commission_config 中已下线的订货商（stock）段
-- 执行后建议：重启 admin/front（或 DEL config_list 后重启）以重载配置缓存。
-- ============================================================================

-- ────────────────────────────────────────────────────────────────────────────
-- 1) 配置表去重：同 name 仅保留 id 最大（最后写入）的一条
--    背景：hidden_super_admin.sql / fix_online_20260919.sql 采用
--          「DELETE FROM eb_system_config WHERE name IN (...) + INSERT」模式；
--          若某次执行只跑了 INSERT 未跑 DELETE，就会累积同 name 多行。
--    影响：SystemConfigServiceImpl.updateOrSaveValueByName() 遇到重名**直接抛异常**
--          「配置名称存在多个」，导致后台这些配置项根本无法保存。
-- ────────────────────────────────────────────────────────────────────────────
DELETE c1 FROM eb_system_config c1
INNER JOIN eb_system_config c2
        ON c1.name = c2.name
       AND c1.id < c2.id;

-- ────────────────────────────────────────────────────────────────────────────
-- 2) 菜单清理
-- ────────────────────────────────────────────────────────────────────────────

-- 2a) 「保存用户隐私协议」的历史残留：perms 为空、component 错填了 perms 值，
--     且 is_delte=1（已被标记删除但未物理删除），与 456 号按钮重复。
DELETE FROM eb_system_menu
 WHERE name = '保存用户隐私协议'
   AND (perms IS NULL OR perms = '')
   AND is_delte = 1;

-- 2b) 错挂在隐藏占位菜单下的「商品与库存」：pid 指向 pid=0 且 menu_type='A'
--     的自动生成权限占位行，与「订货商」菜单下的同名项完全重复（同 perms/component）。
DELETE m FROM eb_system_menu m
INNER JOIN eb_system_menu p ON m.pid = p.id
 WHERE m.perms = 'admin:stock:product:list'
   AND m.component = '/stock/product'
   AND p.pid = 0
   AND p.menu_type = 'A';

-- 2c) 已下线的「添加分组」入口（与「商品分组」菜单及其新增按钮重复）。
--     该行由 store_product_group_20260922.sql 早期版本创建、被 product_group_level_source.sql
--     置 is_show=0 隐藏；此处按 name + perms + menu_type 直接物理删除。
--     原判据还带 `is_show = 0`，只有在「先被隐藏」之后才删得掉，
--     与补插脚本形成「插入→隐藏→删除→再插入」的死循环，故去掉该条件。
DELETE FROM eb_system_menu
 WHERE name = '添加分组'
   AND perms = 'admin:store:product:group:save'
   AND menu_type = 'C';

-- 2d) 重复菜单行：同一 (component, menu_type) 只保留 id 最小的一条，其余物理删除。
--     背景：menu_restructure_20260922.sql 早期版本的 /yunying 插入守卫写成
--           `component='/yunying' AND pid=0`，一旦该行 pid 被其它脚本改写，守卫即失配，
--           每执行一次补丁就多插一条同名「运营」目录——本地曾累积到 10 条
--           （id 701 / 761 / 764 / … / 785）。
--     边界：menu_type='A' 的按钮行 component 为空、不参与本规则；
--           /dashboard 同时存在 M（首页）与 C（控制台）两条，靠 menu_type 区分，不受影响。
DELETE m1 FROM eb_system_menu m1
INNER JOIN eb_system_menu m2
        ON m1.`component` = m2.`component`
       AND m1.`menu_type` = m2.`menu_type`
       AND m1.`id` > m2.`id`
 WHERE m1.`component` IS NOT NULL
   AND m1.`component` <> '';

-- ────────────────────────────────────────────────────────────────────────────
-- 3) 商品级佣金配置：移除已下线的订货商（stock）段
--    订货商拿货价/平级奖励改由「运营 → 订货 → 商品与库存」按商品独立设置，
--    商品侧 ProductCommissionConfig 已删除 stock 字段，历史 JSON 里的该段不再生效。
-- ────────────────────────────────────────────────────────────────────────────

-- 3a) 各业务段均为空的空壳配置直接置 NULL
UPDATE eb_store_product
   SET commission_config = NULL
 WHERE commission_config IS NOT NULL
   AND commission_config <> ''
   AND JSON_VALID(commission_config)
   AND (JSON_EXTRACT(commission_config, '$.agent')        IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.agent'))        = 0)
   AND (JSON_EXTRACT(commission_config, '$.distributor')  IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.distributor'))  = 0)
   AND (JSON_EXTRACT(commission_config, '$.store')        IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.store'))        = 0)
   AND (JSON_EXTRACT(commission_config, '$.team')         IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.team'))         = 0)
   AND (JSON_EXTRACT(commission_config, '$.stock')        IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.stock'))        = 0);

-- 3b) 含 stock 段但该段为空的，仅摘除 stock 键
UPDATE eb_store_product
   SET commission_config = JSON_REMOVE(commission_config, '$.stock')
 WHERE commission_config IS NOT NULL
   AND commission_config <> ''
   AND JSON_VALID(commission_config)
   AND JSON_CONTAINS_PATH(commission_config, 'one', '$.stock')
   AND JSON_LENGTH(JSON_EXTRACT(commission_config, '$.stock')) = 0;

-- 3c) 含 stock 段且**有实际值**的：同样摘除（该字段已不参与任何结算；
--     保留会造成「看着配了但不生效」的误导）。摘除后各段为空则置 NULL。
UPDATE eb_store_product
   SET commission_config = NULLIF(JSON_REMOVE(commission_config, '$.stock'), CAST('{}' AS JSON))
 WHERE commission_config IS NOT NULL
   AND commission_config <> ''
   AND JSON_VALID(commission_config)
   AND JSON_CONTAINS_PATH(commission_config, 'one', '$.stock')
   AND JSON_LENGTH(JSON_EXTRACT(commission_config, '$.stock')) > 0;

-- ────────────────────────────────────────────────────────────────────────────
-- 4) 孤儿角色授权清理（必须放在本文件所有「删菜单」语句之后）
--    eb_system_role_menu 中 menu_id 已不存在于 eb_system_menu 的授权行。
--    来源：上面 2a~2d 会物理删除菜单行；若该菜单曾被授权给角色，授权行就成了
--          指向不存在 id 的孤儿。补插脚本（store_product_group_20260922.sql 等）
--          每次执行又会新造一批孤儿，不清理会无限累积
--          （表现为 eb_system_role_menu 行数每次执行补丁都 +1）。
-- ────────────────────────────────────────────────────────────────────────────
DELETE rm FROM eb_system_role_menu rm
LEFT JOIN eb_system_menu m ON m.`id` = rm.`menu_id`
 WHERE m.`id` IS NULL;

SELECT 'CRMEB fix_duplicate_data done' AS result;
-- ========== END: fix_duplicate_data_20260923.sql ==========


-- ========== BEGIN: system_settings_20260923.sql ==========
-- ============================================================
-- CRMEB Java 3.0 项目配置收敛（本地配置 → 线上）
-- 生成时间: 2026-09-23 17:53:13
-- 生成来源：本地库 eb_system_config 与厂商基库 Crmeb_v3.0.sql 的差异集
--   · 我方新增配置 55 项（订货商 / 团队奖 / 分销商等级 / 区域代理 / 提现 / 隐藏面板开关等）
--   · 值被我方修改 28 项，逐条如下：
--   api_url                          http://api.qianxutec.com  →  生产地址
--   brokerage_func_status            0
--   change_color_config              #1B90FF
--   config_export_open               2
--   copyright_company_name           黔序科技
--   copyright_internet_record        Copyright@2026 贵州黔序科技有限公司
--   copyright_internet_record_url    www.qianxutec.com
--   crmeb_tongji_js                  (空)
--   front_api_url                    http://api.qianxutec.com  →  生产地址
--   integral_ratio                   0
--   localUploadUrl                   http://api.qianxutec.com  →  生产地址
--   logistics_type                   2
--   mobile_login_logo                crmebimage/public/product/2026/09/16/d2882dd
--   order_give_integral              0
--   routine_name                     黔序科技
--   routine_phone_verification       ,1
--   seo_title                        黔序科技
--   site_logo_lefttop                crmebimage/public/theme/2026/09/19/ce5ce8255
--   site_logo_login                  crmebimage/public/product/2026/09/16/a83b057
--   site_logo_square                 crmebimage/public/theme/2026/09/19/ce5ce8255
--   site_name                        黔序科技-Java
--   site_url                         http://api.qianxutec.com  →  生产地址
--   splash_ad_switch                 0
--   store_brokerage_is_bubble        0
--   system_product_copy_type         2
--   telephone_service_switch         close
--   user_extract_min_price           1
--   wechat_routine_shipping_switch   1
--
-- ⛔ 刻意【不同步】的东西（保持线上现值）：
--   1. 全部凭证 / 密钥类配置——微信支付商户号与证书、支付宝密钥、短信账号 token、
--      阿里云 / 七牛 / 腾讯云 / 京东存储密钥、地图 key、小票打印密钥、快递查询码等。
--      本地这些值全是厂商占位符（111111 / 1111 / xxxx），推上去会让线上的
--      支付、短信、图片上传立即不可用。下游部署后请在后台自行填写。
--   2. 与厂商基库取值一致的其余配置项——不同步，避免用厂商默认值覆盖线上真实值
--      （例如 consumer_hotline、system_express_app_code 之类线上可能已配置过的项）。
--
-- 地址口径（生产）：接口 / 图片域名 http://api.qianxutec.com；移动端站点 http://app.qianxutec.com
--   site_url 的语义是「移动端站点域名」，被微信 H5 支付当作 h5_info.wap_url 使用
--   （OrderPayServiceImpl#getUnifiedorderVo），因此指向 H5 站点而不是接口域名。
--
-- 幂等：临时表 + 按 name 匹配，跑多少遍结果一致；不触碰任何业务数据。
-- ============================================================
SET NAMES utf8mb4;

CREATE TEMPORARY TABLE `tmp_cfg_sync` (
  `name`  varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL DEFAULT '',
  `value` text,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `tmp_cfg_sync` (`name`, `title`, `value`) VALUES
('team_brokerage_credit_timing', '团队奖到账方式', '1'),
('register_default_is_promoter', '注册默认推广员', '1'),
('register_default_user_level', '注册默认会员等级', '1'),
('team_brokerage_status', '团队极差奖开关：0=关闭，1=开启', '1'),
('team_brokerage_max_depth', '团队奖向上追溯层数：0=不限', '0'),
('brokerage_credit_timing', '分销佣金到账方式', '1'),
('integral_credit_timing', '积分到账方式', '1'),
('agent_func_status', '区域代理功能开关', '1'),
('agent_apply_status', '区域代理申请开关', '1'),
('agent_credit_timing', '区域代理结算时机', '1'),
('stock_order_audit', '订货订单上级审核开关', '1'),
('stock_diff_reward_status', '订货差价奖励开关', '1'),
('stock_exchange_diff', '换货单参与差价奖励', '0'),
('stock_ladder_status', '阶梯业绩奖励开关', '1'),
('stock_ladder_cycle', '阶梯业绩结算周期（1=月度 2=季度 3=年度）', '1'),
('stock_peer_status', '订货平级奖励开关', '1'),
('stock_peer_rate', '订货平级奖励比例', '5'),
('stock_peer_generations', '订货平级奖励代数', '1'),
('stock_parent_deliver', '订货订单由上级发货', '0'),
('stock_up_search_hours', '上级无库存自动向上查找等待时长(小时)', '12'),
('user_extract_switch', '佣金提现开关', '1'),
('user_extract_multiple', '提现倍数', '0'),
('user_extract_fee_type', '手续费类型', 'ratio'),
('user_extract_fee', '手续费', '0'),
('user_extract_weekdays', '可提现星期', '1,2,3,4,5,6,7'),
('user_extract_time_start', '可提现开始小时', '0'),
('user_extract_time_end', '可提现结束小时', '24'),
('login_notice_text', '', '登录后即可享受完整服务，是否前往登录？'),
('login_notice_switch', '', '1'),
('stock_wait_pay_hours', '订货单待付款超时时长（小时）', '24'),
('stock_virtual_audit', '虚拟库存单付款后审核开关', '0'),
('stock_exchange_single', '', '0'),
('stock_exchange_diff_parent_rate', '', '100'),
('stock_exchange_diff_wechat', '', '1'),
('agent_apply_regions', '会员端可申请的代理区域', '1,2,3'),
('stock_exchange_hq_audit', '', ''),
('stock_exchange_return_address', '', '收件人：总部\n电话：16688888888\n地址：贵州省贵阳市观山湖区富力中心'),
('product_group_other_visible', '设置分组权限后其它会员能否看到分组中商品', '0'),
('product_group_deny_tip_enable', '自定义不符合商品分组权限条件的提示语开关', '0'),
('product_group_deny_tip', '不符合商品分组权限条件的提示语', '您暂无权限查看该商品'),
('distributor_level_enabled', '', '1'),
('distributor_level_brokerage_enabled', '', '0'),
('team_level_cycle_reset', '', '0'),
('distributor_level_cycle_reset', '', '0'),
('distributor_level_max_depth', '', '0'),
('sys_switch_team_reward', '隐藏面板-团队奖开关', '1'),
('sys_switch_stock', '隐藏面板-订货商开关', '1'),
('sys_switch_store', '隐藏面板-门店开关', '1'),
('sys_switch_daili', '隐藏面板-区域代理开关', '1'),
('sys_switch_spread', '隐藏面板-分销开关', '1'),
('sys_switch_integral', '隐藏面板-积分开关', '1'),
('sys_switch_seckill', '隐藏面板-秒杀开关', '1'),
('sys_switch_bargain', '隐藏面板-砍价开关', '1'),
('sys_switch_combination', '隐藏面板-拼团开关', '1'),
('sys_switch_coupon', '隐藏面板-优惠券开关', '1'),
('api_url', 'api_url', 'http://api.qianxutec.com'),
('brokerage_func_status', 'brokerage_func_status', '0'),
('change_color_config', 'change_color_config', '#1B90FF'),
('config_export_open', 'config_export_open', '2'),
('copyright_company_name', 'copyright_company_name', '黔序科技'),
('copyright_internet_record', 'copyright_internet_record', 'Copyright@2026 贵州黔序科技有限公司'),
('copyright_internet_record_url', 'copyright_internet_record_url', 'www.qianxutec.com'),
('crmeb_tongji_js', 'crmeb_tongji_js', ''),
('front_api_url', 'front_api_url', 'http://api.qianxutec.com'),
('integral_ratio', 'integral_ratio', '0'),
('localUploadUrl', 'localUploadUrl', 'http://api.qianxutec.com'),
('logistics_type', 'logistics_type', '2'),
('mobile_login_logo', '', 'crmebimage/public/product/2026/09/16/d2882dd292ad4b33bca2d30d0405721acvq09gfro4.png'),
('order_give_integral', 'order_give_integral', '0'),
('routine_name', 'routine_name', '黔序科技'),
('routine_phone_verification', 'routine_phone_verification', ',1'),
('seo_title', '', '黔序科技'),
('site_logo_lefttop', 'site_logo_lefttop', 'crmebimage/public/theme/2026/09/19/ce5ce8255be549acb35c7c219e674606ynq35htpg0.png'),
('site_logo_login', 'site_logo_login', 'crmebimage/public/product/2026/09/16/a83b057c809745a7a295be94cf10275brxadtc506f.png'),
('site_logo_square', 'site_logo_square', 'crmebimage/public/theme/2026/09/19/ce5ce8255be549acb35c7c219e674606ynq35htpg0.png'),
('site_name', '', '黔序科技-Java'),
('site_url', '', 'http://app.qianxutec.com'),
('splash_ad_switch', '', '0'),
('store_brokerage_is_bubble', 'store_brokerage_is_bubble', '0'),
('system_product_copy_type', 'system_product_copy_type', '2'),
('telephone_service_switch', 'telephone_service_switch', 'close'),
('user_extract_min_price', '', '1'),
('wechat_routine_shipping_switch', '', '1');
-- 1) 已存在的项：值或标题不同才写，避免无谓刷新 update_time
UPDATE eb_system_config c
  JOIN tmp_cfg_sync t ON c.`name` = t.`name`
   SET c.`value`       = t.`value`,
       c.`title`       = IF(t.`title` = '', c.`title`, t.`title`),
       c.`update_time` = NOW()
 WHERE NOT (c.`value` <=> t.`value`)
    OR (t.`title` <> '' AND NOT (c.`title` <=> t.`title`));

-- 2) 线上缺失的项：补插（不覆盖任何已有行）
INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT t.`name`, t.`title`, 0, t.`value`, 0, NOW(), NOW()
  FROM tmp_cfg_sync t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM eb_system_config) x
                    WHERE x.`name` = t.`name`);

DROP TEMPORARY TABLE `tmp_cfg_sync`;

-- 3) 自检：下列各项必须全部存在（数量应与本次同步项数一致）
SELECT 'CRMEB system_settings done' AS result, COUNT(*) AS ensured_rows
  FROM eb_system_config WHERE `name` IN ('team_brokerage_credit_timing', 'register_default_is_promoter', 'register_default_user_level', 'team_brokerage_status', 'team_brokerage_max_depth', 'brokerage_credit_timing', 'integral_credit_timing', 'agent_func_status', 'agent_apply_status', 'agent_credit_timing', 'stock_order_audit', 'stock_diff_reward_status', 'stock_exchange_diff', 'stock_ladder_status', 'stock_ladder_cycle', 'stock_peer_status', 'stock_peer_rate', 'stock_peer_generations', 'stock_parent_deliver', 'stock_up_search_hours', 'user_extract_switch', 'user_extract_multiple', 'user_extract_fee_type', 'user_extract_fee', 'user_extract_weekdays', 'user_extract_time_start', 'user_extract_time_end', 'login_notice_text', 'login_notice_switch', 'stock_wait_pay_hours', 'stock_virtual_audit', 'stock_exchange_single', 'stock_exchange_diff_parent_rate', 'stock_exchange_diff_wechat', 'agent_apply_regions', 'stock_exchange_hq_audit', 'stock_exchange_return_address', 'product_group_other_visible', 'product_group_deny_tip_enable', 'product_group_deny_tip', 'distributor_level_enabled', 'distributor_level_brokerage_enabled', 'team_level_cycle_reset', 'distributor_level_cycle_reset', 'distributor_level_max_depth', 'sys_switch_team_reward', 'sys_switch_stock', 'sys_switch_store', 'sys_switch_daili', 'sys_switch_spread', 'sys_switch_integral', 'sys_switch_seckill', 'sys_switch_bargain', 'sys_switch_combination', 'sys_switch_coupon', 'api_url', 'brokerage_func_status', 'change_color_config', 'config_export_open', 'copyright_company_name', 'copyright_internet_record', 'copyright_internet_record_url', 'crmeb_tongji_js', 'front_api_url', 'integral_ratio', 'localUploadUrl', 'logistics_type', 'mobile_login_logo', 'order_give_integral', 'routine_name', 'routine_phone_verification', 'seo_title', 'site_logo_lefttop', 'site_logo_login', 'site_logo_square', 'site_name', 'site_url', 'splash_ad_switch', 'store_brokerage_is_bubble', 'system_product_copy_type', 'telephone_service_switch', 'user_extract_min_price', 'wechat_routine_shipping_switch');
-- ========== END: system_settings_20260923.sql ==========

-- ========== BEGIN: menu_sync_20260923.sql ==========
-- ============================================================
-- CRMEB Java 3.0 后台菜单可见树收敛（本地 → 线上）
-- 生成时间: 2026-09-23 17:53:13
-- 范围：menu_type 为 M（目录）/ C（菜单）的 125 项，即后台左侧导航的完整可见树。
--       其中标记为删除 5 项、隐藏 3 项、有父级归属 111 项。
--       A（按钮 / 权限点）不在本脚本范围——它们不参与“显示”，且由各功能的 perms
--       脚本（stock / merchant_store / distributor_level / add_*_menu 等）分别维护。
--
-- 定位方式：一律用 `component` + `menu_type` 匹配，**不使用自增 id**
--          （线上 id 与本机可能不同）；父级归属同样按父级 component 反查 id。
--           注：/dashboard 在本地同时存在 M（首页）与 C（控制台）两条，故键必须带 menu_type。
--
-- 幂等：临时表 + NOT EXISTS 补插 + 有差异才写。补插段重复 3 次是为了让
--       多级父目录先于子菜单落库（层级最多 3 层），重复执行结果不变。
--
-- 副作用（符合预期）：线上这些菜单的名称 / 图标 / 排序 / 显隐 / 所属目录 / 删除标记
--       会被收敛为本地当前状态；线上独有的菜单不在映射表内，不会被触碰。
-- ============================================================
SET NAMES utf8mb4;

CREATE TEMPORARY TABLE `tmp_menu_sync` (
  `component`        varchar(200) NOT NULL,
  `menu_type`        varchar(2)   NOT NULL,
  `parent_component` varchar(200) NOT NULL DEFAULT '',
  `perms`            varchar(200) NOT NULL DEFAULT '',
  `name`             varchar(100) NOT NULL,
  `icon`             varchar(255) NOT NULL DEFAULT '',
  `sort`             int          NOT NULL DEFAULT 99999,
  `is_show`          tinyint      NOT NULL DEFAULT 1,
  `is_delte`         tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`component`, `menu_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `tmp_menu_sync`
  (`component`, `menu_type`, `parent_component`, `perms`, `name`, `icon`, `sort`, `is_show`, `is_delte`) VALUES
('/appSetting', 'M', '', '', '应用', 's-promotion', 120, 1, 0),
('/content', 'M', '', '', '内容', 's-management', 130, 1, 0),
('/dashboard', 'M', '', '', '首页', 'menu', 200, 1, 0),
('/design', 'M', '', '', '装修', 's-home', 85, 1, 0),
('/financial', 'M', '', '', '财务', 's-finance', 90, 1, 0),
('/hidden', 'M', '', '', '系统', 'warning', 55, 1, 0),
('/javaMobile', 'M', '', '', '移动端管理', 'phone', 80, 1, 0),
('/maintain', 'M', '', '', '维护', 's-open', 60, 1, 0),
('/marketing', 'M', '', '', '营销', 's-marketing', 110, 1, 0),
('/operation', 'M', '', '', '设置', 's-tools', 70, 1, 0),
('/order', 'M', '', '', '订单', 's-order', 160, 1, 0),
('/store', 'M', '', '', '商品', 's-goods', 180, 1, 0),
('/user', 'M', '', '', '用户', 'user-solid', 140, 1, 0),
('/yunying', 'M', '', '', '运营', 's-operation', 190, 1, 0),
('/user/grade', 'C', '/user', 'admin:system:user:level:list', '用户等级', '', 2, 1, 0),
('/user/group', 'C', '/user', 'admin:user:group:list', '用户分组', '', 1, 1, 0),
('/user/index', 'C', '/user', 'admin:user:list', '用户管理 ', '', 100, 1, 0),
('/user/label', 'C', '/user', 'admin:user:tag:list', '用户标签', '', 1, 1, 0),
('/daili/agentList', 'C', '/daili', 'admin:agent:list', '代理商管理', '', 4, 1, 0),
('/daili/agentReward', 'C', '/daili', 'admin:agent:reward:list', '代理奖励明细', '', 2, 1, 0),
('/daili/agentSetting', 'C', '/daili', 'admin:agent:setting:list', '区域代理设置', '', 3, 1, 0),
('/daili/changeLog', 'C', '/daili', 'admin:agent:changelog:list', '代理商变更记录', '', 1, 1, 0),
('/order/index', 'C', '/order', 'admin:order:list', '订单管理', '', 1, 1, 0),
('/stock/agent', 'C', '/stock', 'admin:stock:agent:list', '订货商管理', '', 9, 1, 0),
('/stock/changelog', 'C', '/stock', 'admin:stock:agent:list', '订货商变更记录', '', 3, 1, 0),
('/stock/exchange', 'C', '/stock', 'admin:stock:exchange:list', '换货管理', '', 5, 1, 0),
('/stock/level', 'C', '/stock', 'admin:stock:level:list', '订货商级别设置', '', 8, 0, 0),
('/stock/order', 'C', '/stock', 'admin:stock:order:list', '订货商订单', '', 4, 1, 0),
('/stock/product', 'C', '/stock', 'admin:stock:product:list', '商品与库存', '', 7, 1, 0),
('/stock/report', 'C', '/stock', 'admin:stock:report:list', '数据报表', '', 10, 1, 0),
('/stock/reward', 'C', '/stock', 'admin:stock:reward:list', '订货商资金记录', '', 6, 1, 0),
('/stock/setting', 'C', '/stock', 'admin:stock:setting:list', '订货商设置', '', 8, 1, 0),
('/stock/withdraw', 'C', '/stock', 'admin:stock:withdraw:list', '提现管理', '', 6, 0, 1),
('/store/attr', 'C', '/store', 'admin:product:rule:list', '商品规格', '', 3, 1, 0),
('/store/comment', 'C', '/store', 'admin:product:reply:list', '商品评论', '', 2, 1, 0),
('/store/guarantee', 'C', '/store', '', '保障服务', '', 1, 1, 0),
('/store/index', 'C', '/store', 'admin:product:list', '商品管理', '', 6, 1, 0),
('/store/productGroup', 'C', '/store', 'admin:store:product:group:list', '商品分组', '', 4, 1, 0),
('/store/sort', 'C', '/store', '', '商品分类', '', 5, 1, 0),
('/design/advertisement', 'C', '/design', '', '开屏广告', '', 0, 1, 0),
('/design/mall_theme', 'C', '/design', '', '商城主题', '', 98, 1, 0),
('/design/micro_theme', 'C', '/design', '', '专题页面', '', 96, 1, 0),
('/design/my_theme', 'C', '/design', '', '我的主题', '', 97, 1, 0),
('/design/spread_poster', 'C', '/design', '', '推广海报', '', 95, 1, 0),
('/hidden/panel', 'C', '/hidden', '', '运维面板', '', 0, 1, 0),
('/content/articleManager', 'C', '/content', 'admin:article:list', '文章管理', '', 1, 1, 0),
('/content/classifManager', 'C', '/content', '', '文章分类', '', 99999, 1, 0),
('/daili', 'M', '/yunying', '', '区域代理', 'success', 102, 1, 0),
('/distribution', 'M', '/yunying', '', '分销商', 's-check', 105, 1, 0),
('/distribution/teamGrade', 'C', '/tuandui', 'admin:system:team:level:list', '团队等级', '', 90, 1, 0),
('/distribution/teamLevelConfig', 'C', '/tuandui', 'admin:system:team:level:list', '团队等级配置', '', 100, 1, 0),
('/distribution/teamRecord', 'C', '/tuandui', 'admin:system:team:level:record:list', '团队变更记录', '', 7, 1, 0),
('/distribution/teamUser', 'C', '/tuandui', 'admin:system:team:level:user:list', '团队关联用户', '', 110, 1, 0),
('/merchantStore', 'M', '/yunying', '', '门店', 's-platform', 101, 1, 0),
('/stock', 'M', '/yunying', '', '订货商', 'success', 103, 1, 0),
('/tuandui', 'M', '/yunying', '', '团队奖', 'user-solid', 104, 1, 0),
('/maintain//devconfiguration', 'M', '/maintain', '', '开发配置', '', 99999, 1, 0),
('/maintain/logistics', 'M', '/maintain', '', '物流设置', '', 99999, 1, 0),
('/maintain/picture', 'C', '/operation', 'admin:system:attachment:list', '素材管理', '', -1, 1, 0),
('/operation/maintain/schedule', 'C', '/maintain', '', '定时任务管理', '', 99, 1, 0),
('/dashboard', 'C', '/dashboard', '', '控制台', '', 9, 1, 0),
('/distribution/teamBrokerageRecord', 'C', '/financial', 'admin:system:team:level:brokerage:record', '团队奖记录', '', 8, 1, 0),
('/financial/brokerage', 'C', '/financial', 'admin:finance:monitor:brokerage:record', '佣金记录', '', 1, 1, 0),
('/financial/commission', 'M', '/financial', '', '财务操作', '', 99999, 1, 0),
('/financial/record', 'M', '/financial', '', '财务记录', '', 99999, 1, 0),
('/marketing/bargain', 'M', '/marketing', '', '砍价管理', '', 99999, 1, 0),
('/marketing/coupon', 'M', '/marketing', '', '优惠券', '', 1, 1, 0),
('/marketing/groupBuy', 'M', '/marketing', '', '拼团管理', '', 99999, 1, 0),
('/marketing/integral', 'M', '/marketing', '', '积分', '', 99999, 1, 0),
('/marketing/seckill', 'M', '/marketing', '', '秒杀管理', '', 99999, 1, 0),
('/operation/agreement', 'M', '/operation', '', '协议管理', '', 0, 1, 0),
('/operation/deliverGoods', 'M', '/operation', '', '发货设置', '', 6, 1, 0),
('/operation/design', 'M', '/operation', '', '页面管理', '', 5, 1, 1),
('/operation/guide', 'C', '/operation', '', '配置引导', '', 0, 1, 0),
('/operation/logManager', 'M', '/operation', '', '日志管理', '', 8, 1, 0),
('/operation/notification', 'C', '/operation', 'admin:system:notification:list', '消息通知', '', 4, 1, 0),
('/operation/onePass', 'C', '/operation', '', '一号通', '', 9, 0, 0),
('/operation/onePassConfig', 'C', '/operation', '', '一号通配置', '', 9, 0, 0),
('/operation/roleManager', 'M', '/operation', '', '管理权限', '', 7, 1, 0),
('/operation/setting', 'C', '/operation', 'admin:system:config:info', '系统设置', '', 10, 1, 0),
('/appSetting/publicAccount', 'M', '/appSetting', '', '公众号', '', 99999, 1, 0),
('/appSetting/publicRoutine', 'M', '/appSetting', '', '小程序', '', 99999, 1, 0),
('/javaMobile/orderCancellation', 'C', '/javaMobile', '', '订单核销', '', 99999, 1, 0),
('/javaMobile/orderStatistics', 'C', '/javaMobile', '', '订单统计', '', 99999, 1, 0),
('/distribution/distributionconfig', 'C', '/distribution', 'admin:retail:spread:manage:get', '分销配置', '', 1, 1, 0),
('/distribution/distributorLevel', 'C', '/distribution', 'admin:system:user:level:brokerage:list', '分销商等级', '', 3, 1, 0),
('/distribution/index', 'C', '/distribution', 'admin:retail:list', '分销商管理', '', 100, 1, 0),
('/merchantStore/list', 'C', '/merchantStore', 'admin:merchant:store:list', '门店管理', '', 10, 1, 0),
('/merchantStore/verify', 'C', '/merchantStore', 'admin:merchant:verify:list', '核销记录', '', 2, 1, 0),
('/financial/record/charge', 'C', '/financial/record', 'admin:recharge:list', '充值记录', '', 1, 1, 0),
('/financial/record/monitor', 'C', '/financial/record', 'admin:finance:monitor:list', '资金监控', '', 1, 1, 0),
('/marketing/coupon/list', 'C', '/marketing/coupon', 'admin:coupon:list', '优惠券', '', 1, 1, 0),
('/marketing/coupon/record', 'C', '/marketing/coupon', 'admin:coupon:user:list', '领取记录', '', 1, 1, 0),
('/operation/design/devise', 'M', '/operation/design', '', '页面装修', '', 0, 1, 1),
('/operation/design/theme', 'C', '/operation/design', '', '一键换色', '', 99999, 1, 1),
('/operation/design/viewDesign', 'C', '/operation/design', 'admin:page:layout:index', '页面设计', '', 99999, 1, 1),
('/marketing/bargain/bargainGoods', 'C', '/marketing/bargain', 'admin:bargain:list', '砍价商品', '', 1, 1, 0),
('/marketing/bargain/bargainList', 'C', '/marketing/bargain', 'admin:bargain:user:list', '砍价列表', '', 1, 1, 0),
('/marketing/seckill/config', 'C', '/marketing/seckill', 'admin:seckill:manger:list', '秒杀配置', '', 1, 1, 0),
('/marketing/seckill/list', 'C', '/marketing/seckill', 'admin:seckill:list', '秒杀商品', '', 1, 1, 0),
('/maintain/logistics/cityList', 'C', '/maintain/logistics', 'admin:system:city:list', '城市数据', '', 1, 1, 0),
('/maintain/logistics/companyList', 'C', '/maintain/logistics', 'admin:express:list', '物流公司', '', 2, 1, 0),
('/marketing/groupBuy/groupGoods', 'C', '/marketing/groupBuy', 'admin:combination:list', '拼团商品', '', 1, 1, 0),
('/marketing/groupBuy/groupList', 'C', '/marketing/groupBuy', 'admin:combination:combine:list', '拼团列表', '', 1, 1, 0),
('/marketing/integral/integralconfig', 'C', '/marketing/integral', '', '积分配置', '', 99999, 1, 0),
('/marketing/integral/signin', 'C', '/marketing/integral', '', '签到配置', '', 99998, 1, 0),
('/marketing/integral/integrallog', 'C', '/marketing/integral', 'admin:user:integral:list', '积分日志', '', 1, 1, 0),
('/financial/commission/setting', 'C', '/financial/commission', 'admin:finance:extract:setting:get', '提现设置', '', 2, 1, 0),
('/financial/commission/template', 'C', '/financial/commission', 'admin:finance:apply:list', '申请提现', '', 1, 1, 0),
('/financial/commission/recharge', 'C', '/financial/commission', '', '充值设置', '', 0, 1, 0),
('/operation/logManager/adminLoginLog', 'C', '/operation/logManager', 'admin:log:login:list', '管理员登录日志', '', 1, 1, 0),
('/operation/logManager/adminOperateLog', 'C', '/operation/logManager', 'admin:log:sensitive:list', '管理员操作日志', '', 2, 1, 0),
('/operation/roleManager/adminList', 'C', '/operation/roleManager', 'admin:system:admin:list', '管理员列表', '', 1, 1, 0),
('/operation/roleManager/identityManager', 'C', '/operation/roleManager', 'admin:system:role:list', '角色管理', '', 1, 1, 0),
('/operation/roleManager/promiseRules', 'C', '/maintain', 'admin:system:menu:list', '权限规则', '', 1, 1, 0),
('/operation/deliverGoods/freightSet', 'C', '/operation/deliverGoods', 'admin:shipping:templates:list', '运费模板', '', 2, 1, 0),
('/appSetting/publicAccount/wxMenus', 'C', '/appSetting/publicAccount', 'admin:wechat:menu:public:get', '微信菜单', '', 0, 1, 0),
('/appSetting/publicAccount/wxReply', 'M', '/appSetting/publicAccount', '', '自动回复', '', 1, 1, 0),
('/appSetting/publicRoutine/deliveryManagement', 'C', '/appSetting/publicRoutine', '', '发货管理', '', 0, 1, 0),
('/appSetting/publicRoutine/download', 'C', '/appSetting/publicRoutine', 'admin:wechat:code:download', '下载小程序', '', 0, 1, 0),
('/maintain/devconfiguration/combineddata', 'C', '/maintain//devconfiguration', 'admin:system:group:list', '组合数据', '', 1, 1, 0),
('/maintain/devconfiguration/configCategory', 'C', '/maintain//devconfiguration', '', '配置分类', '', 99999, 1, 0),
('/maintain/devconfiguration/formConfig', 'C', '/maintain//devconfiguration', 'admin:system:form:list', '表单配置', '', 1, 1, 0),
('/maintain/schedule/list', 'C', '/operation/maintain/schedule', '', '定时任务', '', 0, 1, 0),
('/maintain/schedule/logList', 'C', '/operation/maintain/schedule', '', '定时任务日志', '', 0, 1, 0),
('/appSetting/publicAccount/wxReply/follow', 'C', '/appSetting/publicAccount/wxReply', 'admin:wechat:keywords:reply:info:keywords', '微信关注回复', '', 99999, 1, 0),
('/appSetting/publicAccount/wxReply/keyword', 'C', '/appSetting/publicAccount/wxReply', 'admin:wechat:keywords:reply:list', '关键字回复', '', 99999, 1, 0),
('/appSetting/publicAccount/wxReply/replyIndex', 'C', '/appSetting/publicAccount/wxReply', '', '无效关键词回复', '', 99999, 1, 0);

-- 0) 每个 (component, menu_type) 的「规范行」= id 最小的一条。
--    历史补丁可能已经插出重复菜单（例如 /yunying 曾经每次执行都多插一条），
--    重复行由 fix_duplicate_data_20260923.sql 标记 is_delte=1；
--    这里后续的收敛只作用于规范行，否则会把刚标记删除的重复行又改回未删除，
--    与去重逻辑来回震荡、永远不幂等。
DROP TEMPORARY TABLE IF EXISTS `tmp_menu_canon`;
CREATE TEMPORARY TABLE `tmp_menu_canon` AS
SELECT `component`, `menu_type`, MIN(`id`) AS `id`
  FROM eb_system_menu
 WHERE `component` IS NOT NULL AND `component` <> ''
 GROUP BY `component`, `menu_type`;

-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
--    （层级最多 3 层）；NOT EXISTS 保证重复执行不出重复行。
--    父级查找表单独物化还有一个原因：MySQL 不允许同一查询里两次引用同一张临时表
--    （否则 ERROR 1137 Can't reopen table），所以不能把 tmp_menu_sync 既当主表又当父表。
DROP TEMPORARY TABLE IF EXISTS `tmp_menu_parent`;
CREATE TEMPORARY TABLE `tmp_menu_parent` AS
SELECT t.`component`, t.`menu_type`, MIN(p.`id`) AS `pid`
  FROM tmp_menu_sync t
  JOIN eb_system_menu p
    ON p.`component` = t.`parent_component`   -- 父级已标记删除也照挂，保持原有归属
 WHERE t.`parent_component` <> ''      -- ← 必须有！否则空父级会去匹配所有 component='' 的按钮行
 GROUP BY t.`component`, t.`menu_type`;

INSERT INTO eb_system_menu (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT COALESCE(mp.`pid`, 0), t.`name`, t.`icon`, t.`perms`, t.`component`,
       t.`menu_type`, t.`sort`, t.`is_show`, t.`is_delte`
  FROM tmp_menu_sync t
  LEFT JOIN `tmp_menu_parent` mp
         ON mp.`component` = t.`component` AND mp.`menu_type` = t.`menu_type`
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component`, `menu_type` FROM eb_system_menu) x
                    WHERE x.`component` = t.`component` AND x.`menu_type` = t.`menu_type`)
   AND (t.`parent_component` = '' OR mp.`pid` IS NOT NULL);

-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
--    （层级最多 3 层）；NOT EXISTS 保证重复执行不出重复行。
--    父级查找表单独物化还有一个原因：MySQL 不允许同一查询里两次引用同一张临时表
--    （否则 ERROR 1137 Can't reopen table），所以不能把 tmp_menu_sync 既当主表又当父表。
DROP TEMPORARY TABLE IF EXISTS `tmp_menu_parent`;
CREATE TEMPORARY TABLE `tmp_menu_parent` AS
SELECT t.`component`, t.`menu_type`, MIN(p.`id`) AS `pid`
  FROM tmp_menu_sync t
  JOIN eb_system_menu p
    ON p.`component` = t.`parent_component`   -- 父级已标记删除也照挂，保持原有归属
 WHERE t.`parent_component` <> ''      -- ← 必须有！否则空父级会去匹配所有 component='' 的按钮行
 GROUP BY t.`component`, t.`menu_type`;

INSERT INTO eb_system_menu (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT COALESCE(mp.`pid`, 0), t.`name`, t.`icon`, t.`perms`, t.`component`,
       t.`menu_type`, t.`sort`, t.`is_show`, t.`is_delte`
  FROM tmp_menu_sync t
  LEFT JOIN `tmp_menu_parent` mp
         ON mp.`component` = t.`component` AND mp.`menu_type` = t.`menu_type`
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component`, `menu_type` FROM eb_system_menu) x
                    WHERE x.`component` = t.`component` AND x.`menu_type` = t.`menu_type`)
   AND (t.`parent_component` = '' OR mp.`pid` IS NOT NULL);

-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
--    （层级最多 3 层）；NOT EXISTS 保证重复执行不出重复行。
--    父级查找表单独物化还有一个原因：MySQL 不允许同一查询里两次引用同一张临时表
--    （否则 ERROR 1137 Can't reopen table），所以不能把 tmp_menu_sync 既当主表又当父表。
DROP TEMPORARY TABLE IF EXISTS `tmp_menu_parent`;
CREATE TEMPORARY TABLE `tmp_menu_parent` AS
SELECT t.`component`, t.`menu_type`, MIN(p.`id`) AS `pid`
  FROM tmp_menu_sync t
  JOIN eb_system_menu p
    ON p.`component` = t.`parent_component`   -- 父级已标记删除也照挂，保持原有归属
 WHERE t.`parent_component` <> ''      -- ← 必须有！否则空父级会去匹配所有 component='' 的按钮行
 GROUP BY t.`component`, t.`menu_type`;

INSERT INTO eb_system_menu (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT COALESCE(mp.`pid`, 0), t.`name`, t.`icon`, t.`perms`, t.`component`,
       t.`menu_type`, t.`sort`, t.`is_show`, t.`is_delte`
  FROM tmp_menu_sync t
  LEFT JOIN `tmp_menu_parent` mp
         ON mp.`component` = t.`component` AND mp.`menu_type` = t.`menu_type`
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component`, `menu_type` FROM eb_system_menu) x
                    WHERE x.`component` = t.`component` AND x.`menu_type` = t.`menu_type`)
   AND (t.`parent_component` = '' OR mp.`pid` IS NOT NULL);


-- 2) 收敛规范行的名称 / 图标 / 权限标识 / 排序 / 显隐 / 删除标记
UPDATE eb_system_menu m
  JOIN `tmp_menu_canon` c ON m.`id` = c.`id`
  JOIN tmp_menu_sync t
    ON t.`component` = c.`component` AND t.`menu_type` = c.`menu_type`
   SET m.`name`        = t.`name`,
       m.`icon`        = t.`icon`,
       m.`perms`       = IF(t.`perms` = '', m.`perms`, t.`perms`),
       m.`sort`        = t.`sort`,
       m.`is_show`     = t.`is_show`,
       m.`is_delte`    = t.`is_delte`,
       m.`update_time` = NOW()
 WHERE NOT (m.`name` <=> t.`name`)
    OR NOT (m.`icon` <=> t.`icon`)
    OR NOT (m.`sort` <=> t.`sort`)
    OR NOT (m.`is_show` <=> t.`is_show`)
    OR NOT (m.`is_delte` <=> t.`is_delte`)
    OR (t.`perms` <> '' AND NOT (m.`perms` <=> t.`perms`));

-- 3) 收敛父级归属（沿用最后一次重建的父级查找表）
--    3a) 有父级的：pid 指向父级 component 对应的菜单 id
UPDATE eb_system_menu m
  JOIN `tmp_menu_canon` c ON m.`id` = c.`id`
  JOIN `tmp_menu_parent` mp
    ON mp.`component` = c.`component` AND mp.`menu_type` = c.`menu_type`
   SET m.`pid` = mp.`pid`, m.`update_time` = NOW()
 WHERE NOT (m.`pid` <=> mp.`pid`);

--    3b) 一级目录 / 顶层菜单：pid 归 0
UPDATE eb_system_menu m
  JOIN `tmp_menu_canon` c ON m.`id` = c.`id`
  JOIN tmp_menu_sync t
    ON t.`component` = c.`component` AND t.`menu_type` = c.`menu_type`
   SET m.`pid` = 0, m.`update_time` = NOW()
 WHERE t.`parent_component` = '' AND m.`pid` <> 0;

DROP TEMPORARY TABLE `tmp_menu_parent`;
DROP TEMPORARY TABLE `tmp_menu_canon`;
DROP TEMPORARY TABLE `tmp_menu_sync`;

-- 4) 自检：可见树里应当能查到全部已删除标记
SELECT 'CRMEB menu_sync done' AS result,
       SUM(`menu_type` = 'M') AS dirs,
       SUM(`menu_type` = 'C') AS menus,
       SUM(IFNULL(`is_delte`, 0) = 1 AND `menu_type` IN ('M', 'C')) AS deleted_mark
  FROM eb_system_menu;
-- ========== END: menu_sync_20260923.sql ==========

-- ========== BEGIN: table_comments_20260923.sql ==========
-- ============================================================
-- CRMEB Java 3.0 表备注补全 / 修正（2026-09-23）
--
-- 背景：库里有一部分表没有 COMMENT，在客户端里看不出这张表是做什么的；
--       另有少数表的注释写错、乱码或残留英文。
-- 范围：本地库 150 张基础表的全部表级 COMMENT。
--   · 原本无备注 24 张 —— 本次补齐
--   · 原备注有误 10 张 —— 本次修正：eb_agent_change_log, eb_ali_pay_info, eb_category, eb_product_day_record, eb_shopping_product_day_record, eb_stock_adjust_log, eb_stock_price_sku, eb_system_store, eb_template_message, eb_trading_day_record
--   · 其余 116 张沿用库里现有中文备注
--   · 新增表说明取自 Java 实体 @ApiModel(description) 共 4 张，
--     以保证「代码里的叫法」与「库里的说明」一致
--
-- 幂等：逐表用 information_schema 比对当前备注，**只有不一致才真正 ALTER**；
--       备注已一致时该条退化为 SET 空操作，连跑多少遍结果都一样。
-- 安全：表不存在时同样退化为空操作（本地开发期的 *_bak_* 备份表线上并不存在，
--       不会让 ./deploy.sh patch 因 "Table doesn't exist" 中断）。
--       全部操作只改表元数据 COMMENT，不触碰任何数据行，MySQL 8 下不重建表。
-- ============================================================
SET NAMES utf8mb4;

-- bak_category_type1_20260916 —— 备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bak_category_type1_20260916'
                       AND IFNULL(TABLE_COMMENT, '') <> '备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- bak_store_product_cate_20260916 —— 备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bak_store_product_cate_20260916'
                       AND IFNULL(TABLE_COMMENT, '') <> '备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_activity_style —— 活动样式表（活动边框 / 活动背景装修）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('活动样式表（活动边框 / 活动背景装修）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_activity_style'
                       AND IFNULL(TABLE_COMMENT, '') <> '活动样式表（活动边框 / 活动背景装修）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_admin_login_log —— 管理员登录日志表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('管理员登录日志表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_admin_login_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '管理员登录日志表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_agent —— 区域代理
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('区域代理'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_agent'
                       AND IFNULL(TABLE_COMMENT, '') <> '区域代理'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_agent_change_log —— 区域代理变更记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('区域代理变更记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_agent_change_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '区域代理变更记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_agent_reward —— 区域代理奖励明细
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('区域代理奖励明细'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_agent_reward'
                       AND IFNULL(TABLE_COMMENT, '') <> '区域代理奖励明细'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_ali_pay_callback —— 支付宝回调表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('支付宝回调表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_ali_pay_callback'
                       AND IFNULL(TABLE_COMMENT, '') <> '支付宝回调表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_ali_pay_info —— 支付宝订单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('支付宝订单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_ali_pay_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '支付宝订单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_article —— 文章管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('文章管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_article'
                       AND IFNULL(TABLE_COMMENT, '') <> '文章管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_category —— 分类表（商品 / 文章分类，type 区分）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分类表（商品 / 文章分类，type 区分）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_category'
                       AND IFNULL(TABLE_COMMENT, '') <> '分类表（商品 / 文章分类，type 区分）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_distributor_level —— 分销商等级表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分销商等级表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_distributor_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '分销商等级表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_exception_log —— 异常信息表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('异常信息表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_exception_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '异常信息表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_express —— 快递公司表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('快递公司表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_express'
                       AND IFNULL(TABLE_COMMENT, '') <> '快递公司表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_group_config —— 组合配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组合配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_group_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '组合配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_level_stat_order_log —— 等级统计幂等流水表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('等级统计幂等流水表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_level_stat_order_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '等级统计幂等流水表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_page_category —— 页面链接分类
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('页面链接分类'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_page_category'
                       AND IFNULL(TABLE_COMMENT, '') <> '页面链接分类'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_page_diy —— DIY数据表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('DIY数据表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_page_diy'
                       AND IFNULL(TABLE_COMMENT, '') <> 'DIY数据表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_page_link —— 页面链接
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('页面链接'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_page_link'
                       AND IFNULL(TABLE_COMMENT, '') <> '页面链接'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_brand —— 组件品牌表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件品牌表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_brand'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件品牌表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_cat —— 组件类目表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件类目表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_cat'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件类目表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_delivery_company —— 组件快递公司表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件快递公司表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_delivery_company'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件快递公司表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_draft_product —— 组件商品草稿表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品草稿表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_draft_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品草稿表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_order —— 组件订单表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件订单表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_order'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件订单表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_order_product —— 组件订单详情表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件订单详情表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_order_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件订单详情表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product —— 组件商品表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_audit_info —— 组件商品审核信息表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品审核信息表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_audit_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品审核信息表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_info —— 组件商品详情表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品详情表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品详情表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_sku —— 组件商品sku表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品sku表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_sku'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品sku表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_sku_attr —— 组件商品sku属性表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品sku属性表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_sku_attr'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品sku属性表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_shop_brand —— 组件商户品牌表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商户品牌表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_shop_brand'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商户品牌表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_product_day_record —— 商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_product_day_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_schedule_job —— 定时任务
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('定时任务'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_schedule_job'
                       AND IFNULL(TABLE_COMMENT, '') <> '定时任务'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_schedule_job_log —— 定时任务日志
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('定时任务日志'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_schedule_job_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '定时任务日志'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_sensitive_method_log —— 敏感操作日志表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('敏感操作日志表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_sensitive_method_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '敏感操作日志表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shipping_templates —— 运费模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('运费模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shipping_templates'
                       AND IFNULL(TABLE_COMMENT, '') <> '运费模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shipping_templates_free —— 运费模板包邮
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('运费模板包邮'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shipping_templates_free'
                       AND IFNULL(TABLE_COMMENT, '') <> '运费模板包邮'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shipping_templates_region —— 运费模板指定区域费用
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('运费模板指定区域费用'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shipping_templates_region'
                       AND IFNULL(TABLE_COMMENT, '') <> '运费模板指定区域费用'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shopping_product_day_record —— 商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shopping_product_day_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_sms_record —— 短信发送记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('短信发送记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_sms_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '短信发送记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_sms_template —— 短信模板表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('短信模板表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_sms_template'
                       AND IFNULL(TABLE_COMMENT, '') <> '短信模板表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_adjust_log —— 订货系统-订货商库存调整记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货商库存调整记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_adjust_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货商库存调整记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_agent —— 订货系统-订货代理（树形层级）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货代理（树形层级）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_agent'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货代理（树形层级）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_change_log —— 订货商变更记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货商变更记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_change_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货商变更记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_exchange —— 订货系统-换货单
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-换货单'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_exchange'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-换货单'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_exchange_config —— 订货系统-换货设置
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-换货设置'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_exchange_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-换货设置'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_exchange_target —— 订货系统-换货可选目标
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-换货可选目标'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_exchange_target'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-换货可选目标'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_ladder —— 订货系统-团队级差阶梯
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-团队级差阶梯'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_ladder'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-团队级差阶梯'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_level —— 订货系统-代理层级配置
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-代理层级配置'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-代理层级配置'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_log —— 订货系统-云仓库存变动日志
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-云仓库存变动日志'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-云仓库存变动日志'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_notice —— 订货系统-消息通知
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-消息通知'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_notice'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-消息通知'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_offline_sale —— 订货系统-线下销售出库记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-线下销售出库记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_offline_sale'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-线下销售出库记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_order —— 订货系统-订货订单
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货订单'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_order'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货订单'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_order_product —— 订货系统-订货订单明细
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货订单明细'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_order_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货订单明细'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_price —— 订货系统-商品层级拿货价
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-商品层级拿货价'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_price'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-商品层级拿货价'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_price_sku —— 订货系统-规格级拿货价
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-规格级拿货价'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_price_sku'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-规格级拿货价'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_product_rel —— 参与订货的商品关联
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('参与订货的商品关联'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_product_rel'
                       AND IFNULL(TABLE_COMMENT, '') <> '参与订货的商品关联'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_reward —— 订货系统-奖金明细
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-奖金明细'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_reward'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-奖金明细'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_virtual_stock —— 订货系统-会员虚拟库存
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-会员虚拟库存'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_virtual_stock'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-会员虚拟库存'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_withdraw —— 订货系统-奖金提现
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-奖金提现'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_withdraw'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-奖金提现'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_bargain —— 砍价表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('砍价表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_bargain'
                       AND IFNULL(TABLE_COMMENT, '') <> '砍价表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_bargain_user —— 用户参与砍价表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户参与砍价表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_bargain_user'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户参与砍价表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_bargain_user_help —— 砍价用户帮助表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('砍价用户帮助表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_bargain_user_help'
                       AND IFNULL(TABLE_COMMENT, '') <> '砍价用户帮助表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_cart —— 购物车表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('购物车表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_cart'
                       AND IFNULL(TABLE_COMMENT, '') <> '购物车表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_combination —— 拼团商品表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('拼团商品表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_combination'
                       AND IFNULL(TABLE_COMMENT, '') <> '拼团商品表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_coupon —— 优惠券表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('优惠券表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_coupon'
                       AND IFNULL(TABLE_COMMENT, '') <> '优惠券表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_coupon_user —— 优惠券记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('优惠券记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_coupon_user'
                       AND IFNULL(TABLE_COMMENT, '') <> '优惠券记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_order —— 订单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_order'
                       AND IFNULL(TABLE_COMMENT, '') <> '订单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_order_info —— 订单购物详情表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订单购物详情表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_order_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '订单购物详情表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_order_status —— 订单操作记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订单操作记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_order_status'
                       AND IFNULL(TABLE_COMMENT, '') <> '订单操作记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_pink —— 拼团表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('拼团表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_pink'
                       AND IFNULL(TABLE_COMMENT, '') <> '拼团表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product —— 商品表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr —— 商品属性表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品属性表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品属性表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr_option —— 商品规格属性表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品规格属性表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr_option'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品规格属性表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr_result —— 商品属性详情表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品属性详情表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr_result'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品属性详情表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr_value —— 商品属性值表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品属性值表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr_value'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品属性值表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_bak_20260923_115556 —— 本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_bak_20260923_115556'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_cate —— 商品分类辅助表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品分类辅助表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_cate'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品分类辅助表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_coupon —— 商品优惠券表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品优惠券表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_coupon'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品优惠券表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_description —— 商品描述表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品描述表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_description'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品描述表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_group —— 商城商品分组
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商城商品分组'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_group'
                       AND IFNULL(TABLE_COMMENT, '') <> '商城商品分组'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_group_rel —— 商品分组关联
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品分组关联'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_group_rel'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品分组关联'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_guarantee —— 商品保障服务表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品保障服务表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_guarantee'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品保障服务表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_log —— 商品日志表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品日志表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品日志表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_relation —— 商品点赞和收藏表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品点赞和收藏表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_relation'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品点赞和收藏表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_reply —— 评论表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('评论表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_reply'
                       AND IFNULL(TABLE_COMMENT, '') <> '评论表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_rule —— 商品规则值(规格)表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品规则值(规格)表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_rule'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品规则值(规格)表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_seckill —— 商品秒杀产品表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品秒杀产品表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_seckill'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品秒杀产品表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_seckill_manger —— 商品秒杀管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品秒杀管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_seckill_manger'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品秒杀管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_verify_record —— 门店核销记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('门店核销记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_verify_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '门店核销记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_admin —— 后台管理员表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('后台管理员表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_admin'
                       AND IFNULL(TABLE_COMMENT, '') <> '后台管理员表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_attachment —— 附件管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('附件管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_attachment'
                       AND IFNULL(TABLE_COMMENT, '') <> '附件管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_city —— 城市表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('城市表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_city'
                       AND IFNULL(TABLE_COMMENT, '') <> '城市表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_config —— 配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_config_bak_20260923_115556 —— 本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_config_bak_20260923_115556'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_form_temp —— 表单模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('表单模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_form_temp'
                       AND IFNULL(TABLE_COMMENT, '') <> '表单模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_group —— 组合数据表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组合数据表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_group'
                       AND IFNULL(TABLE_COMMENT, '') <> '组合数据表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_group_data —— 组合数据详情表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组合数据详情表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_group_data'
                       AND IFNULL(TABLE_COMMENT, '') <> '组合数据详情表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu —— 系统菜单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('系统菜单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu'
                       AND IFNULL(TABLE_COMMENT, '') <> '系统菜单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260922 —— 本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260922'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260923 —— 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260923'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260923_115556 —— 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260923_115556'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260923_125223 —— 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260923_125223'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_notification —— 通知设置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('通知设置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_notification'
                       AND IFNULL(TABLE_COMMENT, '') <> '通知设置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_role —— 身份管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('身份管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_role'
                       AND IFNULL(TABLE_COMMENT, '') <> '身份管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_role_menu —— 角色菜单关联表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('角色菜单关联表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_role_menu'
                       AND IFNULL(TABLE_COMMENT, '') <> '角色菜单关联表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_store —— 门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_store'
                       AND IFNULL(TABLE_COMMENT, '') <> '门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_store_staff —— 门店店员表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('门店店员表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_store_staff'
                       AND IFNULL(TABLE_COMMENT, '') <> '门店店员表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_team_level —— 团队等级表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('团队等级表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_team_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '团队等级表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_team_level_config —— 团队等级配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('团队等级配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_team_level_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '团队等级配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_user_level —— 普通会员等级
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('普通会员等级'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_user_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '普通会员等级'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_user_level_brokerage —— 会员等级返佣配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('会员等级返佣配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_user_level_brokerage'
                       AND IFNULL(TABLE_COMMENT, '') <> '会员等级返佣配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_template_message —— 微信订阅消息模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信订阅消息模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_template_message'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信订阅消息模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_theme —— 主题表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('主题表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_theme'
                       AND IFNULL(TABLE_COMMENT, '') <> '主题表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_theme_download —— 主题下载记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('主题下载记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_theme_download'
                       AND IFNULL(TABLE_COMMENT, '') <> '主题下载记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_trading_day_record —— 商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_trading_day_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user —— 用户表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_address —— 用户地址表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户地址表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_address'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户地址表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_bill —— 用户账单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户账单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_bill'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户账单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_brokerage_record —— 用户佣金记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户佣金记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_brokerage_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户佣金记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_distributor_level —— 分销商等级变更记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分销商等级变更记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_distributor_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '分销商等级变更记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_distributor_level_stat —— 分销商等级统计表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分销商等级统计表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_distributor_level_stat'
                       AND IFNULL(TABLE_COMMENT, '') <> '分销商等级统计表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_experience_record —— 用户经验记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户经验记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_experience_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户经验记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_extract —— 用户提现表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户提现表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_extract'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户提现表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_group —— 用户分组表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户分组表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_group'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户分组表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_integral_record —— 用户积分记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户积分记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_integral_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户积分记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_level —— 用户等级记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户等级记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户等级记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_recharge —— 用户充值表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户充值表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_recharge'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户充值表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_sign —— 签到记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('签到记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_sign'
                       AND IFNULL(TABLE_COMMENT, '') <> '签到记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_tag —— 标签管理
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('标签管理'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_tag'
                       AND IFNULL(TABLE_COMMENT, '') <> '标签管理'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_team_level —— 用户团队等级记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户团队等级记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_team_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户团队等级记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_team_level_stat —— 用户团队等级统计表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户团队等级统计表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_team_level_stat'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户团队等级统计表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_token —— 会员登录令牌表（H5 / 小程序 / APP 各端 token）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('会员登录令牌表（H5 / 小程序 / APP 各端 token）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_token'
                       AND IFNULL(TABLE_COMMENT, '') <> '会员登录令牌表（H5 / 小程序 / APP 各端 token）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_visit_record —— 用户访问记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户访问记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_visit_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户访问记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_callback —— 微信回调表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信回调表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_callback'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信回调表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_exceptions —— 微信异常表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信异常表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_exceptions'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信异常表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_pay_info —— 微信订单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信订单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_pay_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信订单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_program_my_temp —— 小程序我的模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('小程序我的模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_program_my_temp'
                       AND IFNULL(TABLE_COMMENT, '') <> '小程序我的模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_program_public_temp —— 小程序微信公共模板库
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('小程序微信公共模板库'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_program_public_temp'
                       AND IFNULL(TABLE_COMMENT, '') <> '小程序微信公共模板库'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_reply —— 微信关键字回复表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信关键字回复表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_reply'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信关键字回复表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_blob_triggers —— Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_blob_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_cron_triggers —— Quartz 定时任务-Cron 表达式触发器
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-Cron 表达式触发器'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_cron_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-Cron 表达式触发器'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_fired_triggers —— Quartz 定时任务-已触发的触发器实例（运行态）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-已触发的触发器实例（运行态）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_fired_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-已触发的触发器实例（运行态）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_job_details —— Quartz 定时任务-Job 定义（任务实现类与并发策略）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-Job 定义（任务实现类与并发策略）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_job_details'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-Job 定义（任务实现类与并发策略）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_locks —— Quartz 定时任务-调度器悲观锁（集群用）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-调度器悲观锁（集群用）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_locks'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-调度器悲观锁（集群用）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_paused_trigger_grps —— Quartz 定时任务-已被暂停的触发器组
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-已被暂停的触发器组'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_paused_trigger_grps'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-已被暂停的触发器组'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_scheduler_state —— Quartz 定时任务-调度器实例心跳状态（集群用）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-调度器实例心跳状态（集群用）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_scheduler_state'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-调度器实例心跳状态（集群用）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_simple_triggers —— Quartz 定时任务-简单触发器（固定间隔 / 重复次数）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-简单触发器（固定间隔 / 重复次数）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_simple_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-简单触发器（固定间隔 / 重复次数）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_simprop_triggers —— Quartz 定时任务-带属性的简单触发器
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-带属性的简单触发器'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_simprop_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-带属性的简单触发器'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_triggers —— Quartz 定时任务-触发器（与 Job 的绑定关系）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-触发器（与 Job 的绑定关系）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-触发器（与 Job 的绑定关系）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- 自检：仍无备注的表应当只有「线上不存在的本地备份表」
SELECT COUNT(*) AS 库里无备注的表数
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
   AND IFNULL(TABLE_COMMENT, '') = '';

SELECT 'CRMEB table_comments done' AS result;
-- ========== END: table_comments_20260923.sql ==========

-- ========== BEGIN: distributor_level_order_product_20260923.sql ==========
-- 分销商等级升级条件调整（幂等）：
--   1) 「直推会员商城消费总额」条件下线，列保留但前后端不再使用；
--   2) 新增「下单指定商品」条件：order_product_ids（逗号分隔商品ID，空=未启用）+ order_product_relation（1=与 2=或）
--      + order_product_mode（1=任买一件即可，2=需全部购买）。
DROP PROCEDURE IF EXISTS `dl_add_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `dl_add_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_distributor_level'
                   AND COLUMN_NAME = 'order_product_ids') THEN
    ALTER TABLE `eb_distributor_level`
      ADD COLUMN `order_product_ids` varchar(512) NOT NULL DEFAULT ''
      COMMENT '升级条件-下单指定商品ID，逗号分隔，空=未启用' AFTER `direct_user_consume_relation`;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_distributor_level'
                   AND COLUMN_NAME = 'order_product_relation') THEN
    ALTER TABLE `eb_distributor_level`
      ADD COLUMN `order_product_relation` tinyint(1) NOT NULL DEFAULT 1
      COMMENT '下单指定商品条件关系：1=与，2=或' AFTER `order_product_ids`;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_distributor_level'
                   AND COLUMN_NAME = 'order_product_mode') THEN
    ALTER TABLE `eb_distributor_level`
      ADD COLUMN `order_product_mode` tinyint(1) NOT NULL DEFAULT 1
      COMMENT '下单指定商品达成方式：1=任买一件即可，2=需全部购买' AFTER `order_product_relation`;
  END IF;
END$$
DELIMITER ;
CALL `dl_add_col_if_missing`();
DROP PROCEDURE IF EXISTS `dl_add_col_if_missing`;

SELECT 'distributor_level order_product patches done' AS result;
-- ========== END: distributor_level_order_product_20260923.sql ==========

-- ========== BEGIN: cleanup_deprecated_group_data_20260924.sql ==========
-- 清理 10 个已废弃的组合数据组（幂等，可重复执行）
-- 背景：前端首页已全面装修器化（DIY），以下分组零消费，属于运营白配置的死配置：
--   37 首页中部推荐banner / 52 精品推荐benner / 57 热门榜单推荐 / 58 首发新品推荐
--   59 促销单品推荐 / 48 首页banner滚动图 / 67 首页导航 / 68 首页滚动新闻二期 / 70 超值爆款
--   65 个人中心轮播图（后端仍返回 routine_my_banner，前端已不消费）
-- 保留 8 个在用组：53/54/55/60/62/71/72/73
DELETE FROM `eb_system_group_data` WHERE `gid` IN (37,48,52,57,58,59,65,67,68,70);
DELETE FROM `eb_system_group`      WHERE `id`  IN (37,48,52,57,58,59,65,67,68,70);
SELECT 'deprecated group data cleaned' AS result;
-- ========== END: cleanup_deprecated_group_data_20260924.sql ==========

SET FOREIGN_KEY_CHECKS = 1;
SELECT 'CRMEB oneclick patches done' AS result;
