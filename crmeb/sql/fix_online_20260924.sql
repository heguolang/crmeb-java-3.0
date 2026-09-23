-- ============================================================
-- 线上库增量修复脚本（2026-09-24）  fix_online_20260924.sql
-- 范围：后台菜单治理（本次上线的全部数据库改动）
-- 依据：线上 crmeb_java3 与本地 crmeb 的 eb_system_menu 全列逐行 diff
--       （比对列：id / pid / name / component / icon / perms / menu_type / sort / is_show / is_delte）
--       结果确认只有下面 4 处差异，无其他漂移。
-- 幂等：可重复执行；条件不满足时 0 行受影响。
--
-- 修复内容：
--   1. 138 权限规则：从「设置 → 管理权限」移到「维护」下
--   2. 158 素材管理：从「维护」移到「设置 → 协议管理」下
--   3. 699 顶部菜单「系统」（qxtec 专属运维入口）排到「维护」之后
--   4. 补插 709 / 710 / 711 三条业务入口菜单 + 超管角色授权
--
-- ⛔ 不要用 02_patches_all.sql 整包跑线上：其中的 update_http_domain 会覆盖
--    线上 api_url / localUploadUrl / site_url，hidden_super_admin 会覆盖 sys_switch_*。
--    本脚本只改菜单表，无任何配置覆盖。
--
-- 执行：
--   mysql -ucrmeb_java3 -p密码 --default-character-set=utf8mb4 crmeb_java3 < fix_online_20260924.sql
-- 执行前先备份：mysqldump ... crmeb_java3 | gzip > 备份.sql.gz
-- ============================================================

SET NAMES utf8mb4;

-- ========== 1. 权限规则：移入「维护」（pid 13） ==========
-- 匹配用 component（不依赖 id 是否漂移），改前判断避免无谓写入
UPDATE `eb_system_menu` m
   SET m.`pid` = 13, m.`update_time` = NOW()
 WHERE m.`component` = '/operation/roleManager/promiseRules'
   AND m.`menu_type` = 'C'
   AND m.`pid` <> 13
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu`
                               WHERE `component` = '/maintain' AND `menu_type` = 'M') p
                WHERE p.`id` = 13);

-- ========== 2. 素材管理：移入「设置 → 协议管理」（pid 12，sort -1） ==========
UPDATE `eb_system_menu` m
   SET m.`pid` = 12, m.`sort` = -1, m.`update_time` = NOW()
 WHERE m.`component` = '/maintain/picture'
   AND m.`menu_type` = 'C'
   AND (m.`pid` <> 12 OR m.`sort` <> -1)
   AND EXISTS (SELECT 1 FROM (SELECT `id` FROM `eb_system_menu`
                               WHERE `component` = '/operation' AND `menu_type` = 'M') p
                WHERE p.`id` = 12);

-- ========== 3. 顶部菜单「系统」（qxtec 专属）：sort 88 → 55，排到「维护」之后 ==========
-- 注意：/hidden 是顶级目录（pid=0），只在此处被 Java 侧按账号过滤下发，
--       eb_system_role_menu 里没有它的授权，这是设计如此，不要补授权。
UPDATE `eb_system_menu` m
   SET m.`sort` = 55, m.`update_time` = NOW()
 WHERE m.`component` = '/hidden'
   AND m.`menu_type` = 'M'
   AND m.`sort` <> 55;

-- ========== 4. 补插三条业务入口菜单 + 超管授权 ==========
-- 幂等键用 component（不用 id）：线上 id 可能与本地不同。
-- 三条菜单含义：
--   推广海报   ← 组合数据 id=60「移动端_我的推广_分享海报」   （装修 下）
--   签到配置   ← 组合数据 id=55「移动端_我的_签到天数配置」   （营销 → 积分 下）
--   充值设置   ← 组合数据 id=62「移动端_充值金额设置」        （财务 → 财务操作 下）
-- 组合数据本身不删——新页面正是靠 gid 取数，仅后台组合数据列表里隐藏（见 admin combinedData.vue）。

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 439, '推广海报', '', '', '/design/spread_poster', 'C', 95, 1, 0
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/design/spread_poster');

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 84, '签到配置', '', '', '/marketing/integral/signin', 'C', 99998, 1, 0
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/marketing/integral/signin');

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 106, '充值设置', '', '', '/financial/commission/recharge', 'C', 0, 1, 0
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component` FROM `eb_system_menu`) x
                    WHERE x.`component` = '/financial/commission/recharge');

-- 超管角色授权（按 component 取真实 id，不写死）
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
  FROM `eb_system_role` r
 CROSS JOIN `eb_system_menu` m
 WHERE r.`level` = 0 AND r.`status` = 1
   AND m.`is_delte` = 0
   AND m.`component` IN ('/design/spread_poster',
                         '/marketing/integral/signin',
                         '/financial/commission/recharge')
   AND NOT EXISTS (SELECT 1 FROM (SELECT `rid`, `menu_id` FROM `eb_system_role_menu`) x
                    WHERE x.`rid` = r.`id` AND x.`menu_id` = m.`id`);

-- ========== 自检 ==========
SELECT 'fix_online_20260924 done' AS result,
       (SELECT COUNT(*) FROM `eb_system_menu`)                                  AS menu_total,
       (SELECT `pid`  FROM `eb_system_menu` WHERE `component` = '/operation/roleManager/promiseRules' LIMIT 1) AS rule_pid,
       (SELECT CONCAT(`pid`, '/', `sort`) FROM `eb_system_menu` WHERE `component` = '/maintain/picture' LIMIT 1) AS picture_pid_sort,
       (SELECT `sort` FROM `eb_system_menu` WHERE `component` = '/hidden' AND `menu_type` = 'M' LIMIT 1) AS hidden_sort,
       (SELECT COUNT(*) FROM `eb_system_menu` WHERE `component` IN
          ('/design/spread_poster', '/marketing/integral/signin', '/financial/commission/recharge')) AS new_entries;
