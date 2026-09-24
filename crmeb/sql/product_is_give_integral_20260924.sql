-- ============================================================
-- 商品是否支持赠送积分（2026-09-24）
-- 幂等
-- ============================================================
SET NAMES utf8mb4;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'is_give_integral'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `is_give_integral` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否支持赠送积分：1支持 0不赠送'' AFTER `give_integral`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
