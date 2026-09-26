-- ============================================================
-- 2026-09-22 今日改动合集（测试环境升级用，按顺序执行）
--
-- 1) menu_restructure_20260922.sql          菜单结构调整
-- 2) fix_corrupted_config_values.sql        清洗开关配置脏引号
-- 3) store_product_group_20260922.sql       商品分组
-- 4) product_commission_config_20260922.sql 商品级佣金字段
--
-- 全部幂等。请先 USE 目标库再执行。
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1/4 BEGIN: menu_restructure_20260922.sql ==========
-- ============================================================
-- 菜单结构调整（2026-09-22）
--
-- 背景：顶部一级菜单过于拥挤，重新组织菜单层级，便于后续管理。
--
-- 内容：
--   1) 新建「运营」一级目录（component=/yunying），把 分销 / 代理 / 团队 /
--      订货 / 门店 这 5 个原一级菜单连同其全部下级功能迁移到它下面；
--   2) 原「运营」(component=/dashboard) 更名为「首页」；
--   3) 「运营」下 5 个子菜单改名 + 重排；
--   4) 「区域代理」子菜单改名 + 重排；
--   5) 「订货商」子菜单改名 + 重排，并隐藏「订货商级别设置」入口
--      （该页功能已并入「订货商设置」页面，见前端对应改动）。
--
-- 幂等：全部按 component 定位，不依赖自增 id，可重复执行。
-- 只改 name / sort / pid / is_show，不动 component 与 perms，
-- 因此不会影响任何页面路由与权限判断。
-- ============================================================
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1) 新建「运营」一级目录（已存在则跳过）
--    ⚠️ 守卫与取值都**不能带 `pid = 0`**：一旦该行 pid 被别的脚本改写
--       （例如 menu_sync 收敛父级归属），守卫就失配，每执行一次就多插一条。
--       此处只按 component + is_delte 判定，并显式把 pid 归正为 0。
-- ------------------------------------------------------------
INSERT INTO `eb_system_menu`
  (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT 0, '运营', 's-operation', NULL, '/yunying', 'M', 190, 1, 0, NOW(), NOW()
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM `eb_system_menu`
        WHERE `component` = '/yunying' AND IFNULL(`is_delte`, 0) = 0
       );

SET @yy_id := (
  SELECT `id` FROM `eb_system_menu`
   WHERE `component` = '/yunying' AND IFNULL(`is_delte`, 0) = 0
   ORDER BY `id` LIMIT 1
);

UPDATE `eb_system_menu`
   SET `pid` = 0
 WHERE `id` = @yy_id AND NOT (`pid` <=> 0);

-- 把 5 个模块迁移到「运营」下（仅移动当前仍在一级的，保证幂等）
UPDATE `eb_system_menu`
   SET `pid` = @yy_id, `update_time` = NOW()
 WHERE `pid` = 0
   AND `is_delte` = 0
   AND @yy_id IS NOT NULL
   AND `component` IN ('/distribution', '/daili', '/tuandui', '/stock', '/merchantStore');

-- ------------------------------------------------------------
-- 2) 原「运营」更名为「首页」
-- ------------------------------------------------------------
UPDATE `eb_system_menu`
   SET `name` = '首页', `update_time` = NOW()
 WHERE `pid` = 0 AND `component` = '/dashboard' AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 3) 「运营」下 5 个子菜单：改名 + 重排
--    显示顺序（自上而下）：分销商 → 团队奖 → 订货商 → 区域代理 → 门店
--    同级按 sort DESC 排列，故 sort 依次递减
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `name` = '分销商',   `sort` = 105, `update_time` = NOW() WHERE `component` = '/distribution'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '团队奖',   `sort` = 104, `update_time` = NOW() WHERE `component` = '/tuandui'       AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商',   `sort` = 103, `update_time` = NOW() WHERE `component` = '/stock'         AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '区域代理', `sort` = 102, `update_time` = NOW() WHERE `component` = '/daili'         AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '门店',     `sort` = 101, `update_time` = NOW() WHERE `component` = '/merchantStore' AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 4) 「区域代理」子菜单：改名 + 重排
--    顺序：代理商管理 → 区域代理设置 → 代理奖励明细 → 代理商变更记录
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `name` = '代理商管理',   `sort` = 4, `update_time` = NOW() WHERE `component` = '/daili/agentList'    AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '区域代理设置', `sort` = 3, `update_time` = NOW() WHERE `component` = '/daili/agentSetting' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 2, `update_time` = NOW() WHERE `component` = '/daili/agentReward' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 1, `update_time` = NOW() WHERE `component` = '/daili/changeLog'   AND `is_delte` = 0;
-- 兼容旧路由名（部分环境该页 component 为 /daili/changelog）
UPDATE `eb_system_menu` SET `sort` = 1, `update_time` = NOW() WHERE `component` = '/daili/changelog'   AND `is_delte` = 0;

-- ------------------------------------------------------------
-- 5) 「订货商」子菜单：改名 + 重排 + 隐藏级别设置入口
--    顺序：数据报表 → 订货商管理 → 订货商设置 → 商品与库存
--          → 订货商资金记录 → 换货管理 → 订货商订单 → 订货商变更记录
-- ------------------------------------------------------------
UPDATE `eb_system_menu` SET `sort` = 10, `update_time` = NOW() WHERE `component` = '/stock/report'   AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 9,  `update_time` = NOW() WHERE `component` = '/stock/agent'    AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 8,  `update_time` = NOW() WHERE `component` = '/stock/setting'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 7,  `update_time` = NOW() WHERE `component` = '/stock/product'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商资金记录', `sort` = 6, `update_time` = NOW() WHERE `component` = '/stock/reward'  AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 5,  `update_time` = NOW() WHERE `component` = '/stock/exchange' AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `name` = '订货商订单',     `sort` = 4, `update_time` = NOW() WHERE `component` = '/stock/order'   AND `is_delte` = 0;
UPDATE `eb_system_menu` SET `sort` = 3,  `update_time` = NOW() WHERE `component` = '/stock/changelog' AND `is_delte` = 0;

