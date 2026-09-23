# 分销商升级条件 / 团队奖 / 订货商升级条件 — 检查与接入清单

> 日期：2026-09-23　代码根：`D:\crmeb-java-3.0\crmeb`
> 本次改动：分销商等级全链路接入 + 团队等级幂等/并发加固 + 订货商统计口径修正

---

## 一、三类规则现状总览

| 规则 | 统计 | 判定/升级 | 结果记录 | 退款回退 | 幂等 | 并发安全 | 周期清零 |
|---|---|---|---|---|---|---|---|
| 团队等级 | ✅ 已实现 | ✅ 已实现 | ✅ 已实现 | ✅ 已实现 | ✅ **本次补齐** | ✅ **本次补齐** | ⚠️ 可选（默认关） |
| 团队奖（极差/平级） | ✅ 已实现 | ✅ 已实现 | ✅ 已实现 | ✅ 已实现 | ⚠️ 见风险说明 | — | — |
| 订货商层级 | ⚠️ 口径 bug **已修** | ✅ 已实现 | ✅ 已实现 | ❌ 未实现（实时聚合，驳回/取消单原会误计） | ✅ 结果幂等 | ✅ **本次补齐 CAS** | ❌ 无 |
| 分销商等级 | ✅ **本次新增** | ✅ **本次新增** | ✅ **本次新增** | ✅ **本次新增** | ✅ **本次新增** | ✅ **本次新增** | ⚠️ 可选（默认关） |

---

## 二、逐项检查明细（含代码位置）

### 2.1 团队等级（`eb_system_team_level`）

| 项 | 状态 | 位置 |
|---|---|---|
| 统计表（6 个金额字段，持久化） | ✅ | `crmeb-common/.../model/user/UserTeamLevelStat.java:34-50` |
| 支付成功累计 | ✅ | `UserTeamLevelServiceImpl.java:343-353`；入口 `OrderPayServiceImpl.java:404` |
| 订单完成累计 | ✅ | `UserTeamLevelServiceImpl.java:355-362`；入口 `StoreOrderTaskServiceImpl.java:303` |
| 退款回退 | ✅ | `UserTeamLevelServiceImpl.java:364-380`；入口 `StoreOrderTaskServiceImpl.java:546` |
| 五条件链式判定 + 与/或 | ✅ | `UserTeamLevelServiceImpl.java:258-317` |
| 自动升级（取 grade 最大） | ✅ | `UserTeamLevelServiceImpl.java:127-192` |
| 等级落点 `eb_user.team_level` + 记录表 | ✅ | `User.java:113`；`UserTeamLevelServiceImpl.java:172-189` |
| **幂等去重** | ✅ **本次新增** | `UserTeamLevelServiceImpl.java:62-124`（`claimOnce`）+ `LevelStatOrderLogDao.insertIgnore` |
| **并发原子累加** | ✅ **本次新增** | `UserTeamLevelStatDao.incrColumn`（`SET col = GREATEST(col+delta,0)`） |
| 人数指标（直推/团队） | ⚠️ 实时 count，未持久化 | `UserTeamLevelServiceImpl.java:283-307`、`UserTeamLevelDao.java:32-37` |
| 只升不降 | ✅ 设计如此 | `UserTeamLevelServiceImpl.java:159-169` |

### 2.2 团队奖（极差奖 / 平级奖）

| 项 | 状态 | 位置 |
|---|---|---|
| 极差奖计算 | ✅ | `TeamBrokerageServiceImpl.java:113-124` |
| 平级奖计算 | ✅ | `TeamBrokerageServiceImpl.java:125-136` |
| 发放触发（支付成功） | ✅ | `OrderPayServiceImpl.java:311` |
| 落库 `eb_user_brokerage_record`（level 10/11） | ✅ | `OrderPayServiceImpl.java:407-412` |
| 到账时机可配 | ✅ | `OrderPayServiceImpl.java:318/348-350` |
| 退款追回（含已到账扣回余额） | ✅ | `StoreOrderTaskServiceImpl.java:484-498 / 522-537` |
| 解冻任务 | ✅ | `BrokerageFrozenTask`（cron `0 0 */1 * * ?`） |

