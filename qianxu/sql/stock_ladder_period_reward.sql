-- 阶梯业绩奖励改造（2026-09-18）
-- 级差模式 → 阶梯模式：团队业绩落入阶梯区间，周期性（月度/季度/年度）一次性结算
-- 每档奖励二选一：reward(固定金额) > 0 直接发固定金额，否则按 团队业绩 × rate(比例%) 发放
-- stock_ladder_cycle 语义变更：1=月度(默认) 2=季度 3=年度

-- 幂等加列
SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_stock_ladder' AND COLUMN_NAME = 'reward'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE eb_stock_ladder ADD COLUMN reward DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT ''阶梯固定奖励金额（元）'' AFTER max_amount',
  'SELECT ''eb_stock_ladder.reward already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE eb_system_config SET title = '阶梯业绩结算周期（1=月度 2=季度 3=年度）' WHERE name = 'stock_ladder_cycle';
UPDATE eb_system_config SET title = '阶梯业绩奖励开关' WHERE name = 'stock_ladder_status';
