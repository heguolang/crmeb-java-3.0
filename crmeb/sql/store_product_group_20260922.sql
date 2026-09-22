-- ============================================================
-- 商品分组（2026-09-22）
--
-- 功能：商城商品可加入多个「商品分组」，按角色/会员分组/等级控制可见性。
--       只管商城展示与购买，不影响订货中心。
--
-- 幂等：表 IF NOT EXISTS；菜单/配置 NOT EXISTS；角色授权 NOT EXISTS。
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 1. 分组主表 ----------
CREATE TABLE IF NOT EXISTS `eb_store_product_group` (
  `id`              int          NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`            varchar(64)  NOT NULL DEFAULT '' COMMENT '分组名称',
  `permission_type` varchar(32)  NOT NULL DEFAULT 'all' COMMENT '权限：all全部/promoter分销商/agent代理商/stock_agent订货商',
  `user_group_ids`  varchar(255) NOT NULL DEFAULT '' COMMENT '会员分组id，逗号分隔，空=不限',
  `user_level_ids`  varchar(255) NOT NULL DEFAULT '' COMMENT '会员等级id，逗号分隔，空=所有等级',
  `level_only`      tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否仅限所选等级：0否 1是',
  `min_buy`         int          NOT NULL DEFAULT 1 COMMENT '起卖数',
  `limit_one`       tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否限购一件：0否 1是',
  `layout`          varchar(16)  NOT NULL DEFAULT 'double' COMMENT '布局：double双列/single单列',
  `style`           tinyint      NOT NULL DEFAULT 1 COMMENT '内容样式 1/2/3',
  `badge`           varchar(255) NOT NULL DEFAULT '' COMMENT '图片角标',
  `title_multi`     tinyint(1)   NOT NULL DEFAULT 0 COMMENT '标题多行显示',
  `sort`            int          NOT NULL DEFAULT 0 COMMENT '排序，越大越靠前',
  `status`          tinyint(1)   NOT NULL DEFAULT 1 COMMENT '状态：0关闭 1开启',
  `is_del`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_del` (`status`, `is_del`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品分组';

-- ---------- 2. 分组-商品关联 ----------
CREATE TABLE IF NOT EXISTS `eb_store_product_group_rel` (
  `id`          int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id`    int NOT NULL DEFAULT 0 COMMENT '分组id',
  `product_id`  int NOT NULL DEFAULT 0 COMMENT '商品id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_product` (`group_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分组关联';

-- ---------- 3. 全局配置 ----------
INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'product_group_other_visible', '设置分组权限后其它会员能否看到分组中商品', 0, '0', 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'product_group_other_visible');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'product_group_deny_tip_enable', '自定义不符合商品分组权限条件的提示语开关', 0, '0', 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'product_group_deny_tip_enable');

INSERT INTO `eb_system_config` (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT 'product_group_deny_tip', '不符合商品分组权限条件的提示语', 0, '您暂无权限查看该商品', 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'product_group_deny_tip');

-- ---------- 4. 菜单（挂到商品一级目录下） ----------
SET @store_pid := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `pid` = 0 AND (`component` = '/store' OR `name` = '商品') AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @store_pid, '商品分组', '', 'admin:store:product:group:list', '/store/productGroup', 'C', 85, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @store_pid IS NOT NULL
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_menu`
          WHERE `component` = '/store/productGroup' AND `is_delte` = 0
       );

SET @pg_menu_id := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/store/productGroup' AND `is_delte` = 0
   ORDER BY `id` LIMIT 1
);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组新增', '', 'admin:store:product:group:save', '', 'A', 1, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:save' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组编辑', '', 'admin:store:product:group:update', '', 'A', 2, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:update' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组删除', '', 'admin:store:product:group:delete', '', 'A', 3, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:delete' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组详情', '', 'admin:store:product:group:info', '', 'A', 4, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:info' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @pg_menu_id, '商品分组配置', '', 'admin:store:product:group:config', '', 'A', 5, 1, 0, NOW(), NOW()
  FROM DUAL WHERE @pg_menu_id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `perms` = 'admin:store:product:group:config' AND `is_delte` = 0);

INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @store_pid, '添加分组', '', 'admin:store:product:group:save', '/store/productGroup/edit', 'C', 84, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE @store_pid IS NOT NULL
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_menu`
          WHERE `component` = '/store/productGroup/edit' AND `is_delte` = 0
       );

-- ---------- 5. 超管角色授权 ----------
INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)
SELECT r.`id`, m.`id`
  FROM `eb_system_role` r
 CROSS JOIN `eb_system_menu` m
 WHERE r.`level` = 0 AND r.`status` = 1
   AND m.`is_delte` = 0
   AND (m.`perms` LIKE 'admin:store:product:group:%'
        OR m.`component` IN ('/store/productGroup', '/store/productGroup/edit'))
   AND NOT EXISTS (
         SELECT 1 FROM `eb_system_role_menu` rm
          WHERE rm.`rid` = r.`id` AND rm.`menu_id` = m.`id`
       );
