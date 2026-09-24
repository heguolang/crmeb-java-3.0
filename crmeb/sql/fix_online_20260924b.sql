-- fix_online_20260924b.sql
-- 依据 2026-09-24 23:48 本地↔线上全量比对生成：
--   表结构：零差异；菜单：零差异；唯一缺差 = 注册默认分销商等级配置键。
-- 只改 eb_system_config 一行，可重复执行。禁止整包跑 02_patches_all.sql。

INSERT INTO eb_system_config (name, title, form_id, value, status)
SELECT 'register_default_distributor_level', '注册默认分销商等级', 0, '1', 0
WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE name = 'register_default_distributor_level');
