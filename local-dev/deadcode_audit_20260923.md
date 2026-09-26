# 全仓库无效代码审计与清理报告（2026-09-23）

> 判定原则：**宁可漏报，不可误报**。只在「全仓库文本（含注释/字符串/XML/YAML）里零引用」时才判为死代码；
> 框架会自己扫描注册的东西一律不动。每一处删除都附判定理由，全部通过编译/构建验证。

## 一、总览

| 类别 | 处理 | 数量 |
|---|---|---|
| 零引用 Java 类（含 4 个级联失效） | **已删除** | 33 |
| 从未被使用的 private 成员（方法/字段） | **已删除** | 48 处 |
| 前端不可达文件（import 图分析） | **已删除** | 13 |
| 重复的 SQL 脚本 | **已删除** | 1 |
| 无法到达的分支（`if(false)` 等） | 扫描结果 **0 处** | 0 |
| 重复逻辑（方法体完全一致） | **仅报告，未自动归并**（理由见第五节） | 7 组 |
| 零引用但由框架注册（`@Service`/`@RestController` 等） | **不动**，仅记录 | 267 |
| `app/`（uni-app，easycom 自动注册） | **不适用该分析模型，未动** | — |
| 图片等资源重复 | 不属代码，未动 | — |

删除文件 **47 个**，修改文件 **39 个**。

---

## 二、零引用的 Java 类（33 个，已删除）

判定：全仓库（java/xml/yml/properties/json/js/vue/sql）中 `\b类名\b` 除自身文件外 **出现 0 次**，
且无 `@Service`/`@RestController`/`@Component`/`@Configuration`/`@Mapper`/`@TableName`/`@RestControllerAdvice` 等容器注解，
也未 `implements ResponseBodyAdvice / HandlerInterceptor / WebMvcConfigurer / BaseMapper` 等框架接口。

### 2.1 结果码枚举（10 个）—— 上游遗留，全套未被任何异常/断言引用

| 文件 | 判定理由 |
|---|---|
| `qianxu-common/.../result/AdminResultCode.java` | 枚举值零引用；同目录实际使用的是 `ResultCode` / `ApiResultCode` |
| `qianxu-common/.../result/CommunityResultCode.java` | 社区模块整体未在本项目启用 |
| `qianxu-common/.../result/MarketingResultCode.java` | 零引用 |
| `qianxu-common/.../result/MemberResultCode.java` | 零引用（会员模块用的是 `ResultCode`） |
| `qianxu-common/.../result/MerchantResultCode.java` | 零引用 |
| `qianxu-common/.../result/OnePassResultCode.java` | 一号通模块回调未接入 |
| `qianxu-common/.../result/OrderResultCode.java` | 零引用 |
| `qianxu-common/.../result/PayResultCode.java` | 零引用 |
| `qianxu-common/.../result/WechatResultCode.java` | 零引用 |
| `qianxu-common/.../result/CouponResultCode.java` | **级联失效**：只被下面 2.4 的 `ShopOrderDetailVo` 引用 |
| `qianxu-common/.../exception/ExceptionCodeEnum.java` | 实现的是同项目 `ExceptionHandler` 接口，但枚举本身零引用、无任何遍历点 |

### 2.2 请求 DTO（13 个）—— 无任何 Controller 形参绑定

| 文件 | 判定理由 |
|---|---|
| `request/MealCodeRequest.java` | 套餐购买请求；无 `@RequestBody`/`@Validated` 使用点 |
| `request/SystemAdminLoginCaptchaRequest.java` | 后台登录验证码请求；对应的登录接口未使用该 DTO |
| `request/SystemConfigAdminRequest.java` | 配置保存请求；实际用的是 `SystemConfigRequest` |
| `request/theme/ThemeRequest.java` | 主题新增/修改请求；装修接口实际用 `ThemeSaveRequest` 等 |
| `request/onepass/OnePassRegisterRequest.java` | 一号通注册请求，零引用 |
| `request/onepass/OnePassUpdateRequest.java` | 同上 |
| `request/onepass/OnePassUserRecordRequest.java` | 同上 |
| `request/onepass/OnePassShipmentCallBackRequest.java` | 同上 |
| `request/page/PageCategoryRequest.java` | 微页面分类请求，零引用 |
| `request/page/PageCategorySearchRequest.java` | 同上 |
| `request/page/PageDiySearchRequest.java` | 微页面搜索请求，零引用 |
| `request/page/PageLinkRequest.java` | 页面链接请求，零引用 |
| `request/page/PageLinkSearchRequest.java` | 同上 |

### 2.3 VO / 响应对象（7 个）

