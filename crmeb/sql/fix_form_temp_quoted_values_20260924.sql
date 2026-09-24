-- ============================================================
-- 修复「下单页没有余额支付选项」（2026-09-24）
--
-- 根因：CRMEB 官方初始数据把部分系统表单（eb_system_form_temp）里
--       radio 的 value / defaultValue 写成了带单引号的 "'1'" / "'0'"
--       （Crmeb_v3.0.sql 第 11840 行 id=80「余额支付」即是）。
--       后台在「设置 → 支付配置」里点一次保存，就会把 **'1'（含引号）**
--       写进 eb_system_config.value。
--
-- 后果：
--       后端 OrderUtils.checkPayType() 做的是 .equals("1")，
--       "'1'".equals("1") == false → 余额支付判定为不支持；
--       前端 app/pages/order/order_confirm/index.vue 做的是
--       parseInt(yuePayStatus) === 1，parseInt("'1'") == NaN → payStatus=2，
--       界面上余额支付一栏直接不出现。
--
-- 本脚本做两件事，全部幂等：
--   1) 表单定义：'"1"' / '"0"' 去引号（根治，防止再次点保存又写坏）
--   2) 已污染的配置值：eb_system_config 里首尾带单引号的 value 去引号
-- ============================================================
SET NAMES utf8mb4;

-- 1) 表单定义去引号（8 个表单：67 公众号支付 / 74 物流 / 80 余额支付 / 123 秒杀 /
--    137 支付宝 / 138 APP自动升级 / 143 易联云打印 / 149 评论设置）
UPDATE `eb_system_form_temp`
   SET `content` = REPLACE(REPLACE(`content`, '"''1''"', '"1"'), '"''0''"', '"0"')
 WHERE `content` LIKE '%"''1''"%'
    OR `content` LIKE '%"''0''"%';

-- 2) 已写坏的配置值去引号（线上实测 3 条：
--    yue_pay_status='1'、ylyprint_status='1'、ylyprint_auto_status='0'）
UPDATE `eb_system_config`
   SET `value` = TRIM(BOTH '''' FROM `value`)
 WHERE `value` LIKE '''%''';
