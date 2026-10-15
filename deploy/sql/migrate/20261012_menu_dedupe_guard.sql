-- 20261012_menu_dedupe_guard.sql
-- 菜单防重护栏（幂等）：同一父级(pid)下、同 component 的 C/M 菜单只保留 id 最小的一条，
-- 其余软删（is_delte=1）。与后台「删除菜单」同一语义，数据保留，改回 0 即恢复。
-- 背景：多个历史补丁各自插入「大数据中心」(/market/screen/hub)，2026-10-12 线上实测同屏出现两条。
-- 对应 qianxu/sql/01_increment.sql 中的同名分节。
--
-- ⚠️ 去重维度必须是 (pid, component)，不能只用 component：
--    「首页(M, /dashboard, pid=0)」与「控制台(C, /dashboard, pid=首页)」是合法父子菜单，
--    同 component 但 pid 不同，只用 component 去重会把「控制台」误删（2026-10-15 实测）。
--
-- 幂等：重复执行结果一致——已被软删的行不再计入 HAVING COUNT(*) > 1，第二次执行命中 0 行。

SET NAMES utf8mb4;

UPDATE `eb_system_menu` m
  JOIN (
    SELECT `pid`, `component`, MIN(`id`) AS keep_id
      FROM `eb_system_menu`
     WHERE `menu_type` IN ('C','M') AND `is_delte` = 0
       AND `component` IS NOT NULL AND `component` <> ''
     GROUP BY `pid`, `component`
    HAVING COUNT(*) > 1
  ) d ON m.`pid` = d.`pid` AND m.`component` = d.`component` AND m.`id` > d.`keep_id`
   SET m.`is_delte` = 1, m.`update_time` = NOW();
