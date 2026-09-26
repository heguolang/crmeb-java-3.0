-- ============================================================
-- 本地开发：把「图片上传域名」切到本机局域网 IP
-- 用法：改下面一行 @LOCAL_IP 即可（ipconfig 查 IPv4）
-- 执行：D:/env/mysql-8.0.29-winx64/bin/mysql.exe -uroot -p123456 qianxu < local_upload_domain.sql
--
-- ⚠️ 必须做第二步：清 Redis 配置缓存，否则接口仍返回旧域名
--    redis-cli -a 123456 -n 7  DEL config_list
--    redis-cli -a 123456 -n 10 DEL config_list
--    （或直接重启 Qianxu-admin.jar / Qianxu-front.jar）
-- ============================================================

SET @LOCAL_IP = '192.168.1.199';
SET @LOCAL_UPLOAD = CONCAT('http://', @LOCAL_IP, ':8080');

-- 1) 数据库：所有重名记录一起改
UPDATE `eb_system_config`
SET `value` = @LOCAL_UPLOAD, `update_time` = NOW()
WHERE `name` = 'localUploadUrl';

-- 2) 去重，只保留 id 最大的一条
--    （重名会导致后台保存报「配置名称存在多个」，必须清）
DELETE FROM `eb_system_config`
WHERE `name` = 'localUploadUrl'
  AND `id` NOT IN (SELECT * FROM (SELECT MAX(`id`) FROM `eb_system_config` WHERE `name` = 'localUploadUrl') t);

-- 3) 校验
SELECT `id`, `name`, `value` FROM `eb_system_config` WHERE `name` = 'localUploadUrl';
