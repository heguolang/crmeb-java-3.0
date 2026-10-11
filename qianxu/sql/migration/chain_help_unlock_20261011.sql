-- 2+1 帮扶解锁（见单冻结）：升老板后，留下的 N 个代理各直推满 X 单礼包才解锁；
-- 未解锁见单按比例进帮扶基金，解锁后解冻且以后不再冻。
SET NAMES utf8mb4;

SET @db := DATABASE();

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='eb_user' AND COLUMN_NAME='chain_help_unlocked') = 0,
  'ALTER TABLE `eb_user` ADD COLUMN `chain_help_unlocked` tinyint(4) NOT NULL DEFAULT 0 COMMENT ''老板帮扶是否解锁0否1是'' AFTER `chain_identity`',
  'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_unlock_orders','1','帮扶解锁：每位留下的代理需直推礼包单数X',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_unlock_orders');

INSERT INTO `eb_system_config` (`name`,`value`,`title`,`status`)
SELECT 'chain_help_freeze_ratio','50','未解锁时见单奖冻结进帮扶基金比例%',0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `eb_system_config` WHERE `name`='chain_help_freeze_ratio');

-- 对齐常见默认金额（仅当仍是初始化默认值时才改，避免覆盖商家已调价）
UPDATE `eb_chain_reward_rule` SET `amount`=100.00, `remark`='见单奖-第1代老板'
 WHERE `type`='see' AND `gen_level`=1 AND `amount`=50.00;

UPDATE `eb_chain_reward_rule` SET `ratio`=10.00, `remark`='平级奖占见单比例%'
 WHERE `type`='peer' AND `gen_level`=1 AND `ratio`=15.00;
