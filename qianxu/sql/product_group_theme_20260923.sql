-- ==================================================================
-- 商品分组：绑定「装修页」（eb_theme，page_type=micro）
--
-- 目的：让商品分组拥有自己的可装修落地页。
--   * 每个分组懒创建一个 eb_theme 记录（page_type=micro，内容存 home_data），
--     通过 eb_store_product_group.theme_id 绑定；
--   * 后台分组编辑页内嵌装修器，装修结果写入该 theme 记录；
--   * H5 分组落地页按 theme_id 渲染装修内容 + 展示分组商品。
--
-- 幂等，可重复执行。
-- ==================================================================
-- 注意：不写 USE `库名` —— 目标库由连接/命令行决定（本地 qianxu、线上 qianxu_java3）。
--       写死库名会让线上 deploy.sh 打到错库或 ERROR 1049 中断后续语句。

-- ----------------------------------------------------------------
-- 1) eb_store_product_group 新增 theme_id（幂等）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_theme_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_theme_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'theme_id') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `theme_id` int NOT NULL DEFAULT 0
      COMMENT '绑定的装修页ID（eb_theme.id，page_type=micro），0=未创建' AFTER `badge`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_theme_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_theme_col_if_missing`;

SELECT 'product_group_theme done' AS result;
