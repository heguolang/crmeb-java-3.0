-- ============================================================
-- 订货系统 数据库脚本（幂等，可重复执行）
-- 功能：微商逐级拿货代理模式 —— 层级树/拿货价/订货订单（上级审核）
--       + 云仓库存 + 换货 + 奖励体系（差价/级差/平级）+ 提现
-- ============================================================

-- 1. 层级配置表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '层级名称（总代/一级代理/二级代理/普通代理）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（小=高层级，用于限制发展下级）',
  `discount` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '默认拿货折扣%（0=完全按价格表，无默认折扣）',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-代理层级配置';

INSERT INTO `eb_stock_level` (`name`, `sort`, `discount`)
SELECT t.name, t.sort, t.discount FROM (
  SELECT '总代' AS name, 10 AS sort, 80.00 AS discount
  UNION ALL SELECT '一级代理', 20, 85.00
  UNION ALL SELECT '二级代理', 30, 90.00
  UNION ALL SELECT '普通代理', 40, 95.00
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_stock_level` l WHERE l.name = t.name);

-- 2. 订货代理表（树形）-----------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_agent` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `level_id` int NOT NULL COMMENT '层级ID（eb_stock_level.id）',
  `parent_id` int NOT NULL DEFAULT '0' COMMENT '上级代理ID（0=上级为总部/平台）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用 1=启用',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_level` (`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-订货代理（树形层级）';

-- 3. 商品拿货价表 ----------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_price` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` int NOT NULL COMMENT '商品ID（eb_store_product.id）',
  `level_id` int NOT NULL COMMENT '层级ID',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '该层级拿货价',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_level` (`product_id`,`level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-商品层级拿货价';

-- 4. 订货订单表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_order` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) NOT NULL COMMENT '订货单号',
  `uid` int NOT NULL COMMENT '下单代理用户UID',
  `agent_id` int NOT NULL COMMENT '下单代理ID（eb_stock_agent.id）',
  `parent_agent_id` int NOT NULL DEFAULT '0' COMMENT '直接上级代理ID（0=上级为总部）',
  `level_name` varchar(32) NOT NULL DEFAULT '' COMMENT '下单时层级名称（冗余）',
  `total_num` int NOT NULL DEFAULT '0' COMMENT '商品总数量',
  `total_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总额（按下单者拿货价）',
  `pay_type` tinyint NOT NULL DEFAULT '2' COMMENT '付款方式：1=微信线上支付 2=后台记账欠款',
  `pay_status` tinyint NOT NULL DEFAULT '0' COMMENT '付款状态：0=未付款 1=已付款',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待上级审核 1=待付款 2=待发货 3=待收货 4=已完成 -1=上级驳回',
  `reject_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
  `audit_time` datetime DEFAULT NULL COMMENT '上级审核时间',
  `pay_time` datetime DEFAULT NULL COMMENT '付款时间',
  `send_time` datetime DEFAULT NULL COMMENT '总部发货时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `express_name` varchar(64) NOT NULL DEFAULT '' COMMENT '快递公司',
  `express_num` varchar(64) NOT NULL DEFAULT '' COMMENT '快递单号',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '订单备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_uid` (`uid`),
  KEY `idx_parent` (`parent_agent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-订货订单';

-- 5. 订货订单明细表 --------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_order_product` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` int NOT NULL COMMENT '订货订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) NOT NULL DEFAULT '' COMMENT '商品名称（冗余）',
  `image` varchar(256) NOT NULL DEFAULT '' COMMENT '商品图（冗余）',
  `num` int NOT NULL DEFAULT '1' COMMENT '数量',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '下单者拿货价（单价）',
  `parent_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '上级拿货价（单价，0=上级为总部无差价）',
  `total_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '小计 = num * price',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-订货订单明细';

-- 6. 换货单表 --------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_exchange` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exchange_no` varchar(32) NOT NULL COMMENT '换货单号',
  `uid` int NOT NULL COMMENT '申请代理用户UID',
  `agent_id` int NOT NULL COMMENT '申请代理ID',
  `parent_agent_id` int NOT NULL DEFAULT '0' COMMENT '直接上级代理ID',
  `order_id` int NOT NULL COMMENT '关联订货订单ID',
  `product_id` int NOT NULL COMMENT '商品ID',
  `product_name` varchar(128) NOT NULL DEFAULT '' COMMENT '商品名称（冗余）',
  `num` int NOT NULL DEFAULT '1' COMMENT '换货数量',
  `reason` varchar(255) NOT NULL DEFAULT '' COMMENT '换货原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待上级审核 1=待总部审核 2=待旧品退回 3=待发新品 4=已完成 -1=驳回',
  `reject_reason` varchar(255) NOT NULL DEFAULT '' COMMENT '驳回原因',
  `back_express_name` varchar(64) NOT NULL DEFAULT '' COMMENT '旧品退回快递公司',
  `back_express_num` varchar(64) NOT NULL DEFAULT '' COMMENT '旧品退回快递单号',
  `new_express_name` varchar(64) NOT NULL DEFAULT '' COMMENT '新品发出快递公司',
  `new_express_num` varchar(64) NOT NULL DEFAULT '' COMMENT '新品发出快递单号',
  `audit_time` datetime DEFAULT NULL COMMENT '上级审核时间',
  `hq_audit_time` datetime DEFAULT NULL COMMENT '总部审核时间',
  `back_time` datetime DEFAULT NULL COMMENT '旧品核验入库时间（回补库存）',
  `send_time` datetime DEFAULT NULL COMMENT '新品发出时间（扣库存）',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_del` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exchange_no` (`exchange_no`),
  KEY `idx_uid` (`uid`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-换货单';

-- 7. 奖金明细表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_reward` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '得奖代理用户UID',
  `type` tinyint NOT NULL COMMENT '奖励类型：1=差价奖励 2=团队级差奖励 3=平级奖励',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '业绩来源：1=订货单 2=换货单',
  `order_no` varchar(32) NOT NULL DEFAULT '' COMMENT '关联单号（订货单号/换货单号）',
  `link_uid` int NOT NULL DEFAULT '0' COMMENT '产生业绩的下级用户UID',
  `base_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '计算基数（业绩金额/差价基数）',
  `rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '比例（%）',
  `reward_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '奖励金额',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '说明',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1=已入账 -1=已失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_order` (`order_no`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-奖金明细';

-- 8. 级差阶梯表 ------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_ladder` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `min_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩下限（含）',
  `max_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '团队业绩上限（0=不限）',
  `rate` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '奖励比例（%）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-团队级差阶梯';

INSERT INTO `eb_stock_ladder` (`min_amount`, `max_amount`, `rate`, `sort`)
SELECT t.min_amount, t.max_amount, t.rate, t.sort FROM (
  SELECT 0 AS min_amount, 10000 AS max_amount, 2.00 AS rate, 1 AS sort
  UNION ALL SELECT 10000, 50000, 5.00, 2
  UNION ALL SELECT 50000, 0, 8.00, 3
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_stock_ladder`);

-- 9. 提现表 ----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_withdraw` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '代理用户UID',
  `withdraw_no` varchar(32) NOT NULL COMMENT '提现单号',
  `price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '提现金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0=待审核 1=已打款 -1=驳回',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '申请备注（收款方式等）',
  `audit_mark` varchar(255) NOT NULL DEFAULT '' COMMENT '审核备注',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-奖金提现';

-- 10. 库存变动日志表 -------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` int NOT NULL COMMENT '商品ID',
  `type` tinyint NOT NULL COMMENT '类型：1=审核通过扣库存 2=驳回回补 3=换货新品扣 4=换货旧品回补 5=手动调整',
  `change_num` int NOT NULL DEFAULT '0' COMMENT '变动数量（±）',
  `before_stock` int NOT NULL DEFAULT '0' COMMENT '变动前库存',
  `after_stock` int NOT NULL DEFAULT '0' COMMENT '变动后库存',
  `link_no` varchar(32) NOT NULL DEFAULT '' COMMENT '关联单号',
  `mark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-云仓库存变动日志';

-- 11. 消息通知表 -----------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_stock_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int NOT NULL COMMENT '接收用户UID',
  `type` tinyint NOT NULL COMMENT '类型：1=订单审核 2=发货通知 3=奖金到账 4=提现审核',
  `title` varchar(64) NOT NULL DEFAULT '' COMMENT '标题',
  `content` varchar(500) NOT NULL DEFAULT '' COMMENT '内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0=未读 1=已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-消息通知';

-- 12. 配置项 ---------------------------------------------------
-- stock_order_audit        订单上级审核开关   1=必须上级审核 0=直接流转总部
-- stock_diff_reward_status 差价奖励开关       1=开启 0=关闭
-- stock_exchange_diff      换货单参与差价奖励 1=参与 0=不参与
-- stock_ladder_status      级差奖励开关       1=开启 0=关闭
-- stock_ladder_cycle       级差结算周期       1=按订单结算 2=按月统计结算
-- stock_peer_status        平级奖励开关       1=开启 0=关闭
-- stock_peer_rate          平级奖励比例（%）  默认5
-- stock_peer_generations   平级奖励代数       默认1（只拿直接平推同级）
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`)
SELECT t.name, t.title, 0, t.value, 0 FROM (
  SELECT 'stock_order_audit'       AS name, '订货订单上级审核开关' AS title, '1' AS value
  UNION ALL SELECT 'stock_diff_reward_status', '订货差价奖励开关', '1'
  UNION ALL SELECT 'stock_exchange_diff',      '换货单参与差价奖励', '0'
  UNION ALL SELECT 'stock_ladder_status',      '订货级差奖励开关', '1'
  UNION ALL SELECT 'stock_ladder_cycle',       '订货级差结算周期', '1'
  UNION ALL SELECT 'stock_peer_status',        '订货平级奖励开关', '1'
  UNION ALL SELECT 'stock_peer_rate',          '订货平级奖励比例', '5'
  UNION ALL SELECT 'stock_peer_generations',   '订货平级奖励代数', '1'
) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` c WHERE c.name = t.name);

-- 13. 后台菜单 -------------------------------------------------
-- 13.1 顶级目录「订货」
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT 0, '订货', 'shopping-cart', '', '/stock', 'M', 95, 1
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `pid`=0 AND `component`='/stock' AND `menu_type`='M');

