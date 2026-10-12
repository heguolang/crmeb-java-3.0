-- ============================================================
-- N+1链动菜单：从「分销商」独立到「大市场」下
--
-- 目标结构：
--   大市场                  ← 一级（优先复用已有「大市场」或 /yunying）
--     └─ N+1链动 (/chain)  ← 二级目录
--          ├─ 链动看板 /chain/dashboard
--          ├─ 链动配置 /chain/config
--          ├─ 链动用户 /chain/users
--          ├─ 链动佣金 /chain/commission
--          ├─ 滑落日志 /chain/slide
--          ├─ 关系审计 /chain/audit
--          ├─ 风控日志 /chain/risk
--          └─ 团队分红 /chain/dividend
--
-- 幂等：可重复执行。
-- ============================================================
SET NAMES utf8mb4;

-- 1) 定位/创建「大市场」一级目录
SET @marketId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `is_delte` = 0 AND `pid` = 0 AND `menu_type` = 'M'
     AND (`name` = '大市场' OR `component` = '/yunying')
   ORDER BY CASE WHEN `name` = '大市场' THEN 0 ELSE 1 END, `id`
   LIMIT 1
);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '大市场', 's-goods', NULL, '/yunying', 'M', 106, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @marketId IS NULL;

SET @marketId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `is_delte` = 0 AND `pid` = 0 AND `menu_type` = 'M'
     AND (`name` = '大市场' OR `component` = '/yunying')
   ORDER BY CASE WHEN `name` = '大市场' THEN 0 ELSE 1 END, `id`
   LIMIT 1
);

-- 统一显示名（不改别人已有的 component，避免和生产不一致）
UPDATE `eb_system_menu`
   SET `name` = '大市场', `is_show` = 1
 WHERE `id` = @marketId;

-- 2) N+1链动二级目录
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @marketId, 'N+1链动', NULL, NULL, '/chain', 'M', 110, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @marketId IS NOT NULL
   AND NOT EXISTS (
       SELECT 1 FROM `eb_system_menu`
        WHERE `component` = '/chain' AND `is_delte` = 0
       );

UPDATE `eb_system_menu`
   SET `pid` = @marketId, `name` = 'N+1链动', `is_show` = 1, `sort` = 110, `menu_type` = 'M'
 WHERE `component` = '/chain' AND `is_delte` = 0 AND @marketId IS NOT NULL;

SET @chainDirId := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/chain' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

-- 3) 旧 /distribution/chain* → /chain/*，并挂到 N+1链动下
UPDATE `eb_system_menu`
   SET `component` = '/chain/dashboard', `pid` = @chainDirId, `name` = '链动看板', `sort` = 80, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainDashboard', '/chain/dashboard');

UPDATE `eb_system_menu`
   SET `component` = '/chain/config', `pid` = @chainDirId, `name` = '链动配置', `sort` = 70, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainConfig', '/chain/config');

UPDATE `eb_system_menu`
   SET `component` = '/chain/users', `pid` = @chainDirId, `name` = '链动用户', `sort` = 60, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainUsers', '/chain/users');

UPDATE `eb_system_menu`
   SET `component` = '/chain/commission', `pid` = @chainDirId, `name` = '链动佣金', `sort` = 50, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainCommission', '/chain/commission');

UPDATE `eb_system_menu`
   SET `component` = '/chain/slide', `pid` = @chainDirId, `name` = '滑落日志', `sort` = 40, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainSlide', '/chain/slide');

UPDATE `eb_system_menu`
   SET `component` = '/chain/audit', `pid` = @chainDirId, `name` = '关系审计', `sort` = 30, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainAudit', '/chain/audit');

UPDATE `eb_system_menu`
   SET `component` = '/chain/risk', `pid` = @chainDirId, `name` = '风控日志', `sort` = 20, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainRisk', '/chain/risk');

UPDATE `eb_system_menu`
   SET `component` = '/chain/dividend', `pid` = @chainDirId, `name` = '团队分红', `sort` = 10, `is_show` = 1
 WHERE `is_delte` = 0 AND @chainDirId IS NOT NULL
   AND `component` IN ('/distribution/chainDividend', '/chain/dividend');

-- 4) 全新环境补齐叶子（已存在则跳过）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动看板', NULL, 'admin:chain:config', '/chain/dashboard', 'C', 80, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dashboard' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动配置', NULL, 'admin:chain:config', '/chain/config', 'C', 70, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/config' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动用户', NULL, 'admin:chain:users', '/chain/users', 'C', 60, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/users' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '链动佣金', NULL, 'admin:chain:commission', '/chain/commission', 'C', 50, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/commission' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '滑落日志', NULL, 'admin:chain:slide', '/chain/slide', 'C', 40, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/slide' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '关系审计', NULL, 'admin:chain:users', '/chain/audit', 'C', 30, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/audit' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '风控日志', NULL, 'admin:chain:risk', '/chain/risk', 'C', 20, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/risk' AND `is_delte` = 0);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @chainDirId, '团队分红', NULL, 'admin:chain:dividend', '/chain/dividend', 'C', 10, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chainDirId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dividend' AND `is_delte` = 0);

-- 5) 分销下若还有旧链动路径残留，隐藏
UPDATE `eb_system_menu`
   SET `is_show` = 0
 WHERE `is_delte` = 0
   AND `component` LIKE '/distribution/chain%';
