-- ============================================================
-- H5 图片仍走 https 时执行本文件（宝塔导入）
-- 根因：eb_system_config.localUploadUrl 仍是 https，且 front 的
--       asyncConfig=true 会把旧值缓存在 Redis database 8
-- 执行后务必：redis-cli -n 8 DEL config_list，再重启 Crmeb-front.jar
-- ============================================================

-- 1) 图片/接口域名改 HTTP
UPDATE `eb_system_config`
SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'local_domain');

-- 移动端站点域名
UPDATE `eb_system_config`
SET `value` = 'http://app.qianxutec.com', `update_time` = NOW()
WHERE `name` = 'site_url';

-- 配置表残留 https 一律替换
UPDATE `eb_system_config`
SET `value` = REPLACE(`value`, 'https://api.qianxutec.com', 'http://api.qianxutec.com'),
    `update_time` = NOW()
WHERE `value` LIKE '%https://api.qianxutec.com%';

UPDATE `eb_system_config`
SET `value` = REPLACE(`value`, 'https://app.qianxutec.com', 'http://app.qianxutec.com'),
    `update_time` = NOW()
WHERE `value` LIKE '%https://app.qianxutec.com%';

-- 2) 附件表完整地址
UPDATE `eb_system_attachment`
SET `att_dir` = REPLACE(`att_dir`, 'https://api.qianxutec.com', 'http://api.qianxutec.com'),
    `satt_dir` = REPLACE(`satt_dir`, 'https://api.qianxutec.com', 'http://api.qianxutec.com')
WHERE `att_dir` LIKE '%https://api.qianxutec.com%'
   OR `satt_dir` LIKE '%https://api.qianxutec.com%';

-- 3) 组合数据（个人中心菜单、轮播等 JSON 里可能已写死 https）
UPDATE `eb_system_group_data`
SET `value` = REPLACE(`value`, 'https://api.qianxutec.com', 'http://api.qianxutec.com')
WHERE `value` LIKE '%https://api.qianxutec.com%';

-- 4) 校验（导入后看结果）
SELECT `name`, `value` FROM `eb_system_config`
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');
