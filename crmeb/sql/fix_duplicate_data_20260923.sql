-- ============================================================================
-- CRMEB 数据清理补丁（2026-09-23）
-- 清理三类历史脏数据，**幂等**，可重复执行；本地与线上同一份脚本。
--   1) eb_system_config 同 name 重复行（补丁 DELETE+INSERT 片段未生效时累积）
--   2) eb_system_menu 残留 / 重复 / 错挂菜单项
--   3) eb_store_product.commission_config 中已下线的订货商（stock）段
-- 执行后建议：重启 admin/front（或 DEL config_list 后重启）以重载配置缓存。
-- ============================================================================

-- ────────────────────────────────────────────────────────────────────────────
-- 1) 配置表去重：同 name 仅保留 id 最大（最后写入）的一条
--    背景：hidden_super_admin.sql / fix_online_20260919.sql 采用
--          「DELETE FROM eb_system_config WHERE name IN (...) + INSERT」模式；
--          若某次执行只跑了 INSERT 未跑 DELETE，就会累积同 name 多行。
--    影响：SystemConfigServiceImpl.updateOrSaveValueByName() 遇到重名**直接抛异常**
--          「配置名称存在多个」，导致后台这些配置项根本无法保存。
-- ────────────────────────────────────────────────────────────────────────────
DELETE c1 FROM eb_system_config c1
INNER JOIN eb_system_config c2
        ON c1.name = c2.name
       AND c1.id < c2.id;

-- ────────────────────────────────────────────────────────────────────────────
-- 2) 菜单清理
-- ────────────────────────────────────────────────────────────────────────────

-- 2a) 「保存用户隐私协议」的历史残留：perms 为空、component 错填了 perms 值，
--     且 is_delte=1（已被标记删除但未物理删除），与 456 号按钮重复。
DELETE FROM eb_system_menu
 WHERE name = '保存用户隐私协议'
   AND (perms IS NULL OR perms = '')
   AND is_delte = 1;

-- 2b) 错挂在隐藏占位菜单下的「商品与库存」：pid 指向 pid=0 且 menu_type='A'
--     的自动生成权限占位行，与「订货商」菜单下的同名项完全重复（同 perms/component）。
DELETE m FROM eb_system_menu m
INNER JOIN eb_system_menu p ON m.pid = p.id
 WHERE m.perms = 'admin:stock:product:list'
   AND m.component = '/stock/product'
   AND p.pid = 0
   AND p.menu_type = 'A';

-- 2c) 已下线的「添加分组」入口（与「商品分组」菜单及其新增按钮重复）。
--     该行由 store_product_group_20260922.sql 早期版本创建、被 product_group_level_source.sql
--     置 is_show=0 隐藏；此处按 name + perms + menu_type 直接物理删除。
--     原判据还带 `is_show = 0`，只有在「先被隐藏」之后才删得掉，
--     与补插脚本形成「插入→隐藏→删除→再插入」的死循环，故去掉该条件。
DELETE FROM eb_system_menu
 WHERE name = '添加分组'
   AND perms = 'admin:store:product:group:save'
   AND menu_type = 'C';

-- 2d) 重复菜单行：同一 (component, menu_type) 只保留 id 最小的一条，其余物理删除。
--     背景：menu_restructure_20260922.sql 早期版本的 /yunying 插入守卫写成
--           `component='/yunying' AND pid=0`，一旦该行 pid 被其它脚本改写，守卫即失配，
--           每执行一次补丁就多插一条同名「运营」目录——本地曾累积到 10 条
--           （id 701 / 761 / 764 / … / 785）。
--     边界：menu_type='A' 的按钮行 component 为空、不参与本规则；
--           /dashboard 同时存在 M（首页）与 C（控制台）两条，靠 menu_type 区分，不受影响。
DELETE m1 FROM eb_system_menu m1
INNER JOIN eb_system_menu m2
        ON m1.`component` = m2.`component`
       AND m1.`menu_type` = m2.`menu_type`
       AND m1.`id` > m2.`id`
 WHERE m1.`component` IS NOT NULL
   AND m1.`component` <> '';

-- ────────────────────────────────────────────────────────────────────────────
-- 3) 商品级佣金配置：移除已下线的订货商（stock）段
--    订货商拿货价/平级奖励改由「运营 → 订货 → 商品与库存」按商品独立设置，
--    商品侧 ProductCommissionConfig 已删除 stock 字段，历史 JSON 里的该段不再生效。
-- ────────────────────────────────────────────────────────────────────────────

-- 3a) 各业务段均为空的空壳配置直接置 NULL
UPDATE eb_store_product
   SET commission_config = NULL
 WHERE commission_config IS NOT NULL
   AND commission_config <> ''
   AND JSON_VALID(commission_config)
   AND (JSON_EXTRACT(commission_config, '$.agent')        IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.agent'))        = 0)
   AND (JSON_EXTRACT(commission_config, '$.distributor')  IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.distributor'))  = 0)
   AND (JSON_EXTRACT(commission_config, '$.store')        IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.store'))        = 0)
   AND (JSON_EXTRACT(commission_config, '$.team')         IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.team'))         = 0)
   AND (JSON_EXTRACT(commission_config, '$.stock')        IS NULL OR JSON_LENGTH(JSON_EXTRACT(commission_config, '$.stock'))        = 0);

-- 3b) 含 stock 段但该段为空的，仅摘除 stock 键
UPDATE eb_store_product
   SET commission_config = JSON_REMOVE(commission_config, '$.stock')
 WHERE commission_config IS NOT NULL
   AND commission_config <> ''
   AND JSON_VALID(commission_config)
   AND JSON_CONTAINS_PATH(commission_config, 'one', '$.stock')
   AND JSON_LENGTH(JSON_EXTRACT(commission_config, '$.stock')) = 0;

-- 3c) 含 stock 段且**有实际值**的：同样摘除（该字段已不参与任何结算；
--     保留会造成「看着配了但不生效」的误导）。摘除后各段为空则置 NULL。
UPDATE eb_store_product
   SET commission_config = NULLIF(JSON_REMOVE(commission_config, '$.stock'), CAST('{}' AS JSON))
 WHERE commission_config IS NOT NULL
   AND commission_config <> ''
   AND JSON_VALID(commission_config)
   AND JSON_CONTAINS_PATH(commission_config, 'one', '$.stock')
   AND JSON_LENGTH(JSON_EXTRACT(commission_config, '$.stock')) > 0;

-- ────────────────────────────────────────────────────────────────────────────
-- 4) 孤儿角色授权清理（必须放在本文件所有「删菜单」语句之后）
--    eb_system_role_menu 中 menu_id 已不存在于 eb_system_menu 的授权行。
--    来源：上面 2a~2d 会物理删除菜单行；若该菜单曾被授权给角色，授权行就成了
--          指向不存在 id 的孤儿。补插脚本（store_product_group_20260922.sql 等）
--          每次执行又会新造一批孤儿，不清理会无限累积
--          （表现为 eb_system_role_menu 行数每次执行补丁都 +1）。
-- ────────────────────────────────────────────────────────────────────────────
DELETE rm FROM eb_system_role_menu rm
LEFT JOIN eb_system_menu m ON m.`id` = rm.`menu_id`
 WHERE m.`id` IS NULL;

SELECT 'CRMEB fix_duplicate_data done' AS result;
