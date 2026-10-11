-- ============================================================
-- 链动流水：奖励类型归一化 2026-10-11
--
-- 背景：旧实现把代数后缀写进了 reward_type（see1/see2/see3、peer1/peer2、
--       see_help1/see_help2），而文档口径已收敛为「见点奖 1 级、平级奖 1 级」，
--       新代码只写 see / peer / see_help / peer_help。
--       后缀残留会让后台与会员端的类型筛选漏掉历史记录，故统一去掉后缀。
--
-- 只改字符串、不动金额与状态，不产生资金影响。幂等。
-- ============================================================
SET NAMES utf8mb4;

UPDATE `eb_chain_commission` SET `reward_type` = 'see_help'  WHERE `reward_type` REGEXP '^see_help[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'peer_help' WHERE `reward_type` REGEXP '^peer_help[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'see'       WHERE `reward_type` REGEXP '^see[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'peer'      WHERE `reward_type` REGEXP '^peer[0-9]+$';
UPDATE `eb_chain_commission` SET `reward_type` = 'direct'    WHERE `reward_type` REGEXP '^direct[0-9]+$';

-- 校验
SELECT `reward_type`, `status`, COUNT(*) AS c, SUM(`amount`) AS amt
  FROM `eb_chain_commission` GROUP BY `reward_type`, `status` ORDER BY `reward_type`, `status`;
