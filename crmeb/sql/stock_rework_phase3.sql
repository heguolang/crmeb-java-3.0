-- ============================================================
-- Stock module phase 3: SKU support + full exchange capability
-- Idempotent: safe to run repeatedly
-- ============================================================

DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN t varchar(64), IN c varchar(64), IN ddl text)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c) THEN
    SET @s = CONCAT('ALTER TABLE ', t, ' ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;

-- 1. SKU level purchase price
CREATE TABLE IF NOT EXISTS eb_stock_price_sku (
  id int(11) NOT NULL AUTO_INCREMENT,
  level_id int(11) NOT NULL DEFAULT 0,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  price decimal(10,2) NOT NULL DEFAULT 0.00,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_level_product_sku (level_id, product_id, sku_key),
  KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Exchange switch per SKU
CREATE TABLE IF NOT EXISTS eb_stock_exchange_config (
  id int(11) NOT NULL AUTO_INCREMENT,
  product_id int(11) NOT NULL DEFAULT 0,
  sku_key varchar(120) NOT NULL DEFAULT '',
  enable tinyint(1) NOT NULL DEFAULT 1,
  min_target_price decimal(10,2) NOT NULL DEFAULT 0.00,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_product_sku (product_id, sku_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Exchange order extra columns
CALL add_col_if_missing('eb_stock_exchange','sku_key','sku_key varchar(120) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','exchange_type','exchange_type tinyint(1) NOT NULL DEFAULT 2');
CALL add_col_if_missing('eb_stock_exchange','target_product_id','target_product_id int(11) NOT NULL DEFAULT 0');
CALL add_col_if_missing('eb_stock_exchange','target_sku_key','target_sku_key varchar(120) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','target_product_name','target_product_name varchar(255) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','target_price','target_price decimal(10,2) NOT NULL DEFAULT 0.00');
CALL add_col_if_missing('eb_stock_exchange','origin_price','origin_price decimal(10,2) NOT NULL DEFAULT 0.00');
CALL add_col_if_missing('eb_stock_exchange','diff_price','diff_price decimal(10,2) NOT NULL DEFAULT 0.00');
CALL add_col_if_missing('eb_stock_exchange','diff_pay_status','diff_pay_status tinyint(1) NOT NULL DEFAULT 0');
CALL add_col_if_missing('eb_stock_exchange','diff_pay_type','diff_pay_type varchar(20) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','diff_pay_time','diff_pay_time datetime DEFAULT NULL');
CALL add_col_if_missing('eb_stock_exchange','real_name','real_name varchar(64) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','phone','phone varchar(20) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','user_address','user_address varchar(255) NOT NULL DEFAULT ''''');
CALL add_col_if_missing('eb_stock_exchange','address_id','address_id int(11) NOT NULL DEFAULT 0');

-- 4. Configs
INSERT INTO eb_system_config (name, value, status, create_time, update_time)
SELECT 'stock_exchange_single', '1', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) x WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_exchange_single'));

INSERT INTO eb_system_config (name, value, status, create_time, update_time)
SELECT 'stock_exchange_diff_parent_rate', '100', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) x WHERE EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'stock_exchange_diff_parent_rate'));

DROP PROCEDURE IF EXISTS add_col_if_missing;
