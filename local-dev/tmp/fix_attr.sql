SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product_attr' AND COLUMN_NAME='is_show_image');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product_attr ADD COLUMN is_show_image tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否显示规格图''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;