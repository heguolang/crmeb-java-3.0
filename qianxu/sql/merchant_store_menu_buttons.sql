INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '保存门店', '', 'admin:merchant:store:save', NULL, 'A', 3, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:save' AND is_delte=0) t);
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '修改门店', '', 'admin:merchant:store:update', NULL, 'A', 4, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:update' AND is_delte=0) t);
INSERT INTO eb_system_menu (pid, name, icon, perms, component, menu_type, sort, is_show, is_delte)
SELECT 684, '删除门店', '', 'admin:merchant:store:delete', NULL, 'A', 5, 0, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM eb_system_menu WHERE perms='admin:merchant:store:delete' AND is_delte=0) t);

-- 若历史数据写成了 M，纠正为 A
UPDATE eb_system_menu SET menu_type='A', is_show=0
WHERE perms IN ('admin:merchant:store:save','admin:merchant:store:update','admin:merchant:store:delete')
  AND menu_type='M';
