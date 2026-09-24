-- ============================================================
-- 资金监控：历史购买/退款账单 link_id 从订单主键修正为真实订单号
-- 幂等：仅当 link_id 为纯数字且能关联到 eb_store_order.id 时更新
-- ============================================================

UPDATE eb_user_bill ub
INNER JOIN eb_store_order so ON ub.link_id = CAST(so.id AS CHAR)
SET ub.link_id = so.order_id
WHERE ub.type IN ('pay_order', 'pay_product', 'pay_product_refund')
  AND ub.link_id REGEXP '^[0-9]+$'
  AND so.order_id IS NOT NULL
  AND so.order_id <> '';
