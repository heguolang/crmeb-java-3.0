-- ============================================================
-- 订货商模块 2026-09-18：变更记录表 + 商品加入制 + 菜单调整
-- ============================================================

-- 1. 订货商变更记录表
CREATE TABLE IF NOT EXISTS `eb_stock_change_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) DEFAULT NULL COMMENT '订货商ID(eb_stock_agent.id)',
  `uid` int(11) DEFAULT NULL COMMENT '用户UID',
  `type` tinyint(4) DEFAULT NULL COMMENT '变更类型：1=新增 2=层级变更 3=上级变更 4=状态变更 5=删除',
  `old_value` varchar(255) DEFAULT NULL COMMENT '变更前',
  `new_value` varchar(255) DEFAULT NULL COMMENT '变更后',
  `mark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货商变更记录';

-- 2. 参与订货的商品关联表（加入制）
CREATE TABLE IF NOT EXISTS `eb_stock_product_rel` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品ID(eb_store_product.id)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参与订货的商品关联';

-- 3. 菜单：订货代理 → 订货商管理
-- 按 component 定位 + 兼保 id：线上 id 与本机可能不同，单靠 id 会改错菜单
UPDATE `eb_system_menu` SET `name` = '订货商管理'
 WHERE `id` = 660 AND `component` = '/stock/agent' AND `is_delte` = 0;

-- 4. 菜单：删除提现管理（含提现审核按钮；不依赖固定 id）
UPDATE `eb_system_menu`
SET `is_delte` = 1, `is_show` = 0
WHERE `component` = '/stock/withdraw'
   OR `perms` LIKE 'admin:stock:withdraw%'
   OR (`name` = '提现管理' AND (`perms` LIKE 'admin:stock:%' OR `component` LIKE '/stock/%'));

UPDATE `eb_system_menu` c
INNER JOIN `eb_system_menu` p ON c.`pid` = p.`id`
SET c.`is_delte` = 1, c.`is_show` = 0
WHERE p.`component` = '/stock/withdraw'
   OR p.`perms` = 'admin:stock:withdraw:list'
   OR (p.`name` = '提现管理' AND p.`perms` LIKE 'admin:stock:%');

-- 5. 菜单：新增订货商变更记录（挂订货一级菜单 652 下，排订货商管理之后）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 652, '订货商变更记录', '', 'admin:stock:agent:list', '/stock/changelog', 'C', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/stock/changelog' AND `is_delte` = 0);
