-- 隐藏面板营销开关（幂等可重复执行）
-- 积分/秒杀/砍价/拼团/优惠券（status=0 为有效配置, value 1=开启 0=关闭）
-- 原来的写法是 DELETE + INSERT，每跑一次就换一批自增 id、update_time 也跟着变；
-- 改成 UPDATE + 缺失才补插，重复执行不再产生任何变化。
UPDATE eb_system_config
   SET title = '隐藏面板-积分开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_integral'
   AND NOT (title <=> '隐藏面板-积分开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-秒杀开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_seckill'
   AND NOT (title <=> '隐藏面板-秒杀开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-砍价开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_bargain'
   AND NOT (title <=> '隐藏面板-砍价开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-拼团开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_combination'
   AND NOT (title <=> '隐藏面板-拼团开关' AND value <=> '1' AND status <=> 0);
UPDATE eb_system_config
   SET title = '隐藏面板-优惠券开关', value = '1', status = 0, update_time = NOW()
 WHERE name = 'sys_switch_coupon'
   AND NOT (title <=> '隐藏面板-优惠券开关' AND value <=> '1' AND status <=> 0);
INSERT INTO eb_system_config (name, title, form_id, value, status, create_time, update_time)
SELECT t.name, t.title, 0, '1', 0, NOW(), NOW() FROM (
  SELECT 'sys_switch_integral' AS name, '隐藏面板-积分开关' AS title
  UNION ALL SELECT 'sys_switch_seckill',    '隐藏面板-秒杀开关'
  UNION ALL SELECT 'sys_switch_bargain',    '隐藏面板-砍价开关'
  UNION ALL SELECT 'sys_switch_combination','隐藏面板-拼团开关'
  UNION ALL SELECT 'sys_switch_coupon',     '隐藏面板-优惠券开关'
) t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT name FROM eb_system_config) x WHERE x.name = t.name);