| 文件 | 判定理由 |
|---|---|
| `vo/ShopOrderDetailVo.java` | 微信小程序「自定义交易组件」订单对象，整条链路未启用 |
| `vo/ShopOrderDeliveryDetailVo.java` | 同上（级联失效源） |
| `vo/ShopOrderDeliveryInfoVo.java` | **级联失效**：仅被 `ShopOrderDeliveryDetailVo` 引用 |
| `vo/ShopOrderPayInfoVo.java` | **级联失效**：仅被 `ShopOrderPayVo` 家族引用 |
| `vo/ShopOrderProductInfoVo.java` | **级联失效**：同上 |
| `vo/ShopAuditGetMinCerBrandInfoItem.java` | 小程序品牌资质回包对象，零引用（其 2 个私有字段一并失效） |
| `vo/ShopAuditGetMinCerBrandInfoItemDataVo.java` | **级联失效**：只被上一个类引用；删完两个字段后已是空壳类 |
| `response/StoreCategoryTreeList.java` | 商品分类树响应，零引用（实际用的是 `CategoryTreeResponse`） |
| `admin/vo/ValidateCode.java` | 后台图形验证码 VO；入口 `AdminLoginController` 未使用 → 该功能未接入 |

### 2.4 说明

- 级联失效指：单遍扫描时它还有 1 个「外部引用」，但那个引用方本身是死代码；删除后复扫才暴露，
  已通过**第二轮、第三轮复扫**清零（`零外部引用的类: 0`）。

---

## 三、从未被使用的 private 成员（48 处，已删除）

判定：`private` 成员**只能在本文件内被调用**，故「在本文件去掉注释/字符串后，该标识符仅出现 1 次（即声明处）」
即为从未被使用。Lombok 类（`@Data/@Getter/@Setter/@Accessors/@Builder`）的私有字段**不做判定**
（getter/setter 由编译期生成，外部可用）；带注解的私有方法（`@PostConstruct` 等容器回调）也排除。

### 3.1 未使用的注入字段（`@Autowired` 注了但整类从未用过）—— 30 处

| 文件 | 字段 |
|---|---|
| `admin/controller/PayComponentProductAuditInfoController.java` | `payComponentProductAuditInfoService` |
| `admin/controller/PayComponentProductInfoController.java` | `payComponentProductInfoService` |
| `admin/controller/PayComponentProductSkuAttrController.java` | `payComponentProductSkuAttrService` |
| `admin/controller/PayComponentProductSkuController.java` | `payComponentProductSkuService` |
| `admin/service/impl/CopyrightServiceImpl.java` | `systemAttachmentService` |
| `service/impl/CosServiceImpl.java` | `systemAttachmentService` |
| `service/impl/ExcelServiceImpl.java` | `systemConfigService` |
| `service/impl/MerchantStoreServiceImpl.java` | `storeOrderService` |
| `service/impl/PayComponentDraftProductServiceImpl.java` | `wechatVideoBeforeService` |
| `service/impl/SmsServiceImpl.java` | `smsRecordService` |
| `service/impl/StoreBargainServiceImpl.java` | `storeProductAttrResultService` |
| `service/impl/StoreCombinationServiceImpl.java` | `storeProductAttrResultService` |
| `service/impl/StoreOrderRefundServiceImpl.java` | `restTemplateUtil` |
| `service/impl/StoreOrderTaskServiceImpl.java` | `redisUtil`、`userBillService`、`componentOrderProductService` |
| `service/impl/StoreProductAttrValueServiceImpl.java` | `systemConfigService` |
| `service/impl/StoreProductGuaranteeServiceImpl.java` | `transactionTemplate` |
| `service/impl/StoreProductReplyServiceImpl.java` | `systemConfigService` |
| `service/impl/StoreSeckillServiceImpl.java` | `storeProductAttrResultService` |
| `service/impl/SystemStoreServiceImpl.java` | `systemConfigService` |
| `service/impl/UserRechargeServiceImpl.java` | `userService` |
| `service/impl/UserServiceImpl.java` | `userAddressService` |
| `service/impl/WechatUserServiceImpl.java` | `articleService`、`userTokenService` |
| `service/impl/WechatVideoSpuServiceImpl.java` | `redisUtil` |
| `service/delete/ProductUtils.java` | `storeProductCouponService` |
| `service/impl/AgentServiceImpl.java` | `transactionTemplate` ← **本次改造残留** |

### 3.2 未使用的 logger 字段（4 处，类上用了 `@Slf4j` 的 `log`，手写 logger 是多余的）

`SensitiveLogAspect.LOGGER`、`WechatShippingRechargeTask.logger`、`ArticleServiceImpl.logger`、`PageDiyServiceImpl.logger`

### 3.3 未使用的 private 方法（14 处）

