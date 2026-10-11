-- ============================================================
-- 2+1 链动：后台精简（配置项 + 菜单 + 奖励规则默认值）2026-10-11
--
-- 依据《2+1链动模式系统需求文档》口径，并落实决策：
--   · 风控（同人/设备/IP 校验、风控日志）整体移除
--   · 「滑落」改为「走人留人」，滑落相关配置项删除
--   · 见点奖收敛为 1 级（仅直属上级老板）
--   · 帮扶＝帮扶基金冻结/解冻，删除旧的「帮扶奖」与超时释放配置
--   · 团队分红保留
--
-- 幂等：全部使用 DELETE + UPDATE + INSERT ... WHERE NOT EXISTS。
-- 执行后请刷新后台配置缓存并重新登录：
--   POST /api/admin/system/config/clear/cache
-- ============================================================
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- ① 删除失效配置项
-- ------------------------------------------------------------
DELETE FROM `eb_system_config` WHERE `name` IN (
  -- 滑落机制（被走人留人取代）
  'chain_slide_enabled',
  'chain_slide_capacity',
  'chain_slide_depth',
  'chain_slide_mode',
  -- 见单奖多代（收敛为 1 级见点奖）
  'chain_see_generations',
  -- 帮扶超时释放 + 旧「每位留人代理直推 X 单」口径
  'chain_help_timeout_days',
  'chain_help_unlock_orders',
  -- 风控 6 项
  'chain_risk_status',
  'chain_risk_window_minutes',
  'chain_risk_max_uid',
  'chain_risk_max_phone',
  'chain_risk_max_ip',
  'chain_risk_max_device'
);

-- ------------------------------------------------------------
-- ② 新增配置项（文档默认值）
-- ------------------------------------------------------------

-- 基础：激活门槛价
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_active_threshold','399','链动激活门槛价(元，订单实付需达标)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_active_threshold');
-- 基础：售后保护期
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_after_sale_days','7','售后保护期(天，满N天无退款才计入链动；0=支付即生效)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_after_sale_days');
-- 身份：代理显示名
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_level_agent_name','代理','链动级别名称：代理',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_level_agent_name');
-- 身份：老板显示名
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_level_boss_name','老板','链动级别名称：老板',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_level_boss_name');
-- 身份：直推位容量 N
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_direct_capacity','2','直推位容量N(直推满N名有效代理晋升老板)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_direct_capacity');
-- 身份：走人留人开关
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_leave_team_enabled','1','走人留人机制开关(1开 0关)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_leave_team_enabled');
-- 奖励：单笔分润封顶
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_max_brokerage_ratio','50','单笔订单链动分润上限(占订单实付%)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_max_brokerage_ratio');
-- 帮扶：解冻条件 N
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_unlock_n','10','帮扶解冻条件N(留人座位各自团队有效订单数)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_unlock_n');
-- 帮扶：解冻条件 M
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_unlock_m','20','帮扶解冻条件M(老板本人团队有效订单数)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_unlock_m');
-- 帮扶：解冻方式
INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_release_mode','1','帮扶基金解冻方式(1一次性 2按订单完成比例逐笔释放)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_release_mode');

-- ------------------------------------------------------------
-- ③ 修正既有配置项的取值/文案（改成文档默认口径）
-- ------------------------------------------------------------
-- 帮扶冻结比例 50% → 30%
UPDATE `eb_system_config`
SET `value` = '30',
    `title` = '帮扶基金冻结比例%(每笔见点/平级奖冻结进帮扶池)'
WHERE `name` = 'chain_help_freeze_ratio'
  AND (`value` = '50' OR `value` = '' OR `value` IS NULL);

-- 文案对齐（其余保持用户已填值不变）
UPDATE `eb_system_config` SET `title`='链动功能开关'          WHERE `name`='chain_func_status';
UPDATE `eb_system_config` SET `title`='链动礼包商品ID(逗号分隔，由「链动产品」同步维护)' WHERE `name`='chain_gift_product_ids';
UPDATE `eb_system_config` SET `title`='有效订单判定时机(1支付即有效 2订单完成才有效)'   WHERE `name`='chain_effective_timing';
UPDATE `eb_system_config` SET `title`='佣金到账时机(1支付到账 2完成到账)'              WHERE `name`='chain_credit_timing';
UPDATE `eb_system_config` SET `title`='佣金冻结天数(0=不冻结)'                         WHERE `name`='chain_freeze_days';
UPDATE `eb_system_config` SET `title`='退款是否降级身份(0否 1是)'                      WHERE `name`='chain_identity_downgrade';
UPDATE `eb_system_config` SET `title`='礼包订单跳过普通分销/团队奖(1跳过，避免叠佣)'    WHERE `name`='chain_skip_normal_brokerage';
UPDATE `eb_system_config` SET `title`='链动规则说明文案(留空则用系统按配置自动生成)'    WHERE `name`='chain_rules_content';
UPDATE `eb_system_config` SET `title`='链动站内通知开关'                                WHERE `name`='chain_notify_status';
UPDATE `eb_system_config` SET `title`='团队分红开关'                                    WHERE `name`='chain_dividend_status';
UPDATE `eb_system_config` SET `title`='团队分红比例%'                                   WHERE `name`='chain_dividend_ratio';
UPDATE `eb_system_config` SET `title`='分红最低团队人数'                                WHERE `name`='chain_dividend_min_team';