⚠️ **遗留风险（本次未改，避免改变现有金额行为）**：支付队列重投时，团队奖仍可能在佣金记录层重复生成；
统计层已加幂等，但 `assignTeamBrokerage` 本身无去重。如需彻底堵住，建议后续按 `orderNo + uid + brokerageLevel`
在 `eb_user_brokerage_record` 上加唯一键。

### 2.3 订货商层级（`eb_stock_level`）

| 项 | 状态 | 位置 |
|---|---|---|
| 升级判定 `checkAndUpgrade` | ✅ | `StockServiceImpl.java:1507-1547` |
| 条件匹配（自购/直推/团队/指定商品，与或可配） | ✅ | `StockServiceImpl.java:1550-1596` |
| 统计（实时 SQL 聚合） | ⚠️ **口径已修** | `StockServiceImpl.java:1599-1613`（`sumPaidOrderAmount`） |
| **排除已驳回(-1)/已取消(-2) 订货单** | ✅ **本次新增** | `StockServiceImpl.java:1611`、`1629` |
| **CAS 更新层级防并发覆盖** | ✅ **本次新增** | `StockServiceImpl.java:1539-1542` + `StockAgentDao.updateLevelCas` |
| 变更流水 `eb_stock_change_log` | ✅ | `StockServiceImpl.java:1541` |
| 触发点（付款/上级审核/总部审核/确认收款） | ✅ | `StockOrderServiceImpl.java:335 / 894 / 961 / 1565` |
| 退款/取消后业绩回退 | ❌ 未实现 | 实时聚合口径下，订单取消后金额自动不计（修复口径后已正确）；但**已发生的层级不回退**（只升不降） |

### 2.4 分销商等级（`eb_distributor_level`）— 本次全新接入

| 项 | 状态 | 位置 |
|---|---|---|
| 统计表 `eb_user_distributor_level_stat` | ✅ **新增** | `model/user/UserDistributorLevelStat.java` |
| 统计累加（本人/直推/直推会员/团队） | ✅ **新增** | `DistributorLevelServiceImpl.applyStats()` |
| 充值额（实时聚合，不落表） | ✅ **新增** | `UserDistributorLevelStatDao.sumRechargeAmount` |
| 人数指标（直推/团队/指定等级） | ✅ **新增** | `UserDistributorLevelStatDao.countDirectUsers / countTeamUsers / countDirectLevelUsers` |
| 八条件链式判定 + 与/或 | ✅ **新增** | `DistributorLevelServiceImpl.meetsCondition()` |
| 自动升级（只升不降） | ✅ **新增** | `DistributorLevelServiceImpl.syncDistributorLevels()` |
| 等级落点 `eb_user.distributor_level_id` | ✅ **新增** | `User.java:116` |
| 变更记录表 `eb_user_distributor_level` | ✅ **新增** | `model/user/UserDistributorLevel.java` |
| 挂入支付成功 / 订单完成 / 退款 | ✅ **新增** | `OrderPayServiceImpl.java:406`、`StoreOrderTaskServiceImpl.java:304 / 549` |
| 幂等去重 | ✅ **新增** | `DistributorLevelServiceImpl.claimOnce()` + `eb_level_stat_order_log` |
| 原子累加（并发安全） | ✅ **新增** | `UserDistributorLevelStatDao.incrColumn` |
| 全量重算接口 | ✅ **新增** | `POST /api/admin/distributor/level/recalc/{uid}` |
| 返佣比例接线（可选，默认关） | ✅ **新增** | `OrderPayServiceImpl.resolveBrokerageRate()` |

---

## 三、新增字段 / 配置项 / 调度入口

### 3.1 数据库

| 对象 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `eb_user.distributor_level_id` | int | 0 | 分销商等级落点 |
| `eb_user_distributor_level_stat` | 新表 | — | 4 个金额统计字段，全 0 |
| `eb_user_distributor_level` | 新表 | — | 等级变更记录 |
| `eb_level_stat_order_log` | 新表 | — | 幂等流水，唯一键 `(order_no, module, scene)` |

