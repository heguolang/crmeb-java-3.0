-- 2+1 链动一期补丁：滑落方向/帮扶超时/规则文案/菜单
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_slide_mode','min_team','链动滑落方向min_team/left/right',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_slide_mode');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_timeout_days','0','帮扶超时天数0关闭',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_timeout_days');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_rules_content','','链动规则说明文案',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_rules_content');

SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '链动看板', NULL, 'admin:chain:config', '/distribution/chainDashboard', 'C', 19, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainDashboard' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '关系审计', NULL, 'admin:chain:users', '/distribution/chainAudit', 'C', 24, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainAudit' LIMIT 1);

-- 定时任务（若已有同名则跳过）：每天 2:30 释放超时帮扶
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`, `create_time`)
SELECT 'ChainHelpTimeoutTask', 'releaseTimeout', '', '0 30 2 * * ?', 0, '链动帮扶超时释放', 0, NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_schedule_job')
  AND NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name`='ChainHelpTimeoutTask' LIMIT 1);
