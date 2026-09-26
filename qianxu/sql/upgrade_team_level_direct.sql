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
