-- ============================================================
-- 后台设置内容导出（由本地实际配置自动生成）
--
-- 内容来源于本地开发库相对「干净基线库」的实际差异，包含：
--   1) eb_system_config  系统配置（站点信息/域名/版权/开关等）
--   2) eb_system_menu    后台菜单与权限点
--   3) eb_system_user_level_brokerage  会员等级返佣配置
--
-- 幂等：配置按 name 定位；菜单按 id 定位（保留父子层级，用 id 做主键冲突更新）。
-- 可重复执行。
-- ============================================================

SET NAMES utf8mb4;

-- ----------------------------
-- 1. eb_system_config（42 项：新增 20，修改 22）
-- ----------------------------

-- [新增] agent_apply_status
UPDATE `eb_system_config` SET `title`='区域代理申请开关', `form_id`='0', `value`='1', `status`='0' WHERE `name`='agent_apply_status';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'agent_apply_status','区域代理申请开关','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='agent_apply_status') t);

-- [新增] agent_credit_timing
UPDATE `eb_system_config` SET `title`='区域代理结算时机', `form_id`='0', `value`='1', `status`='0' WHERE `name`='agent_credit_timing';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'agent_credit_timing','区域代理结算时机','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='agent_credit_timing') t);

-- [新增] agent_func_status
UPDATE `eb_system_config` SET `title`='区域代理功能开关', `form_id`='0', `value`='1', `status`='0' WHERE `name`='agent_func_status';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'agent_func_status','区域代理功能开关','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='agent_func_status') t);

-- [新增] brokerage_credit_timing
UPDATE `eb_system_config` SET `title`='分销佣金到账方式', `form_id`='0', `value`='1', `status`='0' WHERE `name`='brokerage_credit_timing';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'brokerage_credit_timing','分销佣金到账方式','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='brokerage_credit_timing') t);

-- [新增] integral_credit_timing
UPDATE `eb_system_config` SET `title`='积分到账方式', `form_id`='0', `value`='1', `status`='0' WHERE `name`='integral_credit_timing';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'integral_credit_timing','积分到账方式','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='integral_credit_timing') t);

-- [新增] login_notice_switch
UPDATE `eb_system_config` SET `title`='login_notice_switch', `form_id`='148', `value`='1', `status`='0' WHERE `name`='login_notice_switch';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'login_notice_switch','login_notice_switch','148','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='login_notice_switch') t);

-- [新增] login_notice_text
UPDATE `eb_system_config` SET `title`='login_notice_text', `form_id`='148', `value`='登录后即可享受完整服务，是否前往登录？', `status`='0' WHERE `name`='login_notice_text';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'login_notice_text','login_notice_text','148','登录后即可享受完整服务，是否前往登录？','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='login_notice_text') t);

-- [新增] register_default_is_promoter
UPDATE `eb_system_config` SET `title`='注册默认推广员', `form_id`='0', `value`='1', `status`='0' WHERE `name`='register_default_is_promoter';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'register_default_is_promoter','注册默认推广员','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='register_default_is_promoter') t);

-- [新增] register_default_user_level
UPDATE `eb_system_config` SET `title`='注册默认会员等级', `form_id`='0', `value`='1', `status`='0' WHERE `name`='register_default_user_level';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'register_default_user_level','注册默认会员等级','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='register_default_user_level') t);

-- [新增] stock_diff_reward_status
UPDATE `eb_system_config` SET `title`='订货差价奖励开关', `form_id`='0', `value`='1', `status`='0' WHERE `name`='stock_diff_reward_status';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_diff_reward_status','订货差价奖励开关','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_diff_reward_status') t);

-- [新增] stock_exchange_diff
UPDATE `eb_system_config` SET `title`='换货单参与差价奖励', `form_id`='0', `value`='0', `status`='0' WHERE `name`='stock_exchange_diff';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_exchange_diff','换货单参与差价奖励','0','0','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_exchange_diff') t);

-- [新增] stock_ladder_cycle
UPDATE `eb_system_config` SET `title`='订货级差结算周期', `form_id`='0', `value`='1', `status`='0' WHERE `name`='stock_ladder_cycle';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_ladder_cycle','订货级差结算周期','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_ladder_cycle') t);

-- [新增] stock_ladder_status
UPDATE `eb_system_config` SET `title`='订货级差奖励开关', `form_id`='0', `value`='1', `status`='0' WHERE `name`='stock_ladder_status';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_ladder_status','订货级差奖励开关','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_ladder_status') t);

-- [新增] stock_order_audit
UPDATE `eb_system_config` SET `title`='订货订单上级审核开关', `form_id`='0', `value`='1', `status`='0' WHERE `name`='stock_order_audit';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_order_audit','订货订单上级审核开关','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_order_audit') t);

-- [新增] stock_peer_generations
UPDATE `eb_system_config` SET `title`='订货平级奖励代数', `form_id`='0', `value`='1', `status`='0' WHERE `name`='stock_peer_generations';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_peer_generations','订货平级奖励代数','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_peer_generations') t);

