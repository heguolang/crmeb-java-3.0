-- ============================================================
-- 黔序商城品牌化清洗 · 生产/本地库脚本（2026-09-26）
-- 用途：清除运行时数据中的 CRMEB / 众邦 / crmebimage 等品牌痕迹
-- 执行方式：在目标库执行一次；执行后需清 Redis 配置缓存并重启服务
-- 注意：均为幂等 UPDATE，可重复执行
-- ============================================================

-- 1) 微信分享标题/描述（用户可见）
UPDATE `eb_system_config` SET `value` = '黔序商城'
  WHERE `name` = 'wechat_share_title'
    AND (`value` LIKE '%CRMEB%' OR `value` LIKE '%crmeb%' OR `value` = 'QIANXU');

UPDATE `eb_system_config` SET `value` = '黔序商城——黔序科技旗下综合商城'
  WHERE `name` = 'wechat_share_synopsis'
    AND (`value` LIKE '%CRMEB%' OR `value` LIKE '%crmeb%' OR `value` LIKE '%QIANXU%');

-- 2) App 检查更新说明链接（原指向 CRMEB 文档站）
UPDATE `eb_system_config` SET `value` = 'https://app.qianxutec.com'
  WHERE `name` = 'app_update_url' AND `value` LIKE '%kancloud.cn%';

-- 3) 关于我们 / 文章内容中的 CRMEB 公司介绍
UPDATE `eb_article`
   SET `content` = '黔序商城是贵州黔序科技有限公司旗下多端一体化商城平台，覆盖小程序、公众号、H5、APP 多端，为用户提供商品购物、会员权益、分销订货等一站式服务。官方网址：https://www.qianxutec.com'
 WHERE `content` LIKE '%CRMEB%' OR `content` LIKE '%crmeb.com%' OR `content` LIKE '%众邦%';

-- 4) 版权 / 公司名
UPDATE `eb_system_config` SET `value` = '黔序科技'
  WHERE `name` IN ('copyright_company_name', 'routine_name')
    AND (`value` LIKE '%众邦%' OR `value` LIKE '%CRMEB%' OR `value` LIKE '%crmeb%');

UPDATE `eb_system_config` SET `value` = 'Copyright@2026 贵州黔序科技有限公司'
  WHERE `name` = 'copyright_internet_record'
    AND (`value` LIKE '%众邦%' OR `value` LIKE '%CRMEB%' OR `value` LIKE '%crmeb%');

-- 5) 装修默认素材图域名（原挂在 CRMEB 演示站 CDN）
UPDATE `eb_page_diy`
   SET `value` = REPLACE(REPLACE(`value`, 'apia.beta.crmeb.xbdzz.cn', 'api.qianxutec.com'),
                          'apia.crmeb.xbdzz.cn', 'api.qianxutec.com')
 WHERE `value` LIKE '%crmeb.xbdzz.cn%';

UPDATE `eb_page_diy`
   SET `value` = REPLACE(`value`, 'crmebimage', 'qianxuimage')
 WHERE `value` LIKE '%crmebimage%';

-- 6) 组合数据 / 系统配置中的外链与图片路径
UPDATE `eb_system_group_data`
   SET `value` = REPLACE(REPLACE(REPLACE(`value`, 'https://crmeb.com', 'https://www.qianxutec.com'),
                                 'http://crmeb.com', 'https://www.qianxutec.com'),
                         'crmebimage', 'qianxuimage')
 WHERE `value` LIKE '%crmeb.com%' OR `value` LIKE '%crmebimage%';

UPDATE `eb_system_config`
   SET `value` = REPLACE(`value`, 'crmebimage', 'qianxuimage')
 WHERE `value` LIKE '%crmebimage%';

UPDATE `eb_article`
   SET `image_input` = REPLACE(`image_input`, 'crmebimage', 'qianxuimage')
 WHERE `image_input` LIKE '%crmebimage%';

UPDATE `eb_category`
   SET `image` = REPLACE(`image`, 'crmebimage', 'qianxuimage')
 WHERE `image` LIKE '%crmebimage%';

-- 7) 后台「统计」菜单路由标识 + 配置 key
UPDATE `eb_category` SET `url` = 'qianxu_tongji'
 WHERE `url` IN ('crmeb_tongji', 'qianxu_tongji');

UPDATE `eb_system_config` SET `name` = 'qianxu_tongji_js', `title` = 'qianxu_tongji_js'
 WHERE `name` = 'crmeb_tongji_js';

-- 8) 菜单权限标识
UPDATE `eb_system_menu` SET `perms` = 'public:jsconfig:getqianxuchatconfig'
 WHERE `perms` = 'public:jsconfig:getcrmebchatconfig';

UPDATE `eb_system_menu` SET `menu_name` = 'public:jsconfig:getqianxuchatconfig'
 WHERE `menu_name` = 'public:jsconfig:getcrmebchatconfig';

-- 9) Quartz 持久化类名修复（旧包名 com.zbkj → com.qxkj）
UPDATE `QRTZ_JOB_DETAILS` SET `JOB_CLASS_NAME` = 'com.qxkj.admin.quartz.QuartzJob'
 WHERE `JOB_CLASS_NAME` = 'com.zbkj.admin.quartz.QuartzJob';

UPDATE `QRTZ_JOB_DETAILS` SET `JOB_DATA` = REPLACE(`JOB_DATA`, 'com.zbkj.', 'com.qxkj.')
 WHERE `JOB_DATA` LIKE '%com.zbkj.%';

UPDATE `QRTZ_TRIGGERS` SET `JOB_DATA` = REPLACE(`JOB_DATA`, 'com.zbkj.', 'com.qxkj.')
 WHERE `JOB_DATA` LIKE '%com.zbkj.%';

DELETE FROM `QRTZ_FIRED_TRIGGERS`;

-- 10) 复查（应尽量返回 0 行；sms.crmeb.net 为一号通外部 SaaS 可忽略）
SELECT name, value FROM `eb_system_config`
 WHERE (`value` LIKE '%crmeb%' OR `value` LIKE '%CRMEB%' OR `value` LIKE '%众邦%')
   AND `value` NOT LIKE '%sms.crmeb.net%'
 LIMIT 20;
SELECT id FROM `eb_article` WHERE `content` LIKE '%crmeb%' OR `content` LIKE '%CRMEB%' OR `content` LIKE '%众邦%' LIMIT 10;
SELECT COUNT(*) AS diy_crmebimage FROM `eb_page_diy` WHERE `value` LIKE '%crmebimage%';
SELECT COUNT(*) AS cfg_crmebimage FROM `eb_system_config` WHERE `value` LIKE '%crmebimage%';
SELECT COUNT(*) AS zbkj_jobs FROM `QRTZ_JOB_DETAILS` WHERE `JOB_DATA` LIKE '%com.zbkj%';

-- 执行完成后：
--   redis-cli -n 8 DEL config_list （及其他配置缓存库）
--   线上若仍使用库名 crmeb_java3，可择机：
--     RENAME DATABASE 不支持时请 mysqldump 后导入到 qianxu_java3，并改 application-prod.yml
--   图片目录需从 crmebimage 重命名/同步为 qianxuimage
--   重启 qianxu-admin 与 qianxu-front
