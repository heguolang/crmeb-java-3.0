-- ============================================================================
-- 系统运维菜单（仅 qxtec 登录时由后端下发，其他管理员不可见）
-- 幂等：只补插、不删除。旧写法是「DELETE 两行 + INSERT 两行」，每执行一次补丁就
--       换一批自增 id，既让菜单 id 漂移，又让 eb_system_role_menu 里的既有授权
--       全部指向已删除的旧 id，每次都留下 2 条孤儿授权。
-- 注意：名称 / 图标 / 排序的「最终显示值」由 menu_sync_20260923.sql 负责收敛，
--       本脚本只在菜单缺失时按下列初始值补建，不覆盖已有行的显示属性。
-- ============================================================================

-- 1) 顶级目录 /hidden（缺失才建）
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '系统', 'warning', '', '/hidden', 'M', 88, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/hidden');

SET @hidden_pid := (SELECT `id` FROM `eb_system_menu`
                     WHERE `component` = '/hidden' ORDER BY `id` LIMIT 1);

-- 2) 子菜单 /hidden/panel（缺失才建）
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @hidden_pid, '运维面板', '', '', '/hidden/panel', 'C', 0, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @hidden_pid IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/hidden/panel');

-- 3) 修正子菜单归属（只有真的挂错父级时才写，避免无谓刷新 update_time）
UPDATE `eb_system_menu`
   SET `pid` = @hidden_pid
 WHERE `component` = '/hidden/panel'
   AND @hidden_pid IS NOT NULL
   AND NOT (`pid` <=> @hidden_pid);
