-- ============================================================
-- 商品积分抵扣额度 + 抵扣金额是否参与分佣（2026-09-24）
-- 幂等
-- ============================================================
SET NAMES utf8mb4;

-- 单品最多可用多少积分抵扣（0=本商品不支持积分抵扣）
SET @col1 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'integral_deduct'
);
SET @sql1 := IF(@col1 = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `integral_deduct` int NOT NULL DEFAULT 0 COMMENT ''单品最多可用积分抵扣数，0表示不支持积分抵扣'' AFTER `is_give_integral`',
  'SELECT 1');
PREPARE s1 FROM @sql1; EXECUTE s1; DEALLOCATE PREPARE s1;

-- 积分抵扣掉的金额是否仍参与系统分佣（分销/代理/团队等）：1参与 0不参与
SET @col2 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'is_integral_deduct_brokerage'
);
SET @sql2 := IF(@col2 = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `is_integral_deduct_brokerage` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''积分抵扣金额是否参与分佣：1参与 0不参与'' AFTER `integral_deduct`',
  'SELECT 1');
PREPARE s2 FROM @sql2; EXECUTE s2; DEALLOCATE PREPARE s2;
