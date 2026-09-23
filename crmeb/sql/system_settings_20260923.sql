-- ============================================================
-- CRMEB Java 3.0 项目配置收敛（本地配置 → 线上）
-- 生成时间: 2026-09-23 17:53:13
-- 生成来源：本地库 eb_system_config 与厂商基库 Crmeb_v3.0.sql 的差异集
--   · 我方新增配置 55 项（订货商 / 团队奖 / 分销商等级 / 区域代理 / 提现 / 隐藏面板开关等）
--   · 值被我方修改 28 项，逐条如下：
--   api_url                          http://api.qianxutec.com  →  生产地址
--   brokerage_func_status            0
--   change_color_config              #1B90FF
--   config_export_open               2
--   copyright_company_name           黔序科技
--   copyright_internet_record        Copyright@2026 贵州黔序科技有限公司
--   copyright_internet_record_url    www.qianxutec.com
--   crmeb_tongji_js                  (空)
--   front_api_url                    http://api.qianxutec.com  →  生产地址
--   integral_ratio                   0
--   localUploadUrl                   http://api.qianxutec.com  →  生产地址
--   logistics_type                   2
--   mobile_login_logo                crmebimage/public/product/2026/09/16/d2882dd
--   order_give_integral              0
--   routine_name                     黔序科技
--   routine_phone_verification       ,1
--   seo_title                        黔序科技
--   site_logo_lefttop                crmebimage/public/theme/2026/09/19/ce5ce8255
--   site_logo_login                  crmebimage/public/product/2026/09/16/a83b057
--   site_logo_square                 crmebimage/public/theme/2026/09/19/ce5ce8255
--   site_name                        黔序科技-Java
--   site_url                         http://api.qianxutec.com  →  生产地址
--   splash_ad_switch                 0
--   store_brokerage_is_bubble        0
--   system_product_copy_type         2
--   telephone_service_switch         close
--   user_extract_min_price           1
--   wechat_routine_shipping_switch   1
--
-- ⛔ 刻意【不同步】的东西（保持线上现值）：
--   1. 全部凭证 / 密钥类配置——微信支付商户号与证书、支付宝密钥、短信账号 token、
--      阿里云 / 七牛 / 腾讯云 / 京东存储密钥、地图 key、小票打印密钥、快递查询码等。
--      本地这些值全是厂商占位符（111111 / 1111 / xxxx），推上去会让线上的
--      支付、短信、图片上传立即不可用。下游部署后请在后台自行填写。
--   2. 与厂商基库取值一致的其余配置项——不同步，避免用厂商默认值覆盖线上真实值
--      （例如 consumer_hotline、system_express_app_code 之类线上可能已配置过的项）。
--
-- 地址口径（生产）：接口 / 图片域名 http://api.qianxutec.com；移动端站点 http://app.qianxutec.com
--   site_url 的语义是「移动端站点域名」，被微信 H5 支付当作 h5_info.wap_url 使用
--   （OrderPayServiceImpl#getUnifiedorderVo），因此指向 H5 站点而不是接口域名。
--
-- 幂等：临时表 + 按 name 匹配，跑多少遍结果一致；不触碰任何业务数据。
-- ============================================================
SET NAMES utf8mb4;

