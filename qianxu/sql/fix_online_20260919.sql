-- ============================================================
-- 线上库修复脚本（2026-09-19）  fix_online_20260919.sql
-- 依据线上导出 crmeb_java3_2026-09-19_16-04-51 与本地库逐项比对结果生成
-- 幂等：可重复执行
--
-- 修复内容：
--   1. 清理 eb_system_config 重复配置项（同 name 多条，读取不稳定）
--   2. 开启模块开关 sys_switch_*（订货/区域代理/团队奖/门店/分销等被误关）
--   3. 统一排序规则为 utf8mb4_general_ci（现为 0900_ai_ci / utf8_general_ci 混用）
--   4. 修正订货模块菜单排序（sort 重复导致菜单顺序乱）
--
-- 执行：mysql -uroot -p密码 --default-character-set=utf8mb4 crmeb_java3 < fix_online_20260919.sql
-- 执行完重启 jar，并清一次 Redis 配置缓存
-- ============================================================

SET NAMES utf8mb4;

-- ========== 1. 清理重复配置项（每个 name 只保留 id 最大的一条） ==========
DELETE c FROM eb_system_config c
JOIN (SELECT name, MAX(id) AS keep_id FROM eb_system_config GROUP BY name) k
  ON c.name = k.name
WHERE c.id <> k.keep_id;

-- ========== 2. 开启模块开关 ==========
-- 说明：ModuleSwitchInterceptor 会拦截 /api/admin/stock、/api/front/stock、/api/*/agent 等，
--       开关值不等于 '1' 时接口被直接拦截；菜单显隐、团队奖发放同样读这些开关。
--       线上当前全部为 0（关闭），导致订货/代理/门店/分销/团队奖功能不可用。
UPDATE eb_system_config SET value = '1', update_time = NOW()
WHERE name IN (
  'sys_switch_team_reward','sys_switch_stock','sys_switch_store',
  'sys_switch_daili','sys_switch_spread',
  'sys_switch_integral','sys_switch_seckill','sys_switch_bargain',
  'sys_switch_combination','sys_switch_coupon'
);

-- 缺失时补插（幂等）
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time)
SELECT t.name, t.title, 0, '1', 0, NOW(), NOW() FROM (
  SELECT 'sys_switch_team_reward' AS name, '隐藏面板-团队奖开关' AS title
  UNION ALL SELECT 'sys_switch_stock', '隐藏面板-订货商开关'
  UNION ALL SELECT 'sys_switch_store', '隐藏面板-门店开关'
  UNION ALL SELECT 'sys_switch_daili', '隐藏面板-区域代理开关'
  UNION ALL SELECT 'sys_switch_spread', '隐藏面板-分销开关'
  UNION ALL SELECT 'sys_switch_integral', '隐藏面板-积分开关'
  UNION ALL SELECT 'sys_switch_seckill', '隐藏面板-秒杀开关'
  UNION ALL SELECT 'sys_switch_bargain', '隐藏面板-砍价开关'
  UNION ALL SELECT 'sys_switch_combination', '隐藏面板-拼团开关'
  UNION ALL SELECT 'sys_switch_coupon', '隐藏面板-优惠券开关'
) t
WHERE NOT EXISTS (SELECT 1 FROM eb_system_config c WHERE c.name = t.name);

-- ========== 3. 统一排序规则 utf8mb4_general_ci ==========
ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- QRTZ_ 定时器表之间有外键，逐表转换时引用列会短暂不兼容，先关外键检查
SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `bak_category_type1_20260916` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `bak_store_product_cate_20260916` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_activity_style` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_admin_login_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_agent` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_agent_change_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_agent_reward` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_ali_pay_callback` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_ali_pay_info` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_article` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_category` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_exception_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_express` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_group_config` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_page_category` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_page_diy` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_page_link` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_brand` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_cat` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_delivery_company` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_draft_product` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_order` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_order_product` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_product` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_product_audit_info` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_product_info` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_product_sku` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_product_sku_attr` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_pay_component_shop_brand` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_product_day_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_schedule_job` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_schedule_job_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_sensitive_method_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_shipping_templates` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_shipping_templates_free` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_shipping_templates_region` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_shopping_product_day_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_sms_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_sms_template` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_adjust_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_agent` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_change_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_exchange` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_exchange_config` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_exchange_target` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_ladder` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_level` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_notice` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_offline_sale` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_order` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_order_product` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_price` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_price_sku` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_product_rel` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_reward` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_virtual_stock` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_stock_withdraw` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_bargain` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_bargain_user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_bargain_user_help` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_cart` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_combination` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_coupon` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_coupon_user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_order` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_order_info` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_order_status` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_pink` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_attr` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_attr_option` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_attr_result` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_attr_value` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_cate` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_coupon` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_description` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_guarantee` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_log` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_relation` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_reply` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_product_rule` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_seckill` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_seckill_manger` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_store_verify_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_admin` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_attachment` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_city` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_config` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_form_temp` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_group` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_group_data` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_menu` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_notification` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_role` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_role_menu` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_store` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_store_staff` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_team_level` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_team_level_config` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_user_level` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_system_user_level_brokerage` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_template_message` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_theme` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_theme_download` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_trading_day_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_address` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_bill` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_brokerage_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_experience_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_extract` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_group` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_integral_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_level` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_recharge` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_sign` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_tag` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_team_level` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_team_level_stat` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_token` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_user_visit_record` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_wechat_callback` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_wechat_exceptions` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_wechat_pay_info` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_wechat_program_my_temp` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_wechat_program_public_temp` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `eb_wechat_reply` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_blob_triggers` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_cron_triggers` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_fired_triggers` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_job_details` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_locks` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_paused_trigger_grps` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_scheduler_state` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_simple_triggers` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_simprop_triggers` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE `qrtz_triggers` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ========== 4. 修正订货模块菜单排序 ==========
UPDATE eb_system_menu SET sort = 8 WHERE id = 691 AND component = '/stock/level';
UPDATE eb_system_menu SET sort = 6 WHERE id = 682 AND component = '/stock/changelog';
UPDATE eb_system_menu SET sort = 10 WHERE id = 653 AND component = '/stock/report';

-- ========== DONE ==========
SELECT 'fix_online_20260919 done' AS result;
