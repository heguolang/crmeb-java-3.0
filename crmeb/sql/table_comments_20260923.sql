-- ============================================================
-- CRMEB Java 3.0 表备注补全 / 修正（2026-09-23）
--
-- 背景：库里有一部分表没有 COMMENT，在客户端里看不出这张表是做什么的；
--       另有少数表的注释写错、乱码或残留英文。
-- 范围：本地库 150 张基础表的全部表级 COMMENT。
--   · 原本无备注 24 张 —— 本次补齐
--   · 原备注有误 10 张 —— 本次修正：eb_agent_change_log, eb_ali_pay_info, eb_category, eb_product_day_record, eb_shopping_product_day_record, eb_stock_adjust_log, eb_stock_price_sku, eb_system_store, eb_template_message, eb_trading_day_record
--   · 其余 116 张沿用库里现有中文备注
--   · 新增表说明取自 Java 实体 @ApiModel(description) 共 4 张，
--     以保证「代码里的叫法」与「库里的说明」一致
--
-- 幂等：逐表用 information_schema 比对当前备注，**只有不一致才真正 ALTER**；
--       备注已一致时该条退化为 SET 空操作，连跑多少遍结果都一样。
-- 安全：表不存在时同样退化为空操作（本地开发期的 *_bak_* 备份表线上并不存在，
--       不会让 ./deploy.sh patch 因 "Table doesn't exist" 中断）。
--       全部操作只改表元数据 COMMENT，不触碰任何数据行，MySQL 8 下不重建表。
-- ============================================================
SET NAMES utf8mb4;

