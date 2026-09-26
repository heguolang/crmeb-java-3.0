-- 生产环境改为 HTTP（幂等，可重复执行）
-- 接口域名 / 图片域名 / 移动商城接口统一走 api 站点
-- 加「值不同才写」守卫：否则每执行一次都会刷 update_time，重复跑不幂等。
UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()
WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url')
  AND NOT (`value` <=> 'http://api.qianxutec.com');

-- ⚠️ site_url 不能和上面混在一起改：
--    它的语义是「移动端站点域名」，被微信 H5 支付当作 h5_info.wap_url 使用
--    （见 OrderPayServiceImpl#getUnifiedorderVo），必须指向 H5 部署域名，
--    否则微信 H5 支付的 wap_url 与后台配置的支付域名不一致。
UPDATE `eb_system_config` SET `value` = 'http://app.qianxutec.com', `update_time` = NOW()
WHERE `name` = 'site_url'
  AND NOT (`value` <=> 'http://app.qianxutec.com');
