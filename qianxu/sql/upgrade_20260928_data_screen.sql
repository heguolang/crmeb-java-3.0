-- ============================================================
-- 增量脚本 upgrade_20260928_data_screen.sql
-- 范围：后台「大市场」目录下新增「数据中心大屏」目录（M），
--       其下两个页面菜单（均在新标签页打开，便于全屏 / LED 投屏）：
--         1) 运营大数据（/market/screen/operation）
--         2) 市场大数据（/market/screen/market）
-- 说明：
--   1) 幂等可重复执行（NOT EXISTS / 条件 UPDATE）
--   2) 菜单父级按 component 动态查找，不写死菜单 id
--   3) 只加菜单，不改任何业务表 / 配置
-- 配套：admin 前端路由 admin/src/router/modules/market.js
--       页面 admin/src/views/market/screen/index.vue（运营大数据）
--            admin/src/views/market/bigdata/index.vue（市场大数据）
--       新标签页白名单：admin/src/store/modules/user.js NEW_TAB_MENU_PATHS
-- 执行后必做：
--   后台重新登录一次（菜单缓存）
-- ============================================================

SET NAMES utf8mb4;

-- 1) 目录：数据中心大屏（M，挂在「大市场」下）
--    ⚠️ sort 必须低于同级的拼购系统(1)/全球分红(2)：菜单接口按 sort 降序返回，
--    而后台「点击一级菜单 = 跳转第一个叶子子菜单」，若大屏目录排第一，
--    点击「大市场」会直接跳进数据大屏（2026-09-28 用户实测踩中）。0 = 排在末尾。
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '数据中心大屏', 's-data', '', '/market/screen', 'M', 0, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market' AND p.pid = 0 AND p.is_delte = 0
  AND NOT EXISTS (
    SELECT 1 FROM `eb_system_menu`
    WHERE `component` = '/market/screen' AND `menu_type` = 'M' AND `is_delte` = 0
  );

-- 1b) 存量库兜底：目录已存在但 sort>=1 的，压到末尾（同上原因）
UPDATE `eb_system_menu`
SET `sort` = 0
WHERE `component` = '/market/screen' AND `menu_type` = 'M' AND `is_delte` = 0 AND `sort` >= 1;

-- 2) 存量库迁移：原 C 级 /market/screen（数据中心大屏/运营大数据）-> 运营大数据
UPDATE `eb_system_menu`
SET `name` = '运营大数据', `component` = '/market/screen/operation', `icon` = 's-data', `sort` = 1
WHERE `component` = '/market/screen' AND `menu_type` = 'C' AND `is_delte` = 0;

-- 3) 存量库迁移：市场大数据 挂到新目录前缀下
UPDATE `eb_system_menu`
SET `component` = '/market/screen/market', `sort` = 2
WHERE `component` = '/market/bigdata' AND `menu_type` = 'C' AND `is_delte` = 0;

-- 4) 两个页面统一挂到「数据中心大屏」目录下
SET @screen_pid := (
  SELECT id FROM (
    SELECT id FROM `eb_system_menu`
    WHERE `component` = '/market/screen' AND `menu_type` = 'M' AND `is_delte` = 0
    LIMIT 1
  ) t
);
UPDATE `eb_system_menu`
SET `pid` = @screen_pid
WHERE `component` IN ('/market/screen/operation', '/market/screen/market')
  AND `is_delte` = 0
  AND `pid` <> @screen_pid;

-- 5) 全新库兜底：目录下直接补齐两个页面（已存在则跳过）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '运营大数据', 's-data', '', '/market/screen/operation', 'C', 1, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/market/screen' AND m.menu_type = 'M' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/screen/operation' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '市场大数据', 'data-board', '', '/market/screen/market', 'C', 2, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/market/screen' AND m.menu_type = 'M' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/screen/market' AND `is_delte` = 0);

SELECT 'upgrade_20260928_data_screen done' AS result;
