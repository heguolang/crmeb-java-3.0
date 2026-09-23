-- ==================================================================
-- 商品分组：等级来源按权限拆分（会员 / 分销商 / 订货商-代理）
--            + 商品菜单调整（隐藏「添加分组」、商品分组排到商品分类下方）
--
-- 幂等，可重复执行。执行后需清 Redis 配置缓存：
--   redis-cli -a 123456 -n 7  HSET config_list <key> <val>   (仅当改了 eb_system_config 时)
--   菜单为实时查库，无需重启；但前端需重新登录/刷新以拉取新菜单。
-- ==================================================================
-- 注意：不写 USE `库名` —— 目标库由连接/命令行决定（本地 crmeb、线上 crmeb_java3）。
--       写死库名会让线上 deploy.sh 打到错库或 ERROR 1049 中断后续语句。

-- ----------------------------------------------------------------
-- 1) eb_store_product_group 新增两个等级列（幂等）
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS `pg_add_col_if_missing`;
DELIMITER $$
CREATE PROCEDURE `pg_add_col_if_missing`()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'distributor_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `distributor_level_ids` varchar(255) NOT NULL DEFAULT ''
      COMMENT '分销商等级id，逗号分隔' AFTER `user_level_ids`;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'eb_store_product_group'
                   AND COLUMN_NAME = 'stock_level_ids') THEN
    ALTER TABLE `eb_store_product_group`
      ADD COLUMN `stock_level_ids` varchar(255) NOT NULL DEFAULT ''
      COMMENT '订货商/代理商等级id（eb_stock_level），逗号分隔' AFTER `distributor_level_ids`;
  END IF;
END$$
DELIMITER ;
CALL `pg_add_col_if_missing`();
DROP PROCEDURE IF EXISTS `pg_add_col_if_missing`;

-- ----------------------------------------------------------------
-- 2) 商品菜单：隐藏左侧「添加分组」（页面内已有新建按钮，避免重复）
--    按 component 定位，不用自增 id
-- ----------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `is_show` = 0
 WHERE `is_delte` = 0
   AND `menu_type` = 'C'
   AND `component` = '/store/productGroup/edit';

-- ----------------------------------------------------------------
-- 3) 商品下菜单排序：商品管理 > 商品分类 > 商品分组 > 商品规格 > 商品评论 > 保障服务
--    （同级按 sort DESC 排列，值越大越靠上）
-- ----------------------------------------------------------------
UPDATE `eb_system_menu` SET `sort` = 6 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/index';
UPDATE `eb_system_menu` SET `sort` = 5 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/sort';
UPDATE `eb_system_menu` SET `sort` = 4 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/productGroup';
UPDATE `eb_system_menu` SET `sort` = 3 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/attr';
UPDATE `eb_system_menu` SET `sort` = 2 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/comment';
UPDATE `eb_system_menu` SET `sort` = 1 WHERE `is_delte` = 0 AND `pid` = 2 AND `component` = '/store/guarantee';

SELECT 'product_group_level_source done' AS result;
