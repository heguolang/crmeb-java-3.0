SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product_attr_value' AND COLUMN_NAME='is_default');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product_attr_value ADD COLUMN is_default tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否默认''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product_attr_value' AND COLUMN_NAME='is_show');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product_attr_value ADD COLUMN is_show tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否显示''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;