-- ============================================================
-- 拼购活动模块（MySQL 5.7 / 宝塔兼容，幂等，无 DELIMITER）
-- 玩法：购买拼购活动商品参与活动，满 total_num 人成团，
--       随机抽 win_num 人中奖（得商品，正常发货），
--       未中奖者本金全额退回余额账户 + 活动约定补贴佣金；
--       推荐人按直推人数享参与金额 share_rate_low‰ ~ share_rate_high‰ 分享奖励。
-- ============================================================

SET @db = DATABASE();

-- ------------------------------------------------------------
-- 1. 拼购活动表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_pingou_activity` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL DEFAULT '' COMMENT '活动名称',
  `image` varchar(512) DEFAULT '' COMMENT '活动图片',
  `product_id` int(11) NOT NULL COMMENT '拼购商品ID（中奖者所得商品）',
  `product_name` varchar(255) DEFAULT '' COMMENT '拼购商品名称（冗余）',
  `product_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '拼购商品售价（参与金额口径，冗余）',
  `start_time` datetime DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '活动结束时间',
  `total_num` int(11) NOT NULL DEFAULT 10 COMMENT '成团人数',
  `win_num` int(11) NOT NULL DEFAULT 2 COMMENT '中奖人数',
  `refund_subsidy` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '未中奖补贴佣金（每人，元，发放到佣金账户）',
  `share_rate_low` decimal(6,3) NOT NULL DEFAULT 5.000 COMMENT '分享奖励低比例（‰，直推人数未达阈值时）',
  `share_rate_high` decimal(6,3) NOT NULL DEFAULT 10.000 COMMENT '分享奖励高比例（‰，直推人数达到阈值时）',
  `share_rate_threshold` int(11) NOT NULL DEFAULT 10 COMMENT '高比例直推人数阈值（直推参与满 N 人享高比例，已废弃，保留兼容）',
  `share_tiers` text NULL COMMENT '分享奖励阶梯JSON：[{minDirectNum直推人数下限,rewardType(1=比例%|2=固定金额),rewardValue}]，按直推人数匹配最高一档',
  `limit_num` int(11) NOT NULL DEFAULT 0 COMMENT '每人限参与次数（0=不限）',
  `is_show` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否首页/列表展示：1=是 0=否',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越大越靠前）',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=禁用',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_status` (`status`, `is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼购活动表';

-- ------------------------------------------------------------
-- 2. 拼购团期表（一个活动同时只有一个进行中的团，满员即开奖并自动开下一期）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_pingou_team` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `activity_id` int(11) NOT NULL COMMENT '活动ID',
  `team_no` int(11) NOT NULL DEFAULT 1 COMMENT '第几期（该活动累计开团次数）',
  `join_num` int(11) NOT NULL DEFAULT 0 COMMENT '已参团人数',
  `total_num` int(11) NOT NULL DEFAULT 0 COMMENT '成团人数（快照）',
  `win_num` int(11) NOT NULL DEFAULT 0 COMMENT '中奖人数（快照）',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态：0=进行中 1=已完成（已开奖）',
  `open_time` datetime DEFAULT NULL COMMENT '开团时间',
  `win_time` datetime DEFAULT NULL COMMENT '开奖时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_open` (`activity_id`, `status`, `team_no`),
  KEY `idx_activity` (`activity_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼购团期表';

-- ------------------------------------------------------------
-- 3. 拼购参与明细表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_pingou_join` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `team_id` int(11) NOT NULL COMMENT '团期ID',
  `activity_id` int(11) NOT NULL COMMENT '活动ID',
  `uid` int(11) NOT NULL COMMENT '参与用户UID',
  `order_id` int(11) NOT NULL COMMENT '订单主键ID（eb_store_order.id）',
  `order_no` varchar(32) DEFAULT '' COMMENT '订单号',
  `product_id` int(11) NOT NULL COMMENT '拼购商品ID',
  `pay_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '参与金额（该商品行实付金额）',
  `spread_uid` int(11) NOT NULL DEFAULT 0 COMMENT '直接推荐人UID（参与时快照）',
  `is_win` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否中奖：1=中奖 0=未中奖',
  `reward_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '未中奖奖励发放状态：0=未发放 1=已发放',
  `refund_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '已退本金',
  `subsidy_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '已发补贴佣金',
  `share_rate` decimal(6,3) NOT NULL DEFAULT 0.000 COMMENT '本次分享奖励比例（‰，发给推荐人）',
  `share_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '已发分享奖励（发给推荐人）',
  `share_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '分享奖励发放状态：0=未发放 1=已发放 2=无推荐人不发',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_id`),
  KEY `idx_team` (`team_id`),
  KEY `idx_activity_uid` (`activity_id`, `uid`),
  KEY `idx_spread` (`activity_id`, `spread_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼购参与明细表';

-- ------------------------------------------------------------
-- 4. 定时兜底任务：扫描应开奖未开奖的团 / 补发奖励
-- ------------------------------------------------------------
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`)
SELECT 'PingouTeamCheckTask', 'pingouTeamCheck', '', '0 */1 * * * ?', 0, '拼购团开奖兜底任务', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name` = 'PingouTeamCheckTask' AND `is_delte` = 0);

