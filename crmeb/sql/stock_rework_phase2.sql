-- =============================================================
-- 订货商模块二期改造（2026-09-18）
-- 需求2：虚拟库存 + 提货
--   下单可选虚拟库存：付款成功后虚拟库存入账（会员中心可见），不发货；
--   提货 = 用虚拟库存换实物：生成 order_type=2 提货单，总部发货扣云仓。
-- 幂等：可重复执行
-- =============================================================

-- 1. 虚拟库存表（每个用户每商品一条，入账累加 remain_num）
CREATE TABLE IF NOT EXISTS `eb_stock_virtual_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` int(11) NOT NULL COMMENT '会员UID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `product_name` varchar(255) NOT NULL DEFAULT '' COMMENT '商品名称（冗余）',
  `image` varchar(512) NOT NULL DEFAULT '' COMMENT '商品图（冗余）',
  `sku_key` varchar(64) NOT NULL DEFAULT '' COMMENT '规格标识（预留，空=商品级）',
  `num` int(11) NOT NULL DEFAULT 0 COMMENT '累计入账数量',
  `remain_num` int(11) NOT NULL DEFAULT 0 COMMENT '剩余可提货数量',
  `source_order_no` varchar(32) NOT NULL DEFAULT '' COMMENT '最近一次入账来源订货单号',
  `parent_agent_id` int(11) NOT NULL DEFAULT 0 COMMENT '入账时订单上级代理快照（提货单沿用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订货系统-会员虚拟库存';
