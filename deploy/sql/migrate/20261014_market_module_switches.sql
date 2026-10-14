-- ============================================================
-- 20261014_market_module_switches.sql
-- 大市场子模块开关（运维面板可开关，默认全部开启）：
--   sys_switch_chain      2+1链动（/chain 菜单与 api/*/chain 接口）
--   sys_switch_dividend   全球分红（/market/dividend 菜单与 api/admin/dividend 接口）
--   sys_switch_enterprise 企业管理（/market/enterprise 菜单与 api/admin/svc 接口）
--   sys_switch_recruit    招聘系统（/market/recruit 菜单与 api/*/recruit 接口）
--   sys_switch_screen     数据中心大屏（/market/screen 菜单与大数据屏配置接口）
-- 幂等：INSERT ... WHERE NOT EXISTS，可重复执行。
-- ============================================================

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'sys_switch_chain', '隐藏面板-2+1链动开关', 0, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM `eb_system_config`) x WHERE x.name = 'sys_switch_chain');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'sys_switch_dividend', '隐藏面板-全球分红开关', 0, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM `eb_system_config`) x WHERE x.name = 'sys_switch_dividend');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'sys_switch_enterprise', '隐藏面板-企业管理开关', 0, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM `eb_system_config`) x WHERE x.name = 'sys_switch_enterprise');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'sys_switch_recruit', '隐藏面板-招聘系统开关', 0, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM `eb_system_config`) x WHERE x.name = 'sys_switch_recruit');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'sys_switch_screen', '隐藏面板-数据中心大屏开关', 0, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM `eb_system_config`) x WHERE x.name = 'sys_switch_screen');
