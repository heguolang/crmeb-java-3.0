-- 生产环境改为 HTTP（幂等，可重复执行）
-- 图片域名、后台/会员端接口统一走 api 站点
UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');
