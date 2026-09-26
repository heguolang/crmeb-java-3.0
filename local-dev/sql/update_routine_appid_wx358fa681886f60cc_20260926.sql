-- 黔序商城：小程序 appid 切换为 wx358fa681886f60cc
-- 日期：2026-09-26
-- 影响：小程序端 code2session 登录（WeChatConstants.WECHAT_MINI_APPID / WECHAT_MINI_APPSECRET）
-- 幂等：可重复执行

UPDATE `eb_system_config` SET `value` = 'wx358fa681886f60cc'        WHERE `name` = 'routine_appid';
UPDATE `eb_system_config` SET `value` = '000415eee681b6977708613adbe82f29' WHERE `name` = 'routine_appsecret';

-- 执行后必须清配置缓存，否则读的还是旧值：
--   Redis db8: DEL config_list
-- 校验：
--   SELECT name, value FROM eb_system_config WHERE name IN ('routine_appid','routine_appsecret');
