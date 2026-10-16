-- ============================================================
-- AI 配置：第三方接口设置下新增「AI配置」页（DeepSeek）
-- 涉及：eb_system_form_temp / eb_category / eb_system_config
-- 幂等：可重复执行
-- ============================================================
SET NAMES utf8mb4;

-- 1) 表单模板
INSERT INTO `eb_system_form_temp` (`name`, `info`, `content`, `create_time`, `update_time`)
SELECT
  'AI助手配置',
  'DeepSeek AI 助手配置',
  '{"formRef":"elForm","formModel":"formData","size":"medium","labelPosition":"right","labelWidth":160,"formRules":"rules","gutter":15,"disabled":false,"span":24,"formBtns":true,"fields":[{"__config__":{"label":"启用AI助手：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-radio-group","tagIcon":"radio","layout":"colFormItem","span":24,"optionType":"default","regList":[],"required":false,"tips":false,"tipsDesc":"关闭后后台 AI 助手不可用","formId":901,"renderKey":1760600009001,"defaultValue":"1"},"__slot__":{"options":[{"label":"开启","value":"1"},{"label":"关闭","value":"0"}]},"style":{},"size":"medium","disabled":false,"__vModel__":"deepseek_enabled"},{"__config__":{"label":"DeepSeek API Key：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-input","tagIcon":"input","required":false,"layout":"colFormItem","span":24,"document":"https://platform.deepseek.com","regList":[],"tips":true,"tipsDesc":"在 DeepSeek 开放平台创建并填写，保存后优先生效（未填则回退到服务器 application.yml）","formId":902,"renderKey":1760600009002},"__slot__":{"prepend":"","append":""},"placeholder":"sk-xxxxxxxx","style":{"width":"60%"},"clearable":true,"show-password":true,"maxlength":128,"show-word-limit":false,"readonly":false,"disabled":false,"__vModel__":"deepseek_api_key"},{"__config__":{"label":"模型名称：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-input","tagIcon":"input","required":false,"layout":"colFormItem","span":24,"regList":[],"tips":true,"tipsDesc":"默认 deepseek-chat","formId":903,"renderKey":1760600009003},"__slot__":{"prepend":"","append":""},"placeholder":"deepseek-chat","style":{"width":"40%"},"clearable":true,"disabled":false,"__vModel__":"deepseek_model"},{"__config__":{"label":"接口地址：","labelWidth":null,"showLabel":true,"changeTag":true,"tag":"el-input","tagIcon":"input","required":false,"layout":"colFormItem","span":24,"regList":[],"tips":true,"tipsDesc":"一般无需修改","formId":904,"renderKey":1760600009004},"__slot__":{"prepend":"","append":""},"placeholder":"https://api.deepseek.com/chat/completions","style":{"width":"60%"},"clearable":true,"disabled":false,"__vModel__":"deepseek_api_url"}]}',
  NOW(),
  NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `eb_system_form_temp` WHERE `name` = 'AI助手配置'
);

SET @aiFormId := (SELECT `id` FROM `eb_system_form_temp` WHERE `name` = 'AI助手配置' ORDER BY `id` DESC LIMIT 1);

-- 2) 挂到「第三方接口设置」分类下
SET @thirdPid := (
  SELECT `id` FROM `eb_category`
   WHERE `type` = 6 AND `status` = 1
     AND (`name` = '第三方接口设置' OR `name` LIKE '第三方接口%')
   ORDER BY CASE WHEN `name` = '第三方接口设置' THEN 0 ELSE 1 END, `id`
   LIMIT 1
);

SET @parentPath := (SELECT IFNULL(`path`, '/0/') FROM `eb_category` WHERE `id` = @thirdPid LIMIT 1);

INSERT INTO `eb_category`
  (`pid`, `path`, `name`, `type`, `url`, `extra`, `status`, `sort`, `create_time`, `update_time`)
SELECT
  @thirdPid,
  CONCAT(@parentPath, @thirdPid, '/'),
  'AI配置',
  6,
  'ai_deepseek',
  CAST(@aiFormId AS CHAR),
  1,
  99,
  NOW(),
  NOW()
FROM DUAL
WHERE @thirdPid IS NOT NULL
  AND @aiFormId IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `eb_category`
     WHERE `type` = 6 AND `name` = 'AI配置'
       AND (`pid` = @thirdPid OR `url` = 'ai_deepseek')
  );

-- 若此前已插入但 extra 为空/错误，纠正表单关联
-- 用数值比较，避免 utf8mb4_general_ci / utf8mb4_0900_ai_ci 混用报错
UPDATE `eb_category`
   SET `extra` = CAST(@aiFormId AS CHAR CHARSET utf8mb4) COLLATE utf8mb4_general_ci,
       `update_time` = NOW()
 WHERE `type` = 6 AND `name` = 'AI配置' AND `url` = 'ai_deepseek'
   AND @aiFormId IS NOT NULL
   AND (
     `extra` IS NULL OR `extra` = ''
     OR CAST(`extra` AS UNSIGNED) <> @aiFormId
   );

-- 3) 配置项
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'deepseek_enabled', '启用AI助手', @aiFormId, '1', 0, NOW(), NOW()
  FROM DUAL
 WHERE @aiFormId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'deepseek_enabled');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'deepseek_api_key', 'DeepSeek API Key', @aiFormId, '', 0, NOW(), NOW()
  FROM DUAL
 WHERE @aiFormId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'deepseek_api_key');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'deepseek_model', 'DeepSeek模型', @aiFormId, 'deepseek-chat', 0, NOW(), NOW()
  FROM DUAL
 WHERE @aiFormId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'deepseek_model');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'deepseek_api_url', 'DeepSeek接口地址', @aiFormId, 'https://api.deepseek.com/chat/completions', 0, NOW(), NOW()
  FROM DUAL
 WHERE @aiFormId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'deepseek_api_url');

UPDATE `eb_system_config`
   SET `form_id` = @aiFormId, `update_time` = NOW()
 WHERE `name` IN ('deepseek_enabled', 'deepseek_api_key', 'deepseek_model', 'deepseek_api_url')
   AND @aiFormId IS NOT NULL
   AND (`form_id` IS NULL OR `form_id` = 0 OR `form_id` <> @aiFormId);

SELECT
  @thirdPid AS third_party_category_id,
  @aiFormId AS ai_form_id,
  (SELECT COUNT(*) FROM `eb_category` WHERE `name` = 'AI配置' AND `type` = 6) AS ai_category_count;
