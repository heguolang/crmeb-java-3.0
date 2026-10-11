-- ============================================================
-- 链动流水：回填佣金钱包流水关联 + 状态对齐 2026-10-11
--
-- 背景：旧实现生成 eb_chain_commission 时既恒写 status=1（待结算），
--       又没有存 brokerage_record_id，导致后台「链动佣金」列表状态失真、
--       会员端「冻结中」把已到账佣金也算进去。
--
-- 做法：用 (uid + 订单号 + 金额 + 佣金等级) 精确匹配 eb_user_brokerage_record，
--       回填 brokerage_record_id，并把状态对齐为钱包流水的真实状态：
--         2 冻结中 / 3 已结算 / 4 已失效
--       帮扶基金行（status=5，brokerage_record_id=0）无钱包流水，保持不变。
--
-- 幂等；只改关联字段与状态，不动金额。执行后：
--   SELECT status, COUNT(*), SUM(amount) FROM eb_chain_commission GROUP BY status;
-- ============================================================
SET NAMES utf8mb4;

-- 注意：eb_chain_commission 与 eb_user_brokerage_record 排序规则不同
--       （utf8mb4_0900_ai_ci vs utf8mb4_general_ci），跨表比较需显式 COLLATE。
UPDATE `eb_chain_commission` c
  JOIN (
        SELECT c2.`id` AS cid,
               MIN(r.`id`)     AS rid,
               MIN(r.`status`) AS rstatus
          FROM `eb_chain_commission` c2
          JOIN `eb_user_brokerage_record` r
            ON r.`uid`     = c2.`user_id`
           AND r.`link_id` = c2.`order_id` COLLATE utf8mb4_general_ci
           AND r.`price`   = c2.`amount`
           AND r.`brokerage_level` = CASE c2.`reward_type`
                 WHEN 'direct' THEN 20
                 WHEN 'see'    THEN 21
                 WHEN 'peer'   THEN 22
                 WHEN 'help'   THEN 23
                 ELSE NULL END
         WHERE c2.`brokerage_record_id` = 0
           AND c2.`status` <> 5
         GROUP BY c2.`id`
       ) m ON m.cid = c.`id`
   SET c.`brokerage_record_id` = m.rid,
       c.`status` = CASE m.rstatus
                      WHEN 3 THEN 3
                      WHEN 2 THEN 2
                      WHEN 4 THEN 4
                      ELSE c.`status` END,
       c.`update_time` = NOW();

-- 校验
SELECT '--- 链动流水状态分布 ---' AS info;
SELECT `status`, COUNT(*) AS c, SUM(`amount`) AS amt
  FROM `eb_chain_commission` GROUP BY `status` ORDER BY `status`;
SELECT '--- 未回填关联的行（帮扶基金行属正常）---' AS info;
SELECT `status`, COUNT(*) AS c FROM `eb_chain_commission`
 WHERE `brokerage_record_id` = 0 GROUP BY `status`;