-- ------------------------------------------------------------
-- 5. 菜单：顶级「大市场」+ 拼购系统 + 活动管理/团记录 + 按钮权限
-- ------------------------------------------------------------
-- 5.1 顶级大市场
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 0, '大市场', 'goods', '', '/market', 'M', 65, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market' AND `pid` = 0 AND `is_delte` = 0);

-- 5.2 拼购系统栏目
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '拼购系统', 'goods', '', '/market/pingou', 'M', 1, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/market' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou' AND `is_delte` = 0);

-- 5.3 活动管理
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '拼购活动', '', 'admin:pingou:activity:list', '/market/pingou/activity', 'C', 1, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market/pingou' AND p.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou/activity' AND `is_delte` = 0);

-- 5.4 团记录
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '拼购团记录', '', 'admin:pingou:team:list', '/market/pingou/team', 'C', 2, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market/pingou' AND p.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou/team' AND `is_delte` = 0);

-- 5.5 按钮权限
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '保存拼购活动', '', 'admin:pingou:activity:save', NULL, 'A', 3, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/activity' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:activity:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '修改拼购活动', '', 'admin:pingou:activity:update', NULL, 'A', 4, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/activity' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:activity:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '删除拼购活动', '', 'admin:pingou:activity:delete', NULL, 'A', 5, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/activity' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:activity:delete' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '修改拼购活动状态', '', 'admin:pingou:activity:status', NULL, 'A', 6, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/activity' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:activity:status' AND `is_delte` = 0);

-- ----------------------------
-- 拼购模块开关（控制面板·功能开关·模式推广）默认开启
-- ----------------------------
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time)
SELECT 'sys_switch_pingou', '隐藏面板-拼购开关', 0, '1', 0, NOW(), NOW()
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM eb_system_config) x WHERE x.name='sys_switch_pingou');

-- ------------------------------------------------------------
-- 6. 拼购等级设置（全局默认分享奖励阶梯）
--    活动未单独设置分享奖励阶梯时，结算按本表等级比例兜底
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_pingou_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '等级名称（如 A级）',
  `min_direct_num` int(11) NOT NULL DEFAULT 1 COMMENT '直推人数下限（含）',
  `reward_rate` decimal(6,3) NOT NULL DEFAULT 5.000 COMMENT '分享奖励比例（%，按被推荐人参与金额计算）',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越大越靠前）',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼购等级设置表（全局默认分享奖励阶梯）';

-- 默认等级：仅表内无有效数据时插入一条 A 级（直推≥1人 5%）
INSERT INTO `eb_pingou_level` (`name`, `min_direct_num`, `reward_rate`, `sort`)
SELECT 'A级', 1, 5.000, 1
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM `eb_pingou_level` WHERE `is_del` = 0) t);

-- 6.1 菜单：大市场 → 拼购系统 → 拼购等级
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '拼购等级', '', 'admin:pingou:level:list', '/market/pingou/level', 'C', 3, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market/pingou' AND p.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou/level' AND `is_delte` = 0);

-- 6.2 按钮权限
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '保存拼购等级', '', 'admin:pingou:level:save', NULL, 'A', 4, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/level' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:level:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '修改拼购等级', '', 'admin:pingou:level:update', NULL, 'A', 5, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/level' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:level:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT c.id, '删除拼购等级', '', 'admin:pingou:level:delete', NULL, 'A', 6, 0, 0
FROM `eb_system_menu` c
WHERE c.component = '/market/pingou/level' AND c.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:pingou:level:delete' AND `is_delte` = 0);
