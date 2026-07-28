-- 会员等级菜单 + 会员返佣配置菜单（增量，已有库执行）
-- 执行前请备份；建议：mysql --default-character-set=utf8mb4 ...

SET NAMES utf8mb4;

-- 用户等级菜单（挂在“用户”下）
SET @userMenuId := (SELECT id FROM eb_system_menu WHERE component = '/user' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @userMenuId, '用户等级', NULL, 'admin:system:user:level:list', '/user/grade', 'C', 2, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @userMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/user/grade' LIMIT 1);

-- 会员返佣配置菜单（挂在“分销”下）
SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '会员返佣配置', NULL, 'admin:system:user:level:brokerage:list', '/distribution/brokerageConfig', 'C', 3, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/brokerageConfig' LIMIT 1);
