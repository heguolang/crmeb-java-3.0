-- 链动侧栏精简：只显示 看板/产品/配置/用户；其余功能并入 Tab，菜单隐藏
SET NAMES utf8mb4;

-- 显示
UPDATE `eb_system_menu` SET `is_show` = 1, `sort` = 80, `update_time` = NOW()
 WHERE `component` = '/chain/dashboard' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `is_show` = 1, `sort` = 70, `update_time` = NOW()
 WHERE `component` = '/chain/product' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `is_show` = 1, `sort` = 60, `update_time` = NOW()
 WHERE `component` = '/chain/config' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `is_show` = 1, `sort` = 50, `update_time` = NOW()
 WHERE `component` = '/chain/users' AND `is_delte` = 0;

-- 隐藏（功能保留，看板/用户内 Tab 可查；也可直连路由）
UPDATE `eb_system_menu` SET `is_show` = 0, `update_time` = NOW()
 WHERE `is_delte` = 0 AND `component` IN (
   '/chain/commission',
   '/chain/slide',
   '/chain/audit',
   '/chain/risk',
   '/chain/dividend'
 );

-- 兼容旧路径
UPDATE `eb_system_menu` SET `is_show` = 0, `update_time` = NOW()
 WHERE `is_delte` = 0 AND (
   `component` LIKE '/distribution/chainCommission%'
   OR `component` LIKE '/distribution/chainSlide%'
   OR `component` LIKE '/distribution/chainAudit%'
   OR `component` LIKE '/distribution/chainRisk%'
   OR `component` LIKE '/distribution/chainDividend%'
 );
