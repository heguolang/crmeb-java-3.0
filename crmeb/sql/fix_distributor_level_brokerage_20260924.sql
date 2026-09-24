-- ============================================================
-- 修复「下单后拿不到分销奖（推广佣金）」（2026-09-24）
--
-- 现象：订单支付后只生成了「获得团队极差奖」，
--       一级/二级「获得推广佣金」一条都没有。
--
-- 根因：
--   OrderPayServiceImpl.assignCommission() → resolveBrokerageRate() 取值顺序是
--     1) distributorLevelService.getBrokerageRate(uid, level)
--        该方法开头判断配置 distributor_level_brokerage_enabled != '1' 就直接返回 null；
--     2) 回落 getLevelBrokerageRate(会员等级) → 读 eb_system_user_level_brokerage
--        线上只有 level_id=1（is_del=1）和 level_id=2 两行，level_id=3（白银会员）没有，
--        → 比例取不到 → fallbackRate = 0 → 佣金算成 0 → 不生成记录。
--
-- 处理：启用「分销商等级返佣」，让推广佣金按分销商等级
--      （健康大使 一级20%、健康导师 一级30%）计算，与已建的等级体系一致。
--
-- 幂等：可重复执行。
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `value`, `status`)
SELECT 'distributor_level_brokerage_enabled', '1', 1
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_brokerage_enabled');

UPDATE `eb_system_config`
   SET `value` = '1'
 WHERE `name` = 'distributor_level_brokerage_enabled'
   AND `value` <> '1';
