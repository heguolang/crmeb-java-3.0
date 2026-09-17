-- ============================================================
-- 部署环境配置（幂等，可重复执行）
-- 说明：CRMEB 初始库里这些配置是占位符（https://111111），
--       不修改会导致图片不显示、接口请求打到错误地址。
-- ★ 部署到服务器时，把下面的 127.0.0.1 换成实际域名/IP，端口按实际调整：
--     8080 = 管理端 API（后端 admin）
--     8081 = 移动端 API（后端 front）
-- ============================================================

-- 1) 后台管理端接口 + 图片访问根地址（影响后台图片显示）
UPDATE `eb_system_config` SET `value` = 'http://127.0.0.1:8080'
WHERE `name` IN ('api_url', 'localUploadUrl') AND `value` <> 'http://127.0.0.1:8080';

-- 2) 会员端/移动端接口地址（H5、小程序请求地址）
UPDATE `eb_system_config` SET `value` = 'http://127.0.0.1:8081'
WHERE `name` IN ('front_api_url', 'site_url') AND `value` <> 'http://127.0.0.1:8081';

-- 3) 站点名称
UPDATE `eb_system_config` SET `value` = '黔序科技-Java'
WHERE `name` = 'site_name' AND `value` <> '黔序科技-Java';

-- 4) 备案号链接（页脚展示）
UPDATE `eb_system_config` SET `value` = 'www.qianxutec.com'
WHERE `name` = 'copyright_internet_record_url' AND `value` <> 'www.qianxutec.com';

-- 说明：以下支付/第三方域名仍为初始化占位符，按需在后台自行配置：
--   ali_pay_*（支付宝）、GETEWAY_URL（一号通网关）、
--   alUploadUrl / qnUploadUrl / txUploadUrl / jdUploadUrl（OSS 外链）、
--   yzf_h5_url（易支付）
-- 这些在本地/内网环境用不到，保持原值不影响主流程。
