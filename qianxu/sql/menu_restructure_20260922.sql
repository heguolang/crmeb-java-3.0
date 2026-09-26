-- ============================================================
-- 菜单结构调整（2026-09-22）
--
-- 背景：顶部一级菜单过于拥挤，重新组织菜单层级，便于后续管理。
--
-- 内容：
--   1) 新建「运营」一级目录（component=/yunying），把 分销 / 代理 / 团队 /
--      订货 / 门店 这 5 个原一级菜单连同其全部下级功能迁移到它下面；
--   2) 原「运营」(component=/dashboard) 更名为「首页」；
--   3) 「运营」下 5 个子菜单改名 + 重排；
--   4) 「区域代理」子菜单改名 + 重排；
--   5) 「订货商」子菜单改名 + 重排，并隐藏「订货商级别设置」入口
-- 注：本文件不再手写  = NOW()。该列声明了 ON UPDATE CURRENT_TIMESTAMP，
--     行内容有变化时会自动刷新；手写会让「跑两遍 diff 为空」的幂等判据失效。
--      （该页功能已并入「订货商设置」页面，见前端对应改动）。
--
-- 幂等：全部按 component 定位，不依赖自增 id，可重复执行。
-- 只改 name / sort / pid / is_show，不动 component 与 perms，
-- 因此不会影响任何页面路由与权限判断。
-- ============================================================
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1) 新建「运营」一级目录（已存在则跳过）
--    ⚠️ 判存在只看 component + 未删除，**不要再加 pid = 0**：
--       只要 /yunying 的 pid 因任何原因（人工调整、其它脚本）不再是 0，
--       带上 pid = 0 的守卫就会失配，于是每跑一次补丁就多插一条重复的「运营」。
-- ------------------------------------------------------------
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '运营', 's-operation', NULL, '/yunying', 'M', 190, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM `eb_system_menu`
        WHERE `component` = '/yunying' AND `is_delte` = 0
       );

SET @yy_id := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/yunying' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

-- 把 5 个模块迁移到「运营」下（仅移动当前仍在一级的，保证幂等）
UPDATE `eb_system_menu`
   SET `pid` = @yy_id
 WHERE `pid` = 0
   AND `is_delte` = 0
   AND @yy_id IS NOT NULL
   AND `component` IN ('/distribution', '/daili', '/tuandui', '/stock', '/merchantStore');

-- ------------------------------------------------------------
-- 2) 原「运营」更名为「首页」
-- ------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `name` = '首页'
 WHERE `pid` = 0 AND `component` = '/dashboard' AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 3) 「运营」下 5 个子菜单：改名 + 重排
--    显示顺序（自上而下）：分销商 → 团队奖 → 订货商 → 区域代理 → 门店
--    同级按 sort DESC 排列，故 sort 依次递减
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `name` = '分销商',   `sort` = 105 WHERE `component` = '/distribution'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '团队奖',   `sort` = 104 WHERE `component` = '/tuandui'       AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商',   `sort` = 103 WHERE `component` = '/stock'         AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '区域代理', `sort` = 102 WHERE `component` = '/daili'         AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '门店',     `sort` = 101 WHERE `component` = '/merchantStore' AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 4) 「区域代理」子菜单：改名 + 重排
--    顺序：代理商管理 → 区域代理设置 → 代理奖励明细 → 代理商变更记录
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `name` = '代理商管理',   `sort` = 4 WHERE `component` = '/daili/agentList'    AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '区域代理设置', `sort` = 3 WHERE `component` = '/daili/agentSetting' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 2 WHERE `component` = '/daili/agentReward' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 1 WHERE `component` = '/daili/changeLog'   AND `is_delte` = 0;
-- 兼容旧路由名（部分环境该页 component 为 /daili/changelog）
UPDATE `eb_system_menu` SET `sort` = 1 WHERE `component` = '/daili/changelog'   AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 5) 「订货商」子菜单：改名 + 重排 + 隐藏级别设置入口
--    顺序：数据报表 → 订货商管理 → 订货商设置 → 商品与库存
--          → 订货商资金记录 → 换货管理 → 订货商订单 → 订货商变更记录
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `sort` = 10 WHERE `component` = '/stock/report'   AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 9 WHERE `component` = '/stock/agent'    AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 8 WHERE `component` = '/stock/setting'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 7 WHERE `component` = '/stock/product'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商资金记录', `sort` = 6 WHERE `component` = '/stock/reward'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 5 WHERE `component` = '/stock/exchange' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商订单',     `sort` = 4 WHERE `component` = '/stock/order'   AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 3 WHERE `component` = '/stock/changelog' AND `is_delte` = 0;

-- 「订货商级别设置」入口隐藏（功能已并入「订货商设置」页，权限与记录保留）
-- 注：/stock/withdraw（提现管理）原本已隐藏，此处不处理
UPDATE `eb_system_menu`
   SET `is_show` = 0
 WHERE `is_delte` = 0
   AND (`component` = '/stock/level' OR (`name` = '订货商级别设置' AND `perms` LIKE 'admin:stock:level%'));

-- ------------------------------------------------------------
-- 校验：一级菜单 与「运营」下的子菜单
-- ------------------------------------------------------------
SELECT '--- 一级菜单（按显示顺序）---' AS info;
SELECT `id`, `name`, `component`, `sort`
  FROM `eb_system_menu`
 WHERE `pid` = 0 AND `is_show` = 1 AND `is_delte` = 0
 ORDER BY `sort` DESC;

SELECT '--- 「运营」下的子菜单 ---' AS info;
SELECT c.`id`, c.`name`, c.`component`, c.`sort`
  FROM `eb_system_menu` c
  JOIN `eb_system_menu` p ON c.`pid` = p.`id`
 WHERE p.`component` = '/yunying' AND c.`is_show` = 1 AND c.`is_delte` = 0
 ORDER BY c.`sort` DESC;

SELECT '--- 「订货商」下的子菜单 ---' AS info;
SELECT c.`id`, c.`name`, c.`component`, c.`sort`
  FROM `eb_system_menu` c
  JOIN `eb_system_menu` p ON c.`pid` = p.`id`
 WHERE p.`component` = '/stock' AND c.`is_show` = 1 AND c.`is_delte` = 0
 ORDER BY c.`sort` DESC;
