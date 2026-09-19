-- 系统运维菜单（仅 qxtec 登录时由后端下发，其他管理员不可见；幂等）
DELETE FROM eb_system_menu WHERE component='/hidden' OR component='/hidden/panel';
INSERT INTO eb_system_menu (pid, name, icon, component, perms, menu_type, sort, is_show, is_delte)
VALUES (0, '系统运维', 'cog', '/hidden', '', 'M', 88, 1, 0);
SET @hidden_pid = LAST_INSERT_ID();
INSERT INTO eb_system_menu (pid, name, icon, component, perms, menu_type, sort, is_show, is_delte)
VALUES (@hidden_pid, '运维面板', '', '/hidden/panel', '', 'C', 0, 1, 0);
