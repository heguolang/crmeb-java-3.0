-- 链动产品：指定礼包商品，并配置直推奖/见单奖/平级奖（优先于全局奖励规则）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eb_chain_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `direct_amount` decimal(12,2) NOT NULL DEFAULT 100.00 COMMENT '直推奖固定金额',
  `see_ratio` decimal(8,2) NOT NULL DEFAULT 10.00 COMMENT '见点奖比例%（订单实付 × 比例）',
  `see_amount` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '见点奖固定金额（旧字段，兼容保留；see_ratio>0 时以比例为准）',
  `peer_ratio` decimal(8,2) NOT NULL DEFAULT 10.00 COMMENT '平级奖占见点比例%',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1启用礼包 0停用',
  `sort` int(11) NOT NULL DEFAULT 0,
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动礼包产品及分佣配置';

-- 菜单：大市场 → 2+1链动 → 链动产品
SET @chainDirId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/chain' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, CONVERT(UNHEX('E993BEE58AA8E4BAA7E59381') USING utf8mb4), NULL, 'admin:chain:product', '/chain/product', 'C', 75, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/product' AND `is_delte` = 0 LIMIT 1);

UPDATE `eb_system_menu`
   SET `pid` = @chainDirId,
       `name` = CONVERT(UNHEX('E993BEE58AA8E4BAA7E59381') USING utf8mb4),
       `sort` = 75, `is_show` = 1, `perms` = 'admin:chain:product'
 WHERE `component` = '/chain/product' AND `is_delte` = 0 AND @chainDirId IS NOT NULL;

-- 超管角色授权
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
  FROM `eb_system_role` r
 CROSS JOIN `eb_system_menu` m
 WHERE r.`level` = 0 AND r.`status` = 1
   AND m.`is_delte` = 0
   AND m.`component` = '/chain/product'
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_role_menu` rm
          WHERE rm.`rid` = r.`id` AND rm.`menu_id` = m.`id`
       );