-- ------------------------------------------------------------
-- ④ 菜单精简：9 项 → 7 项
--   删：滑落日志 / 关系审计（合并为「关系变更」）、风控日志
--   链动看板 / 链动配置 / 链动产品 / 链动用户 / 链动佣金 / 关系变更 / 团队分红
-- ------------------------------------------------------------
SET @chainMenuId := (SELECT id FROM `eb_system_menu` WHERE `component` = '/chain' AND `menu_type` = 'M' LIMIT 1);

-- 先清权限关联，再删菜单
-- ⚠️ 不要删 '/chain/relation'（那是合并后的「关系变更」目标菜单；
--    删掉再插会让菜单 id 每次执行都漂移，破坏幂等）
DELETE FROM `eb_system_role_menu`
WHERE `menu_id` IN (SELECT id FROM (
  SELECT id FROM `eb_system_menu`
  WHERE `component` IN ('/chain/risk', '/chain/slide', '/chain/audit')
) t);

DELETE FROM `eb_system_menu`
WHERE `component` IN ('/chain/risk', '/chain/slide', '/chain/audit');

-- 新建「关系变更」（含走人留人/滑落轨迹 + 管理员调整审计）
INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chainMenuId,'关系变更',NULL,'admin:chain:slide','/chain/relation','C',40,1,0,NOW(),NOW()
FROM DUAL
WHERE @chainMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/relation' LIMIT 1);

-- 既有菜单补齐/校正（sort 降序）
UPDATE `eb_system_menu` SET `sort`=80, `perms`='admin:chain:config'  WHERE `component`='/chain/dashboard';
UPDATE `eb_system_menu` SET `sort`=75, `perms`='admin:chain:product' WHERE `component`='/chain/product';
UPDATE `eb_system_menu` SET `sort`=70, `perms`='admin:chain:config'  WHERE `component`='/chain/config';
UPDATE `eb_system_menu` SET `sort`=60, `perms`='admin:chain:users'   WHERE `component`='/chain/users';
UPDATE `eb_system_menu` SET `sort`=50, `perms`='admin:chain:commission' WHERE `component`='/chain/commission';
UPDATE `eb_system_menu` SET `sort`=10, `perms`='admin:chain:dividend'   WHERE `component`='/chain/dividend';

-- ------------------------------------------------------------
-- ⑤ 奖励规则：收敛为文档口径三条
--   直推奖 固定 100 元 / 见点奖 实付 × 10% / 平级奖 下级见点 × 10%
--   删除旧的帮扶奖(help)与多代见单(see 2/3 代)
-- ------------------------------------------------------------
DELETE FROM `eb_chain_reward_rule` WHERE `type` = 'help';
DELETE FROM `eb_chain_reward_rule` WHERE `type` = 'see' AND `gen_level` > 1;
DELETE FROM `eb_chain_reward_rule` WHERE `type` = 'direct' AND `gen_level` > 1;

UPDATE `eb_chain_reward_rule`
SET `amount` = 100.00, `ratio` = 0.00, `status` = 1, `trigger_event` = 'order_effective'
WHERE `type` = 'direct' AND `gen_level` = 1;

UPDATE `eb_chain_reward_rule`
SET `amount` = 0.00, `ratio` = 10.00, `status` = 1, `trigger_event` = 'order_effective'
WHERE `type` = 'see' AND `gen_level` = 1;

UPDATE `eb_chain_reward_rule`
SET `amount` = 0.00, `ratio` = 10.00, `status` = 1, `trigger_event` = 'order_effective'
WHERE `type` = 'peer' AND `gen_level` = 1;

INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`create_time`,`update_time`)
SELECT 'direct',1,100.00,0.00,1,'order_effective',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='direct' AND `gen_level`=1);
INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`create_time`,`update_time`)
SELECT 'see',1,0.00,10.00,1,'order_effective',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='see' AND `gen_level`=1);
INSERT INTO `eb_chain_reward_rule` (`type`,`gen_level`,`amount`,`ratio`,`status`,`trigger_event`,`create_time`,`update_time`)
SELECT 'peer',1,0.00,10.00,1,'order_effective',NOW(),NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_chain_reward_rule` WHERE `type`='peer' AND `gen_level`=1);

-- ------------------------------------------------------------
-- ⑥ 校验
-- ------------------------------------------------------------
SELECT '--- 链动配置项 ---' AS info;
SELECT `name`, `value`, `title` FROM `eb_system_config` WHERE `name` LIKE 'chain%' ORDER BY `name`;
SELECT '--- 链动菜单（应为 7 项，M 目录除外）---' AS info;
SELECT id, pid, name, perms, component, sort FROM `eb_system_menu`
WHERE component LIKE '/chain%' ORDER BY menu_type DESC, sort DESC;
SELECT '--- 奖励规则（应为 3 条）---' AS info;
SELECT id, type, gen_level, amount, ratio, status FROM `eb_chain_reward_rule` ORDER BY type, gen_level;
