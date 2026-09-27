-- 2026-09-27 资金监控「订单号」历史数据修复：数字主键 → 真实单号
-- 背景：订货单余额支付/退款、换货差价支付/退款四类流水曾把 link_id 写成表主键 id，
--       资金监控列表「订单号」列显示成 53、52 这类小数字。
--       代码已改为写入真实单号（SK.../HE...），本脚本修复历史行。
-- 安全性：仅按「title + 纯数字 link_id + 能精确 JOIN 到对应单据」三重条件命中，
--         不会碰提现/充值/后台操作等无单号流水；幂等查询已兼容新旧两种 link_id。
-- 回滚：link_id 无法逆向还原（数字→单号不可逆），执行前先备份：
--       CREATE TABLE IF NOT EXISTS eb_user_bill_bak_20260927 AS
--       SELECT * FROM eb_user_bill WHERE title IN ('购买商品','订货退款','换货差价','换货差价退款')
--         AND link_id REGEXP '^[0-9]+$';

-- ① 备份（可重复执行，已存在则跳过）
CREATE TABLE IF NOT EXISTS eb_user_bill_bak_20260927 AS
SELECT * FROM eb_user_bill
WHERE title IN ('购买商品','订货退款','换货差价','换货差价退款')
  AND link_id REGEXP '^[0-9]+$';

-- ② 订货单相关：购买商品 / 订货退款 → 订货单号（SK...）
UPDATE eb_user_bill b INNER JOIN eb_stock_order so
    ON b.link_id = CONVERT(so.id, CHAR) COLLATE utf8mb4_general_ci
SET b.link_id = so.order_no
WHERE b.title IN ('购买商品','订货退款')
  AND b.link_id REGEXP '^[0-9]+$';

-- ③ 换货相关：换货差价 / 换货差价退款 → 换货单号（HE...）
UPDATE eb_user_bill b INNER JOIN eb_stock_exchange se
    ON b.link_id = CONVERT(se.id, CHAR) COLLATE utf8mb4_general_ci
SET b.link_id = se.exchange_no
WHERE b.title IN ('换货差价','换货差价退款')
  AND b.link_id REGEXP '^[0-9]+$';

-- ④ 核对（执行后应返回 0 行）
SELECT b.id, b.link_id, b.title FROM eb_user_bill b
LEFT JOIN eb_stock_order so ON b.link_id = CONVERT(so.id, CHAR) COLLATE utf8mb4_general_ci
LEFT JOIN eb_stock_exchange se ON b.link_id = CONVERT(se.id, CHAR) COLLATE utf8mb4_general_ci
WHERE b.title IN ('购买商品','订货退款','换货差价','换货差价退款')
  AND b.link_id REGEXP '^[0-9]+$';