-- 13.2 页面菜单
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT m.id, t.name, '', t.perms, t.component, 'C', t.sort, 1
FROM `eb_system_menu` m
JOIN (
  SELECT '订货代理' AS name, 'admin:stock:agent:list'      AS perms, '/stock/agent'    AS component, 1 AS sort
  UNION ALL SELECT '商品与库存', 'admin:stock:product:list', '/stock/product', 2
  UNION ALL SELECT '订货订单',   'admin:stock:order:list',   '/stock/order',   3
  UNION ALL SELECT '换货管理',   'admin:stock:exchange:list','/stock/exchange',4
  UNION ALL SELECT '奖金明细',   'admin:stock:reward:list',  '/stock/reward',  5
  UNION ALL SELECT '提现管理',   'admin:stock:withdraw:list','/stock/withdraw',6
  UNION ALL SELECT '奖励规则',   'admin:stock:setting:list', '/stock/setting', 7
  UNION ALL SELECT '数据报表',   'admin:stock:report:list',  '/stock/report',  8
) t
WHERE m.component='/stock' AND m.menu_type='M'
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.component = t.component AND x.menu_type='C');

-- 13.3 按钮权限
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`)
SELECT p.id, t.name, '', t.perms, '', 'A', t.sort, 0
FROM `eb_system_menu` p
JOIN (
  SELECT '订货代理' AS page, '新增代理' AS name, 'admin:stock:agent:save'      AS perms, 1 AS sort
  UNION ALL SELECT '订货代理', '修改代理',   'admin:stock:agent:update',    2
  UNION ALL SELECT '订货代理', '删除代理',   'admin:stock:agent:delete',    3
  UNION ALL SELECT '商品与库存', '设置拿货价', 'admin:stock:price:save',    1
  UNION ALL SELECT '商品与库存', '调整库存',   'admin:stock:log:adjust',    2
  UNION ALL SELECT '订货订单', '审核订单',   'admin:stock:order:audit',     1
  UNION ALL SELECT '订货订单', '确认收款',   'admin:stock:order:pay',       2
  UNION ALL SELECT '订货订单', '订单发货',   'admin:stock:order:send',      3
  UNION ALL SELECT '换货管理', '换货审核',   'admin:stock:exchange:audit',  1
  UNION ALL SELECT '换货管理', '换货入库',   'admin:stock:exchange:back',   2
  UNION ALL SELECT '换货管理', '换货发货',   'admin:stock:exchange:send',   3
  UNION ALL SELECT '提现管理', '提现审核',   'admin:stock:withdraw:audit',  1
  UNION ALL SELECT '奖励规则', '保存规则',   'admin:stock:setting:save',    1
  UNION ALL SELECT '数据报表', '导出报表',   'admin:stock:report:export',   1
) t
JOIN `eb_system_menu` c ON c.component = CASE t.page
    WHEN '订货代理' THEN '/stock/agent'
    WHEN '商品与库存' THEN '/stock/product'
    WHEN '订货订单' THEN '/stock/order'
    WHEN '换货管理' THEN '/stock/exchange'
    WHEN '提现管理' THEN '/stock/withdraw'
    WHEN '奖励规则' THEN '/stock/setting'
    WHEN '数据报表' THEN '/stock/report' END
  AND c.menu_type='C'
WHERE p.id = c.id
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` x WHERE x.perms = t.perms AND x.menu_type='A');
