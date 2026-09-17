-- ============================================================
-- 补齐 eb_system_team_level 的「直推订单」基线列
--
-- 背景：仓库里的 crmeb/sql/upgrade_team_level_direct.sql 使用
--       `AFTER direct_order_amount` 作为定位锚点，但仓库中
--       【没有任何脚本】创建过 direct_order_amount /
--       direct_order_trigger_type 这两列（上游是在自己库里手工加的，
--       未提交成脚本）。因此本机从 migration/system_team_level.sql
--       建出来的表缺这两列，直接跑 upgrade 脚本会报
--       ERROR 1054: Unknown column 'direct_order_amount'。
--
-- 作用：幂等补齐这两列，使 upgrade_team_level_direct.sql 可正常执行。
-- 顺序：本脚本 → crmeb/sql/upgrade_team_level_direct.sql
-- ============================================================

SET @db = DATABASE();

-- ---------- direct_order_amount ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level'
        AND COLUMN_NAME = 'direct_order_amount') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_order_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''直推订单金额门槛(元)'' AFTER `team_order_amount`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- direct_order_trigger_type ----------
SET @s = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level'
        AND COLUMN_NAME = 'direct_order_trigger_type') = 0,
    'ALTER TABLE `eb_system_team_level` ADD COLUMN `direct_order_trigger_type` tinyint(1) NOT NULL DEFAULT 2 COMMENT ''直推订单统计时机：1=支付成功，2=订单完成'' AFTER `direct_order_amount`',
    'DO 0');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 校验 ----------
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_system_team_level'
ORDER BY ORDINAL_POSITION;
