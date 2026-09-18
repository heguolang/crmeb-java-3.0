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