CREATE TEMPORARY TABLE `tmp_cfg_sync` (
  `name`  varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL DEFAULT '',
  `value` text,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `tmp_cfg_sync` (`name`, `title`, `value`) VALUES
('team_brokerage_credit_timing', '团队奖到账方式', '1'),
('register_default_is_promoter', '注册默认推广员', '1'),
('register_default_user_level', '注册默认会员等级', '1'),
('team_brokerage_status', '团队极差奖开关：0=关闭，1=开启', '1'),
('team_brokerage_max_depth', '团队奖向上追溯层数：0=不限', '0'),
('brokerage_credit_timing', '分销佣金到账方式', '1'),
('integral_credit_timing', '积分到账方式', '1'),
('agent_func_status', '区域代理功能开关', '1'),
('agent_apply_status', '区域代理申请开关', '1'),
('agent_credit_timing', '区域代理结算时机', '1'),
('stock_order_audit', '订货订单上级审核开关', '1'),
('stock_diff_reward_status', '订货差价奖励开关', '1'),
('stock_exchange_diff', '换货单参与差价奖励', '0'),
('stock_ladder_status', '阶梯业绩奖励开关', '1'),
('stock_ladder_cycle', '阶梯业绩结算周期（1=月度 2=季度 3=年度）', '1'),
('stock_peer_status', '订货平级奖励开关', '1'),
('stock_peer_rate', '订货平级奖励比例', '5'),
('stock_peer_generations', '订货平级奖励代数', '1'),
('stock_parent_deliver', '订货订单由上级发货', '0'),
('stock_up_search_hours', '上级无库存自动向上查找等待时长(小时)', '12'),
('user_extract_switch', '佣金提现开关', '1'),
('user_extract_multiple', '提现倍数', '0'),
('user_extract_fee_type', '手续费类型', 'ratio'),
('user_extract_fee', '手续费', '0'),
('user_extract_weekdays', '可提现星期', '1,2,3,4,5,6,7'),
('user_extract_time_start', '可提现开始小时', '0'),
('user_extract_time_end', '可提现结束小时', '24'),
('login_notice_text', '', '登录后即可享受完整服务，是否前往登录？'),
('login_notice_switch', '', '1'),
('stock_wait_pay_hours', '订货单待付款超时时长（小时）', '24'),
('stock_virtual_audit', '虚拟库存单付款后审核开关', '0'),
('stock_exchange_single', '', '0'),
('stock_exchange_diff_parent_rate', '', '100'),
('stock_exchange_diff_wechat', '', '1'),
('agent_apply_regions', '会员端可申请的代理区域', '1,2,3'),
('stock_exchange_hq_audit', '', ''),
('stock_exchange_return_address', '', '收件人：总部\n电话：16688888888\n地址：贵州省贵阳市观山湖区富力中心'),
('product_group_other_visible', '设置分组权限后其它会员能否看到分组中商品', '0'),
('product_group_deny_tip_enable', '自定义不符合商品分组权限条件的提示语开关', '0'),
('product_group_deny_tip', '不符合商品分组权限条件的提示语', '您暂无权限查看该商品'),
('distributor_level_enabled', '', '1'),
('distributor_level_brokerage_enabled', '', '0'),
('team_level_cycle_reset', '', '0'),
('distributor_level_cycle_reset', '', '0'),
('distributor_level_max_depth', '', '0'),
('sys_switch_team_reward', '隐藏面板-团队奖开关', '1'),
('sys_switch_stock', '隐藏面板-订货商开关', '1'),
('sys_switch_store', '隐藏面板-门店开关', '1'),
('sys_switch_daili', '隐藏面板-区域代理开关', '1'),
('sys_switch_spread', '隐藏面板-分销开关', '1'),
('sys_switch_integral', '隐藏面板-积分开关', '1'),
('sys_switch_seckill', '隐藏面板-秒杀开关', '1'),
('sys_switch_bargain', '隐藏面板-砍价开关', '1'),
('sys_switch_combination', '隐藏面板-拼团开关', '1'),
('sys_switch_coupon', '隐藏面板-优惠券开关', '1'),
('api_url', 'api_url', 'http://api.qianxutec.com'),
('brokerage_func_status', 'brokerage_func_status', '0'),
('change_color_config', 'change_color_config', '#1B90FF'),
('config_export_open', 'config_export_open', '2'),
('copyright_company_name', 'copyright_company_name', '黔序科技'),
('copyright_internet_record', 'copyright_internet_record', 'Copyright@2026 贵州黔序科技有限公司'),
('copyright_internet_record_url', 'copyright_internet_record_url', 'www.qianxutec.com'),
('crmeb_tongji_js', 'crmeb_tongji_js', ''),
('front_api_url', 'front_api_url', 'http://api.qianxutec.com'),
('integral_ratio', 'integral_ratio', '0'),
('localUploadUrl', 'localUploadUrl', 'http://api.qianxutec.com'),
('logistics_type', 'logistics_type', '2'),
('mobile_login_logo', '', 'crmebimage/public/product/2026/09/16/d2882dd292ad4b33bca2d30d0405721acvq09gfro4.png'),
('order_give_integral', 'order_give_integral', '0'),
('routine_name', 'routine_name', '黔序科技'),
('routine_phone_verification', 'routine_phone_verification', ',1'),
('seo_title', '', '黔序科技'),
('site_logo_lefttop', 'site_logo_lefttop', 'crmebimage/public/theme/2026/09/19/ce5ce8255be549acb35c7c219e674606ynq35htpg0.png'),
('site_logo_login', 'site_logo_login', 'crmebimage/public/product/2026/09/16/a83b057c809745a7a295be94cf10275brxadtc506f.png'),
('site_logo_square', 'site_logo_square', 'crmebimage/public/theme/2026/09/19/ce5ce8255be549acb35c7c219e674606ynq35htpg0.png'),
('site_name', '', '黔序科技-Java'),
('site_url', '', 'http://app.qianxutec.com'),
('splash_ad_switch', '', '0'),
('store_brokerage_is_bubble', 'store_brokerage_is_bubble', '0'),
('system_product_copy_type', 'system_product_copy_type', '2'),
('telephone_service_switch', 'telephone_service_switch', 'close'),
('user_extract_min_price', '', '1'),
('wechat_routine_shipping_switch', '', '1');
-- 1) 已存在的项：值或标题不同才写，避免无谓刷新 update_time
UPDATE eb_system_config c
  JOIN tmp_cfg_sync t ON c.`name` = t.`name`
   SET c.`value`       = t.`value`,
       c.`title`       = IF(t.`title` = '', c.`title`, t.`title`),
       c.`update_time` = NOW()
 WHERE NOT (c.`value` <=> t.`value`)
    OR (t.`title` <> '' AND NOT (c.`title` <=> t.`title`));

