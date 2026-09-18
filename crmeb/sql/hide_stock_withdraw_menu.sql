-- ============================================================
-- 隐藏订货「提现管理」（功能已取消，不再展示）
-- 宝塔可单独导入；执行后重新登录后台刷新菜单
-- ============================================================

-- 按路由/权限隐藏（不依赖固定 id，避免环境 id 不一致删不掉）
UPDATE `eb_system_menu`
SET `is_delte` = 1, `is_show` = 0
WHERE `component` = '/stock/withdraw'
   OR `perms` LIKE 'admin:stock:withdraw%'
   OR (`name` = '提现管理' AND (`perms` LIKE 'admin:stock:%' OR `component` LIKE '/stock/%'));

-- 顺带隐藏其子按钮（若仍挂在提现管理下）
UPDATE `eb_system_menu` c
INNER JOIN `eb_system_menu` p ON c.`pid` = p.`id`
SET c.`is_delte` = 1, c.`is_show` = 0
WHERE p.`component` = '/stock/withdraw'
   OR p.`perms` = 'admin:stock:withdraw:list'
   OR (p.`name` = '提现管理' AND p.`perms` LIKE 'admin:stock:%');

-- 校验
SELECT `id`, `pid`, `name`, `perms`, `component`, `is_show`, `is_delte`
FROM `eb_system_menu`
WHERE `name` LIKE '%提现%' OR `perms` LIKE '%withdraw%' OR `component` LIKE '%withdraw%';
