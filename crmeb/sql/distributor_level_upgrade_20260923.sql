-- ============================================================
-- 分销商等级：接入统计 + 自动升级判定
-- 同时补：等级统计幂等流水表（团队等级/分销商等级共用）
-- 幂等：可重复执行
-- ============================================================

-- 1) eb_user 增加分销商等级落点字段
SET @exist := (SELECT COUNT(1) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'distributor_level_id');
SET @sql := IF(@exist = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `distributor_level_id` int(11) NOT NULL DEFAULT 0 COMMENT ''分销商等级ID，0=无'' AFTER `team_level`',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2) 分销商等级统计表（持久化累加，支付成功口径，退款回退）
CREATE TABLE IF NOT EXISTS `eb_user_distributor_level_stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户ID',
  `total_consume_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '累计商城总消费额（本人）',
  `direct_consume_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '直推商城消费总额（一级下级订单）',
  `direct_user_consume_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '直推会员商城消费总额（一级下级且 level>0）',
  `team_product_amount` decimal(20,2) NOT NULL DEFAULT 0.00 COMMENT '团队商品总消费额（整条推荐链下级订单）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商等级统计表';

-- 3) 分销商等级变更记录表
CREATE TABLE IF NOT EXISTS `eb_user_distributor_level` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL COMMENT '用户ID',
  `level_id` int(11) NOT NULL DEFAULT 0 COMMENT '分销商等级ID',
  `grade` int(11) NOT NULL DEFAULT 0 COMMENT '等级权重',
  `mark` varchar(255) DEFAULT '' COMMENT '备注',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁止',
  `is_del` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商等级变更记录表';

-- 4) 等级统计幂等流水表（防止同一订单重复累加/重复回退）
--    module: TEAM=团队等级 DISTRIBUTOR=分销商等级
--    scene : PAID=支付成功 COMPLETE=订单完成 REFUND=退款回退
CREATE TABLE IF NOT EXISTS `eb_level_stat_order_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `module` varchar(32) NOT NULL COMMENT '模块：TEAM / DISTRIBUTOR',
  `scene` varchar(32) NOT NULL COMMENT '场景：PAID / COMPLETE / REFUND',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_module_scene` (`order_no`, `module`, `scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='等级统计幂等流水表';

-- 5) 配置项（不存在才插入，已存在不覆盖）
INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_enabled' AS n, '1' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_enabled');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_brokerage_enabled' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_brokerage_enabled');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'team_level_cycle_reset' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'team_level_cycle_reset');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_cycle_reset' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_cycle_reset');

INSERT INTO `eb_system_config` (`name`, `value`, `status`, `create_time`, `update_time`)
SELECT * FROM (SELECT 'distributor_level_max_depth' AS n, '0' AS v, 0 AS s, NOW() AS c, NOW() AS u) t
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'distributor_level_max_depth');

-- 6) 历史数据回填：把已支付订单的金额补进分销商统计表（幂等，只补不存在的 uid 行）
--    只初始化行，具体金额由「全量重算」接口/任务补齐，避免此处全表扫描拖慢部署
INSERT INTO `eb_user_distributor_level_stat` (`uid`, `total_consume_amount`, `direct_consume_amount`,
                                              `direct_user_consume_amount`, `team_product_amount`)
SELECT u.`uid`, 0, 0, 0, 0
FROM `eb_user` u
WHERE NOT EXISTS (SELECT 1 FROM `eb_user_distributor_level_stat` s WHERE s.`uid` = u.`uid`);

-- 7) 校验
SELECT 'eb_user.distributor_level_id' AS item,
       (SELECT COUNT(1) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'distributor_level_id') AS ok;
SELECT `name`, `value` FROM `eb_system_config`
WHERE `name` IN ('distributor_level_enabled','distributor_level_brokerage_enabled',
                 'team_level_cycle_reset','distributor_level_cycle_reset','distributor_level_max_depth');
