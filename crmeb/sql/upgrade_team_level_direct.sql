-- ============================================================
-- 团队等级条件关系改版：全局 condition_relation → 每两个条件之间的链式关系
--   self_team_relation  : 自购门槛 与/或 团队门槛
--   team_direct_relation: 团队门槛 与/或 直推门槛
-- 追加：直推XX等级人数条件（等级来源于用户级别 eb_system_user_level）
--   direct_level_relation: 直推门槛 与/或 直推等级人数
--   direct_level_id      : 目标用户等级id，0=未启用
--   direct_level_count   : 直推达到该等级的人数门槛
-- 说明：脚本幂等，可重复执行。
-- ============================================================

SET @db = DATABASE();

-- ---------- 删除旧的全局条件关系列 ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'condition_relation') > 0,
    'ALTER TABLE `eb_system_team_level` DROP COLUMN `condition_relation`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.self_team_relation ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'self_team_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `self_team_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''自购与团队条件关系：1=与，2=或'' AFTER `direct_order_amount`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.team_direct_relation ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_direct_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_direct_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''团队与直推条件关系：1=与，2=或'' AFTER `self_team_relation`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.direct_level_relation ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_level_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_level_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''直推金额与直推等级人数条件关系：1=与，2=或'' AFTER `team_direct_relation`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.direct_level_id ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_level_id') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_level_id` int NOT NULL DEFAULT 0 COMMENT ''直推等级人数-目标用户等级id(来源eb_system_user_level)，0=未启用'' AFTER `direct_level_relation`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.direct_level_count ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'direct_level_count') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_level_count` int NOT NULL DEFAULT 0 COMMENT ''直推达到目标用户等级的人数门槛，0=未启用'' AFTER `direct_level_id`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.team_level_relation ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_level_relation') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_level_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''直推等级人数与团队级别人数条件关系：1=与，2=或'' AFTER `direct_level_count`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.team_level_id ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_level_id') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_level_id` int NOT NULL DEFAULT 0 COMMENT ''团队级别人数-目标用户等级id(来源eb_system_user_level)，0=未启用'' AFTER `team_level_relation`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- eb_system_team_level.team_level_count ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level' AND COLUMN_NAME = 'team_level_count') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `team_level_count` int NOT NULL DEFAULT 0 COMMENT ''团队中达到目标用户等级的人数门槛，0=未启用'' AFTER `team_level_id`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;
