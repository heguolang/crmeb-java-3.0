-- ============================================================
-- 需求3：后台用户管理「更多」新增「修改登录密码」权限点
-- 父级：39 用户管理
-- 幂等：可重复执行
-- ============================================================

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 39, '修改登录密码', '', 'admin:user:update:password', '', 'A', 99999, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:user:update:password'
);

-- 校验
SELECT id, pid, name, perms, menu_type FROM eb_system_menu WHERE perms = 'admin:user:update:password';
