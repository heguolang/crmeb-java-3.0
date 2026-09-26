-- ============================================================
-- 黔序商城品牌化清洗 · 生产库脚本（2026-09-26）
-- 用途：清除运行时数据中的 QIANXU 品牌痕迹
-- 执行方式：在生产库执行一次；执行后需清 Redis 配置缓存并重启服务
-- 注意：均为幂等 UPDATE，可重复执行
-- ============================================================

-- 1) 微信分享标题/描述（用户可见）
UPDATE `eb_system_config` SET `value` = '黔序商城'
  WHERE `name` = 'wechat_share_title' AND `value` LIKE '%CRMEB%';

UPDATE `eb_system_config` SET `value` = '黔序商城——黔序科技旗下综合商城'
  WHERE `name` = 'wechat_share_synopsis' AND `value` LIKE '%CRMEB%';

-- 2) App 检查更新说明链接（原指向 QIANXU 文档站）
UPDATE `eb_system_config` SET `value` = 'https://app.qianxutec.com'
  WHERE `name` = 'app_update_url' AND `value` LIKE '%kancloud.cn%';

-- 3) 关于我们文章内容（原为 QIANXU 公司介绍）
UPDATE `eb_article`
   SET `content` = '黔序商城是贵州黔序科技有限公司旗下多端一体化商城平台，覆盖小程序、公众号、H5、APP 多端，为用户提供商品购物、会员权益、分销订货等一站式服务。官方网址：https://www.qianxutec.com'
 WHERE `content` LIKE '%CRMEB通过将CRM%';

-- 4) 装修默认素材图域名（原挂在 QIANXU 演示站 CDN）
--    ⚠️ 前置条件：qianxu/sql/brand_preset_images_list.txt 中的素材已转存到自己图床
UPDATE `eb_page_diy`
   SET `value` = REPLACE(`value`, 'apia.beta.crmeb.xbdzz.cn', 'api.qianxutec.com')
 WHERE `value` LIKE '%crmeb.xbdzz.cn%';

-- 5) 组合数据演示外链
UPDATE `eb_system_group_data`
   SET `value` = REPLACE(`value`, 'https://crmeb.com', 'https://www.qianxutec.com')
 WHERE `value` LIKE '%//crmeb.com%';

-- 6) 后台「统计」菜单路由标识（eb_category.url）
UPDATE `eb_category` SET `url` = 'qianxu_tongji'
 WHERE `url` = 'crmeb_tongji';

-- 7) Quartz 持久化类名修复（2026-09-26 生产部署现场补录，本地/线上都要跑）
--    背景：QRTZ_JOB_DETAILS.JOB_CLASS_NAME 列 + JOB_DATA/TRIGGERS.JOB_DATA 序列化 BLOB 内
--    嵌旧包名 com.zbkj.*，admin 启动 job recovery 报 ClassNotFoundException。
--    com.zbkj 与 com.qxkj 等长（4 字符），字节级 REPLACE 不破坏序列化流结构。
UPDATE `QRTZ_JOB_DETAILS` SET `JOB_CLASS_NAME` = 'com.qxkj.admin.quartz.QuartzJob'
 WHERE `JOB_CLASS_NAME` = 'com.zbkj.admin.quartz.QuartzJob';

UPDATE `QRTZ_JOB_DETAILS` SET `JOB_DATA` = REPLACE(`JOB_DATA`, 'com.zbkj.', 'com.qxkj.')
 WHERE `JOB_DATA` LIKE '%com.zbkj.%';

UPDATE `QRTZ_TRIGGERS` SET `JOB_DATA` = REPLACE(`JOB_DATA`, 'com.zbkj.', 'com.qxkj.')
 WHERE `JOB_DATA` LIKE '%com.zbkj.%';

--    停机期间的运行时残留（FIRE_STATE 也是序列化 blob），停机状态下清空安全
DELETE FROM `QRTZ_FIRED_TRIGGERS`;

-- 8) 复查（应返回 0 行）
SELECT name FROM `eb_system_config`
 WHERE `value` LIKE '%crmeb.net%' AND `value` NOT LIKE '%crmebimage%' LIMIT 10;
SELECT id FROM `eb_article` WHERE `content` LIKE '%crmeb.%' LIMIT 10;
SELECT COUNT(*) FROM `QRTZ_JOB_DETAILS` WHERE `JOB_DATA` LIKE '%com.zbkj%';

-- 执行完成后：
--   redis-cli -n 8 DEL config_list （及其他配置缓存库）
--   重启 qianxu-admin 与 qianxu-front 服务
--   验证：后台登录、H5 分享标题、App 关于我们
