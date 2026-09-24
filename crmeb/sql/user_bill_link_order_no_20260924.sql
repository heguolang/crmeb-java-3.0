-- ============================================================
-- 资金监控：历史购买/退款账单 link_id 从订单主键修正为真实订单号
-- 幂等：仅当 link_id 为纯数字且能关联到 eb_store_order.id 时更新
--
-- 注意：JOIN 必须用数值比较（CAST ... AS UNSIGNED），不能字符串比较。
-- eb_user_bill.link_id 与 eb_store_order.order_id 的 COLLATE 可能不同
-- （本地 utf8mb4_general_ci / 线上 utf8mb4_unicode_ci），
-- 字符串直接等号会报 ERROR 1267 Illegal mix of collations。
-- ============================================================

UPDATE eb_user_bill ub
INNER JOIN eb_store_order so ON CAST(ub.link_id AS UNSIGNED) = so.id
SET ub.link_id = so.order_id
WHERE ub.type IN ('pay_order', 'pay_product', 'pay_product_refund')
  AND ub.link_id REGEXP '^[0-9]+$'
  AND so.order_id IS NOT NULL
  AND so.order_id <> '';
