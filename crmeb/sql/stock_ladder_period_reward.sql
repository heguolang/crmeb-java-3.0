-- 阶梯业绩奖励改造（2026-09-18）
-- 级差模式 → 阶梯固定金额+比例模式：团队业绩落在阶梯区间，周期性（月度/季度/年度）一次性结算
-- 奖励金额 = reward(固定金额) + 团队业绩 × rate(比例%)
-- stock_ladder_cycle 语义变更：1=月度(默认) 2=季度 3=年度

ALTER TABLE eb_stock_ladder
    ADD COLUMN reward DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '阶梯固定奖励金额（元）' AFTER max_amount;

UPDATE eb_system_config SET title = '阶梯业绩结算周期（1=月度 2=季度 3=年度）' WHERE name = 'stock_ladder_cycle';
UPDATE eb_system_config SET title = '阶梯业绩奖励开关' WHERE name = 'stock_ladder_status';
