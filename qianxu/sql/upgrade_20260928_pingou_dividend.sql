-- ============================================================
-- 增量脚本 upgrade_20260928_pingou_dividend.sql
-- 范围：拼购系统（活动/团/参与/等级 + 菜单/开关/任务）
--       + 全球分红（分红等级表/记录表/配置/菜单/任务）
--       + 拼购补贴·分享奖励流水账户错标修正
-- 说明：
--   1) 幂等可重复执行：全部走 IF NOT EXISTS / NOT EXISTS / information_schema 判断
--   2) MySQL 5.7+ 兼容（无 DELIMITER，加列走 PREPARE）
--   3) 菜单父级一律按 component 动态查找，不写死菜单 id
--   4) 本脚本只做库结构/基础数据；必须配合新版 Qianxu-admin.jar / Qianxu-front.jar 使用
-- 执行后必做：
--   a) 刷新配置缓存：POST /api/admin/system/config/clear/cache（否则新增配置读不到）
--   b) 后台重新登录（菜单缓存）
--   c) 检查 sys_switch_pingou='1'（ModuleSwitchInterceptor 按 '1' 放行拼购接口）
-- ============================================================

SET @db = DATABASE();

-- ============================================================
-- 一、拼购模块
-- ============================================================

-- 1.1 拼购活动表
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
  `share_rate_low` decimal(6,3) NOT NULL DEFAULT 5.000 COMMENT '分享奖励低比例（‰，直推人数未达阈值时，已废弃保留兼容）',
  `share_rate_high` decimal(6,3) NOT NULL DEFAULT 10.000 COMMENT '分享奖励高比例（‰，直推人数达到阈值时，已废弃保留兼容）',
  `share_rate_threshold` int(11) NOT NULL DEFAULT 10 COMMENT '高比例直推人数阈值（已废弃，保留兼容）',
  `share_tiers` text NULL COMMENT '分享奖励阶梯JSON：[{levelId拼购等级ID(可空),minDirectNum直推人数下限,rewardType(1=比例%|2=固定金额),rewardValue}]，按直推人数匹配最高一档；带levelId=该等级独立设置，缺省等级走eb_pingou_level默认比例',
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

-- 1.2 拼购团期表
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

-- 1.3 拼购参与明细表
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

-- 1.4 拼购兜底任务（满员未开奖 / 补发奖励）
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`)
SELECT 'PingouTeamCheckTask', 'pingouTeamCheck', '', '0 */1 * * * ?', 0, '拼购团开奖兜底任务', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name` = 'PingouTeamCheckTask' AND `is_delte` = 0);

-- 1.5 拼购模块开关（控制面板·功能开关，默认开启）
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'sys_switch_pingou', '隐藏面板-拼购开关', 0, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM `eb_system_config`) x WHERE x.name = 'sys_switch_pingou');

-- 1.6 顶级菜单：大市场
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 0, '大市场', 'goods', '', '/market', 'M', 65, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market' AND `pid` = 0 AND `is_delte` = 0);

-- 1.7 栏目：拼购系统
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '拼购系统', 'goods', '', '/market/pingou', 'M', 1, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/market' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou' AND `is_delte` = 0);

-- 1.8 页面：拼购活动 / 拼购团记录
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '拼购活动', '', 'admin:pingou:activity:list', '/market/pingou/activity', 'C', 1, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market/pingou' AND p.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou/activity' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '拼购团记录', '', 'admin:pingou:team:list', '/market/pingou/team', 'C', 2, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market/pingou' AND p.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou/team' AND `is_delte` = 0);

-- 1.9 页面：拼购等级（全局默认分享奖励阶梯）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT p.id, '拼购等级', '', 'admin:pingou:level:list', '/market/pingou/level', 'C', 3, 1, 0
FROM `eb_system_menu` p
WHERE p.component = '/market/pingou' AND p.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/pingou/level' AND `is_delte` = 0);

-- 1.10 按钮权限：活动
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

-- 1.11 按钮权限：拼购等级
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

-- 1.12 拼购等级设置表 + 默认 A 级（直推≥1人 5%）
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

INSERT INTO `eb_pingou_level` (`name`, `min_direct_num`, `reward_rate`, `sort`)
SELECT 'A级', 1, 5.000, 1
WHERE NOT EXISTS (SELECT 1 FROM (SELECT id FROM `eb_pingou_level` WHERE `is_del` = 0) t);

-- ============================================================
-- 二、全球分红
-- ============================================================

