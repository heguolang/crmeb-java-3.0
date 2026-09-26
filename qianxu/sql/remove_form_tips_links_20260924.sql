-- ============================================================
-- 清除系统设置表单中的第三方「点击查看详细」帮助链接
-- 表：eb_system_form_temp
-- MySQL 5.7 兼容，幂等
--
-- 说明：
-- 1) 关闭 tips / tipsIsLink，并清空「点击查看详细」文案
-- 2) tipsLink（www.qianxutec.com）需配合脚本 _clean_tips_links.ps1 清空
--    或依赖前端 Parser.vue 已不再渲染 tipsIsLink 链接
-- ============================================================

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"tips":true', '"tips":false')
WHERE `content` LIKE '%"tips":true%';

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"tipsIsLink":true', '"tipsIsLink":false')
WHERE `content` LIKE '%"tipsIsLink":true%';

UPDATE `eb_system_form_temp`
SET `content` = REPLACE(`content`, '"tipsDesc":"点击查看详细"', '"tipsDesc":""')
WHERE `content` LIKE '%点击查看详细%';