-- [新增] stock_peer_rate
UPDATE `eb_system_config` SET `title`='订货平级奖励比例', `form_id`='0', `value`='5', `status`='0' WHERE `name`='stock_peer_rate';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_peer_rate','订货平级奖励比例','0','5','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_peer_rate') t);

-- [新增] stock_peer_status
UPDATE `eb_system_config` SET `title`='订货平级奖励开关', `form_id`='0', `value`='1', `status`='0' WHERE `name`='stock_peer_status';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'stock_peer_status','订货平级奖励开关','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='stock_peer_status') t);

-- [新增] team_brokerage_credit_timing
UPDATE `eb_system_config` SET `title`='团队奖到账方式', `form_id`='0', `value`='1', `status`='0' WHERE `name`='team_brokerage_credit_timing';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'team_brokerage_credit_timing','团队奖到账方式','0','1','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='team_brokerage_credit_timing') t);

-- [新增] team_brokerage_max_depth
UPDATE `eb_system_config` SET `title`='团队奖向上追溯层数：0=不限', `form_id`='0', `value`='0', `status`='0' WHERE `name`='team_brokerage_max_depth';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'team_brokerage_max_depth','团队奖向上追溯层数：0=不限','0','0','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='team_brokerage_max_depth') t);

-- [新增] team_brokerage_status
UPDATE `eb_system_config` SET `title`='团队极差奖开关：0=关闭，1=开启', `form_id`='0', `value`='0', `status`='0' WHERE `name`='team_brokerage_status';
INSERT INTO `eb_system_config` (`name`,`title`,`form_id`,`value`,`status`,`create_time`,`update_time`) SELECT 'team_brokerage_status','团队极差奖开关：0=关闭，1=开启','0','0','0',NOW(),NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM `eb_system_config` WHERE `name`='team_brokerage_status') t);

-- [修改] api_url : value
UPDATE `eb_system_config` SET `value`='http://127.0.0.1:8080', `update_time`=NOW() WHERE `name`='api_url';

-- [修改] config_export_open : value
UPDATE `eb_system_config` SET `value`='2', `update_time`=NOW() WHERE `name`='config_export_open';

-- [修改] copyright_company_name : value
UPDATE `eb_system_config` SET `value`='黔序科技', `update_time`=NOW() WHERE `name`='copyright_company_name';

-- [修改] copyright_internet_record : value
UPDATE `eb_system_config` SET `value`='Copyright@2026 贵州黔序科技有限公司', `update_time`=NOW() WHERE `name`='copyright_internet_record';

-- [修改] copyright_internet_record_url : value
UPDATE `eb_system_config` SET `value`='www.qianxutec.com', `update_time`=NOW() WHERE `name`='copyright_internet_record_url';

-- [修改] front_api_url : value
UPDATE `eb_system_config` SET `value`='http://127.0.0.1:8081', `update_time`=NOW() WHERE `name`='front_api_url';

-- [修改] localUploadUrl : value
UPDATE `eb_system_config` SET `value`='http://127.0.0.1:8080', `update_time`=NOW() WHERE `name`='localUploadUrl';

-- [修改] logistics_type : value
UPDATE `eb_system_config` SET `value`='2', `update_time`=NOW() WHERE `name`='logistics_type';

-- [修改] mobile_login_logo : value
UPDATE `eb_system_config` SET `value`='Y3JtZWJpbWFnZS9wdWJsaWMvcHJvZHVjdC8yMDI2LzA5LzE2L2QyODgyZGQyOTJhZDRiMzNiY2Ey\nZDMwZDA0MDU3MjFhY3ZxMDlnZnJvNC5wbmc=', `update_time`=NOW() WHERE `name`='mobile_login_logo';

-- [修改] routine_name : value
UPDATE `eb_system_config` SET `value`='黔序科技', `update_time`=NOW() WHERE `name`='routine_name';

-- [修改] routine_phone_verification : value
UPDATE `eb_system_config` SET `value`=',1', `update_time`=NOW() WHERE `name`='routine_phone_verification';

-- [修改] seo_title : value
UPDATE `eb_system_config` SET `value`='黔序科技', `update_time`=NOW() WHERE `name`='seo_title';

-- [修改] site_logo_lefttop : value
UPDATE `eb_system_config` SET `value`='Y3JtZWJpbWFnZS9wdWJsaWMvcHJvZHVjdC8yMDI2LzA5LzE2L2QyODgyZGQyOTJhZDRiMzNiY2Ey\nZDMwZDA0MDU3MjFhY3ZxMDlnZnJvNC5wbmc=', `update_time`=NOW() WHERE `name`='site_logo_lefttop';

-- [修改] site_logo_login : value
UPDATE `eb_system_config` SET `value`='Y3JtZWJpbWFnZS9wdWJsaWMvcHJvZHVjdC8yMDI2LzA5LzE2L2E4M2IwNTdjODA5NzQ1YTdhMjk1\nYmU5NGNmMTAyNzVicnhhZHRjNTA2Zi5wbmc=', `update_time`=NOW() WHERE `name`='site_logo_login';