-- 「订货商级别设置」入口隐藏（功能已并入「订货商设置」页，权限与记录保留）
-- 注：/stock/withdraw（提现管理）原本已隐藏，此处不处理
UPDATE `eb_system_menu`
   SET `is_show` = 0, `update_time` = NOW()
 WHERE `is_delte` = 0
   AND (`component` = '/stock/level' OR (`name` = '订货商级别设置' AND `perms` LIKE 'admin:stock:level%'));

-- ------------------------------------------------------------
-- 校验：一级菜单 与「运营」下的子菜单
-- ------------------------------------------------------------
SELECT '--- 一级菜单（按显示顺序）---' AS info;
SELECT `id`, `name`, `component`, `sort`
  FROM `eb_system_menu`
 WHERE `pid` = 0 AND `is_show` = 1 AND `is_delte` = 0
 ORDER BY `sort` DESC;

SELECT '--- 「运营」下的子菜单 ---' AS info;
SELECT c.`id`, c.`name`, c.`component`, c.`sort`
  FROM `eb_system_menu` c
  JOIN `eb_system_menu` p ON c.`pid` = p.`id`
 WHERE p.`component` = '/yunying' AND c.`is_show` = 1 AND c.`is_delte` = 0
 ORDER BY c.`sort` DESC;

SELECT '--- 「订货商」下的子菜单 ---' AS info;
SELECT c.`id`, c.`name`, c.`component`, c.`sort`
  FROM `eb_system_menu` c
  JOIN `eb_system_menu` p ON c.`pid` = p.`id`
 WHERE p.`component` = '/stock' AND c.`is_show` = 1 AND c.`is_delte` = 0
 ORDER BY c.`sort` DESC;
-- ========== 1/4 END: menu_restructure_20260922.sql ==========

-- ========== 2/4 BEGIN: fix_corrupted_config_values.sql ==========
-- ============================================================
-- 修复：部分开关类配置被写成了带引号的字符串（如 value = '1' 而非 1）
--
-- 成因：原始全量包 Qianxu_v3.0.sql 中这 8 行数据就带了多余引号，
--       且 created == updated（从未有人在后台改过），属于源数据缺陷。
--
-- ⚠️ 注意（2026-09-22 补充）：只清数据不够，还会复发！
--   代码 Constants.CONFIG_FORM_SWITCH_OPEN 原值为 "'1'"（带引号），
--   与源数据配套；把数据清成干净的 1 后，该常量必须同步改为 "1"，
--   否则 equals 匹配不上 → 支付页再次出现「暂无支付方式」。
--   2026-09-22 已把常量改为 "1"（随代码提交），本脚本保留用于清洗存量脏数据。
--
-- 注意：新的全量 SQL 已修正源数据，全新安装无需执行本脚本。
--       仅用于修复按旧包安装过的存量数据库。
-- 幂等：只处理首尾同时为单引号的值，可重复执行。
-- ============================================================

SET NAMES utf8mb4;

UPDATE `eb_system_config`
SET `value` = TRIM(BOTH char(39) FROM `value`)
WHERE `value` LIKE CONCAT(char(39), '%', char(39));

-- 修复后应为：pay_weixin_open=1 / open_upgrade=0 等干净的 0|1
-- ========== 2/4 END: fix_corrupted_config_values.sql ==========

-- ========== 3/4 BEGIN: store_product_group_20260922.sql ==========
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
-- ========== 3/4 END: store_product_group_20260922.sql ==========

-- ========== 4/4 BEGIN: product_commission_config_20260922.sql ==========
-- ============================================================
-- 商品级佣金设置（2026-09-22）
--
-- 单个商品可单独配置分销/代理/订货/门店佣金；
-- 字段为空表示取全局/等级配置；显式 0 表示该商品无此项奖励。
-- 幂等：列不存在才添加。
-- ============================================================
SET NAMES utf8mb4;

-- 前置：eb_store_product 存在 slider_image varchar(2000) + flat_pattern varchar(1000)，
-- utf8mb4 下约 12000 字节，已超过 InnoDB 8126 字节行上限。若仍是 Compact 行格式，
-- 下面的 ADD COLUMN 会报 ERROR 1118 (Row size too large)。先转 Dynamic（长列走溢出页，
-- 行内只留指针），MySQL 5.7+ 默认即为此格式，安全。
SET @row_fmt := (
  SELECT ROW_FORMAT FROM information_schema.TABLES
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
);
SET @sql_alter := IF(@row_fmt <> 'Dynamic',
  'ALTER TABLE `eb_store_product` ROW_FORMAT=DYNAMIC',
  'SELECT 1');
PREPARE stmt0 FROM @sql_alter;
EXECUTE stmt0;
DEALLOCATE PREPARE stmt0;

-- MySQL 5.7 兼容：用 information_schema 判断后 ADD COLUMN
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'eb_store_product'
     AND COLUMN_NAME = 'commission_config'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `eb_store_product` ADD COLUMN `commission_config` text NULL COMMENT ''商品级佣金配置JSON：空字段取全局，0表示该商品无此项'' AFTER `is_sub`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- ========== 4/4 END: product_commission_config_20260922.sql ==========

SET FOREIGN_KEY_CHECKS = 1;
SELECT 'today_patches_20260922 done' AS result;