-- 2) 线上缺失的项：补插（不覆盖任何已有行）
INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT t.`name`, t.`title`, 0, t.`value`, 0, NOW(), NOW()
  FROM tmp_cfg_sync t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM eb_system_config) x
                    WHERE x.`name` = t.`name`);

DROP TEMPORARY TABLE `tmp_cfg_sync`;

-- 3) 自检：下列各项必须全部存在（数量应与本次同步项数一致）
SELECT 'CRMEB system_settings done' AS result, COUNT(*) AS ensured_rows
  FROM eb_system_config WHERE `name` IN ('team_brokerage_credit_timing', 'register_default_is_promoter', 'register_default_user_level', 'team_brokerage_status', 'team_brokerage_max_depth', 'brokerage_credit_timing', 'integral_credit_timing', 'agent_func_status', 'agent_apply_status', 'agent_credit_timing', 'stock_order_audit', 'stock_diff_reward_status', 'stock_exchange_diff', 'stock_ladder_status', 'stock_ladder_cycle', 'stock_peer_status', 'stock_peer_rate', 'stock_peer_generations', 'stock_parent_deliver', 'stock_up_search_hours', 'user_extract_switch', 'user_extract_multiple', 'user_extract_fee_type', 'user_extract_fee', 'user_extract_weekdays', 'user_extract_time_start', 'user_extract_time_end', 'login_notice_text', 'login_notice_switch', 'stock_wait_pay_hours', 'stock_virtual_audit', 'stock_exchange_single', 'stock_exchange_diff_parent_rate', 'stock_exchange_diff_wechat', 'agent_apply_regions', 'stock_exchange_hq_audit', 'stock_exchange_return_address', 'product_group_other_visible', 'product_group_deny_tip_enable', 'product_group_deny_tip', 'distributor_level_enabled', 'distributor_level_brokerage_enabled', 'team_level_cycle_reset', 'distributor_level_cycle_reset', 'distributor_level_max_depth', 'sys_switch_team_reward', 'sys_switch_stock', 'sys_switch_store', 'sys_switch_daili', 'sys_switch_spread', 'sys_switch_integral', 'sys_switch_seckill', 'sys_switch_bargain', 'sys_switch_combination', 'sys_switch_coupon', 'api_url', 'brokerage_func_status', 'change_color_config', 'config_export_open', 'copyright_company_name', 'copyright_internet_record', 'copyright_internet_record_url', 'crmeb_tongji_js', 'front_api_url', 'integral_ratio', 'localUploadUrl', 'logistics_type', 'mobile_login_logo', 'order_give_integral', 'routine_name', 'routine_phone_verification', 'seo_title', 'site_logo_lefttop', 'site_logo_login', 'site_logo_square', 'site_name', 'site_url', 'splash_ad_switch', 'store_brokerage_is_bubble', 'system_product_copy_type', 'telephone_service_switch', 'user_extract_min_price', 'wechat_routine_shipping_switch');
