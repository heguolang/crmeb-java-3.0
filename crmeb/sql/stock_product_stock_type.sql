-- Stock module: per-product virtual/physical stock support (idempotent)
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

CALL add_col_if_missing('eb_stock_product_rel','support_virtual','support_virtual tinyint(1) NOT NULL DEFAULT 1');
CALL add_col_if_missing('eb_stock_product_rel','support_physical','support_physical tinyint(1) NOT NULL DEFAULT 1');

DROP PROCEDURE IF EXISTS add_col_if_missing;
SELECT id, product_id, support_virtual, support_physical FROM eb_stock_product_rel;
