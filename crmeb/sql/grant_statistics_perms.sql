-- ============================================================
-- 补齐首页/统计相关按钮权限（解决 overview 等「不允许访问」）
-- 超级管理员(roles含1)登录后会加载全部非目录菜单权限
-- 导入后：退出重新登录后台
-- ============================================================

-- 已存在但被删的，恢复
UPDATE `eb_system_menu`
SET `is_delte` = 0
WHERE `perms` LIKE 'admin:statistics:%' AND `is_delte` = 1;

-- 1) 确保菜单权限存在（按钮类型 A）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 0, t.name, '', t.perms, '', 'A', 0, 0, 0
FROM (
  SELECT '首页统计' AS name, 'admin:statistics:home:index' AS perms
  UNION ALL SELECT '用户图表', 'admin:statistics:home:chart:user'
  UNION ALL SELECT '用户购买图表', 'admin:statistics:home:chart:user:buy'
  UNION ALL SELECT '订单图表', 'admin:statistics:home:chart:order'
  UNION ALL SELECT '订单周图表', 'admin:statistics:home:chart:order:week'
  UNION ALL SELECT '订单月图表', 'admin:statistics:home:chart:order:month'
  UNION ALL SELECT '订单年图表', 'admin:statistics:home:chart:order:year'
  UNION ALL SELECT '经营数据', 'admin:statistics:home:operating:data'
  UNION ALL SELECT '用户概览', 'admin:statistics:user:overview'
  UNION ALL SELECT '用户概览列表', 'admin:statistics:user:overview:list'
  UNION ALL SELECT '用户渠道', 'admin:statistics:user:channel'
  UNION ALL SELECT '用户性别', 'admin:statistics:user:sex'
  UNION ALL SELECT '用户地区', 'admin:statistics:user:area'
  UNION ALL SELECT '用户总量', 'admin:statistics:user:total:data'
  UNION ALL SELECT '交易数据', 'admin:statistics:trade:data'
  UNION ALL SELECT '交易概览', 'admin:statistics:trade:overview'
  UNION ALL SELECT '交易趋势', 'admin:statistics:trade:trend'
  UNION ALL SELECT '商品数据', 'admin:statistics:product:data'
  UNION ALL SELECT '商品排行', 'admin:statistics:product:ranking'
  UNION ALL SELECT '商品趋势', 'admin:statistics:product:trend'
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `eb_system_menu` m WHERE m.`perms` = t.perms
);

-- 2) 非超管角色：把上述权限挂到 level=0 角色
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
FROM `eb_system_role` r
CROSS JOIN `eb_system_menu` m
WHERE r.`level` = 0
  AND r.`status` = 1
  AND m.`perms` LIKE 'admin:statistics:%'
  AND m.`is_delte` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `eb_system_role_menu` rm WHERE rm.`rid` = r.`id` AND rm.`menu_id` = m.`id`
  );

-- 3) 校验
SELECT m.`id`, m.`perms`, m.`name`, m.`is_delte`
FROM `eb_system_menu` m
WHERE m.`perms` LIKE 'admin:statistics:%'
ORDER BY m.`perms`;
