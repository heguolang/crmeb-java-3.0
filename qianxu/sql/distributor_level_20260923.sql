-- =============================================================
-- 分销商等级（原「会员返佣配置」重构）
--   1) 新建独立的分销等级表 eb_distributor_level，不再依赖 eb_system_user_level
--   2) 按现有会员等级与返佣配置预置初始数据，保证平滑过渡
--   3) 菜单「会员返佣配置」改名为「分销商等级」，同步前端路由
-- MySQL 5.7 兼容，全部幂等，可重复执行
-- =============================================================

SET NAMES utf8mb4;

-- -------------------------------------------------------------
-- 1. 分销商等级表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `eb_distributor_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '分销等级名称',
  `grade` int NOT NULL DEFAULT '1' COMMENT '等级权重，数值越大等级越高',
  `self_brokerage_rate` int NOT NULL DEFAULT '0' COMMENT '自购返佣比例(%)',
  `brokerage_rate_one` int NOT NULL DEFAULT '0' COMMENT '一级返佣比例(%)',
  `brokerage_rate_two` int NOT NULL DEFAULT '0' COMMENT '二级返佣比例(%)',
  `direct_user_count` int NOT NULL DEFAULT '0' COMMENT '升级条件-直推会员人数(人)',
  `direct_user_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推会员人数条件关系：1=与，2=或',
  `team_user_count` int NOT NULL DEFAULT '0' COMMENT '升级条件-团队会员人数(人)',
  `team_user_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '团队会员人数条件关系：1=与，2=或',
  `direct_level_id` int NOT NULL DEFAULT '0' COMMENT '升级条件-直推指定等级ID，0=未启用',
  `direct_level_count` int NOT NULL DEFAULT '0' COMMENT '升级条件-直推指定等级人数(人)',
  `direct_level_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推指定等级条件关系：1=与，2=或',
  `total_consume_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-累计商城总消费额(元)',
  `total_consume_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '累计商城总消费额条件关系：1=与，2=或',
  `total_recharge_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-总充值额(元)',
  `total_recharge_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '总充值额条件关系：1=与，2=或',
  `team_product_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-团队商品总消费额(元)',
  `team_product_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '团队商品总消费额条件关系：1=与，2=或',
  `direct_consume_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-直推商城消费总额(元)',
  `direct_consume_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推商城消费总额条件关系：1=与，2=或',
  `direct_user_consume_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '升级条件-直推会员商城消费总额(元)',
  `direct_user_consume_relation` tinyint(1) NOT NULL DEFAULT '1' COMMENT '直推会员商城消费总额条件关系：1=与，2=或',
  `is_show` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否显示：1=显示，0=隐藏',
  `is_del` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0=否，1=是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_grade` (`grade`) USING BTREE,
  KEY `idx_is_del` (`is_del`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='分销商等级表';

-- -------------------------------------------------------------
-- 2. 预置初始数据：按现有会员等级 + 其返佣配置迁移（幂等，按名称去重）
--    返佣配置取该等级在 eb_system_user_level_brokerage 里最新的一条
--    （不区分是否软删，保证老配置的数值不丢）
-- -------------------------------------------------------------
INSERT INTO `eb_distributor_level`
  (`name`, `grade`, `self_brokerage_rate`, `brokerage_rate_one`, `brokerage_rate_two`,
   `is_show`, `is_del`, `create_time`, `update_time`)
SELECT
  ul.`name`,
  ul.`grade`,
  COALESCE(b.`self_brokerage_rate`, 0),
  COALESCE(b.`brokerage_rate_one`, 0),
  COALESCE(b.`brokerage_rate_two`, 0),
  1, 0, NOW(), NOW()
FROM `eb_system_user_level` ul
LEFT JOIN (
  SELECT level_id, MAX(id) AS max_id
    FROM `eb_system_user_level_brokerage`
   GROUP BY level_id
) bm ON bm.`level_id` = ul.`id`
LEFT JOIN `eb_system_user_level_brokerage` b ON b.`id` = bm.`max_id`
WHERE ul.`is_del` = 0
  AND NOT EXISTS (
        SELECT 1 FROM `eb_distributor_level` dl WHERE dl.`name` = ul.`name` AND dl.`is_del` = 0
      );

-- -------------------------------------------------------------
-- 3. 菜单改名 + 路由同步（按 perms 幂等定位，不用自增 id）
--    perms 保持不变，避免影响角色授权与后端鉴权
-- -------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `name` = '分销商等级',
       `component` = '/distribution/distributorLevel'
 WHERE `perms` = 'admin:system:user:level:brokerage:list';

SELECT 'distributor_level patches done' AS result;
