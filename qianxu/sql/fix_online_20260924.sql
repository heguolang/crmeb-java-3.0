-- ============================================================
-- 线上增量脚本 2026-09-24（对应 commit aaa4f7b / 2393b32）
-- 只改 4 张表/配置：eb_user_extract / eb_store_product / eb_user_bill / eb_system_config + eb_system_form_temp
-- 全部幂等，可重复执行。禁止整包跑 02_patches_all.sql。
-- ============================================================
SET NAMES utf8mb4;
-- ========== BEGIN: balance_extract_setting_20260924.sql ==========
-- ============================================================
-- 2026-09-24 提现设置：余额提现 + 支持银行
-- 幂等，可重复执行
-- ============================================================
SET NAMES utf8mb4;

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
-- ========== END: balance_extract_setting_20260924.sql ==========

-- ========== BEGIN: product_is_give_integral_20260924.sql ==========
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
-- ========== END: product_is_give_integral_20260924.sql ==========

-- ========== BEGIN: product_integral_deduct_20260924.sql ==========
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
-- ========== END: product_integral_deduct_20260924.sql ==========

-- ========== BEGIN: user_bill_link_order_no_20260924.sql ==========
-- ============================================================
-- 资金监控：历史购买/退款账单 link_id 从订单主键修正为真实订单号
-- 幂等：仅当 link_id 为纯数字且能关联到 eb_store_order.id 时更新
--
-- 注意：JOIN 必须用数值比较（CAST ... AS UNSIGNED），不能字符串比较。
-- eb_user_bill.link_id 与 eb_store_order.order_id 的 COLLATE 可能不同
-- （本地 utf8mb4_general_ci / 线上 utf8mb4_unicode_ci），
-- 字符串直接等号会报 ERROR 1267 Illegal mix of collations。
-- ============================================================

UPDATE eb_user_bill ub
INNER JOIN eb_store_order so ON CAST(ub.link_id AS UNSIGNED) = so.id
SET ub.link_id = so.order_id
WHERE ub.type IN ('pay_order', 'pay_product', 'pay_product_refund')
  AND ub.link_id REGEXP '^[0-9]+$'
  AND so.order_id IS NOT NULL
  AND so.order_id <> '';
-- ========== END: user_bill_link_order_no_20260924.sql ==========

-- ========== BEGIN: remove_form_tips_links_20260924.sql ==========
-- ============================================================
-- 清除系统设置表单中的第三方「点击查看详细」帮助链接
-- 表：eb_system_form_temp
-- MySQL 5.7 兼容，幂等
--
-- 说明：
-- 1) 关闭 tips / tipsIsLink，并清空「点击查看详细」文案
-- 2) tipsLink（www.qianxutec.com）需配合脚本 _clean_tips_links.ps1 清空
--    或依赖前端 Parser.vue 已不再渲染 tipsIsLink 链接
-- ============================================================

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"tips":true', '"tips":false')
WHERE `content` LIKE '%"tips":true%';

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"tipsIsLink":true', '"tipsIsLink":false')
WHERE `content` LIKE '%"tipsIsLink":true%';

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"tipsDesc":"点击查看详细"', '"tipsDesc":""')
WHERE `content` LIKE '%点击查看详细%';
-- ========== END: remove_form_tips_links_20260924.sql ==========

