# QIANXU Java 3.0 数据库表清单

> 库名 `qianxu`（线上 `crmeb_java3`），共 150 张基础表，全部已带中文备注。
> 表备注由 `qianxu/sql/table_comments_20260923.sql` 幂等维护，已并入 `qianxu/sql/oneclick/02_patches_all.sql`。

## 模块索引

| 模块 | 表数 |
| --- | --- |
| 系统设置与权限 | 12 |
| 会员与用户 | 16 |
| 等级体系（会员 / 团队 / 分销商 / 订货商） | 8 |
| 商品 | 17 |
| 订单与交易 | 5 |
| 营销活动 | 7 |
| 订货系统（微商逐级拿货） | 19 |
| 区域代理 | 3 |
| 内容与装修 | 7 |
| 微信与支付 | 21 |
| 门店与配送 | 7 |
| 短信 | 2 |
| 统计报表 | 3 |
| 定时任务 | 12 |
| 日志与审计 | 3 |
| 本地备份表（线上不存在） | 8 |
| **合计** | **150** |

## 系统设置与权限

| 表名 | 说明 |
| --- | --- |
| `eb_group_config` | 组合配置表 |
| `eb_system_admin` | 后台管理员表 |
| `eb_system_attachment` | 附件管理表 |
| `eb_system_city` | 城市表 |
| `eb_system_config` | 配置表 |
| `eb_system_form_temp` | 表单模板 |
| `eb_system_group` | 组合数据表 |
| `eb_system_group_data` | 组合数据详情表 |
| `eb_system_menu` | 系统菜单表 |
| `eb_system_notification` | 通知设置表 |
| `eb_system_role` | 身份管理表 |
| `eb_system_role_menu` | 角色菜单关联表 |

## 会员与用户

| 表名 | 说明 |
| --- | --- |
| `eb_user` | 用户表 |
| `eb_user_address` | 用户地址表 |
| `eb_user_bill` | 用户账单表 |
| `eb_user_brokerage_record` | 用户佣金记录表 |
| `eb_user_distributor_level` | 分销商等级变更记录表 |
| `eb_user_distributor_level_stat` | 分销商等级统计表 |
| `eb_user_experience_record` | 用户经验记录表 |
| `eb_user_extract` | 用户提现表 |
| `eb_user_group` | 用户分组表 |
| `eb_user_integral_record` | 用户积分记录表 |
| `eb_user_level` | 用户等级记录表 |
| `eb_user_recharge` | 用户充值表 |
| `eb_user_sign` | 签到记录表 |
| `eb_user_tag` | 标签管理 |
| `eb_user_token` | 会员登录令牌表（H5 / 小程序 / APP 各端 token） |
| `eb_user_visit_record` | 用户访问记录表 |

## 等级体系（会员 / 团队 / 分销商 / 订货商）

| 表名 | 说明 |
| --- | --- |
| `eb_distributor_level` | 分销商等级表 |
| `eb_level_stat_order_log` | 等级统计幂等流水表 |
| `eb_system_team_level` | 团队等级表 |
| `eb_system_team_level_config` | 团队等级配置表 |
| `eb_system_user_level` | 普通会员等级 |
| `eb_system_user_level_brokerage` | 会员等级返佣配置表 |
| `eb_user_team_level` | 用户团队等级记录表 |
| `eb_user_team_level_stat` | 用户团队等级统计表 |

## 商品

| 表名 | 说明 |
| --- | --- |
| `eb_category` | 分类表（商品 / 文章分类，type 区分） |
| `eb_store_cart` | 购物车表 |
| `eb_store_product` | 商品表 |
| `eb_store_product_attr` | 商品属性表 |
| `eb_store_product_attr_option` | 商品规格属性表 |
| `eb_store_product_attr_result` | 商品属性详情表 |
| `eb_store_product_attr_value` | 商品属性值表 |
| `eb_store_product_cate` | 商品分类辅助表 |
| `eb_store_product_coupon` | 商品优惠券表 |
| `eb_store_product_description` | 商品描述表 |
| `eb_store_product_group` | 商城商品分组 |
| `eb_store_product_group_rel` | 商品分组关联 |
| `eb_store_product_guarantee` | 商品保障服务表 |
| `eb_store_product_log` | 商品日志表 |
| `eb_store_product_relation` | 商品点赞和收藏表 |
| `eb_store_product_reply` | 评论表 |
| `eb_store_product_rule` | 商品规则值(规格)表 |

