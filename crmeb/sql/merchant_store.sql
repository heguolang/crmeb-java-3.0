-- ============================================================
-- 门店模块 2026-09-18：门店(提货点扩展) + 产品门店权限 + 核销记录 + 菜单
-- 幂等：可重复执行
-- ============================================================

-- 工具：按列名幂等加列
DROP PROCEDURE IF EXISTS crmeb_add_col;
DELIMITER $$
CREATE PROCEDURE crmeb_add_col(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;
  END IF;
END$$
DELIMITER ;

CALL crmeb_add_col('eb_system_store', 'self_pickup', '`self_pickup` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否支持到店自提：1=是 0=否''');
CALL crmeb_add_col('eb_system_store', 'delivery', '`delivery` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否支持上门配送：1=是 0=否''');
CALL crmeb_add_col('eb_system_store', 'delivery_radius', '`delivery_radius` decimal(10,2) NOT NULL DEFAULT 5.00 COMMENT ''配送服务半径(公里)''');
CALL crmeb_add_col('eb_system_store', 'verify_fee', '`verify_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''门店核销服务费''');
CALL crmeb_add_col('eb_system_store', 'pickup_fee', '`pickup_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''到店自提服务费''');
CALL crmeb_add_col('eb_system_store', 'delivery_fee', '`delivery_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT ''上门配送服务费''');
CALL crmeb_add_col('eb_system_store', 'leader_uid', '`leader_uid` int(11) NOT NULL DEFAULT 0 COMMENT ''门店负责人用户UID(eb_user.uid)''');
CALL crmeb_add_col('eb_system_store', 'leader_name', '`leader_name` varchar(64) DEFAULT '''' COMMENT ''门店负责人昵称(冗余)''');

CALL crmeb_add_col('eb_store_product', 'is_store', '`is_store` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''是否支持门店服务：1=是 0=否''');
CALL crmeb_add_col('eb_store_product', 'store_self_pickup', '`store_self_pickup` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''门店-是否支持自提：1=是 0=否''');
CALL crmeb_add_col('eb_store_product', 'store_delivery', '`store_delivery` tinyint(1) NOT NULL DEFAULT 0 COMMENT ''门店-是否支持配送：1=是 0=否''');

DROP PROCEDURE IF EXISTS crmeb_add_col;

-- 门店核销记录表
CREATE TABLE IF NOT EXISTS `eb_store_verify_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `store_id` int(11) NOT NULL COMMENT '门店ID(eb_system_store.id)',
  `store_name` varchar(128) DEFAULT '' COMMENT '门店名称(冗余)',
  `order_id` int(11) DEFAULT NULL COMMENT '订单ID(eb_store_order.id)',
  `order_no` varchar(32) DEFAULT '' COMMENT '订单号',
  `verify_code` varchar(32) DEFAULT '' COMMENT '核销码',
  `product_info` varchar(1024) DEFAULT '' COMMENT '核销商品概要',
  `verify_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '核销方式：1=核销码核销',
  `service_fee` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '本次核销服务费',
  `pay_price` decimal(10,2) DEFAULT NULL COMMENT '订单支付金额',
  `order_status` tinyint(4) DEFAULT NULL COMMENT '核销后订单状态',
  `verify_uid` int(11) DEFAULT NULL COMMENT '核销操作人UID(用户端)',
  `verify_name` varchar(64) DEFAULT '' COMMENT '核销操作人昵称',
  `verify_source` tinyint(4) NOT NULL DEFAULT 1 COMMENT '核销来源：1=门店负责人端 2=平台后台',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
  PRIMARY KEY (`id`),
  KEY `idx_store` (`store_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_code` (`verify_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门店核销记录';

-- 后台左侧栏菜单：门店
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 0, '门店', '', '', '/merchantStore', 'M', 96, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore' AND `pid` = 0 AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '门店管理', '', 'admin:merchant:store:list', '/merchantStore/list', 'C', 1, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore/list' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '核销记录', '', 'admin:merchant:verify:list', '/merchantStore/verify', 'C', 2, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/merchantStore/verify' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '保存门店', '', 'admin:merchant:store:save', NULL, 'M', 3, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '修改门店', '', 'admin:merchant:store:update', NULL, 'M', 4, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '删除门店', '', 'admin:merchant:store:delete', NULL, 'M', 5, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/merchantStore/list' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:merchant:store:delete' AND `is_delte` = 0);
