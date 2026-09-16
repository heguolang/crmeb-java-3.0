-- ============================================================
-- 需求4：未登录用户进入首页提示「去登录」弹窗 + 后台开关
-- 位置：后台 -> 设置 -> 系统基础配置 (form_id = 148)
-- 幂等：可重复执行
-- ============================================================

-- 1. 新增两个配置项（开关 + 文案）
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'login_notice_switch', 'login_notice_switch', 148, '1', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `eb_system_config`) t WHERE t.`name` = 'login_notice_switch');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'login_notice_text', 'login_notice_text', 148, '登录后即可享受完整服务，是否前往登录？', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM (SELECT * FROM `eb_system_config`) t WHERE t.`name` = 'login_notice_text');

-- 2. 在 form_id=148 的 content JSON 末尾 fields 数组中追加两个字段定义
--    el-switch   -> login_notice_switch
--    el-input    -> login_notice_text
UPDATE `eb_system_form_temp`
SET `content` = CONCAT(
      LEFT(`content`, CHAR_LENGTH(`content`) - 2),
      ',',
      '{"__config__":{"label":"未登录访问首页提示登录：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-switch","tagIcon":"switch","required":false,"tips":false,"tipsDesc":"","tipsIsLink":false,"tipsLink":"","layout":"colFormItem","span":24,"document":"https://element.eleme.cn/#/zh-CN/component/switch","formId":120,"renderKey":1762845400001,"defaultValue":"1"},"active-text":"开启","inactive-text":"关闭","active-color":"#13ce66","inactive-color":"#ff4949","active-value":"1","inactive-value":"0","disabled":false,"__vModel__":"login_notice_switch"},',
      '{"__config__":{"label":"提示登录文案：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-input","tagIcon":"input","required":false,"tips":false,"tipsDesc":"","tipsIsLink":false,"tipsLink":"","layout":"colFormItem","span":24,"document":"https://element.eleme.cn/#/zh-CN/component/input","formId":121,"renderKey":1762845400002},"__slot__":{"prepend":"","append":""},"placeholder":"请输入提示登录文案：","style":{"width":"50%"},"clearable":true,"prefix-icon":"","suffix-icon":"","maxlength":100,"show-word-limit":false,"readonly":false,"disabled":false,"__vModel__":"login_notice_text"}',
      ']}'
    )
WHERE `id` = 148
  AND `content` LIKE '%news_slides_limit%'
  AND `content` NOT LIKE '%login_notice_switch%';

-- 3. 校验
SELECT `id`, `name`, `title`, `form_id`, `value` FROM `eb_system_config` WHERE `name` IN ('login_notice_switch','login_notice_text');
