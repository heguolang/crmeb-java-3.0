-- 2026-09-18 issue batch (doc: system issue collection)

-- 1) stock adjust log (issue 9: adjust virtual / physical stock)
CREATE TABLE IF NOT EXISTS `eb_stock_adjust_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) NOT NULL DEFAULT 0 COMMENT 'stock agent id',
  `uid` int(11) NOT NULL DEFAULT 0 COMMENT 'user id',
  `product_id` int(11) NOT NULL DEFAULT 0,
  `sku_key` varchar(128) NOT NULL DEFAULT '' COMMENT 'sku key, empty = whole product',
  `stock_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=physical 2=virtual',
  `num` int(11) NOT NULL DEFAULT 0 COMMENT 'positive=add negative=deduct',
  `mark` varchar(255) NOT NULL DEFAULT '',
  `is_del` tinyint(1) NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='stock agent inventory adjust log';

-- 2) agent change log for reseller agents (issue 2)
CREATE TABLE IF NOT EXISTS `eb_agent_change_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `agent_id` int(11) NOT NULL DEFAULT 0,
  `uid` int(11) NOT NULL DEFAULT 0,
  `type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=add 2=level change 3=status change 4=delete 5=ratio change',
  `old_value` varchar(255) NOT NULL DEFAULT '',
  `new_value` varchar(255) NOT NULL DEFAULT '',
  `mark` varchar(255) NOT NULL DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent` (`agent_id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='reseller agent change log';

-- 3) menu: rename 664? no -> 654 reward rule to stock setting (issue 13)
-- 按 component 定位 + 兼保 id：线上 id 与本机可能不同，单靠 id 会改错菜单
UPDATE `eb_system_menu` SET `name` = '订货商设置'
 WHERE `id` = 654 AND `component` = '/stock/setting' AND `is_delte` = 0;

-- 4) menu: new "level display" page under stock, below stock setting (issue 13)
INSERT INTO `eb_system_menu` (`id`, `pid`, `name`, `component`, `perms`, `menu_type`, `sort`, `icon`, `is_show`, `is_delte`, `create_time`)
SELECT 691, 652, '层级显示', '/stock/level', 'admin:stock:level:list', 'C', 5, '', 1, 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `id` = 691));

INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT 1, 691 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` WHERE `rid` = 1 AND `menu_id` = 691);

-- 5) menu: reseller agent change log (issue 2)
INSERT INTO `eb_system_menu` (`id`, `pid`, `name`, `component`, `perms`, `menu_type`, `sort`, `icon`, `is_show`, `is_delte`, `create_time`)
SELECT 692, 641, '代理商变更记录', '/daili/changeLog', 'admin:agent:changelog:list', 'C', 4, '', 1, 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1) t WHERE EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `id` = 692));

INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT 1, 692 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` WHERE `rid` = 1 AND `menu_id` = 692);

-- 6) new config: agent apply regions (issue 8)
INSERT INTO `eb_system_config` (`name`, `title`, `value`, `status`, `create_time`)
SELECT 'agent_apply_regions', '会员端可申请的代理区域', '1,2,3', 0, NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'agent_apply_regions');