-- [修改] site_logo_square : value
UPDATE `eb_system_config` SET `value`='Y3JtZWJpbWFnZS9wdWJsaWMvcHJvZHVjdC8yMDI2LzA5LzE2LzA2YzIwNjBhNjViYTQ3YzU4OGRk\nNjY0MWNlM2RmZGMwc2tkOWR3MGwwei5wbmc=', `update_time`=NOW() WHERE `name`='site_logo_square';

-- [修改] site_name : value
UPDATE `eb_system_config` SET `value`='黔序科技-Java', `update_time`=NOW() WHERE `name`='site_name';

-- [修改] site_url : value
UPDATE `eb_system_config` SET `value`='http://127.0.0.1:8081', `update_time`=NOW() WHERE `name`='site_url';

-- [修改] splash_ad_switch : value
UPDATE `eb_system_config` SET `value`='0', `update_time`=NOW() WHERE `name`='splash_ad_switch';

-- [修改] store_brokerage_is_bubble : value
UPDATE `eb_system_config` SET `value`='0', `update_time`=NOW() WHERE `name`='store_brokerage_is_bubble';

-- [修改] system_product_copy_type : value
UPDATE `eb_system_config` SET `value`='2', `update_time`=NOW() WHERE `name`='system_product_copy_type';

-- [修改] telephone_service_switch : value
UPDATE `eb_system_config` SET `value`='close', `update_time`=NOW() WHERE `name`='telephone_service_switch';

-- [修改] wechat_routine_shipping_switch : value
UPDATE `eb_system_config` SET `value`='1', `update_time`=NOW() WHERE `name`='wechat_routine_shipping_switch';

-- ----------------------------
-- 2. eb_system_menu（131 条：新增 123，修改 8）
--    用 id 做主键冲突更新，保证菜单父子层级关系正确
-- ----------------------------

