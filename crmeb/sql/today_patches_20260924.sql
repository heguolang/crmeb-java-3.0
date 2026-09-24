-- ============================================================
-- 2026-09-24 今日改动合集
--
-- 1) balance_extract_setting_20260924.sql  余额提现设置 + 支持银行
-- 2) product_is_give_integral_20260924.sql 商品是否支持送积分
--
-- 全部幂等。请先 USE 目标库再执行。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 提现表增加类别字段（brokerage=佣金 / balance=余额）
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_user_extract'
     AND COLUMN_NAME = 'extract_category'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `eb_user_extract` ADD COLUMN `extract_category` varchar(20) NOT NULL DEFAULT ''brokerage'' COMMENT ''提现类别：brokerage佣金 balance余额'' AFTER `extract_type`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `eb_user_extract`
   SET `extract_category` = 'brokerage'
 WHERE `extract_category` IS NULL OR `extract_category` = '';

-- 2) 余额提现配置默认值
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_switch','余额提现开关','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_switch') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_min_price','余额最低提现金额','0','1','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_min_price') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_multiple','余额提现倍数','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_multiple') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_fee_type','余额手续费类型','0','ratio','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_fee_type') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_fee','余额手续费','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_fee') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_weekdays','余额可提现星期','0','1,2,3,4,5,6,7','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_weekdays') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_time_start','余额可提现开始小时','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_time_start') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_balance_extract_time_end','余额可提现结束小时','0','24','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_balance_extract_time_end') t);

-- 3) 支持银行默认值（已存在则不覆盖）
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_bank','提现支持银行','0',
       '中国工商银行\n中国建设银行\n中国农业银行\n中国银行\n交通银行\n招商银行\n中国邮政储蓄银行\n中信银行\n中国光大银行\n兴业银行\n浦发银行\n民生银行',
       '0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_bank') t);

-- 4) 商品是否支持赠送积分
SET @col_exists2 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'is_give_integral'
);
SET @sql2 := IF(@col_exists2 = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `is_give_integral` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否支持赠送积分：1支持 0不赠送'' AFTER `give_integral`',
  'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

SET FOREIGN_KEY_CHECKS = 1;

-- ========== BEGIN: product_integral_deduct_20260924.sql ==========
SET @col_id1 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'integral_deduct'
);
SET @sql_id1 := IF(@col_id1 = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `integral_deduct` int NOT NULL DEFAULT 0 COMMENT ''单品最多可用积分抵扣数，0表示不支持积分抵扣'' AFTER `is_give_integral`',
  'SELECT 1');
PREPARE sid1 FROM @sql_id1; EXECUTE sid1; DEALLOCATE PREPARE sid1;

SET @col_id2 := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'is_integral_deduct_brokerage'
);
SET @sql_id2 := IF(@col_id2 = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `is_integral_deduct_brokerage` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''积分抵扣金额是否参与分佣：1参与 0不参与'' AFTER `integral_deduct`',
  'SELECT 1');
PREPARE sid2 FROM @sql_id2; EXECUTE sid2; DEALLOCATE PREPARE sid2;
-- ========== END: product_integral_deduct ==========

