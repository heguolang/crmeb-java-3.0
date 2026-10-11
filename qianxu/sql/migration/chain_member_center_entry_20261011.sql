-- ============================================================
-- 链动中心：会员中心装修入口 + 会员端页面链接登记（2026-10-11）
--
-- 背景：
--   会员中心（/pages/user/index.vue）是纯装修器驱动的页面，「代理中心」也是先在
--   装修器「导航组」里被勾中才出现在会员中心。本脚本按同一机制把「链动中心」
--   加进「导航组」的可选预设列表（show=0，需在装修器里勾选才展示），
--   与会员端页面 /pages/promoter/chain_center/index 打通。
--
-- 前置：
--   1) 会员端已发布新版本（含 chain_center / chain_commission / chain_team 三个页面）
--   2) 后台 admin 已发布（linkaddress/linkData.json 已登记「链动中心」等 4 个链接）
-- 幂等：
--   WHERE 同时判断「含锚点」且「尚未含 chain_center/index」，重复执行不会追加多条。
-- ============================================================
SET NAMES utf8mb4;

-- ① 自定义装修数据（用户保存过的个人中心装修）
UPDATE `eb_theme`
SET `user_data` = REPLACE(
        `user_data`,
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]}',
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_center/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动收益","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_commission/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动团队","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_team/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动规则","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_rules/index","tips":"请输入链接"}]}'
    )
WHERE `user_data` LIKE '%"value":"代理中心"%'
  AND `user_data` NOT LIKE '%chain_center/index%';

-- ② 主题默认装修数据（未自定义时回落到这份）
UPDATE `eb_theme`
SET `user_default_data` = REPLACE(
        `user_default_data`,
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]}',
        '{"img":"","show":false,"icon":"icon-ic_gift1","type":0,"info":[{"max":4,"title":"标题","value":"代理中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/users/agent/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动中心","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_center/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动收益","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_commission/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动团队","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_team/index","tips":"请输入链接"}]},{"img":"","show":false,"icon":"icon-ic_star","type":0,"info":[{"max":4,"title":"标题","value":"链动规则","tips":"选填，不超过4个字"},{"max":100,"title":"链接","value":"/pages/promoter/chain_rules/index","tips":"请输入链接"}]}'
    )
WHERE `user_default_data` LIKE '%"value":"代理中心"%'
  AND `user_default_data` NOT LIKE '%chain_center/index%';

-- ③ 校验：应各返回 1 行（命中说明已写入）
SELECT id, title,
       `user_data`         LIKE '%chain_center/index%' AS user_data_ok,
       `user_default_data` LIKE '%chain_center/index%' AS user_default_ok
FROM `eb_theme`
WHERE `user_data` IS NOT NULL OR `user_default_data` IS NOT NULL;
