-- 本地默认设置快照（配置项 + 装修页）—— ⛔ 已停用，不参与线上部署
-- 生成时间: 2026-09-19 12:46:39 ／ 停用时间: 2026-09-23
--
-- 停用原因：原本用 REPLACE INTO 按主键整表覆盖，会把【线上真实配置】一并冲掉——
--   pay_weixin_app_*（微信支付商户号/密钥）、APP_PRIVATE_KEY / ALIPAY_PUBLIC_KEY、
--   sms_account / sms_token、txAccessKey / qnAccessKey / jdAccessKey 存储密钥、
--   store_brokerage_*（分销比例）等，执行后线上支付、短信、上传会立即不可用。
-- 替代方案：线上配置改由 qianxu/sql/system_settings_20260923.sql 幂等收敛
--   （只改「与厂商基库不同的差异项」，凭证类一律不同步）。
-- 如需查看停用前的原始快照：
--   git show <停用前最近一次提交>:qianxu/sql/local_default_settings.sql
SET NAMES utf8mb4;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- ⛔ 已停用：同上，会整页覆盖线上装修页（eb_page_diy）内容；如需查看原始快照见文件头。


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;