# 会员等级 / 分销奖 / 团队奖 迁移说明（qianxu_java-3.0）

从 `cremb` 迁入的双轨分销能力，**未改动 cremb 任何文件**。

## 一、请先执行 SQL（按顺序）

目录：`qianxu/sql/migration/`

1. `member_level_config.sql` — 会员等级升级条件、赠送积分字段
2. `system_user_level_brokerage.sql` — 会员等级返佣表（自购/一级/二级）
3. `system_team_level.sql` — 团队等级表 + 配置表 + 团队相关菜单
4. `user_team_level.sql` — 用户 `team_level` 字段 + 统计/变更记录表
5. `team_brokerage_record_menu.sql` — 团队奖资金记录菜单（可与 3 重复，幂等）
6. `register_default_promoter_level.sql` — 注册默认推广员/默认会员等级
7. `credit_timing_config.sql` — 积分/分销佣金/团队奖到账时机
8. `user_grade_brokerage_menu.sql` — 用户等级、会员返佣配置菜单

执行后请给管理员角色勾选新菜单权限，或重新登录后台刷新权限。

## 二、业务逻辑摘要

| 轨道 | 说明 |
|------|------|
| 会员等级分销奖 | 按上级/自己匹配的会员等级读取 `eb_system_user_level_brokerage` 比例，生成自购(0)/一级(1)/二级(2) 佣金 |
| 团队奖 | 沿 `spread_uid` 向上，按团队等级极差比例 + 平级奖，记录 `brokerage_level=10/11` |
| 推广员 | 分销配置可设「注册默认推广员」「注册默认会员等级」 |
| 到账时机 | 支付即到账 / 订单完成后到账（积分、分销佣金、团队奖可分别配置） |

## 三、后台入口

- 用户 → 用户等级
- 分销 → 分销配置 / 会员返佣配置 / 团队等级 / 团队等级配置 / 团队关联用户 / 团队变更记录 / 团队奖资金记录
- 用户管理列表可手动改用户团队等级

## 四、配置建议

1. 先配会员等级 + 各等级返佣比例  
2. 再配团队等级 + 团队等级配置（极差/平级比例、开关、追溯层数）  
3. 分销配置里打开分销功能，按需设置注册默认推广员与到账时机  
