-- 非业务日志归档定时任务：超过保留天数迁入 *_archive，默认保留 30 天
SET NAMES utf8mb4;

INSERT INTO `eb_system_config` (`name`, `value`, `title`, `form_id`)
SELECT 'log_archive_keep_days', '30', '日志归档保留天数', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name` = 'log_archive_keep_days');

INSERT INTO `eb_schedule_job` (`bean_name`, `method_name`, `params`, `cron_expression`, `status`, `remark`, `is_delte`, `create_time`)
SELECT 'ArchiveLogTask', 'archiveOldLogs', '', '0 20 3 * * ?', 0, '非业务日志归档(默认保留30天)', 0, NOW()
FROM DUAL
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='eb_schedule_job')
  AND NOT EXISTS (SELECT 1 FROM `eb_schedule_job` WHERE `bean_name`='ArchiveLogTask' LIMIT 1);

UPDATE `eb_schedule_job`
   SET `method_name` = 'archiveOldLogs',
       `cron_expression` = '0 20 3 * * ?',
       `status` = 0,
       `remark` = '非业务日志归档(默认保留30天)',
       `is_delte` = 0
 WHERE `bean_name` = 'ArchiveLogTask';
