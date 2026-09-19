-- ============================================================
-- 测试环境修复脚本 FIX_TEST_ENV.sql
-- 基于备份 crmeb_java3_2026-09-19_15-46-40 分析生成
-- 库: crmeb_java3 ；执行前建议先备份
-- 宝塔「SQL」窗口可整段执行
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `crmeb_java3`;

-- ---------- 0. 执行前自检（可看结果） ----------
SELECT 'products' AS k, COUNT(*) AS c FROM eb_store_product WHERE IFNULL(is_del,0)=0
UNION ALL SELECT 'attr', COUNT(*) FROM eb_store_product_attr WHERE IFNULL(is_del,0)=0
UNION ALL SELECT 'attr_value', COUNT(*) FROM eb_store_product_attr_value WHERE IFNULL(is_del,0)=0
UNION ALL SELECT 'dup_config', COUNT(*) FROM (
  SELECT name FROM eb_system_config GROUP BY name HAVING COUNT(*)>1
) t;

-- ---------- 1. 配置去重 ----------
-- 备份里约 29 组同名双份（ALL_IN_ONE 重复导入导致）
-- 1a) logo：旧值是 base64 乱码，保留较大 id（正常路径）
DELETE t1 FROM `eb_system_config` t1
INNER JOIN `eb_system_config` t2 ON t1.name = t2.name AND t1.id < t2.id
WHERE t1.name IN ('site_logo_lefttop', 'site_logo_square', 'site_logo_login');

-- 1b) 其余同名保留最小 id
DELETE t1 FROM `eb_system_config` t1
INNER JOIN `eb_system_config` t2 ON t1.name = t2.name AND t1.id > t2.id;

-- ---------- 2. 域名强制为测试 API（已正确也会幂等） ----------
UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');

-- 支付宝回调勿用 localhost
UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com/api/alipay/aliPayNotify', `update_time` = NOW()
WHERE `name` = 'NOTIFY' AND `value` LIKE '%localhost%';

-- ---------- 3. 主题/DIY 本地域名替换（无匹配则 0 行，安全） ----------
UPDATE `eb_theme` SET `home_data` = REPLACE(`home_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `home_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `home_data` = REPLACE(`home_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `home_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `category_data` = REPLACE(`category_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `category_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `category_data` = REPLACE(`category_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `category_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `detail_data` = REPLACE(`detail_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `detail_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `detail_data` = REPLACE(`detail_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `detail_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `user_data` = REPLACE(`user_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `user_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `user_data` = REPLACE(`user_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `user_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `theme_data` = REPLACE(`theme_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `theme_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `theme_data` = REPLACE(`theme_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `theme_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `home_default_data` = REPLACE(`home_default_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `home_default_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `home_default_data` = REPLACE(`home_default_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `home_default_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `category_default_data` = REPLACE(`category_default_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `category_default_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `category_default_data` = REPLACE(`category_default_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `category_default_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `detail_default_data` = REPLACE(`detail_default_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `detail_default_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `detail_default_data` = REPLACE(`detail_default_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `detail_default_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `user_default_data` = REPLACE(`user_default_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `user_default_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `user_default_data` = REPLACE(`user_default_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `user_default_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_theme` SET `theme_default_data` = REPLACE(`theme_default_data`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `theme_default_data` LIKE '%127.0.0.1:8080%';
UPDATE `eb_theme` SET `theme_default_data` = REPLACE(`theme_default_data`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `theme_default_data` LIKE '%127.0.0.1:8081%';
UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `value` LIKE '%127.0.0.1:8080%';
UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `value` LIKE '%127.0.0.1:8081%';

-- ---------- 4. 缺列补齐（列已存在会报 Duplicate column，可忽略继续） ----------
-- 若确认已有 sku_key 可跳过本段
-- ALTER TABLE `eb_stock_order_product` ADD COLUMN `sku_key` varchar(64) DEFAULT '' COMMENT '规格标识' AFTER `product_id`;

-- ---------- 5. 核心：为无规格商品补默认 SKU（解决后台编辑卡死 / Index 0 Size 0） ----------
-- 备份中 6 个商品全部无 attr / attr_value：101,159,167,168,170,171
-- 已有规格的商品不会被改动

INSERT INTO `eb_store_product_attr` (`product_id`, `attr_name`, `attr_values`, `type`, `is_del`, `is_show_image`)
SELECT p.id, '规格', '默认', 0, 0, 0
FROM `eb_store_product` p
WHERE IFNULL(p.is_del, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM `eb_store_product_attr` a
    WHERE a.product_id = p.id AND IFNULL(a.is_del, 0) = 0
  );

INSERT INTO `eb_store_product_attr_value`
(`product_id`, `suk`, `stock`, `sales`, `price`, `image`, `unique`, `cost`, `bar_code`,
 `ot_price`, `weight`, `volume`, `brokerage`, `brokerage_two`, `type`, `quota`, `quota_show`,
 `attr_value`, `is_del`, `version`, `is_default`, `is_show`)
SELECT
  p.id,
  '默认',
  IFNULL(p.stock, 0),
  IFNULL(p.sales, 0),
  IFNULL(p.price, 0),
  IFNULL(p.image, ''),
  LEFT(CONCAT('auto', p.id), 8),
  IFNULL(p.cost, 0),
  '',
  IFNULL(p.ot_price, 0),
  0, 0, 0, 0,
  0, 0, 0,
  '{"规格":"默认"}',
  0, 0, 1, 1
FROM `eb_store_product` p
WHERE IFNULL(p.is_del, 0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM `eb_store_product_attr_value` v
    WHERE v.product_id = p.id AND IFNULL(v.is_del, 0) = 0
  );

-- 单规格商品 spec_type 置 0（与默认 SKU 一致）
UPDATE `eb_store_product` p
SET p.spec_type = 0
WHERE IFNULL(p.is_del, 0) = 0
  AND IFNULL(p.spec_type, 0) <> 0
  AND (
    SELECT COUNT(*) FROM `eb_store_product_attr_value` v
    WHERE v.product_id = p.id AND IFNULL(v.is_del, 0) = 0
  ) = 1;

-- ---------- 6. 执行后校验 ----------
SELECT 'products' AS k, COUNT(*) AS c FROM eb_store_product WHERE IFNULL(is_del,0)=0
UNION ALL SELECT 'attr', COUNT(*) FROM eb_store_product_attr WHERE IFNULL(is_del,0)=0
UNION ALL SELECT 'attr_value', COUNT(*) FROM eb_store_product_attr_value WHERE IFNULL(is_del,0)=0
UNION ALL SELECT 'products_missing_sku', COUNT(*) FROM eb_store_product p
  WHERE IFNULL(p.is_del,0)=0 AND NOT EXISTS (
    SELECT 1 FROM eb_store_product_attr_value v WHERE v.product_id=p.id AND IFNULL(v.is_del,0)=0
  )
UNION ALL SELECT 'dup_config', COUNT(*) FROM (
  SELECT name FROM eb_system_config GROUP BY name HAVING COUNT(*)>1
) t;

SELECT id, name, value FROM eb_system_config
WHERE name IN ('localUploadUrl','api_url','front_api_url','site_url','NOTIFY');

SET FOREIGN_KEY_CHECKS = 1;
SELECT 'FIX_TEST_ENV done' AS result;

-- 执行后务必：
-- 1) redis-cli -a 密码 -n 8 FLUSHDB   （或清配置相关 key）
-- 2) 重启 admin + front jar
-- 3) 后台打开商品编辑页验证不再卡死
-- 说明：本备份 Quartz 表已齐全且 collation 正确，无需重建 qrtz_*