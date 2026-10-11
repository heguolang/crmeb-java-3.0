-- ============================================================
-- 链动文案统一：历史数据里的「见单奖」→「见点奖」2026-10-11
--
-- 背景：旧实现把奖励写成「见单奖第1代」「见单帮扶基金第2代」，
--       文档口径统一叫「见点奖」。新代码已写「见点奖」，此处只回补历史数据。
-- 幂等：REPLACE 只对命中的行生效，重复执行无副作用。
-- ============================================================
SET NAMES utf8mb4;

-- 1) 链动流水 remark
UPDATE `eb_chain_commission`
   SET `mark` = REPLACE(`mark`, '见单帮扶基金', '见点奖帮扶基金')
 WHERE `mark` LIKE '%见单帮扶基金%';

UPDATE `eb_chain_commission`
   SET `mark` = REPLACE(`mark`, '见单奖', '见点奖')
 WHERE `mark` LIKE '%见单奖%';

-- 2) 奖励规则 remark（后台「链动设置 - 奖励规则」展示用）
UPDATE `eb_chain_reward_rule`
   SET `remark` = '见点奖-第1代老板'
 WHERE `type` = 'see' AND `remark` LIKE '%见单奖%';

SELECT 'chain mark wording fix done' AS msg;
