-- ============================================================
-- 增量脚本 upgrade_20260928_user_identity_level.sql
-- 范围：会员管理「身份」栏 —— 人工指定分红等级 / 拼购等级
--       + eb_user 新增 dividend_level_id / pingou_level_id 两列
--       + 配套筛选（团队等级、拼购等级）所需的索引
-- 说明：
--   1) 幂等可重复执行：全部走 information_schema 判断 + PREPARE
--   2) MySQL 5.7+ 兼容（无 DELIMITER）
--   3) 语义（重要）：
--      0 = 未指定 → 走原有自动判定
--                    分红：按分红等级自身升级条件判定（可同时达标多个）
--                    拼购：按直推人数命中 eb_pingou_level 阶梯
--      > 0 = 人工指定 → 以人工值为准，属强干预
--                    分红：只在该等级参与分红，不再参与其它等级
--                    拼购：任何拼购活动都固定按该等级的档位拿分享奖励，不看直推人数
--   4) 本脚本只做库结构，必须配合新版 Qianxu-admin.jar 使用
-- ============================================================

SET @db = DATABASE();

-- ------------------------------------------------------------
-- 一、eb_user 新增人工指定等级列
-- ------------------------------------------------------------

-- 1.1 人工指定分红等级
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'dividend_level_id') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `dividend_level_id` int(11) NOT NULL DEFAULT 0 COMMENT ''人工指定分红等级ID，0=未指定（按分红等级自身升级条件自动判定）'' AFTER `distributor_level_id`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.2 人工指定拼购等级
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'pingou_level_id') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `pingou_level_id` int(11) NOT NULL DEFAULT 0 COMMENT ''人工指定拼购等级ID，0=未指定（按直推人数自动命中阶梯）'' AFTER `dividend_level_id`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 二、筛选索引（团队等级 / 拼购等级 后台列表筛选用）
-- ------------------------------------------------------------

-- 2.1 团队等级索引
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user' AND INDEX_NAME = 'idx_team_level') = 0,
  'ALTER TABLE `eb_user` ADD INDEX `idx_team_level` (`team_level`)',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2.2 拼购等级索引
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user' AND INDEX_NAME = 'idx_pingou_level') = 0,
  'ALTER TABLE `eb_user` ADD INDEX `idx_pingou_level` (`pingou_level_id`)',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2.3 分红等级索引
SET @sql = (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user' AND INDEX_NAME = 'idx_dividend_level') = 0,
  'ALTER TABLE `eb_user` ADD INDEX `idx_dividend_level` (`dividend_level_id`)',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 三、校验（应输出 2 行，均为 1）
-- ------------------------------------------------------------
SELECT
  MAX(COLUMN_NAME = 'dividend_level_id') AS has_dividend_level_id,
  MAX(COLUMN_NAME = 'pingou_level_id')    AS has_pingou_level_id
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user'
  AND COLUMN_NAME IN ('dividend_level_id', 'pingou_level_id');
