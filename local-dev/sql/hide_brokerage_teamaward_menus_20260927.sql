-- 2026-09-27 隐藏后台「佣金记录」「团队奖记录」两栏菜单
-- 背景：资金监控页（/financial/record/monitor）已并入佣金 + 团队奖流水展示，
--       原两栏与资金监控重复，后台不再单独立入口。
-- 范围：仅后台菜单显示（eb_system_menu.is_show），不删行、不动后端接口、不动会员端。
-- 回滚：UPDATE eb_system_menu SET is_show=1 WHERE id IN (108,553);

UPDATE eb_system_menu SET is_show = 0 WHERE id = 108 AND name = '佣金记录';
UPDATE eb_system_menu SET is_show = 0 WHERE id = 553 AND name = '团队奖记录';

-- 执行后需清后台菜单缓存 / 重新登录生效
