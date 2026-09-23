-- ==================================================================
-- 商品分组：权限口径调整（区域代理等级 + 社群团队等级）
--
-- 背景：
--   permission_type 原为 all / promoter / agent / stock_agent，
--   其中 agent（原「仅代理商」）复用了 eb_stock_level 的等级列表，
--   而「代理商」在系统里实际是独立模块「区域代理」（eb_agent），
--   其等级是固定枚举：1=省级(省代) 2=市级(市代) 3=区级(区代)。
--
-- 本次调整：
--   1) agent 语义明确为「仅区域代理」，等级改用固定枚举 1/2/3，
--      落到新列 agent_level_ids（不再用 eb_stock_level）；
--   2) 新增权限类型 team「仅社群团队」，等级来源为
--      后台「运营 - 团队等级配置」→ eb_system_team_level.id，
--      落到新列 team_level_ids。
--
-- 字段语义：
--   * agent_level_ids：区域代理等级 1=省代 2=市代 3=区代，逗号分隔，空=不限等级
--   * team_level_ids ：社群团队等级（eb_system_team_level.id），逗号分隔，空=不限等级
--
-- 注意：agent 类型不再读 stock_level_ids（值域不同，1/2/3 vs eb_stock_level.id），
--      变更前已存在的 agent 分组需在后台重新选择等级。
--
-- 幂等，可重复执行。
-- ==================================================================
USE `crmeb`;

-- ----------------------------------------------------------------
-- 1) 新增 agent_level_ids（区域代理等级）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_agent_level_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_agent_level_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'agent_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `agent_level_ids` varchar(64) NOT NULL DEFAULT ''
      COMMENT '区域代理等级（eb_agent.level：1=省代 2=市代 3=区代），逗号分隔' AFTER `distributor_level_ids`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_agent_level_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_agent_level_col_if_missing`;

-- ----------------------------------------------------------------
-- 2) 新增 team_level_ids（社群团队等级）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_team_level_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_team_level_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'team_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `team_level_ids` varchar(255) NOT NULL DEFAULT ''
      COMMENT '社群团队等级id（eb_system_team_level.id），逗号分隔' AFTER `stock_level_ids`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_team_level_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_team_level_col_if_missing`;

-- ----------------------------------------------------------------
-- 3) 刷新 permission_type / stock_level_ids 列注释（幂等）
-- ----------------------------------------------------------------
ALTER TABLE `eb_store_product_group`
  MODIFY COLUMN `permission_type` varchar(32) NOT NULL DEFAULT 'all'
  COMMENT '权限：all全部会员/promoter仅分销商/agent仅区域代理/stock_agent仅订货商/team仅社群团队';

ALTER TABLE `eb_store_product_group`
  MODIFY COLUMN `stock_level_ids` varchar(255) NOT NULL DEFAULT ''
  COMMENT '订货商等级id（eb_stock_level），逗号分隔';

SELECT 'product_group_permission done' AS result;
