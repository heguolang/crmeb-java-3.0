-- ----------------------------------------------------------------------
-- 补历史「虚拟库存入账」流水（2026-09-21）
-- 背景：虚拟采购单入账（creditVirtualStock）此前只加 eb_stock_virtual_stock.remain_num，
--       从不写 eb_stock_adjust_log，导致会员端「库存记录-虚拟库存」只有提货/转卖出库，
--       看不到采购入库。代码已修（入账时补写流水），本脚本为修复前已入账的历史单补数。
--
-- 幂等：同一订单已有入账流水（stock_type=2 且 num>0 且 mark 含该单号）则跳过，可重复执行。
-- 口径：只补「已完成(status=4)」的虚拟采购单（order_type=1 且 stock_type=2），
--       这些单在付款/审核通过时即已入账；未完成单本就不该有入账流水。
-- ----------------------------------------------------------------------

INSERT INTO eb_stock_adjust_log
    (agent_id, uid, link_uid, product_id, sku_key, stock_type, num, mark, is_del, create_time)
SELECT * FROM (
    SELECT o.agent_id,
           o.uid,
           0 AS link_uid,
           p.product_id,
           IFNULL(p.sku_key, '') AS sku_key,
           2 AS stock_type,
           p.num,
           CONCAT('虚拟库存入账（订单 ', o.order_no, '），本次增加 ', p.num) AS mark,
           0 AS is_del,
           o.create_time
      FROM eb_stock_order o
      JOIN eb_stock_order_product p ON p.order_id = o.id
     WHERE o.is_del = 0
       AND o.order_type = 1
       AND o.stock_type = 2
       AND o.status = 4
       AND p.num > 0
       AND NOT EXISTS (
             SELECT 1 FROM eb_stock_adjust_log a
              WHERE a.agent_id = o.agent_id
                AND a.stock_type = 2
                AND a.num > 0
                AND a.is_del = 0
                AND a.mark LIKE CONCAT('%', o.order_no, '%')
           )
) t;
