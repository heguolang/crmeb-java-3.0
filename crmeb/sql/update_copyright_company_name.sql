-- ============================================================
-- 需求3：后台管理界面底部版权信息改为「黔序科技」
-- 说明：后台页脚组件 admin/src/components/copyright/index.vue
--       优先取接口返回的 companyName（即本配置项），
--       取不到时回退到组件内硬编码文案（本次一并同步修改）。
-- 幂等：可重复执行
-- ============================================================

-- 更新公司名称（配置键 copyright_company_name）
UPDATE `eb_system_config`
SET `value` = '黔序科技'
WHERE `name` = 'copyright_company_name';

-- 若该配置项不存在则补一条（归属到「版权信息」表单，与同组配置保持一致）
INSERT INTO `eb_system_config` (`name`, `title`, `value`, `form_id`, `status`)
SELECT 'copyright_company_name', 'copyright_company_name', '黔序科技', 0, 1
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_config`) t
  WHERE t.`name` = 'copyright_company_name'
);

-- ---------- 校验 ----------
SELECT id, name, value, form_id
FROM `eb_system_config`
WHERE `name` = 'copyright_company_name';
