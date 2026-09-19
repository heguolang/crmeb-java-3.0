-- 隐藏面板营销开关（幂等可重复执行）
-- 积分/秒杀/砍价/拼团/优惠券（status=0 为有效配置, value 1=开启 0=关闭）
DELETE FROM eb_system_config WHERE name IN
('sys_switch_integral','sys_switch_seckill','sys_switch_bargain','sys_switch_combination','sys_switch_coupon');
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time) VALUES
('sys_switch_integral','隐藏面板-积分开关',0,'1',0,NOW(),NOW()),
('sys_switch_seckill','隐藏面板-秒杀开关',0,'1',0,NOW(),NOW()),
('sys_switch_bargain','隐藏面板-砍价开关',0,'1',0,NOW(),NOW()),
('sys_switch_combination','隐藏面板-拼团开关',0,'1',0,NOW(),NOW()),
('sys_switch_coupon','隐藏面板-优惠券开关',0,'1',0,NOW(),NOW());
