-- ============================================================
-- 修复门店按钮权限（新增门店报「不允许访问」）
-- 根因：save/update/delete 被建成 menu_type='M'(目录)，
--       超管 getAllPermissions() 会排除 M，导致鉴权失败
-- 导入后重新登录后台
-- ============================================================

-- 1) 纠正类型：按钮必须是 A
UPDATE `eb_system_menu`
SET `menu_type` = 'A', `is_delte` = 0, `is_show` = 0
WHERE `perms` IN (
  'admin:merchant:store:save',
  'admin:merchant:store:update',
  'admin:merchant:store:delete',
  'admin:merchant:store:list',
  'admin:merchant:verify:list'
) AND `menu_type` = 'M';

-- list/verify 若挂在页面上应为 C，不要改成 A；上面只修按钮
UPDATE `eb_system_menu`
SET `menu_type` = 'C', `is_show` = 1
WHERE `perms` IN ('admin:merchant:store:list', 'admin:merchant:verify:list')
  AND `component` IS NOT NULL AND `component` <> '';

-- 2) 确保按钮权限存在（挂在门店管理页下）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, t.name, '', t.perms, '', 'A', t.sort, 0, 0
FROM `eb_system_menu` m
JOIN (
  SELECT '保存门店' AS name, 'admin:merchant:store:save' AS perms, 3 AS sort
  UNION ALL SELECT '修改门店', 'admin:merchant:store:update', 4
  UNION ALL SELECT '删除门店', 'admin:merchant:store:delete', 5
) t
WHERE m.`component` = '/merchantStore/list' AND m.`is_delte` = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.`perms` = t.perms AND x.`is_delte` = 0);

-- 3) 再保险：凡是这三条权限一律改为 A
UPDATE `eb_system_menu`
SET `menu_type` = 'A', `is_delte` = 0
WHERE `perms` IN (
  'admin:merchant:store:save',
  'admin:merchant:store:update',
  'admin:merchant:store:delete'
);

-- 4) 挂到超级管理员角色（level=0）
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
FROM `eb_system_role` r
CROSS JOIN `eb_system_menu` m
WHERE r.`level` = 0 AND r.`status` = 1
  AND m.`perms` IN (
    'admin:merchant:store:list',
    'admin:merchant:store:save',
    'admin:merchant:store:update',
    'admin:merchant:store:delete',
    'admin:merchant:verify:list'
  )
  AND m.`is_delte` = 0
  AND NOT EXISTS (
    SELECT 1 FROM `eb_system_role_menu` rm WHERE rm.`rid` = r.`id` AND rm.`menu_id` = m.`id`
  );

-- 5) 校验
SELECT `id`, `pid`, `name`, `perms`, `component`, `menu_type`, `is_delte`
FROM `eb_system_menu`
WHERE `perms` LIKE 'admin:merchant:%'
ORDER BY `perms`;
