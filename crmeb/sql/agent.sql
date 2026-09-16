-- ============================================================
-- 区域代理功能 数据库脚本（幂等，可重复执行）
-- 功能：省/市/区三级区域代理 + 按订单收货地址自动发放奖励
-- ============================================================

-- 1. 区域代理表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_agent` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `level` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '代理级别：1=省级 2=市级 3=区级',
  `province` varchar(64) NOT NULL DEFAULT '' COMMENT '省',
  `city` varchar(64) NOT NULL DEFAULT '' COMMENT '市',
  `district` varchar(64) NOT NULL DEFAULT '' COMMENT '区/县',
  `region_name` varchar(192) NOT NULL DEFAULT '' COMMENT '区域全称（省市区拼接）',
  `match_key` varchar(64) NOT NULL DEFAULT '' COMMENT '订单地址匹配关键词（省名/市名/区名）',
  `ratio` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待审核 1=已通过 2=已拒绝',
  `apply_mark` varchar(255) NOT NULL DEFAULT '' COMMENT '申请说明/后台备注',
  `check_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='区域代理';

-- 2. 代理奖励明细表 --------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_agent_reward` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agent_id` int NOT NULL COMMENT '代理ID（eb_agent.id）',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `order_id` varchar(32) NOT NULL COMMENT '订单号',
  `order_pay_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单实付金额',
  `ratio` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `reward_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '奖励金额',
  `region_name` varchar(192) NOT NULL DEFAULT '' COMMENT '命中的代理区域',
  `record_id` int NOT NULL DEFAULT '0' COMMENT '关联佣金记录ID（eb_user_brokerage_record.id）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=待入账 2=已入账 3=已失效',
  `credit_time` datetime DEFAULT NULL COMMENT '入账时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_agent` (`order_id`,`agent_id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='区域代理奖励明细';

-- 3. 配置项 ----------------------------------------------------
-- agent_func_status   代理功能开关   1=开启 0=关闭
-- agent_apply_status  代理申请开关   1=开放申请 0=仅后台设置
-- agent_credit_timing 结算时机       1=订单付款成功 2=订单完成
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT t.name, t.title, 0, t.value, 0 FROM (
  SELECT 'agent_func_status'   AS name, '区域代理功能开关' AS title, '1' AS value
  UNION ALL SELECT 'agent_apply_status',   '区域代理申请开关', '1'
  UNION ALL SELECT 'agent_credit_timing',  '区域代理结算时机', '1'
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` c WHERE c.name = t.name);

-- 4. 后台菜单 --------------------------------------------------
-- 4.1 顶级目录「代理」
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT 0, '代理', 'clipboard', '', '/daili', 'M', 96, 1
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid`=0 AND `component`='/daili' AND `menu_type`='M');

-- 4.2 三个页面菜单
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT m.id, t.name, '', t.perms, t.component, 'C', t.sort, 1
FROM `eb_system_menu` m
JOIN (
  SELECT '代理管理' AS name, 'admin:agent:list'        AS perms, '/daili/agentList'   AS component, 1 AS sort
  UNION ALL SELECT '代理奖励明细', 'admin:agent:reward:list',  '/daili/agentReward', 2
  UNION ALL SELECT '代理设置',     'admin:agent:setting:list', '/daili/agentSetting', 3
) t
WHERE m.component='/daili' AND m.menu_type='M'
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.component = t.component AND x.menu_type='C');

-- 4.3 按钮权限（挂在对应页面下）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT p.id, t.name, '', t.perms, '', 'A', t.sort, 0
FROM `eb_system_menu` p
JOIN (
  SELECT '代理管理' AS page, '添加代理' AS name, 'admin:agent:save'    AS perms, 1 AS sort
  UNION ALL SELECT '代理管理', '修改代理',   'admin:agent:update', 2
  UNION ALL SELECT '代理管理', '审核代理',   'admin:agent:audit',  3
  UNION ALL SELECT '代理管理', '删除代理',   'admin:agent:delete', 4
  UNION ALL SELECT '代理管理', '省市区数据', 'admin:system:city:list:tree', 5
  UNION ALL SELECT '代理设置', '保存设置',   'admin:agent:setting:save', 1
) t
JOIN `eb_system_menu` c ON c.component = CASE t.page
    WHEN '代理管理' THEN '/daili/agentList'
    WHEN '代理设置' THEN '/daili/agentSetting' END
  AND c.menu_type='C'
WHERE p.id = c.id
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.perms = t.perms AND x.menu_type='A');
