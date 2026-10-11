-- ============================================================
-- 链动后台菜单：收敛到最终形态（幂等 / 可重复执行）2026-10-11
--
-- 最终形态：挂在「大市场」→「2+1链动」目录下，共 7 项
--   链动看板 /chain/dashboard
--   链动产品 /chain/product
--   链动配置 /chain/config
--   链动用户 /chain/users
--   链动佣金 /chain/commission
--   关系变更 /chain/relation     ← 原「滑落日志」+「关系审计」合并
--   团队分红 /chain/dividend
--
-- 本段负责三件事，保证反复执行后结果一致：
--   ① 删除已废弃组件（滑落日志 / 关系审计 / 风控日志，以及早期挂在分销商下的同名菜单）
--   ② 同一 component 去重（保留最小 id）
--   ③ 补齐缺失项并回挂到「2+1链动」目录
-- 放在整个升级脚本的最后执行。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- ① 删除已废弃组件 ----------
-- 注：不要用 FIND_IN_SET + 用户变量（变量与列的 collation 不一致会报 1267）
DELETE FROM `eb_system_role_menu`
 WHERE `menu_id` IN (SELECT `id` FROM `eb_system_menu`
                      WHERE `component` IN (
                        '/chain/slide','/chain/audit','/chain/risk',
                        '/distribution/chainSlide','/distribution/chainAudit','/distribution/chainRisk',
                        '/distribution/chainDashboard','/distribution/chainProduct','/distribution/chainConfig',
                        '/distribution/chainUsers','/distribution/chainCommission','/distribution/chainDividend'));

DELETE FROM `eb_system_menu`
 WHERE `component` IN (
   '/chain/slide','/chain/audit','/chain/risk',
   '/distribution/chainSlide','/distribution/chainAudit','/distribution/chainRisk',
   '/distribution/chainDashboard','/distribution/chainProduct','/distribution/chainConfig',
   '/distribution/chainUsers','/distribution/chainCommission','/distribution/chainDividend');

-- ---------- ② 同一 component 去重（保留最小 id） ----------
DROP TEMPORARY TABLE IF EXISTS `tmp_chain_keep`;
CREATE TEMPORARY TABLE `tmp_chain_keep` (
  `component` varchar(255) NOT NULL,
  `keep_id` int NOT NULL,
  PRIMARY KEY (`component`)
);
INSERT INTO `tmp_chain_keep` (`component`, `keep_id`)
SELECT `component`, MIN(`id`) FROM `eb_system_menu`
 WHERE `component` LIKE '/chain/%'
 GROUP BY `component` HAVING COUNT(*) > 1;

DELETE rm FROM `eb_system_role_menu` rm
  JOIN `eb_system_menu` m ON m.`id` = rm.`menu_id`
  JOIN `tmp_chain_keep` k ON k.`component` = m.`component`
 WHERE m.`id` <> k.`keep_id`;

DELETE m FROM `eb_system_menu` m
  JOIN `tmp_chain_keep` k ON k.`component` = m.`component`
 WHERE m.`id` <> k.`keep_id`;

DROP TEMPORARY TABLE IF EXISTS `tmp_chain_keep`;

-- ---------- ③ 目录归位：/chain 挂在「大市场」下（找不到则退回「分销商」） ----------
SET @chain_market_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/market' ORDER BY `id` LIMIT 1);
SET @chain_fallback_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/distribution' ORDER BY `id` LIMIT 1);
SET @chain_parent_id := IFNULL(@chain_market_id, IFNULL(@chain_fallback_id, 0));
SET @chain_dir_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/chain' ORDER BY `id` LIMIT 1);

UPDATE `eb_system_menu`
   SET `name` = '2+1链动', `pid` = @chain_parent_id, `menu_type` = 'M', `sort` = 110,
       `is_show` = 1, `is_delte` = 0, `update_time` = NOW()
 WHERE `id` = @chain_dir_id AND @chain_dir_id IS NOT NULL;

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_parent_id, '2+1链动', 'clipboard', NULL, '/chain', 'M', 110, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @chain_dir_id IS NULL AND @chain_parent_id > 0;

SET @chain_dir_id := (SELECT `id` FROM `eb_system_menu` WHERE `component` = '/chain' ORDER BY `id` LIMIT 1);

-- ---------- ④ 补齐 7 个叶子项 ----------
INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动看板', NULL, 'admin:chain:config', '/chain/dashboard', 'C', 80, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dashboard');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动产品', NULL, 'admin:chain:product', '/chain/product', 'C', 75, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/product');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动配置', NULL, 'admin:chain:config', '/chain/config', 'C', 70, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/config');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动用户', NULL, 'admin:chain:users', '/chain/users', 'C', 60, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/users');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '链动佣金', NULL, 'admin:chain:commission', '/chain/commission', 'C', 50, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/commission');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '关系变更', NULL, 'admin:chain:slide', '/chain/relation', 'C', 40, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/relation');

INSERT INTO `eb_system_menu` (`pid`,`name`,`icon`,`perms`,`component`,`menu_type`,`sort`,`is_show`,`is_delte`,`create_time`,`update_time`)
SELECT @chain_dir_id, '团队分红', NULL, 'admin:chain:dividend', '/chain/dividend', 'C', 10, 1, 0, NOW(), NOW()
FROM DUAL WHERE @chain_dir_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/chain/dividend');

-- ---------- ⑤ 授权补偿：凡已授权「大市场」的角色，补授链动目录与其叶子 ----------
INSERT INTO `eb_system_role_menu` (`rid`,`menu_id`)
SELECT DISTINCT rm.`rid`, m.`id`
  FROM `eb_system_role_menu` rm
  JOIN `eb_system_menu` m ON m.`component` = '/chain' OR m.`component` LIKE '/chain/%'
 WHERE rm.`menu_id` = @chain_parent_id
   AND NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` x
                    WHERE x.`rid` = rm.`rid` AND x.`menu_id` = m.`id`);

SELECT 'chain menu finalize done' AS msg;
