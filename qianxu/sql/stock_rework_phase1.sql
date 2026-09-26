-- =============================================================
-- 订货商模块一期改造（2026-09-18）
-- 需求1：下单即付款 + 收货地址 + 无库存自动上浮（等待匹配状态）
-- 状态机新增：10=等待匹配上级  -2=已取消
-- 说明：收货地址复用系统原有用户地址簿 eb_user_address，
--       下单时按 addressId 引用并把收货人/电话/地址快照进订货单。
-- 幂等：可重复执行（列存在时跳过）
-- =============================================================

-- 幂等辅助过程：列不存在才 ADD COLUMN（MySQL 5.7 无 ADD COLUMN IF NOT EXISTS）
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

-- 1. eb_stock_order 加列（逐列判断，注意 AFTER 的先后依赖顺序）
CALL add_col_if_missing('eb_stock_order', 'address_id',
  '`address_id` int(11) DEFAULT NULL COMMENT ''下单时选用的用户地址ID'' AFTER `mark`');
CALL add_col_if_missing('eb_stock_order', 'real_name',
  '`real_name` varchar(64) DEFAULT '''' COMMENT ''收货人姓名（下单地址快照）'' AFTER `address_id`');
CALL add_col_if_missing('eb_stock_order', 'phone',
  '`phone` varchar(32) DEFAULT '''' COMMENT ''收货人电话（下单地址快照）'' AFTER `real_name`');
CALL add_col_if_missing('eb_stock_order', 'user_address',
  '`user_address` varchar(500) DEFAULT '''' COMMENT ''收货地址（下单地址快照）'' AFTER `phone`');
CALL add_col_if_missing('eb_stock_order', 'stock_type',
  '`stock_type` tinyint(1) DEFAULT 1 COMMENT ''库存类型：1=实体 2=虚拟（二期启用虚拟）''');
CALL add_col_if_missing('eb_stock_order', 'order_type',
  '`order_type` tinyint(1) DEFAULT 1 COMMENT ''订单类型：1=采购 2=虚拟提货 3=换货（一期固定1）''');
CALL add_col_if_missing('eb_stock_order', 'cancel_time',
  '`cancel_time` datetime DEFAULT NULL COMMENT ''取消时间'' AFTER `finish_time`');

-- 2. eb_stock_order_product 加列（规格地基，一期先记录，取价商品级）
CALL add_col_if_missing('eb_stock_order_product', 'sku_key',
  '`sku_key` varchar(64) DEFAULT '''' COMMENT ''规格标识（空=商品级）'' AFTER `product_id`');

-- 3. 配置项
-- stock_wait_pay_hours  订货单待付款超时自动取消时长（小时），默认24
-- stock_virtual_audit   虚拟库存单付款后审核开关（二期启用）0=付款即完成 1=需审核
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT t.name, t.title, 0, t.value, 0 FROM (
  SELECT 'stock_wait_pay_hours' AS name, '订货单待付款超时时长（小时）' AS title, '24' AS value
  UNION ALL SELECT 'stock_virtual_audit', '虚拟库存单付款后审核开关', '0'
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` c WHERE c.name = t.name);