| 文件 | 方法 | 判定理由 |
|---|---|---|
| `common/vo/MyRecord.java` | `getFiledType` | 唯一调用点在**注释掉的行**（`// if (getFiledType(...))`）里 |
| `service/impl/AgentServiceImpl.java` | （无，见字段） | — |
| `service/impl/OrderPayServiceImpl.java` | `calculateCommissionByRate` | 佣金改按等级取比例后遗留，已被 `resolveBrokerageRate` 取代 |
| `service/impl/OrderTaskServiceImpl.java` | `getJavaBeanStoreOrder` | 零调用 |
| `service/impl/PageDiyServiceImpl.java` | `getModifiedJsonString` | 零调用 |
| `service/impl/PayComponentCatServiceImpl.java` | `assembleRedisData` | 零调用 |
| `service/impl/ShippingTemplatesFreeServiceImpl.java` | `updateStatus` | 零调用 |
| `service/impl/ShippingTemplatesRegionServiceImpl.java` | `updateStatus` | 零调用 |
| `service/impl/StockRewardServiceImpl.java` | `directChildren`、`parseDecimal` | **本次改造残留**：越级奖下线后不再需要 |
| `service/impl/StoreOrderServiceImpl.java` | `getRequestTimeWhere`、`getStatusWhere` | 零调用 |
| `service/impl/SystemConfigServiceImpl.java` | `getConfigByName` | 零调用（实际走 Redis 的 `get()`） |
| `service/impl/UserTeamLevelServiceImpl.java` | `nonNegative` | **本次改造残留**，零调用 |

> 另有 5 处字段位于被整文件删除的类中（`ShopAuditGetMinCerBrandInfoItem*`），随文件一并消失。

---

## 四、前端与脚本（14 个，已删除）

判定：从 `main.js` / `permission.js` 出发做 import 图可达性分析；
`require.context` / 字符串拼接路径等动态写法命中的目录**整目录豁免**；最后再过一道「basename 在别处出现过就不报」的文本兜底。

| 文件 | 判定理由 |
|---|---|
| `admin/src/api/configApi.js` | 全项目无路径引用（同名**函数** `configApi` 在 `api/distribution.js` 等文件里，是两回事） |
| `admin/src/api/configTabApi.js` | 零引用 |
| `admin/src/components/Share/DropdownMenu.vue` | 零引用 |
| `admin/src/components/Tinymce/dynamicLoadScript.js` | `Tinymce/index.vue` 未引用它 |
| `admin/src/components/uploadVideo2/index copy.vue` | 与同目录 `index.vue` 并存的旧副本 |
| `admin/src/libs/modal-coupon.js` | `$modalCoupon` 实际定义在 `components/couponList/couponFrom/index.js`；`main.js:155` 的注册是**注释掉的** |
| `admin/src/libs/modal-sure.js` | `main.js:63` 的 `modalSure` 来自 `@/libs/public`，非此文件 |
| `admin/src/styles/font/mobile copy.json` | 与 `mobile.json` 并存的旧副本 |
| `admin/src/utils/open-window.js` | 零引用（vue-element-admin 遗留） |
| `admin/src/vendor/Export2Excel.js` | 零引用 |
| `admin/src/vendor/Export2Zip.js` | 零引用 |
| `admin/src/views/login/verifition/Verify/SilderVerify.vue` | 真实使用的是 `verifySlider.vue`（`Verify.vue` 只 import 了它） |
| `admin/src/views/login/verifition/utils/fomat.ts` | 零引用 |
| `local-dev/sql/product_group_level_source.sql` | 与 `qianxu/sql/product_group_level_source.sql` 内容重复（仅差已废弃的 `USE` 行） |

---

## 五、重复逻辑：7 组（**仅报告，未自动归并**）

判定：方法体（**保留字符串字面量**）去掉注释与空白后哈希一致，且 ≥8 行。

| # | 位置 | 规模 | 建议 |
|---|---|---|---|
| 1 | `IosServiceImpl.giveNewPeopleCoupon()` ↔ `UserCenterServiceImpl.giveNewPeopleCoupon()` | 43 行 | 抽到公共 Service（**涉及发券，改动前需单独验证**） |
| 2 | `OrderPayServiceImpl.pushMessagePink()` ↔ `StorePinkServiceImpl.pushMessageOrder()` | 38 行 | 抽到通知工具类 |
| 3 | `ProductStatisticsServiceImpl.calculateRatio()` ↔ `TradeStatisticsServiceImpl.calculateRatio()` | 12 行 | 抽 `StatUtil` |
| 4 | `IosServiceImpl.checkValidateCode()` ↔ `UserCenterServiceImpl.checkValidateCode()`（`LoginServiceImpl` 还有第 3 份） | 10 行 | 抽到验证码工具 |
| 5 | admin `TaskExecutorConfig.initTaskExecutor()` ↔ front `TaskExecutorConfig.initTaskExecutor()` | 14 行 | **不合并**：admin/front 是两个独立 Spring Boot 应用，各需自己的配置类 |
| 6 | admin `DruidConfig.druidServlet()` ↔ front `DruidConfig.druidServlet()` | 9 行 | **不合并**，同上 |
| 7 | admin `DruidConfig.filterRegistrationBean()` ↔ front `DruidConfig.filterRegistrationBean()` | 8 行 | **不合并**，同上 |