-- 2.1 分红等级表：比例与升级条件独立维护（条件项与分销商等级同一套，
--     可参考分销商等级配置，但达标判定基于会员自身统计，与分销商等级本身无关）
CREATE TABLE IF NOT EXISTS `eb_dividend_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '分红等级名称',
  `dividend_rate` decimal(6,3) NOT NULL DEFAULT 0.000 COMMENT '分红比例（%，按分红基数）',
  `direct_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-直推会员人数(人)',
  `direct_user_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '直推会员人数条件关系：1=与，2=或',
  `team_user_count` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-团队会员人数(人)',
  `team_user_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '团队会员人数条件关系：1=与，2=或',
  `direct_level_id` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-直推指定分销商等级ID，0=未启用',
  `direct_level_count` int(11) NOT NULL DEFAULT 0 COMMENT '升级条件-直推指定等级人数(人)',
  `direct_level_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '直推指定等级条件关系：1=与，2=或',
  `total_consume_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-累计商城总消费额(元)',
  `total_consume_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '累计商城总消费额条件关系：1=与，2=或',
  `total_recharge_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-总充值额(元)',
  `total_recharge_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '总充值额条件关系：1=与，2=或',
  `team_product_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-团队商品总消费额(元)',
  `team_product_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '团队商品总消费额条件关系：1=与，2=或',
  `direct_consume_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '升级条件-直推商城消费总额(元)',
  `direct_consume_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '直推商城消费总额条件关系：1=与，2=或',
  `order_product_ids` varchar(500) DEFAULT '' COMMENT '升级条件-下单指定商品ID，逗号分隔，空=未启用',
  `order_product_relation` tinyint(1) NOT NULL DEFAULT 1 COMMENT '下单指定商品条件关系：1=与，2=或',
  `order_product_mode` tinyint(1) NOT NULL DEFAULT 1 COMMENT '下单指定商品达成方式：1=任买一件即可，2=需全部购买',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_is_del` (`is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全球分红等级表';

-- 2.2 分红结算记录表（uk_period_no 幂等防重发）
CREATE TABLE IF NOT EXISTS `eb_dividend_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period_no` varchar(32) NOT NULL DEFAULT '' COMMENT '期号：日D20260928/周W2026-W39/月M2026-09',
  `cycle_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '周期类型：1=日，2=周，3=月',
  `base_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '分红基数：1=订单总业绩，2=订单利润',
  `base_amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '本期分红基数总额',
  `order_count` int(11) NOT NULL DEFAULT 0 COMMENT '本期参与统计订单数',
  `total_paid` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '本期分红总发放金额',
  `member_count` int(11) NOT NULL DEFAULT 0 COMMENT '本期参与分红会员人次',
  `mark` varchar(500) DEFAULT '' COMMENT '备注（每档均分明细说明）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '结算时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_no` (`period_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全球分红结算记录表';

-- 2.3 分红发放明细表（uk period_no+uid+level_id，一人可命中多档）
CREATE TABLE IF NOT EXISTS `eb_dividend_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `record_id` int(11) NOT NULL DEFAULT 0 COMMENT '结算记录ID',
  `period_no` varchar(32) NOT NULL DEFAULT '' COMMENT '期号',
  `uid` int(11) NOT NULL DEFAULT 0 COMMENT '会员uid',
  `level_id` int(11) NOT NULL DEFAULT 0 COMMENT '分红等级ID',
  `level_name` varchar(50) DEFAULT '' COMMENT '分红等级名称',
  `rate` decimal(6,3) NOT NULL DEFAULT 0.000 COMMENT '分红比例（%）',
  `amount` decimal(16,2) NOT NULL DEFAULT 0.00 COMMENT '分红金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发放时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_uid_level` (`period_no`, `uid`, `level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全球分红发放明细表';

-- 2.4 分红配置（开关默认关 / 周期默认日 / 基数默认业绩）
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'sys_dividend_status', '全球分红-开关', 0, '0', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'sys_dividend_status');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'sys_dividend_cycle', '全球分红-周期类型', 0, '1', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'sys_dividend_cycle');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT 'sys_dividend_base', '全球分红-分红基数', 0, '1', 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'sys_dividend_base');

-- 2.5 菜单：大市场 → 全球分红(目录) → 分红设置/分红等级/分红记录（父级按 component 动态查找）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT m.id, '全球分红', 's-claim', '', '/market/dividend', 'M', 2, 1, 0
FROM `eb_system_menu` m
WHERE m.component = '/market' AND m.pid = 0 AND m.is_delte = 0
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend' AND `is_delte` = 0);

SET @dividend_dir := (SELECT id FROM `eb_system_menu` WHERE `component` = '/market/dividend' AND `is_delte` = 0 LIMIT 1);

-- 2.5.1 已部署环境补图标：老版本脚本建目录时 icon 为空，NOT EXISTS 守卫下 INSERT 不会重跑，
--       必须用 UPDATE 补齐（幂等，可重复执行）
UPDATE `eb_system_menu` SET `icon` = 's-claim'
WHERE `component` = '/market/dividend' AND `is_delte` = 0 AND (`icon` IS NULL OR `icon` = '');

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @dividend_dir, '分红设置', '', 'admin:dividend:info', '/market/dividend/setting', 'C', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend/setting' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @dividend_dir, '分红等级', '', 'admin:dividend:info', '/market/dividend/level', 'C', 2, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend/level' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @dividend_dir, '分红记录', '', 'admin:dividend:info', '/market/dividend/record', 'C', 3, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/market/dividend/record' AND `is_delte` = 0);

-- 2.6 分红周期结算检查任务（每分钟检查，命中周期时间点才结算）
INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`)
SELECT 'DividendCheckTask', 'dividendCheck', '', '0 */1 * * * ?', 0, '全球分红周期结算检查任务', 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name` = 'DividendCheckTask' AND `is_delte` = 0);

-- ============================================================
-- 三、存量数据修正
-- ============================================================

-- 3.1 拼购补贴/分享奖励流水错标修正：
--     这两类钱实际进佣金账户（brokerage_price），历史版本曾把 UserBill.category 错写为 now_money，
--     导致资金监控「余额」页误统计。只修错标行，金额不动。
UPDATE `eb_user_bill`
SET `category` = 'brokerage_price'
WHERE `title` IN ('拼购未中奖补贴', '拼购分享奖励')
  AND `category` = 'now_money';
