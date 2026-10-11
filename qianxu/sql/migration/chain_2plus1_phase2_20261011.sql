-- 2+1 链动二期：风控 / 通知 / 团队分红 / 看板配置
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eb_chain_risk_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL DEFAULT 0,
  `risk_type` varchar(32) NOT NULL COMMENT 'ip/phone/uid/device',
  `risk_key` varchar(128) NOT NULL DEFAULT '',
  `hit_count` int(11) NOT NULL DEFAULT 0,
  `action` varchar(32) NOT NULL DEFAULT 'block' COMMENT 'block/warn',
  `detail` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_type_time` (`risk_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动风控日志';

CREATE TABLE IF NOT EXISTS `eb_chain_notify_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `notify_type` varchar(32) NOT NULL COMMENT 'upgrade/commission/help/dividend/risk',
  `title` varchar(128) NOT NULL DEFAULT '',
  `content` varchar(500) NOT NULL DEFAULT '',
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动站内通知';

CREATE TABLE IF NOT EXISTS `eb_chain_dividend_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period` varchar(16) NOT NULL COMMENT '账期yyyy-MM',
  `user_id` int(11) NOT NULL,
  `team_sales` decimal(14,2) NOT NULL DEFAULT 0.00,
  `ratio` decimal(8,2) NOT NULL DEFAULT 0.00,
  `amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `team_size` int(11) NOT NULL DEFAULT 0,
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1已发放 0作废',
  `mark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_period_user` (`period`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链动团队分红记录';

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_risk_status','1','链动风控开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_risk_status');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_risk_window_minutes','60','风控时间窗(分钟)',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_risk_window_minutes');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_risk_max_uid','3','同用户时间窗内礼包单上限',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_risk_max_uid');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_risk_max_phone','3','同手机时间窗内礼包单上限',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_risk_max_phone');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_risk_max_ip','8','同IP时间窗内礼包单上限',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_risk_max_ip');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_risk_max_device','5','同设备时间窗内礼包单上限',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_risk_max_device');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_notify_status','1','链动站内通知开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_notify_status');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_dividend_status','0','团队分红开关',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_dividend_status');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_dividend_ratio','5','团队分红比例%',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_dividend_ratio');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_dividend_min_team','2','分红最低团队人数',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_dividend_min_team');

-- 通知场景（默认短信关闭，站内通知独立实现）
INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainUpgrade','链动','链动身份晋升通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainUpgrade');

INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainCommission','链动','链动佣金到账通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainCommission');

INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainHelp','链动','链动帮扶完成通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainHelp');

INSERT INTO `eb_system_notification` (`mark`,`type`,`description`,`is_wechat`,`wechat_id`,`is_routine`,`routine_id`,`is_sms`,`sms_id`,`send_type`,`create_time`)
SELECT 'chainDividend','链动','链动团队分红通知',0,0,0,0,0,0,1,NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_notification` WHERE `mark`='chainDividend');

SET @distributionMenuId := (SELECT id FROM eb_system_menu WHERE component = '/distribution' AND menu_type = 'M' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '风控日志', NULL, 'admin:chain:risk', '/distribution/chainRisk', 'C', 25, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainRisk' LIMIT 1);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`, `create_time`, `update_time`)
SELECT @distributionMenuId, '团队分红', NULL, 'admin:chain:dividend', '/distribution/chainDividend', 'C', 26, 1, 0, NOW(), NOW()
FROM DUAL
WHERE @distributionMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `component` = '/distribution/chainDividend' LIMIT 1);

INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`, `create_time`)
SELECT 'ChainDividendTask', 'runMonthly', '', '0 15 3 1 * ?', 0, '链动团队分红(每月1日)', 0, NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_schedule_job')
  AND NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name`='ChainDividendTask' LIMIT 1);
