-- 移除一号通，切换阿里云短信（模拟默认开启）
-- 执行库：业务库（如 qianxu）

-- 1) 阿里云短信配置（mock=1 时仅打日志，验证码仍写入 Redis，本地可联调）
INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'aliyun_sms_mock', '阿里云短信模拟发送', 0, '1', 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE `name` = 'aliyun_sms_mock');

INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'aliyun_sms_access_key_id', '阿里云短信AccessKeyId', 0, '', 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE `name` = 'aliyun_sms_access_key_id');

INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'aliyun_sms_access_key_secret', '阿里云短信AccessKeySecret', 0, '', 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE `name` = 'aliyun_sms_access_key_secret');

INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'aliyun_sms_sign_name', '阿里云短信签名', 0, '黔序商城', 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE `name` = 'aliyun_sms_sign_name');

INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'aliyun_sms_region_id', '阿里云短信地域', 0, 'cn-hangzhou', 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE `name` = 'aliyun_sms_region_id');

INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'aliyun_sms_verify_template_code', '阿里云验证码模板CODE', 0, 'SMS_MOCK_VERIFY', 1, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM eb_system_config WHERE `name` = 'aliyun_sms_verify_template_code');

-- 2) 强制物流走阿里云、商品采集走 99Api、关闭电子面单
UPDATE eb_system_config SET `value` = '2', `update_time` = NOW() WHERE `name` = 'logistics_type';
UPDATE eb_system_config SET `value` = '2', `update_time` = NOW() WHERE `name` = 'system_product_copy_type';
UPDATE eb_system_config SET `value` = '2', `update_time` = NOW() WHERE `name` = 'config_export_open';

-- 3) 清理一号通 access/secret（若存在）
UPDATE eb_system_config SET `value` = '', `update_time` = NOW() WHERE `name` IN ('access_key', 'secret_key');

-- 4) 隐藏一号通相关菜单
UPDATE eb_system_menu SET `is_show` = 0, `update_time` = NOW()
WHERE `component` IN ('/operation/onePass', '/operation/onePassConfig')
   OR `perms` LIKE 'admin:pass:%'
   OR `name` LIKE '%一号通%';