## 订单与交易

| 表名 | 说明 |
| --- | --- |
| `eb_store_combination` | 拼团商品表 |
| `eb_store_order` | 订单表 |
| `eb_store_order_info` | 订单购物详情表 |
| `eb_store_order_status` | 订单操作记录表 |
| `eb_store_pink` | 拼团表 |

## 营销活动

| 表名 | 说明 |
| --- | --- |
| `eb_store_bargain` | 砍价表 |
| `eb_store_bargain_user` | 用户参与砍价表 |
| `eb_store_bargain_user_help` | 砍价用户帮助表 |
| `eb_store_coupon` | 优惠券表 |
| `eb_store_coupon_user` | 优惠券记录表 |
| `eb_store_seckill` | 商品秒杀产品表 |
| `eb_store_seckill_manger` | 商品秒杀管理表 |

## 订货系统（微商逐级拿货）

| 表名 | 说明 |
| --- | --- |
| `eb_stock_adjust_log` | 订货系统-订货商库存调整记录 |
| `eb_stock_agent` | 订货系统-订货代理（树形层级） |
| `eb_stock_change_log` | 订货商变更记录 |
| `eb_stock_exchange` | 订货系统-换货单 |
| `eb_stock_exchange_config` | 订货系统-换货设置 |
| `eb_stock_exchange_target` | 订货系统-换货可选目标 |
| `eb_stock_ladder` | 订货系统-团队级差阶梯 |
| `eb_stock_level` | 订货系统-代理层级配置 |
| `eb_stock_log` | 订货系统-云仓库存变动日志 |
| `eb_stock_notice` | 订货系统-消息通知 |
| `eb_stock_offline_sale` | 订货系统-线下销售出库记录 |
| `eb_stock_order` | 订货系统-订货订单 |
| `eb_stock_order_product` | 订货系统-订货订单明细 |
| `eb_stock_price` | 订货系统-商品层级拿货价 |
| `eb_stock_price_sku` | 订货系统-规格级拿货价 |
| `eb_stock_product_rel` | 参与订货的商品关联 |
| `eb_stock_reward` | 订货系统-奖金明细 |
| `eb_stock_virtual_stock` | 订货系统-会员虚拟库存 |
| `eb_stock_withdraw` | 订货系统-奖金提现 |

## 区域代理

| 表名 | 说明 |
| --- | --- |
| `eb_agent` | 区域代理 |
| `eb_agent_change_log` | 区域代理变更记录 |
| `eb_agent_reward` | 区域代理奖励明细 |

## 内容与装修

| 表名 | 说明 |
| --- | --- |
| `eb_activity_style` | 活动样式表（活动边框 / 活动背景装修） |
| `eb_article` | 文章管理表 |
| `eb_page_category` | 页面链接分类 |
| `eb_page_diy` | DIY数据表 |
| `eb_page_link` | 页面链接 |
| `eb_theme` | 主题表 |
| `eb_theme_download` | 主题下载记录表 |

## 微信与支付

| 表名 | 说明 |
| --- | --- |
| `eb_ali_pay_callback` | 支付宝回调表 |
| `eb_ali_pay_info` | 支付宝订单表 |
| `eb_pay_component_brand` | 组件品牌表(视频号) |
| `eb_pay_component_cat` | 组件类目表(视频号) |
| `eb_pay_component_delivery_company` | 组件快递公司表(视频号) |
| `eb_pay_component_draft_product` | 组件商品草稿表(视频号) |
| `eb_pay_component_order` | 组件订单表(视频号) |
| `eb_pay_component_order_product` | 组件订单详情表(视频号) |
| `eb_pay_component_product` | 组件商品表(视频号) |
| `eb_pay_component_product_audit_info` | 组件商品审核信息表(视频号) |
| `eb_pay_component_product_info` | 组件商品详情表(视频号) |
| `eb_pay_component_product_sku` | 组件商品sku表(视频号) |
| `eb_pay_component_product_sku_attr` | 组件商品sku属性表(视频号) |
| `eb_pay_component_shop_brand` | 组件商户品牌表(视频号) |
| `eb_template_message` | 微信订阅消息模板 |
| `eb_wechat_callback` | 微信回调表 |
| `eb_wechat_exceptions` | 微信异常表 |
| `eb_wechat_pay_info` | 微信订单表 |
| `eb_wechat_program_my_temp` | 小程序我的模板 |
| `eb_wechat_program_public_temp` | 小程序微信公共模板库 |
| `eb_wechat_reply` | 微信关键字回复表 |