-- [修改] id=104 分销商管理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '104','8','分销商管理',NULL,'admin:retail:list','/distribution/index','C','100','1','0','2021-11-16 16:52:00','2026-09-17 01:30:23' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/index' AND COALESCE(`perms`,'')='admin:retail:list' AND `id`<>'104') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=39 用户管理 
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '39','4','用户管理 ',NULL,'admin:user:list','/user/index','C','100','1','0','2021-11-16 16:05:50','2026-09-17 01:22:46' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/user/index' AND COALESCE(`perms`,'')='admin:user:list' AND `id`<>'39') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=419 一号通应用保存
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '419','420','一号通应用保存','','admin:pass:appsave','','A','0','0','0','2023-08-25 16:30:02','2026-09-16 14:45:45' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:pass:appsave' AND `id`<>'419') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=420 一号通配置
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '420','12','一号通配置','','','/operation/onePassConfig','C','9','0','0','2023-08-26 17:39:29','2026-09-16 12:22:44' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/operation/onePassConfig' AND COALESCE(`perms`,'')='' AND `id`<>'420') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=421 一号通应用获取
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '421','420','一号通应用获取','','admin:pass:appget','','A','0','0','0','2023-08-28 10:11:45','2026-09-16 14:45:40' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:pass:appget' AND `id`<>'421') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=433 取消商家寄件
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '433','420','取消商家寄件','','admin:pass:shipment:cancel','','A','0','0','0','2023-08-29 16:14:39','2026-09-16 14:45:50' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:pass:shipment:cancel' AND `id`<>'433') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=434 商家寄件-快递列表
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '434','420','商家寄件-快递列表','','admin:pass:shipment:express','','A','0','0','0','2023-08-29 16:15:05','2026-09-16 14:45:55' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:pass:shipment:express' AND `id`<>'434') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [修改] id=435 一号通
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '435','12','一号通','','','/operation/onePass','C','9','0','0','2023-08-30 16:35:11','2026-09-16 12:30:18' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/operation/onePass' AND COALESCE(`perms`,'')='' AND `id`<>'435') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=549 团队等级
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '549','633','团队等级',NULL,'admin:system:team:level:list','/distribution/teamGrade','C','90','1','0','2026-07-29 14:28:01','2026-09-16 14:51:07' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/teamGrade' AND COALESCE(`perms`,'')='admin:system:team:level:list' AND `id`<>'549') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=550 团队等级配置
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '550','633','团队等级配置',NULL,'admin:system:team:level:list','/distribution/teamLevelConfig','C','100','1','0','2026-07-29 14:28:01','2026-09-16 14:51:01' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/teamLevelConfig' AND COALESCE(`perms`,'')='admin:system:team:level:list' AND `id`<>'550') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=551 团队关联用户
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '551','633','团队关联用户',NULL,'admin:system:team:level:user:list','/distribution/teamUser','C','110','1','0','2026-07-29 14:28:01','2026-09-16 14:51:16' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/teamUser' AND COALESCE(`perms`,'')='admin:system:team:level:user:list' AND `id`<>'551') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=552 团队变更记录
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '552','633','团队变更记录',NULL,'admin:system:team:level:record:list','/distribution/teamRecord','C','7','1','0','2026-07-29 14:28:01','2026-09-16 14:49:08' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/teamRecord' AND COALESCE(`perms`,'')='admin:system:team:level:record:list' AND `id`<>'552') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=553 团队奖记录
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '553','9','团队奖记录',NULL,'admin:system:team:level:brokerage:record','/distribution/teamBrokerageRecord','C','8','1','0','2026-07-29 14:28:01','2026-09-16 14:50:46' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/teamBrokerageRecord' AND COALESCE(`perms`,'')='admin:system:team:level:brokerage:record' AND `id`<>'553') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=554 团队等级添加
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '554','549','团队等级添加',NULL,'admin:system:team:level:save','','A','1','1','0','2026-07-29 14:28:01','2026-07-29 14:28:01' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:system:team:level:save' AND `id`<>'554') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=555 团队等级修改
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '555','549','团队等级修改',NULL,'admin:system:team:level:update','','A','2','1','0','2026-07-29 14:28:01','2026-07-29 14:28:01' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:system:team:level:update' AND `id`<>'555') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=556 团队等级删除
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '556','549','团队等级删除',NULL,'admin:system:team:level:delete','','A','3','1','0','2026-07-29 14:28:01','2026-07-29 14:28:01' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:system:team:level:delete' AND `id`<>'556') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=557 团队等级启用/禁用
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '557','549','团队等级启用/禁用',NULL,'admin:system:team:level:use','','A','4','1','0','2026-07-29 14:28:01','2026-07-29 14:28:01' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:system:team:level:use' AND `id`<>'557') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=558 用户等级
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '558','4','用户等级',NULL,'admin:system:user:level:list','/user/grade','C','2','1','0','2026-07-29 14:28:02','2026-07-29 14:28:02' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/user/grade' AND COALESCE(`perms`,'')='admin:system:user:level:list' AND `id`<>'558') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=559 会员返佣配置
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '559','8','会员返佣配置',NULL,'admin:system:user:level:brokerage:list','/distribution/brokerageConfig','C','3','1','0','2026-07-29 14:28:02','2026-07-29 14:28:02' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/distribution/brokerageConfig' AND COALESCE(`perms`,'')='admin:system:user:level:brokerage:list' AND `id`<>'559') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=560 admin:activitystyle:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '560','0','admin:activitystyle:delete',NULL,'admin:activitystyle:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:activitystyle:delete' AND `id`<>'560') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=561 admin:activitystyle:edite
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '561','0','admin:activitystyle:edite',NULL,'admin:activitystyle:edite',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:activitystyle:edite' AND `id`<>'561') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=562 admin:activitystyle:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '562','0','admin:activitystyle:list',NULL,'admin:activitystyle:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:activitystyle:list' AND `id`<>'562') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=563 admin:activitystyle:save
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '563','0','admin:activitystyle:save',NULL,'admin:activitystyle:save',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:activitystyle:save' AND `id`<>'563') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=564 admin:activitystyle:updatestatus
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '564','0','admin:activitystyle:updatestatus',NULL,'admin:activitystyle:updatestatus',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:activitystyle:updatestatus' AND `id`<>'564') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=565 admin:agreement:merincomming:info
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '565','0','admin:agreement:merincomming:info',NULL,'admin:agreement:merincomming:info',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:agreement:merincomming:info' AND `id`<>'565') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=566 admin:agreement:merincomming:save
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '566','0','admin:agreement:merincomming:save',NULL,'admin:agreement:merincomming:save',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:agreement:merincomming:save' AND `id`<>'566') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=567 admin:combination:combine:list:count
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '567','0','admin:combination:combine:list:count',NULL,'admin:combination:combine:list:count',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:combination:combine:list:count' AND `id`<>'567') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=568 admin:copyright:update:company:info
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '568','0','admin:copyright:update:company:info',NULL,'admin:copyright:update:company:info',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:copyright:update:company:info' AND `id`<>'568') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=570 admin:order:statistics
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '570','0','admin:order:statistics',NULL,'admin:order:statistics',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:order:statistics' AND `id`<>'570') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=571 admin:order:statistics:data
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '571','0','admin:order:statistics:data',NULL,'admin:order:statistics:data',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:order:statistics:data' AND `id`<>'571') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=572 admin:order:video:send
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '572','0','admin:order:video:send',NULL,'admin:order:video:send',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:order:video:send' AND `id`<>'572') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=573 admin:order:write:confirm
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '573','0','admin:order:write:confirm',NULL,'admin:order:write:confirm',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:order:write:confirm' AND `id`<>'573') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=574 admin:pagediy:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '574','0','admin:pagediy:delete',NULL,'admin:pagediy:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:delete' AND `id`<>'574') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=575 admin:pagediy:getdefault
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '575','0','admin:pagediy:getdefault',NULL,'admin:pagediy:getdefault',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:getdefault' AND `id`<>'575') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=576 admin:pagediy:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '576','0','admin:pagediy:list',NULL,'admin:pagediy:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:list' AND `id`<>'576') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=577 admin:pagediy:save
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '577','0','admin:pagediy:save',NULL,'admin:pagediy:save',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:save' AND `id`<>'577') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=578 admin:pagediy:setdefault
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '578','0','admin:pagediy:setdefault',NULL,'admin:pagediy:setdefault',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:setdefault' AND `id`<>'578') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=579 admin:pagediy:update
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '579','0','admin:pagediy:update',NULL,'admin:pagediy:update',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:update' AND `id`<>'579') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=580 admin:pagediy:updatename
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '580','0','admin:pagediy:updatename',NULL,'admin:pagediy:updatename',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pagediy:updatename' AND `id`<>'580') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=581 admin:pass:shipment:callback
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '581','0','admin:pass:shipment:callback',NULL,'admin:pass:shipment:callback',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pass:shipment:callback' AND `id`<>'581') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=582 admin:pay:component:delivery:company:get
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '582','0','admin:pay:component:delivery:company:get',NULL,'admin:pay:component:delivery:company:get',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:delivery:company:get' AND `id`<>'582') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=583 admin:pay:component:product:add
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '583','0','admin:pay:component:product:add',NULL,'admin:pay:component:product:add',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:add' AND `id`<>'583') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=584 admin:pay:component:product:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '584','0','admin:pay:component:product:delete',NULL,'admin:pay:component:product:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:delete' AND `id`<>'584') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=585 admin:pay:component:product:delisting
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '585','0','admin:pay:component:product:delisting',NULL,'admin:pay:component:product:delisting',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:delisting' AND `id`<>'585') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=586 admin:pay:component:product:draft:info
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '586','0','admin:pay:component:product:draft:info',NULL,'admin:pay:component:product:draft:info',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:draft:info' AND `id`<>'586') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=587 admin:pay:component:product:draft:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '587','0','admin:pay:component:product:draft:list',NULL,'admin:pay:component:product:draft:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:draft:list' AND `id`<>'587') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=588 admin:pay:component:product:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '588','0','admin:pay:component:product:list',NULL,'admin:pay:component:product:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:list' AND `id`<>'588') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=589 admin:pay:component:product:listing
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '589','0','admin:pay:component:product:listing',NULL,'admin:pay:component:product:listing',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:listing' AND `id`<>'589') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=590 admin:pay:component:product:update
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '590','0','admin:pay:component:product:update',NULL,'admin:pay:component:product:update',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:pay:component:product:update' AND `id`<>'590') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=591 admin:product:listbyids
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '591','0','admin:product:listbyids',NULL,'admin:product:listbyids',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:product:listbyids' AND `id`<>'591') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=592 admin:sms:applys
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '592','0','admin:sms:applys',NULL,'admin:sms:applys',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:sms:applys' AND `id`<>'592') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=593 admin:sms:modify:sign
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '593','0','admin:sms:modify:sign',NULL,'admin:sms:modify:sign',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:sms:modify:sign' AND `id`<>'593') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=594 admin:sms:temp:apply
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '594','0','admin:sms:temp:apply',NULL,'admin:sms:temp:apply',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:sms:temp:apply' AND `id`<>'594') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=595 admin:sms:temps
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '595','0','admin:sms:temps',NULL,'admin:sms:temps',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:sms:temps' AND `id`<>'595') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=596 admin:statistics:product:data
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '596','0','admin:statistics:product:data',NULL,'admin:statistics:product:data',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:product:data' AND `id`<>'596') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=597 admin:statistics:product:ranking
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '597','0','admin:statistics:product:ranking',NULL,'admin:statistics:product:ranking',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:product:ranking' AND `id`<>'597') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=598 admin:statistics:product:trend
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '598','0','admin:statistics:product:trend',NULL,'admin:statistics:product:trend',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:product:trend' AND `id`<>'598') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=599 admin:statistics:trade:data
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '599','0','admin:statistics:trade:data',NULL,'admin:statistics:trade:data',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:trade:data' AND `id`<>'599') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=600 admin:statistics:trade:overview
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '600','0','admin:statistics:trade:overview',NULL,'admin:statistics:trade:overview',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:trade:overview' AND `id`<>'600') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=601 admin:statistics:trade:trend
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '601','0','admin:statistics:trade:trend',NULL,'admin:statistics:trade:trend',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:trade:trend' AND `id`<>'601') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=602 admin:statistics:user:area
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '602','0','admin:statistics:user:area',NULL,'admin:statistics:user:area',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:user:area' AND `id`<>'602') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=603 admin:statistics:user:channel
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '603','0','admin:statistics:user:channel',NULL,'admin:statistics:user:channel',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:user:channel' AND `id`<>'603') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=604 admin:statistics:user:overview
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '604','0','admin:statistics:user:overview',NULL,'admin:statistics:user:overview',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:user:overview' AND `id`<>'604') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=605 admin:statistics:user:overview:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '605','0','admin:statistics:user:overview:list',NULL,'admin:statistics:user:overview:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:user:overview:list' AND `id`<>'605') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=606 admin:statistics:user:sex
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '606','0','admin:statistics:user:sex',NULL,'admin:statistics:user:sex',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:user:sex' AND `id`<>'606') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=607 admin:statistics:user:total:data
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '607','0','admin:statistics:user:total:data',NULL,'admin:statistics:user:total:data',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:statistics:user:total:data' AND `id`<>'607') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=608 admin:system:config:clear:cache
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '608','0','admin:system:config:clear:cache',NULL,'admin:system:config:clear:cache',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:config:clear:cache' AND `id`<>'608') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=609 admin:system:order:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '609','0','admin:system:order:list',NULL,'admin:system:order:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:order:list' AND `id`<>'609') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=610 admin:system:staff:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '610','0','admin:system:staff:delete',NULL,'admin:system:staff:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:staff:delete' AND `id`<>'610') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=611 admin:system:staff:info
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '611','0','admin:system:staff:info',NULL,'admin:system:staff:info',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:staff:info' AND `id`<>'611') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=612 admin:system:staff:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '612','0','admin:system:staff:list',NULL,'admin:system:staff:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:staff:list' AND `id`<>'612') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=613 admin:system:staff:save
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '613','0','admin:system:staff:save',NULL,'admin:system:staff:save',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:staff:save' AND `id`<>'613') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=614 admin:system:staff:update
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '614','0','admin:system:staff:update',NULL,'admin:system:staff:update',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:staff:update' AND `id`<>'614') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=615 admin:system:staff:update:status
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '615','0','admin:system:staff:update:status',NULL,'admin:system:staff:update:status',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:staff:update:status' AND `id`<>'615') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=616 admin:system:status:info
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '616','0','admin:system:status:info',NULL,'admin:system:status:info',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:status:info' AND `id`<>'616') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=617 admin:system:store:completely:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '617','0','admin:system:store:completely:delete',NULL,'admin:system:store:completely:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:completely:delete' AND `id`<>'617') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=618 admin:system:store:count
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '618','0','admin:system:store:count',NULL,'admin:system:store:count',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:count' AND `id`<>'618') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=619 admin:system:store:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '619','0','admin:system:store:delete',NULL,'admin:system:store:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:delete' AND `id`<>'619') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=620 admin:system:store:info
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '620','0','admin:system:store:info',NULL,'admin:system:store:info',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:info' AND `id`<>'620') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=621 admin:system:store:list
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '621','0','admin:system:store:list',NULL,'admin:system:store:list',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:list' AND `id`<>'621') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=622 admin:system:store:recovery
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '622','0','admin:system:store:recovery',NULL,'admin:system:store:recovery',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:recovery' AND `id`<>'622') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=623 admin:system:store:save
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '623','0','admin:system:store:save',NULL,'admin:system:store:save',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:save' AND `id`<>'623') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=624 admin:system:store:update
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '624','0','admin:system:store:update',NULL,'admin:system:store:update',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:update' AND `id`<>'624') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=625 admin:system:store:update:status
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '625','0','admin:system:store:update:status',NULL,'admin:system:store:update:status',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:store:update:status' AND `id`<>'625') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=626 admin:system:user:level:delete
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '626','0','admin:system:user:level:delete',NULL,'admin:system:user:level:delete',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:user:level:delete' AND `id`<>'626') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=627 admin:system:user:level:save
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '627','0','admin:system:user:level:save',NULL,'admin:system:user:level:save',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:user:level:save' AND `id`<>'627') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=628 admin:system:user:level:update
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '628','0','admin:system:user:level:update',NULL,'admin:system:user:level:update',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:user:level:update' AND `id`<>'628') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=629 admin:system:user:level:use
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '629','0','admin:system:user:level:use',NULL,'admin:system:user:level:use',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:system:user:level:use' AND `id`<>'629') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=630 admin:upload:file
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '630','0','admin:upload:file',NULL,'admin:upload:file',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:upload:file' AND `id`<>'630') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=631 admin:upload:image
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '631','0','admin:upload:image',NULL,'admin:upload:image',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='admin:upload:image' AND `id`<>'631') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=632 public:jsconfig:getcrmebchatconfig
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '632','0','public:jsconfig:getcrmebchatconfig',NULL,'public:jsconfig:getcrmebchatconfig',NULL,'A','99999','1','0','2026-09-16 02:00:35','2026-09-16 02:00:35' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')=NULL AND COALESCE(`perms`,'')='public:jsconfig:getcrmebchatconfig' AND `id`<>'632') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=633 团队
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '633','0','团队','user-solid','','/tuandui','M','95','1','0','2026-09-16 14:47:51','2026-09-16 14:48:38' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='M' AND COALESCE(`component`,'')='/tuandui' AND COALESCE(`perms`,'')='' AND `id`<>'633') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=634 修改登录密码
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '634','39','修改登录密码','','admin:user:update:password','','A','99999','1','0','2026-09-16 16:12:12','2026-09-16 16:12:12' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:user:update:password' AND `id`<>'634') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=635 日志管理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '635','12','日志管理','','','/operation/logManager','M','8','1','0','2026-09-16 16:33:39','2026-09-16 16:33:39' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='M' AND COALESCE(`component`,'')='/operation/logManager' AND COALESCE(`perms`,'')='' AND `id`<>'635') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=636 管理员登录日志
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '636','635','管理员登录日志','','admin:log:login:list','/operation/logManager/adminLoginLog','C','1','1','0','2026-09-16 16:33:39','2026-09-16 16:33:39' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/operation/logManager/adminLoginLog' AND COALESCE(`perms`,'')='admin:log:login:list' AND `id`<>'636') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=637 管理员操作日志
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '637','635','管理员操作日志','','admin:log:sensitive:list','/operation/logManager/adminOperateLog','C','2','1','0','2026-09-16 16:33:39','2026-09-16 16:33:39' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/operation/logManager/adminOperateLog' AND COALESCE(`perms`,'')='admin:log:sensitive:list' AND `id`<>'637') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=638 登录日志详情
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '638','635','登录日志详情','','admin:log:login:info','','A','1','1','0','2026-09-16 16:33:39','2026-09-16 16:33:39' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:log:login:info' AND `id`<>'638') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=639 删除登录日志
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '639','635','删除登录日志','','admin:log:login:delete','','A','2','1','0','2026-09-16 16:33:39','2026-09-16 16:33:39' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:log:login:delete' AND `id`<>'639') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=640 删除操作日志
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '640','635','删除操作日志','','admin:log:sensitive:delete','','A','3','1','0','2026-09-16 16:33:39','2026-09-16 16:33:39' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:log:sensitive:delete' AND `id`<>'640') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=641 代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '641','0','代理','success','','/daili','M','96','1','0','2026-09-17 02:40:56','2026-09-17 03:15:27' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='M' AND COALESCE(`component`,'')='/daili' AND COALESCE(`perms`,'')='' AND `id`<>'641') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=642 代理设置
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '642','641','代理设置','','admin:agent:setting:list','/daili/agentSetting','C','3','1','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/daili/agentSetting' AND COALESCE(`perms`,'')='admin:agent:setting:list' AND `id`<>'642') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=643 代理奖励明细
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '643','641','代理奖励明细','','admin:agent:reward:list','/daili/agentReward','C','2','1','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/daili/agentReward' AND COALESCE(`perms`,'')='admin:agent:reward:list' AND `id`<>'643') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=644 代理管理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '644','641','代理管理','','admin:agent:list','/daili/agentList','C','1','1','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/daili/agentList' AND COALESCE(`perms`,'')='admin:agent:list' AND `id`<>'644') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=645 保存设置
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '645','642','保存设置','','admin:agent:setting:save','','A','1','0','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:agent:setting:save' AND `id`<>'645') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=646 删除代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '646','644','删除代理','','admin:agent:delete','','A','4','0','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:agent:delete' AND `id`<>'646') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=647 审核代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '647','644','审核代理','','admin:agent:audit','','A','3','0','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:agent:audit' AND `id`<>'647') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=648 修改代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '648','644','修改代理','','admin:agent:update','','A','2','0','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:agent:update' AND `id`<>'648') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=649 添加代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '649','644','添加代理','','admin:agent:save','','A','1','0','0','2026-09-17 02:40:56','2026-09-17 02:40:56' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:agent:save' AND `id`<>'649') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=652 订货
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '652','0','订货','success','','/stock','M','95','1','0','2026-09-17 04:23:20','2026-09-17 10:41:11' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='M' AND COALESCE(`component`,'')='/stock' AND COALESCE(`perms`,'')='' AND `id`<>'652') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=653 数据报表
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '653','652','数据报表','','admin:stock:report:list','/stock/report','C','8','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/report' AND COALESCE(`perms`,'')='admin:stock:report:list' AND `id`<>'653') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=654 奖励规则
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '654','652','奖励规则','','admin:stock:setting:list','/stock/setting','C','7','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/setting' AND COALESCE(`perms`,'')='admin:stock:setting:list' AND `id`<>'654') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=655 提现管理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '655','652','提现管理','','admin:stock:withdraw:list','/stock/withdraw','C','6','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/withdraw' AND COALESCE(`perms`,'')='admin:stock:withdraw:list' AND `id`<>'655') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=656 奖金明细
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '656','652','奖金明细','','admin:stock:reward:list','/stock/reward','C','5','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/reward' AND COALESCE(`perms`,'')='admin:stock:reward:list' AND `id`<>'656') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=657 换货管理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '657','652','换货管理','','admin:stock:exchange:list','/stock/exchange','C','4','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/exchange' AND COALESCE(`perms`,'')='admin:stock:exchange:list' AND `id`<>'657') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=658 订货订单
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '658','652','订货订单','','admin:stock:order:list','/stock/order','C','3','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/order' AND COALESCE(`perms`,'')='admin:stock:order:list' AND `id`<>'658') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=659 商品与库存
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '659','652','商品与库存','','admin:stock:product:list','/stock/product','C','2','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/product' AND COALESCE(`perms`,'')='admin:stock:product:list' AND `id`<>'659') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=660 订货代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '660','652','订货代理','','admin:stock:agent:list','/stock/agent','C','1','1','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='C' AND COALESCE(`component`,'')='/stock/agent' AND COALESCE(`perms`,'')='admin:stock:agent:list' AND `id`<>'660') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=668 导出报表
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '668','653','导出报表','','admin:stock:report:export','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:report:export' AND `id`<>'668') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=669 保存规则
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '669','654','保存规则','','admin:stock:setting:save','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:setting:save' AND `id`<>'669') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=670 提现审核
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '670','655','提现审核','','admin:stock:withdraw:audit','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:withdraw:audit' AND `id`<>'670') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=671 换货发货
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '671','657','换货发货','','admin:stock:exchange:send','','A','3','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:exchange:send' AND `id`<>'671') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=672 换货入库
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '672','657','换货入库','','admin:stock:exchange:back','','A','2','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:exchange:back' AND `id`<>'672') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=673 换货审核
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '673','657','换货审核','','admin:stock:exchange:audit','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:exchange:audit' AND `id`<>'673') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=674 订单发货
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '674','658','订单发货','','admin:stock:order:send','','A','3','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:order:send' AND `id`<>'674') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=675 确认收款
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '675','658','确认收款','','admin:stock:order:pay','','A','2','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:order:pay' AND `id`<>'675') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=676 审核订单
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '676','658','审核订单','','admin:stock:order:audit','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:order:audit' AND `id`<>'676') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=677 调整库存
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '677','659','调整库存','','admin:stock:log:adjust','','A','2','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:log:adjust' AND `id`<>'677') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=678 设置拿货价
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '678','659','设置拿货价','','admin:stock:price:save','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:price:save' AND `id`<>'678') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=679 删除代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '679','660','删除代理','','admin:stock:agent:delete','','A','3','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:agent:delete' AND `id`<>'679') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=680 修改代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '680','660','修改代理','','admin:stock:agent:update','','A','2','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:agent:update' AND `id`<>'680') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
-- [新增] id=681 新增代理
INSERT INTO `eb_system_menu` (`id`,`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT '681','660','新增代理','','admin:stock:agent:save','','A','1','0','0','2026-09-17 04:23:20','2026-09-17 04:23:20' FROM DUAL
  WHERE NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu` WHERE `menu_type`='A' AND COALESCE(`component`,'')='' AND COALESCE(`perms`,'')='admin:stock:agent:save' AND `id`<>'681') g)
  ON DUPLICATE KEY UPDATE `pid`=VALUES(`pid`), `name`=VALUES(`name`), `icon`=VALUES(`icon`), `perms`=VALUES(`perms`), `component`=VALUES(`component`), `menu_type`=VALUES(`menu_type`), `sort`=VALUES(`sort`), `is_show`=VALUES(`is_show`), `is_delte`=VALUES(`is_delte`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);

