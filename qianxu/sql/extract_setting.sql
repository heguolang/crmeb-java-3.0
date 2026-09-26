-- ============================================================
-- 提现设置：菜单 + 配置项默认值
-- ============================================================

-- 1. 菜单：与「申请提现」同级增加「提现设置」
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.pid, '提现设置', '', 'admin:finance:extract:setting:get', '/financial/commission/setting', 'C', 2, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/financial/commission/template' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/financial/commission/setting' AND `is_delte` = 0);

-- 按钮权限：保存
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '提现设置保存', '', 'admin:finance:extract:setting:save', NULL, 'A', 1, 0, 0
FROM `eb_system_menu` m
WHERE m.component = '/financial/commission/setting' AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:finance:extract:setting:save' AND `is_delte` = 0);

-- 2. 配置默认值
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_switch','佣金提现开关','0','1','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_switch') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_multiple','提现倍数','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_multiple') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_fee_type','手续费类型','0','ratio','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_fee_type') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_fee','手续费','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_fee') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_weekdays','可提现星期','0','1,2,3,4,5,6,7','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_weekdays') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_time_start','可提现开始小时','0','0','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_time_start') t);

INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`)
SELECT 'user_extract_time_end','可提现结束小时','0','24','0',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='user_extract_time_end') t);