## 门店与配送

| 表名 | 说明 |
| --- | --- |
| `eb_express` | 快递公司表 |
| `eb_shipping_templates` | 运费模板 |
| `eb_shipping_templates_free` | 运费模板包邮 |
| `eb_shipping_templates_region` | 运费模板指定区域费用 |
| `eb_store_verify_record` | 门店核销记录 |
| `eb_system_store` | 门店自提点（含店长 leader_uid、核销 / 自提费用、配送范围） |
| `eb_system_store_staff` | 门店店员表 |

## 短信

| 表名 | 说明 |
| --- | --- |
| `eb_sms_record` | 短信发送记录表 |
| `eb_sms_template` | 短信模板表 |

## 统计报表

| 表名 | 说明 |
| --- | --- |
| `eb_product_day_record` | 商品日统计（单商品维度：访客 / 收藏 / 加购 / 下单 / 支付金额） |
| `eb_shopping_product_day_record` | 商城商品日统计（新增商品数 + 全站商品页访客 / 收藏 / 加购 / 下单） |
| `eb_trading_day_record` | 商城交易日统计（订单量 / 支付额 / 退款、充值、余额支付、佣金） |

## 定时任务

| 表名 | 说明 |
| --- | --- |
| `eb_schedule_job` | 定时任务 |
| `eb_schedule_job_log` | 定时任务日志 |
| `qrtz_blob_triggers` | Quartz 定时任务-Blob 型触发器（自定义 Trigger 序列化数据） |
| `qrtz_cron_triggers` | Quartz 定时任务-Cron 表达式触发器 |
| `qrtz_fired_triggers` | Quartz 定时任务-已触发的触发器实例（运行态） |
| `qrtz_job_details` | Quartz 定时任务-Job 定义（任务实现类与并发策略） |
| `qrtz_locks` | Quartz 定时任务-调度器悲观锁（集群用） |
| `qrtz_paused_trigger_grps` | Quartz 定时任务-已被暂停的触发器组 |
| `qrtz_scheduler_state` | Quartz 定时任务-调度器实例心跳状态（集群用） |
| `qrtz_simple_triggers` | Quartz 定时任务-简单触发器（固定间隔 / 重复次数） |
| `qrtz_simprop_triggers` | Quartz 定时任务-带属性的简单触发器 |
| `qrtz_triggers` | Quartz 定时任务-触发器（与 Job 的绑定关系） |

## 日志与审计

| 表名 | 说明 |
| --- | --- |
| `eb_admin_login_log` | 管理员登录日志表 |
| `eb_exception_log` | 异常信息表 |
| `eb_sensitive_method_log` | 敏感操作日志表 |

## 本地备份表（线上不存在）

| 表名 | 说明 |
| --- | --- |
| `bak_category_type1_20260916` | 备份表：eb_category 中 type=1 的分类（reset_default_category.sql 分类改造前快照，2026-09-16） |
| `bak_store_product_cate_20260916` | 备份表：eb_store_product 的 id/cate_id/store_name（reset_default_category.sql 商品分类改造前快照，2026-09-16） |
| `eb_store_product_bak_20260923_115556` | 本地备份表：eb_store_product 商品分组改造前快照（2026-09-23 11:55，仅本地存在） |
| `eb_system_config_bak_20260923_115556` | 本地备份表：eb_system_config 配置收敛前快照（2026-09-23 11:55，仅本地存在） |
| `eb_system_menu_bak_20260922` | 本地备份表：eb_system_menu 菜单重构前快照（2026-09-22，仅本地存在） |
| `eb_system_menu_bak_20260923` | 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23，仅本地存在） |
| `eb_system_menu_bak_20260923_115556` | 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 11:55，仅本地存在） |
| `eb_system_menu_bak_20260923_125223` | 本地备份表：eb_system_menu 菜单调整前快照（2026-09-23 12:52，仅本地存在） |

