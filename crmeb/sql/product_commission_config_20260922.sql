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
