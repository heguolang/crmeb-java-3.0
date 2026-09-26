-- ============================================================
-- 修复：团队关联用户 Unknown column 'stat.direct_paid_amount'
-- 原因：旧版 eb_user_team_level_stat 无直推金额字段，后续 CREATE IF NOT EXISTS 不会补列
-- 宝塔可单独导入；幂等，可重复执行
-- ============================================================

SET @db = DATABASE();

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

-- 校验
SHOW COLUMNS FROM `eb_user_team_level_stat` LIKE 'direct_%';