-- ----------------------------
-- 3. eb_system_user_level_brokerage（会员等级返佣比例）
-- ----------------------------

INSERT INTO `eb_system_user_level_brokerage` (`level_id`,`self_brokerage_rate`,`brokerage_rate_one`,`brokerage_rate_two`,`is_del`,`create_time`,`update_time`) VALUES ('1','0','10','10','1','2026-09-16 14:52:29','2026-09-16 14:52:37')
  ON DUPLICATE KEY UPDATE `level_id`=VALUES(`level_id`), `self_brokerage_rate`=VALUES(`self_brokerage_rate`), `brokerage_rate_one`=VALUES(`brokerage_rate_one`), `brokerage_rate_two`=VALUES(`brokerage_rate_two`), `is_del`=VALUES(`is_del`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
INSERT INTO `eb_system_user_level_brokerage` (`level_id`,`self_brokerage_rate`,`brokerage_rate_one`,`brokerage_rate_two`,`is_del`,`create_time`,`update_time`) VALUES ('2','10','10','10','0','2026-09-16 14:53:05','2026-09-16 14:53:05')
  ON DUPLICATE KEY UPDATE `level_id`=VALUES(`level_id`), `self_brokerage_rate`=VALUES(`self_brokerage_rate`), `brokerage_rate_one`=VALUES(`brokerage_rate_one`), `brokerage_rate_two`=VALUES(`brokerage_rate_two`), `is_del`=VALUES(`is_del`), `create_time`=VALUES(`create_time`), `update_time`=VALUES(`update_time`);
