-- MariaDB 兼容补丁：CRMEB 部分 SQL 使用了 MySQL 5.7 的内置函数 ANY_VALUE()，
-- MariaDB 没有该函数，会报 "FUNCTION crmeb.ANY_VALUE does not exist"。
-- 本脚本创建同名兼容函数（恒等返回）。只需执行一次，随库持久保存。
-- 适用：MariaDB 10.x（本部署 10.6.28）

USE crmeb;

CREATE FUNCTION IF NOT EXISTS ANY_VALUE(x LONGTEXT) RETURNS LONGTEXT
DETERMINISTIC NO SQL
RETURN x;
