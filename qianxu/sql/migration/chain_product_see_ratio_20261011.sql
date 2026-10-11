-- ============================================================
-- 链动产品：见点奖由「固定金额」改为「比例%」2026-10-11
--
-- 背景：文档口径为「见点奖 = 订单实付 × 10%」，为与全局奖励规则一致，
--       商品级覆盖也改为按比例。旧字段 see_amount 保留（兼容），
--       代码中 see_ratio>0 时优先按比例计算。
--
-- 幂等：用 information_schema 判断列是否存在后再 ALTER。
-- ============================================================
SET NAMES utf8mb4;

SET @colExists := (
  SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
   WHERE `TABLE_SCHEMA` = DATABASE()
     AND `TABLE_NAME` = 'eb_chain_product'
     AND `COLUMN_NAME` = 'see_ratio'
);

SET @ddl := IF(@colExists = 0,
  'ALTER TABLE `eb_chain_product` ADD COLUMN `see_ratio` decimal(8,2) NOT NULL DEFAULT 10.00 COMMENT ''见点奖比例%（订单实付 × 比例）'' AFTER `direct_amount`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 旧字段见单奖固定金额语义修正：已有数据若为 100 而比例未设，统一按比例 10% 口径
UPDATE `eb_chain_product` SET `see_ratio` = 10.00 WHERE `see_ratio` IS NULL OR `see_ratio` = 0;

-- 校验
SELECT id, product_id, direct_amount, see_ratio, see_amount, peer_ratio, status
  FROM `eb_chain_product` ORDER BY id;