**为什么不自动归并**：这些方法的每一份都在被调用，删除任何一份都会改变行为；
「归并」本质是**重构**（提取公共实现），需要单独验证。
更关键的是：本检测最初把字符串字面量也归一化了，导致
`ProductUtils.getTaobaoProductInfo` / `getTmallProductInfo` 被误判为重复——
它们真正的差异就在字符串（`item.getString("desc")` vs `item.getString("descUrl")`）。
已修正检测器（保留字面量）后重跑，上表是修正后的结果。

---

## 六、明确「未动」的东西及原因

1. **267 个零引用但由框架注册的类**（`XxxServiceImpl` 实现接口后被 `@Autowired` 按接口注入、
   `@RestController` 由 HTTP 路由可达、`@Configuration`/`@Aspect`/`@RestControllerAdvice` 由容器装配）。
   它们源码里没有「名字」级引用，但**删掉会导致启动失败或接口消失**。
2. **`app/` 目录全部未动**：uni-app 用 `easycom` 规则按目录约定自动注册组件、页面由 `pages.json` 驱动，
   import 图分析在此**不成立**（把 `manifest.json`、`vue.config.js`、`Authorize.vue` 都误报为不可达），
   故不做任何判定。
3. **model / vo / dto 中通过反射序列化的类**：只要有 Controller 形参或字段引用就会被判为「被引用」，
   本次报出来的都是**连引用都没有**的，不存在误删。
4. **`admin/src/views/**/index.vue` 里内容完全相同的路由容器**（`<template><router-view/></template>`）：
   共 7 个文件哈希一致，但每个都被各自路由 `import()` 引用，属**正常设计**，不是冗余。
5. **图片资源重复**（`qianxuimage` 与 `db-data/qianxu_image` 大量同哈希文件）：运行时静态资源，不属代码。

---

## 七、验证证据

| 验证项 | 命令 | 结果 |
|---|---|---|
| Java 类死代码清零 | `deadcode_scan.py` 第三轮 | `零外部引用的类: 0`、`从未被使用的 private 成员: 0` |
| 前端死代码清零 | `deadcode_scan_front.py admin/src` | `不可达（可删候选）: 0` |
| 残余引用兜底 | 对 33 个被删类名做全仓库 grep（java/xml/yml/properties/vue/js/ts/sql） | **零命中** |
| Java 全量编译 | `mvnw.sh -o -DskipTests -pl qianxu-common,qianxu-service,qianxu-admin,qianxu-front -am compile`（先 `touch` 全部源文件强制重编） | 4 模块 **BUILD SUCCESS** |
| 后台生产构建 | `vue-cli-service build` | **DONE Build complete**，无 error |

> 「touch 全部源文件」是为了绕过 Maven 增量编译器（它会因 qianxu-front 源文件时间戳未变而跳过编译），
> 否则 `qianxu-front` 会显示 `Nothing to compile`，等于**没验证**。

---

## 八、如何复跑

```bash
PY="C:/Users/Administrator/.workbuddy/binaries/python/versions/3.13.12/python.exe"

$PY local-dev/tools/deadcode_scan.py                 # Java：零引用类 / 未用 private 成员
$PY local-dev/tools/deadcode_scan_front.py admin/src # 前端：import 图不可达文件
$PY local-dev/tools/deadcode_scan_dup.py             # 重复方法体

# 按扫描结果生成清理动作（先干跑，确认无误后加 --apply）
$PY local-dev/tools/deadcode_scan.py --json /tmp/dead.json
$PY local-dev/tools/deadcode_apply.py /tmp/dead.json
$PY local-dev/tools/deadcode_apply.py /tmp/dead.json --apply
```

⚠️ 三个判定陷阱（脚本已内置规避，改动脚本时务必保留）：
1. 注解必须从 `class` **关键字位置**往前取——若从匹配起点取，`@Service` 会被漏读，
   导致 `OrderServiceImpl` 这类按接口注入的实现类被误判为死代码（实测踩过）。
2. `@RestControllerAdvice` 不在常规 `@Component` 家族里，必须单独列入框架注解白名单
   （否则 `GlobalExceptionHandler` / `ResultAdvice` 会被误删，直接影响全局异常处理）。
3. 重复逻辑检测**不能剥离字符串字面量**，否则只剩字符串不同的方法会被误判为重复。
