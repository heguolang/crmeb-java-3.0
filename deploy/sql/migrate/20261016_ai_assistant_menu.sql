-- ============================================================
-- AI 助手菜单（DeepSeek 问答页）
-- 幂等：可重复执行
-- ============================================================
SET NAMES utf8mb4;

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, 'AI助手', 'service', 'admin:ai:chat', '/ai/assistant', 'C', 2, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (
   SELECT 1 FROM `eb_system_menu`
    WHERE `component` = '/ai/assistant' AND `is_delte` = 0
 );

-- 给所有已有角色授权该菜单
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
  FROM `eb_system_role` r
  CROSS JOIN `eb_system_menu` m
 WHERE m.`component` = '/ai/assistant'
   AND m.`is_delte` = 0
   AND NOT EXISTS (
     SELECT 1 FROM `eb_system_role_menu` x
      WHERE x.`rid` = r.`id` AND x.`menu_id` = m.`id`
   );

SELECT 'ai assistant menu done' AS msg;