-- bak_category_type1_20260916 —— 备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bak_category_type1_20260916'
                       AND IFNULL(TABLE_COMMENT, '') <> '备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- bak_store_product_cate_20260916 —— 备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bak_store_product_cate_20260916'
                       AND IFNULL(TABLE_COMMENT, '') <> '备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_activity_style —— 活动样式表（活动边框 / 活动背景装修）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('活动样式表（活动边框 / 活动背景装修）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_activity_style'
                       AND IFNULL(TABLE_COMMENT, '') <> '活动样式表（活动边框 / 活动背景装修）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_admin_login_log —— 管理员登录日志表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('管理员登录日志表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_admin_login_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '管理员登录日志表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_agent —— 区域代理
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('区域代理'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_agent'
                       AND IFNULL(TABLE_COMMENT, '') <> '区域代理'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_agent_change_log —— 区域代理变更记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('区域代理变更记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_agent_change_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '区域代理变更记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_agent_reward —— 区域代理奖励明细
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('区域代理奖励明细'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_agent_reward'
                       AND IFNULL(TABLE_COMMENT, '') <> '区域代理奖励明细'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_ali_pay_callback —— 支付宝回调表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('支付宝回调表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_ali_pay_callback'
                       AND IFNULL(TABLE_COMMENT, '') <> '支付宝回调表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_ali_pay_info —— 支付宝订单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('支付宝订单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_ali_pay_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '支付宝订单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_article —— 文章管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('文章管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_article'
                       AND IFNULL(TABLE_COMMENT, '') <> '文章管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_category —— 分类表（商品 / 文章分类，type 区分）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分类表（商品 / 文章分类，type 区分）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_category'
                       AND IFNULL(TABLE_COMMENT, '') <> '分类表（商品 / 文章分类，type 区分）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_distributor_level —— 分销商等级表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分销商等级表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_distributor_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '分销商等级表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_exception_log —— 异常信息表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('异常信息表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_exception_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '异常信息表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_express —— 快递公司表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('快递公司表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_express'
                       AND IFNULL(TABLE_COMMENT, '') <> '快递公司表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_group_config —— 组合配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组合配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_group_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '组合配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_level_stat_order_log —— 等级统计幂等流水表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('等级统计幂等流水表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_level_stat_order_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '等级统计幂等流水表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_page_category —— 页面链接分类
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('页面链接分类'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_page_category'
                       AND IFNULL(TABLE_COMMENT, '') <> '页面链接分类'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_page_diy —— DIY数据表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('DIY数据表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_page_diy'
                       AND IFNULL(TABLE_COMMENT, '') <> 'DIY数据表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_page_link —— 页面链接
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('页面链接'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_page_link'
                       AND IFNULL(TABLE_COMMENT, '') <> '页面链接'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_brand —— 组件品牌表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件品牌表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_brand'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件品牌表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_cat —— 组件类目表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件类目表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_cat'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件类目表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_delivery_company —— 组件快递公司表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件快递公司表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_delivery_company'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件快递公司表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_draft_product —— 组件商品草稿表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品草稿表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_draft_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品草稿表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_order —— 组件订单表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件订单表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_order'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件订单表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_order_product —— 组件订单详情表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件订单详情表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_order_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件订单详情表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product —— 组件商品表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_audit_info —— 组件商品审核信息表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品审核信息表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_audit_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品审核信息表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_info —— 组件商品详情表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品详情表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品详情表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_sku —— 组件商品sku表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品sku表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_sku'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品sku表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_product_sku_attr —— 组件商品sku属性表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商品sku属性表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_product_sku_attr'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商品sku属性表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_pay_component_shop_brand —— 组件商户品牌表(视频号)
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组件商户品牌表(视频号)'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_pay_component_shop_brand'
                       AND IFNULL(TABLE_COMMENT, '') <> '组件商户品牌表(视频号)'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_product_day_record —— 商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_product_day_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_schedule_job —— 定时任务
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('定时任务'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_schedule_job'
                       AND IFNULL(TABLE_COMMENT, '') <> '定时任务'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_schedule_job_log —— 定时任务日志
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('定时任务日志'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_schedule_job_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '定时任务日志'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_sensitive_method_log —— 敏感操作日志表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('敏感操作日志表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_sensitive_method_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '敏感操作日志表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shipping_templates —— 运费模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('运费模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shipping_templates'
                       AND IFNULL(TABLE_COMMENT, '') <> '运费模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shipping_templates_free —— 运费模板包邮
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('运费模板包邮'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shipping_templates_free'
                       AND IFNULL(TABLE_COMMENT, '') <> '运费模板包邮'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shipping_templates_region —— 运费模板指定区域费用
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('运费模板指定区域费用'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shipping_templates_region'
                       AND IFNULL(TABLE_COMMENT, '') <> '运费模板指定区域费用'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_shopping_product_day_record —— 商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_shopping_product_day_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_sms_record —— 短信发送记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('短信发送记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_sms_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '短信发送记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_sms_template —— 短信模板表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('短信模板表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_sms_template'
                       AND IFNULL(TABLE_COMMENT, '') <> '短信模板表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_adjust_log —— 订货系统-订货商库存调整记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货商库存调整记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_adjust_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货商库存调整记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_agent —— 订货系统-订货代理（树形层级）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货代理（树形层级）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_agent'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货代理（树形层级）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_change_log —— 订货商变更记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货商变更记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_change_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货商变更记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_exchange —— 订货系统-换货单
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-换货单'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_exchange'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-换货单'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_exchange_config —— 订货系统-换货设置
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-换货设置'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_exchange_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-换货设置'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_exchange_target —— 订货系统-换货可选目标
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-换货可选目标'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_exchange_target'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-换货可选目标'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_ladder —— 订货系统-团队级差阶梯
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-团队级差阶梯'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_ladder'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-团队级差阶梯'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_level —— 订货系统-代理层级配置
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-代理层级配置'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-代理层级配置'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_log —— 订货系统-云仓库存变动日志
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-云仓库存变动日志'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-云仓库存变动日志'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_notice —— 订货系统-消息通知
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-消息通知'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_notice'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-消息通知'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_offline_sale —— 订货系统-线下销售出库记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-线下销售出库记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_offline_sale'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-线下销售出库记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_order —— 订货系统-订货订单
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货订单'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_order'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货订单'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_order_product —— 订货系统-订货订单明细
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-订货订单明细'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_order_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-订货订单明细'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_price —— 订货系统-商品层级拿货价
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-商品层级拿货价'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_price'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-商品层级拿货价'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_price_sku —— 订货系统-规格级拿货价
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-规格级拿货价'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_price_sku'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-规格级拿货价'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_product_rel —— 参与订货的商品关联
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('参与订货的商品关联'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_product_rel'
                       AND IFNULL(TABLE_COMMENT, '') <> '参与订货的商品关联'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_reward —— 订货系统-奖金明细
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-奖金明细'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_reward'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-奖金明细'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_virtual_stock —— 订货系统-会员虚拟库存
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-会员虚拟库存'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_virtual_stock'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-会员虚拟库存'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_stock_withdraw —— 订货系统-奖金提现
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订货系统-奖金提现'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_withdraw'
                       AND IFNULL(TABLE_COMMENT, '') <> '订货系统-奖金提现'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_bargain —— 砍价表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('砍价表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_bargain'
                       AND IFNULL(TABLE_COMMENT, '') <> '砍价表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_bargain_user —— 用户参与砍价表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户参与砍价表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_bargain_user'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户参与砍价表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_bargain_user_help —— 砍价用户帮助表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('砍价用户帮助表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_bargain_user_help'
                       AND IFNULL(TABLE_COMMENT, '') <> '砍价用户帮助表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_cart —— 购物车表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('购物车表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_cart'
                       AND IFNULL(TABLE_COMMENT, '') <> '购物车表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_combination —— 拼团商品表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('拼团商品表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_combination'
                       AND IFNULL(TABLE_COMMENT, '') <> '拼团商品表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_coupon —— 优惠券表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('优惠券表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_coupon'
                       AND IFNULL(TABLE_COMMENT, '') <> '优惠券表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_coupon_user —— 优惠券记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('优惠券记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_coupon_user'
                       AND IFNULL(TABLE_COMMENT, '') <> '优惠券记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_order —— 订单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_order'
                       AND IFNULL(TABLE_COMMENT, '') <> '订单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_order_info —— 订单购物详情表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订单购物详情表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_order_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '订单购物详情表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_order_status —— 订单操作记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('订单操作记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_order_status'
                       AND IFNULL(TABLE_COMMENT, '') <> '订单操作记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_pink —— 拼团表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('拼团表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_pink'
                       AND IFNULL(TABLE_COMMENT, '') <> '拼团表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product —— 商品表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr —— 商品属性表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品属性表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品属性表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr_option —— 商品规格属性表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品规格属性表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr_option'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品规格属性表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr_result —— 商品属性详情表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品属性详情表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr_result'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品属性详情表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_attr_value —— 商品属性值表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品属性值表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_attr_value'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品属性值表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_bak_20260923_115556 —— 本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_bak_20260923_115556'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_cate —— 商品分类辅助表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品分类辅助表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_cate'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品分类辅助表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_coupon —— 商品优惠券表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品优惠券表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_coupon'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品优惠券表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_description —— 商品描述表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品描述表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_description'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品描述表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_group —— 商城商品分组
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商城商品分组'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_group'
                       AND IFNULL(TABLE_COMMENT, '') <> '商城商品分组'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_group_rel —— 商品分组关联
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品分组关联'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_group_rel'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品分组关联'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_guarantee —— 商品保障服务表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品保障服务表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_guarantee'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品保障服务表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_log —— 商品日志表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品日志表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_log'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品日志表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_relation —— 商品点赞和收藏表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品点赞和收藏表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_relation'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品点赞和收藏表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_reply —— 评论表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('评论表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_reply'
                       AND IFNULL(TABLE_COMMENT, '') <> '评论表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_product_rule —— 商品规则值(规格)表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品规则值(规格)表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_product_rule'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品规则值(规格)表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_seckill —— 商品秒杀产品表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品秒杀产品表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_seckill'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品秒杀产品表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_seckill_manger —— 商品秒杀管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商品秒杀管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_seckill_manger'
                       AND IFNULL(TABLE_COMMENT, '') <> '商品秒杀管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_store_verify_record —— 门店核销记录
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('门店核销记录'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_store_verify_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '门店核销记录'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_admin —— 后台管理员表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('后台管理员表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_admin'
                       AND IFNULL(TABLE_COMMENT, '') <> '后台管理员表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_attachment —— 附件管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('附件管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_attachment'
                       AND IFNULL(TABLE_COMMENT, '') <> '附件管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_city —— 城市表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('城市表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_city'
                       AND IFNULL(TABLE_COMMENT, '') <> '城市表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_config —— 配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_config_bak_20260923_115556 —— 本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_config_bak_20260923_115556'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_form_temp —— 表单模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('表单模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_form_temp'
                       AND IFNULL(TABLE_COMMENT, '') <> '表单模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_group —— 组合数据表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组合数据表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_group'
                       AND IFNULL(TABLE_COMMENT, '') <> '组合数据表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_group_data —— 组合数据详情表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('组合数据详情表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_group_data'
                       AND IFNULL(TABLE_COMMENT, '') <> '组合数据详情表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu —— 系统菜单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('系统菜单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu'
                       AND IFNULL(TABLE_COMMENT, '') <> '系统菜单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260922 —— 本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260922'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260923 —— 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260923'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260923_115556 —— 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260923_115556'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_menu_bak_20260923_125223 —— 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_menu_bak_20260923_125223'
                       AND IFNULL(TABLE_COMMENT, '') <> '本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_notification —— 通知设置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('通知设置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_notification'
                       AND IFNULL(TABLE_COMMENT, '') <> '通知设置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_role —— 身份管理表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('身份管理表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_role'
                       AND IFNULL(TABLE_COMMENT, '') <> '身份管理表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_role_menu —— 角色菜单关联表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('角色菜单关联表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_role_menu'
                       AND IFNULL(TABLE_COMMENT, '') <> '角色菜单关联表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_store —— 门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_store'
                       AND IFNULL(TABLE_COMMENT, '') <> '门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_store_staff —— 门店店员表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('门店店员表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_store_staff'
                       AND IFNULL(TABLE_COMMENT, '') <> '门店店员表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_team_level —— 团队等级表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('团队等级表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_team_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '团队等级表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_team_level_config —— 团队等级配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('团队等级配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_team_level_config'
                       AND IFNULL(TABLE_COMMENT, '') <> '团队等级配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_user_level —— 普通会员等级
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('普通会员等级'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_user_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '普通会员等级'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_system_user_level_brokerage —— 会员等级返佣配置表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('会员等级返佣配置表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_system_user_level_brokerage'
                       AND IFNULL(TABLE_COMMENT, '') <> '会员等级返佣配置表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_template_message —— 微信订阅消息模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信订阅消息模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_template_message'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信订阅消息模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_theme —— 主题表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('主题表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_theme'
                       AND IFNULL(TABLE_COMMENT, '') <> '主题表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_theme_download —— 主题下载记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('主题下载记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_theme_download'
                       AND IFNULL(TABLE_COMMENT, '') <> '主题下载记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_trading_day_record —— 商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_trading_day_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user —— 用户表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_address —— 用户地址表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户地址表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_address'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户地址表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_bill —— 用户账单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户账单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_bill'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户账单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_brokerage_record —— 用户佣金记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户佣金记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_brokerage_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户佣金记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_distributor_level —— 分销商等级变更记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分销商等级变更记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_distributor_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '分销商等级变更记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_distributor_level_stat —— 分销商等级统计表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('分销商等级统计表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_distributor_level_stat'
                       AND IFNULL(TABLE_COMMENT, '') <> '分销商等级统计表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_experience_record —— 用户经验记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户经验记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_experience_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户经验记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_extract —— 用户提现表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户提现表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_extract'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户提现表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_group —— 用户分组表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户分组表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_group'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户分组表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_integral_record —— 用户积分记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户积分记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_integral_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户积分记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_level —— 用户等级记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户等级记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户等级记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_recharge —— 用户充值表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户充值表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_recharge'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户充值表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_sign —— 签到记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('签到记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_sign'
                       AND IFNULL(TABLE_COMMENT, '') <> '签到记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_tag —— 标签管理
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('标签管理'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_tag'
                       AND IFNULL(TABLE_COMMENT, '') <> '标签管理'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_team_level —— 用户团队等级记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户团队等级记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_team_level'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户团队等级记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_team_level_stat —— 用户团队等级统计表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户团队等级统计表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_team_level_stat'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户团队等级统计表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_token —— 会员登录令牌表（H5 / 小程序 / APP 各端 token）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('会员登录令牌表（H5 / 小程序 / APP 各端 token）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_token'
                       AND IFNULL(TABLE_COMMENT, '') <> '会员登录令牌表（H5 / 小程序 / APP 各端 token）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_user_visit_record —— 用户访问记录表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('用户访问记录表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user_visit_record'
                       AND IFNULL(TABLE_COMMENT, '') <> '用户访问记录表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_callback —— 微信回调表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信回调表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_callback'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信回调表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_exceptions —— 微信异常表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信异常表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_exceptions'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信异常表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_pay_info —— 微信订单表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信订单表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_pay_info'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信订单表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_program_my_temp —— 小程序我的模板
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('小程序我的模板'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_program_my_temp'
                       AND IFNULL(TABLE_COMMENT, '') <> '小程序我的模板'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_program_public_temp —— 小程序微信公共模板库
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('小程序微信公共模板库'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_program_public_temp'
                       AND IFNULL(TABLE_COMMENT, '') <> '小程序微信公共模板库'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- eb_wechat_reply —— 微信关键字回复表
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('微信关键字回复表'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_wechat_reply'
                       AND IFNULL(TABLE_COMMENT, '') <> '微信关键字回复表'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_blob_triggers —— Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_blob_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_cron_triggers —— Quartz 定时任务-Cron 表达式触发器
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-Cron 表达式触发器'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_cron_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-Cron 表达式触发器'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_fired_triggers —— Quartz 定时任务-已触发的触发器实例（运行态）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-已触发的触发器实例（运行态）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_fired_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-已触发的触发器实例（运行态）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_job_details —— Quartz 定时任务-Job 定义（任务实现类与并发策略）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-Job 定义（任务实现类与并发策略）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_job_details'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-Job 定义（任务实现类与并发策略）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_locks —— Quartz 定时任务-调度器悲观锁（集群用）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-调度器悲观锁（集群用）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_locks'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-调度器悲观锁（集群用）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_paused_trigger_grps —— Quartz 定时任务-已被暂停的触发器组
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-已被暂停的触发器组'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_paused_trigger_grps'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-已被暂停的触发器组'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_scheduler_state —— Quartz 定时任务-调度器实例心跳状态（集群用）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-调度器实例心跳状态（集群用）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_scheduler_state'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-调度器实例心跳状态（集群用）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_simple_triggers —— Quartz 定时任务-简单触发器（固定间隔 / 重复次数）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-简单触发器（固定间隔 / 重复次数）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_simple_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-简单触发器（固定间隔 / 重复次数）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_simprop_triggers —— Quartz 定时任务-带属性的简单触发器
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-带属性的简单触发器'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_simprop_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-带属性的简单触发器'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- qrtz_triggers —— Quartz 定时任务-触发器（与 Job 的绑定关系）
SET @s := COALESCE((SELECT CONCAT('ALTER TABLE `', TABLE_NAME, '` COMMENT = ', QUOTE('Quartz 定时任务-触发器（与 Job 的绑定关系）'))
                      FROM information_schema.TABLES
                     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'qrtz_triggers'
                       AND IFNULL(TABLE_COMMENT, '') <> 'Quartz 定时任务-触发器（与 Job 的绑定关系）'), 'SET @tc_noop := 1');
PREPARE st_tc FROM @s; EXECUTE st_tc; DEALLOCATE PREPARE st_tc;

-- 自检：仍无备注的表应当只有「线上不存在的本地备份表」
SELECT COUNT(*) AS 库里无备注的表数
  FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'
   AND IFNULL(TABLE_COMMENT, '') = '';

SELECT 'CRMEB table_comments done' AS result;