### 3.2 配置项（`eb_system_config`）

| name | 默认 | 含义 |
|---|---|---|
| `distributor_level_enabled` | **1** | 分销商等级统计与自动升级总开关，0=关闭 |
| `distributor_level_brokerage_enabled` | **0** | 返佣改按分销商等级取值；0=保持原「按会员等级取返佣」不变 |
| `distributor_level_max_depth` | **0** | 团队业绩向上累计层数上限，0=不限 |
| `team_level_cycle_reset` | **0** | 团队等级统计周期清零，0=永不清零 |
| `distributor_level_cycle_reset` | **0** | 分销商等级统计周期清零，0=永不清零 |

> 修改配置后**必须清 Redis 配置缓存**：`redis-cli -a 123456 -n 7 HSET config_list <name> <value>`
> （db10 同样处理），否则接口仍返回旧值。

### 3.3 调度入口

| 类 | 方法 | cron | 默认行为 |
|---|---|---|---|
| `admin/task/level/LevelStatCycleResetTask` | `reset()` | `0 0 2 1 * ?`（每月 1 号 02:00） | 两个 cycle_reset 配置都是 0，空转不做任何事 |

已有的订单链路任务（`OrderPaySuccessTask` / `OrderCompleteTask` / `OrderRefundTask`）无需改动，
分销商等级已挂在这些任务的下游业务方法里。

---

## 四、自查验证步骤

```bash
# 1) 建表与配置（幂等，可重复执行）
mysql -uroot -p123456 crmeb < crmeb/sql/distributor_level_upgrade_20260923.sql
# 或走一键补丁：cd crmeb/sql/oneclick && ./deploy.sh patch

# 2) 重算某个用户的分销商等级统计（后台接口，需 admin token）
curl -X POST -H "Authori-zation: <token>" http://127.0.0.1:8080/api/admin/distributor/level/recalc/66

# 3) 看统计是否算对
SELECT * FROM eb_user_distributor_level_stat WHERE uid=66;
# 期望：total_consume_amount = 该用户已支付且未退款订单总额

# 4) 配一个门槛后重算，验证自动升级
UPDATE eb_distributor_level SET total_consume_amount=100 WHERE id=1;
curl -X POST -H "Authori-zation: <token>" .../recalc/66
SELECT uid, distributor_level_id FROM eb_user WHERE uid=66;   # 期望 = 1
SELECT * FROM eb_user_distributor_level WHERE uid=66;          # 期望有升级记录

# 5) 验证只升不降：门槛改到 10000 再重算，等级应保持 1 不被清空

# 6) 验证幂等：同一订单号重复插入幂等流水，第二次应影响 0 行
INSERT IGNORE INTO eb_level_stat_order_log(order_no,module,scene) VALUES ('T1','DISTRIBUTOR','PAID');
INSERT IGNORE INTO eb_level_stat_order_log(order_no,module,scene) VALUES ('T1','DISTRIBUTOR','PAID');

# 7) 真实下单跑一遍：下单 → 支付 → 看统计表累加、等级是否升级；再退款 → 看统计回退
```

---

## 五、已知未实现 / 需业务确认

1. **团队奖重复发放风险**：统计层已幂等，佣金记录层未加去重（改了会影响现有金额逻辑，未动）。
2. **人数指标未持久化**：直推/团队人数仍是实时 count，数据量大时判定会变慢；团队等级同理（既有设计，未改）。
3. **订货商已升级层级不回退**：订单取消后业绩不再计入，但已升的层级不会降（与"只升不降"策略一致）。
4. **无会员端展示**：分销商等级目前只有后台，前端 `crmeb-front` 无相关接口与页面。
5. **分销商等级 5 个预置等级的条件门槛全为 0**，按设计「全 0 视为未配置，不参与自动升级」，
   需要在后台逐个配置门槛后才会生效。
