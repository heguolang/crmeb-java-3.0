SET NAMES utf8mb4;
-- guarantee_ids
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='guarantee_ids');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product ADD COLUMN guarantee_ids varchar(64) NULL DEFAULT NULL COMMENT ''保障服务ids''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
-- is_store
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='is_store');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product ADD COLUMN is_store tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否支持门店服务''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
-- store_self_pickup
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='store_self_pickup');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product ADD COLUMN store_self_pickup tinyint(1) NOT NULL DEFAULT 0 COMMENT ''门店自提''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
-- store_delivery
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='store_delivery');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product ADD COLUMN store_delivery tinyint(1) NOT NULL DEFAULT 0 COMMENT ''门店配送''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
-- activity_style
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_store_product' AND COLUMN_NAME='activity_style');
SET @s := IF(@c=0, 'ALTER TABLE eb_store_product ADD COLUMN activity_style varchar(255) NULL DEFAULT NULL COMMENT ''活动边框''', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
SHOW COLUMNS FROM eb_store_product LIKE 'guarantee_ids';
SHOW COLUMNS FROM eb_store_product LIKE 'is_store';