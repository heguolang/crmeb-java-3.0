# PROJECT_NOTES.md — CRMEB Java 3.0 项目细节手册

> 本文件是 `D:\WorkBuddy\java系统\.workbuddy\memory\MEMORY.md` 的**详细版附录**。
> MEMORY.md 只留硬规则；动手做下面这些事之前，先读这里对应章节。

---

## A. 资金监控 / 账单类查询约定（2026-09-22 起）

涉及 `eb_user_bill` / `eb_user_integral_record` / `eb_user_brokerage_record`：

- **`title` 是自由文本**，业务代码会不断写入新标题（`用户订单付款成功`、`换货差价`、`订货奖金`…）。
  **绝不要用「枚举 → 精确标题」硬编码匹配**，必然会漏。
  正确做法：Mapper 同时支持 `titleList`（IN 精确，用于固定常量标题）和
  `titleLikeList`（LIKE 模糊，`<foreach separator=" or ">`，用于自由文本）。
- **佣金分散在两张表**：`eb_user_bill.category='brokerage_price'` +
  `eb_user_brokerage_record`（分销/代理/提现/订货奖金）。查佣金**必须 UNION 两表**，
  否则订货商数据全丢。
- **分销佣金标题是固定常量**，见 `BrokerageRecordConstants`：
  `获得推广佣金` / `获得自购返佣` / `获得团队极差奖` / `获得团队平级奖` / `获得区域代理奖励` /
  `提现申请` / `提现申请拒绝` / `佣金转余额` / `后台操作`。这类可以精确匹配。
- **避坑**：模糊匹配的关键字别太宽。`order` 用 LIKE "佣金" 会撞 `后台修改佣金`（与 `admin`
  分类重叠）；`stock` 用 "团队" 会撞 `获得团队极差奖`。
  改完务必做「各子项求和 == 不限总数」的口径闭合验证。
- 新增明细枚举要**三处同步**：`FundsMonitorRequest.@StringContains` 白名单 +
  Service 层 switch 映射 + 前端 `titleOptionsByCategory` 下拉。

---

## B. 后台 UI 规范（2026-09-22 起，所有新栏目按此改）

**已固化两个全局样式基座，改 UI 时先看这两个文件，不要在页面里重复造轮子：**

| 文件 | 提供 |
|---|---|
| `admin/src/theme/list-page.scss` | 列表页基座：筛选区、列表工具条、浅蓝表头表格、键值行、状态标签、空态 |
| `admin/src/theme/element.scss` | `.op-bar` / `.op-btn`（操作栏按钮）+ element-ui 组件覆盖 |

两者都经 `admin/src/theme/index.scss` → `main.js` 引入，**生产构建自动包含**。

**标准结构**：
```
.filter-head > .filter-head__title / .filter-head__toggle
.filter-form > .filter-group > .filter-group__title + .filter-grid > .el-form-item
.list-toolbar > .list-toolbar__actions / .list-toolbar__tip
.list-table(设 --list-cols) > .list-head > .list-head__cell
                            > .list-body > .list-row > .list-cell
.kv > .kv__k + .kv__v(.kv__v--num / .kv__v--money) + .kv__uid + .kv__copy
.status-tag(--primary/--warning/--info/--danger/--success)
.inline-status(--on/--off)
.op-bar > .op-btn
```

**两条铁律**：
1. **列宽只用 `--list-cols` 注入**，`.list-head` 与 `.list-row` 共用同一变量才能严格对齐：
   ```scss
   .list-table { --list-cols: 40px minmax(170px,1.1fr) minmax(280px,1.8fr) 150px 120px 200px; }
   ```
2. **颜色只用 CSS 变量**（`var(--prev-color-primary)` / `--prev-color-primary-light-9`），
   不硬编码色值，这样切换运行时主题能自动跟随。

**注意 `element.scss` 里两条 `!important` 全局规则**（改按钮前必看）：
```scss
.el-button { padding: 9px 15px !important; }
.el-button + .el-button { margin-left: 14px !important; }
```
页面级 scoped 样式**压不住**它们，需要改按钮尺寸/间距只能走全局 class 或同样加 `!important`。

**字体/字号基准**：表内正文 13px / 行高 24px，表头 14px/600，数字用 `tabular-nums`。

---

## C. 菜单结构调整约定（2026-09-22 起）

**菜单是数据库驱动的**：`eb_system_menu`
（字段：`id, pid, name, icon, perms, component, menu_type, sort, is_show, is_delte`）。

**常规调整（改名 / 排序 / 换位置）只动 `name` / `sort` / `pid`**，不要碰 `component` 和 `perms`
—— 只改中文名和排序不会影响任何页面跳转。

**例外：模块整体重命名时**（如 2026-09-23「会员返佣配置」→「分销商等级」）可以改 `component`，
因为角色授权是按 **menu id**（`eb_system_role.rules`）而非 component 存的，改 component 不影响授权。
但**必须同步改 `admin/src/router/modules/*.js` 里的 path**，否则菜单点了 404。
`perms` 仍然**保留原值不动** —— 后端 `@PreAuthorize` 认的是 perms 字符串，
改了要连同 Controller 一起改，属于额外风险，没必要。

**关键规则**：
- 同级菜单按 **`sort` DESC 排列（值越大越靠上）**。
- **一级菜单 / 目录的 `component` 不能为空**：`path` 由它推导，为空会被前端
  `filter(item => item.path)` 过滤掉，菜单直接不显示。虚拟目录给个占位路径即可。
- 隐藏菜单入口用 `is_show = 0`（保留记录与权限）。
- MySQL 里 `UPDATE ... SET pid=(SELECT ... FROM 同一张表)` 会报错，
  需派生表包装：`SET pid=(SELECT id FROM (SELECT id FROM ...) t)`。

**⚠️ 页面标题来自静态路由，不是数据库**：
`admin/src/router/modules/*.js`（`stock.js` / `daili.js` 等）是静态路由，`meta.title` **硬编码**。
所以**改数据库菜单名后，左侧/顶部菜单名会变，但浏览器标题和页头标题不变**；
要一起改就得动对应的 `router/modules/*.js`。
反过来也有好处：菜单入口隐藏后，该 URL 仍可直接访问，不会 404。

**菜单调整脚本统一归档**到 `local-dev/sql/`。
**改前先备份**：`CREATE TABLE eb_system_menu_bak_YYYYMMDD AS SELECT * FROM eb_system_menu;`
（2026-09-22 的备份表：`eb_system_menu_bak_20260922`）

### 商品菜单当前顺序（2026-09-23 起）

`pid=2`（商品）下按 sort DESC：

```
商品管理(6) > 商品分类(5) > 商品分组(4) > 商品规格(3) > 商品评论(2) > 保障服务(1)
```

- 原先 702「商品分组」=85、708「添加分组」=84 顶在最上面，其余全是 sort=1。
- `708 添加分组`（component `/store/productGroup/edit`）已 `is_show=0` 隐藏
  —— 页面内已有「新建分组」按钮，重复。
- **只隐藏不删**：`/store/productGroup/edit/:id` 是列表页「编辑」的跳转目标，删菜单可以，
  但**路由必须保留**，否则点「编辑」404。
- 菜单接口是**实时查库**，改完立刻生效，不用重启；但后台需要重新登录/刷新才拉到新菜单。

---

## D. 商品分组的等级来源 + 编辑页（2026-09-23 起）

表 `eb_store_product_group` 用**独立的等级列**，按权限各管一边，**切换权限不串值**：

| 权限 permission_type | 后台显示 | 等级下拉标题 | 字段 | 数据源 |
|---|---|---|---|---|
| `all` | 全部会员 | 会员等级 | `user_level_ids` | `eb_system_user_level` / `levelListApi` |
| `promoter` | 仅分销商 | 分销商等级 | `distributor_level_ids` | `eb_distributor_level` / `distributorLevelListApi` |
| `agent` | **仅区域代理** | 区域代理等级 | `agent_level_ids` | **固定枚举 1=省代 2=市代 3=区代**（前端写死，无接口） |
| `stock_agent` | 仅订货商 | 订货商等级 | `stock_level_ids` | `eb_stock_level` / `stockLevelListApi` |
| `team` | **仅社群团队** | 社群团队等级 | `team_level_ids` | `eb_system_team_level` / `teamLevelAllApi` |

**⚠️「区域代理」是独立模块，不是订货商那套。**
区域代理 = 菜单「区域代理 /daili」+ 表 `eb_agent`，
等级是**固定三级** `eb_agent.level`（1=省级/2=市级/3=区级，后台文案「省级代理/市级代理/区级代理」）。
所以 `agent` 的等级下拉**绝不能接 `stockLevelListApi`** —— `eb_stock_level` 是订货商/代理层级表
（总代、一级代理、市级订货商、门店…），与区域代理无关。

**⚠️ `agent` 语义变更（2026-09-23）**：原先 `agent` 复用 `stock_level_ids`（`eb_stock_level.id`），
现在改读 `agent_level_ids`（1/2/3）。**值域不同，旧数据不会自动迁移**，
变更前若已有 `permission_type='agent'` 的分组，需在后台重新选择等级（本地无存量）。
`stock_level_ids` 现在**只服务 `stock_agent`**。

后端判定 `StoreProductGroupServiceImpl`：

- `matchRole()`：`all` 放行 → `promoter` 看 `isPromoter` → `agent` 看 `eb_agent` 是否**已审核通过**
  → `stock_agent` 看 `eb_stock_agent` 是否启用 → `team` 看 `eb_user.team_level > 0`；
  **非 all 类型且未登录一律 false（游客看不到）**。
- `matchUserLevel()`：按权限取对应列比对
  （`user.level` / `user.distributorLevelId` / `eb_agent.level` / `eb_stock_agent.level_id` / `user.team_level`）。
- **`levelOnly=false` 或对应等级列为空 → 视为不限**（别破坏这条）。
- 对外出口（Front 商品列表/详情/下单都接了）：`getHiddenProductIds` / `canViewProduct` /
  `canBuyProduct` / `assertPurchaseAllowed`。

### 编辑页布局：双 Tab（2026-09-23 接入装修后）

`admin/src/views/store/productGroup/edit.vue`：

- **页面装修** tab：**内嵌真装修器** `design/theme_editor/devise/diyIndex.vue`；
- **基础设置** tab：原三栏里的「手机预览 + 表单」（左栏那个只读的假组件库已删除）。

内嵌装修器有三个硬性要求（复用必看）：

1. **先改 query 再渲染**。`diyIndex` 在 `created()` 里读 `$route.query`
   （`id`=装修页 id、`type`、`page_type`、`name`），所以宿主必须
   `await this.$router.replace({query})` **之后**才让它挂载（用 `v-if="diyReady"` 卡住时机）。
   分组 id 走 **path 参数**（路由已存在：`productGroup/edit/:id?`），
   `query.id` 让给装修页 id，两者不冲突。
2. **必须 `provide: { reload, setDirty }`**（diyIndex 声明了 `inject` 这两个）。
   `reload` 用来自增强制重建（本项目宿主里实现为 `diyKey += 1`）；
   `setDirty(false)` 在保存成功时被调用，可用来收起宿主按钮的 loading。
3. **保存调 `saveConfig(1)`**（只保存不跳页）。`saveConfig(2)` 会
   `window.location.replace('/design/micro_theme')` 直接跳走，不能用在宿主页里。
   宿主壳不要复用 `edit_theme/components/HomeEditor.vue`，它只是三行转发。

### 商品分组 = 一个「微页面」

系统里**没有独立的商品分组落地页**，分组原本只是装修器里一个组件（选分组过滤商品）。
现在的做法是复用通用微页面机制：

- 每个分组**懒创建**一条 `eb_theme` 记录：`eb_store_product_group.theme_id` 指向它，
  `page_type='micro'`、`type=0`（自建）、`title='商品分组-{分组名}'`（便于在「装修 → 专题页面」里区分）。
- 微页面复用 `type=home` 存取，所以**装修内容落在 `eb_theme.home_data`**。
- 接口：
  - Admin `POST /api/admin/store/product/group/theme/{id}` → 返回 themeId（幂等，不存在则创建）。
    `StoreProductGroupServiceImpl.ensureTheme()`，CAS 回写 theme_id 防并发。
  - Front `GET /api/front/product/group/detail/{id}` → `{id,name,theme_id,layout,style,badge,
    title_multi,min_buy,limit_one}`。已在 `WebConfig` 免登录白名单里。
- H5 落地页 `app/pages/activity/product_group/index.vue`（`pages.json` 的 `pages/activity` 子包已注册）：
  顶部用 `themePage` mixin 的 `initThemePage('home', {id: theme_id})` 取装修数据交给 `<PageDesign :microPage>`，
  下方自己渲染该分组商品网格（`getThemeProduct({group_ids})`）。

**已知限制**：装修页 `pageType='home'`，装修器左栏按既有规则**不显示「商品组件/用户组件」**
（`arraySort` 里商品组件仅非 home/user 时显示），与「装修 → 专题页面」一致。

### 分组商品在装修里的呈现（2026-09-23 二次修正）

第一版是「H5 无条件在装修区下方追加商品网格」，结果**后台装修时看不见商品、不知道它在哪**。
现在改为三条规则：

1. **装修器顶部提示条**（`edit.vue` 的 `.pg-goodsbar`）：显示「本分组有 N 件商品」
   （N 取自 `GET /group/info` 已返回的 `productCount`，无需新接口）+ 一句位置说明。
2. **「一键插入分组商品」按钮** → `$refs.diy.appendProductGroupComponent(groupId)`
   （`diyIndex.vue` 新增的 public 方法）→ 往装修数据里插一个「商品选项卡」组件并把
   「选择方式」预设为 `tabVal=5`（指定分组）+ `productGroupConfig.activeValue=[groupId]`。
   插入后商品出现在手机预览里，**可拖动决定位置**。
3. **H5 去重**：`product_group/index.vue` 先用 `checkDiyHasGroupGoods()` 扫描装修数据，
   若已存在「绑定本分组的商品选项卡」→ 商品位置交给装修，**不再自动追加**；
   否则维持原来的兜底追加。判定用限定深度的递归扫描，不依赖数据层级。

「商品选项卡」= 后台 `home_product`（**type 0 基础组件，home 页可见**）→ H5 `promotionList`，
数据来源枚举 `指定商品(1)/指定分类(3)/商品标签(4)/指定分组(5)`。

**编程式插组件的坑（务必照抄既有链路，别手写 defaultConfig）**：
- 必须复用 `addDomCon(lConfig条目, 1)`（`defaultArraySort` 内部要调 `data.element.data().defaultConfig`）；
- `defaultArray` 的 key **不是** `entry.num`，而是 `swapArray` 重写后的 `mConfig[activeIndex].num`；
  取配置最稳的方式是 `mConfig[activeIndex]` 拿组件后按 `id` 在 `defaultArray` 里找；
- 改完要 `commit('mobildConfig/UPDATEARR')` 一次才会刷新右栏与预览。

详细机制见技能 `crmeb-theme-diy`。

### 页面预览（2026-09-23，装修 Tab 右上角）

`edit.vue` 顶部操作区的「预览页面」按钮（装修 Tab 且分组已保存时才显示）→ 弹窗内含三样：

1. **手机壳 iframe**（`<iframe :key="previewKey" :src="previewUrl">`）实时加载 H5 落地页；
2. **二维码**（复用项目已装依赖 `qrcodejs2`，与系统其它预览一致）；
3. **复制链接 / 新窗口打开**。

**地址规则**：`{H5域名}/#/pages/activity/product_group/index?id={分组id}`。

- **必须带 `#/`** —— `app/manifest.json` 里 `h5.router.mode = "hash"`。
  （系统里 `theme_editor/devise/list.vue` 的 `${BaseURL}pages/annex/special/index?id=x`
  是**无 `#` 的旧写法**，H5 下其实打不开，别照抄。）
- H5 域名 ≠ `site_url`。`site_url / api_url` 都是**接口域名**（`api.qianxutec.com`），
  商城 H5 域名是 `app/config/app.js` 里的 `HTTP_H5_URL`：
  **生产 `http://app.qianxutec.com`、本地调试 `http://127.0.0.1:8090`**（manifest `h5.devServer.port=8090`）。
  库里**没有**存这个域名，所以做成前端常量 `DEFAULT_H5_BASE` + 弹窗可改 + `localStorage`
  （key `pg_h5_preview_base`）记忆。
- 线上 H5 站点（nginx）**没有 `X-Frame-Options` / CSP `frame-ancestors`**，所以后台跨域 iframe 可以嵌（已实测响应头）。

**几个易错点**：
- `destroy-on-close` 让每次打开都重建，避免 iframe 常驻在后台；
- 二维码必须在 `@opened` 里画（弹窗未展开时 `$refs.previewQr` 拿不到/尺寸为 0）；
- `previewUrl` 变化（改域名）时 `watch` 里同时重画二维码 + `previewKey += 1` 重载 iframe；
- 保存装修后 H5 里仍是旧内容，需点「刷新预览」（`previewKey += 1`）；
- 弹窗顶部按 `isDirty` 变色提示「有未保存改动，预览的是已保存内容」。

**H5 无法本地编译验证**：`app/package.json` 只有 `mp-html`、没有 scripts，是 HBuilderX 项目。
改完只能在 HBuilderX 里跑一次。本地可做两件事兜底：
① 抽出 `<script>` 块走 `node --check` 验语法；
② 把判定方法正则抽出来用 `new Function` 组装，喂真实接口响应验证（`vue.indexOf('xxx() {')` 要带 `{`，
否则会匹配到 `init()` 里的 `this.xxx()` 调用点）。
另外 `app` 端 request 封装 resolve 的是**整个 CommonResult**，取数据要用 `res.data`。

---

## E. 线上增量脚本约定（2026-09-22 起）

**凡改动了数据库相关的东西（菜单 / 配置 / 表结构）要让线上生效，都要补增量脚本。**

```
crmeb/sql/
├── *.sql                          单点增量脚本（命名如 add_ / fix_ / hide_，均幂等）
├── migration/                     模块级脚本
└── oneclick/                      ★ 一键部署
    ├── 02_patches_all.sql         已有库只补功能（幂等，不动业务数据）★ 线上用这个
    ├── 03_schema_increment_*.sql  只补表结构（无任何数据语句）
    ├── ALL_IN_ONE*.sql            全量导入（含本地数据残留，线上慎用）
    └── deploy.sh / deploy.bat
```

- **线上执行**：`cd crmeb/sql/oneclick && ./deploy.sh patch`
- **新增单脚本后必须追加到 `02_patches_all.sql`**：
  按 `-- ========== BEGIN: xxx.sql ==========` 分节，插在文件末尾
  `SET FOREIGN_KEY_CHECKS = 1;` 之前。
- **脚本必须幂等**：按 `component` / `perms` 定位而**不要用自增 id**（线上 id 与本机可能不同），
  新建用 `INSERT ... SELECT ... WHERE NOT EXISTS` 防重。
- 执行完 **重启 admin/front jar + 清 Redis 配置缓存**，再重新登录刷新菜单。
- **提交 git 时不要带本地业务数据**：`crmeb/crmebimage/public/**`（本地上传的图片）等要留在工作区。

### ⚠️ 铁律：`oneclick/02_patches_all.sql` 是「生成物」，且曾被转码污染（2026-09-23）

该文件由 `crmeb/sql/*.sql` 各独立脚本**拼接**而成，历史上某次拼接经过
**有损转码（UTF-8 → GBK → UTF-8，非法字节被替换成 `?`）**，
导致 **37 个分节里 33 个全部损坏**：

- `add_missing_team_level_tables.sql` 分节的 `CREATE TABLE` **字符串引号被吃掉** → 语法错误
  （`ERROR 1064 near '0.00' COMMENT ...`），线上 `deploy.sh patch` 必然失败、缺表
- 多分节中文注释变 `???`，含中文的 `INSERT` 值会乱码入库（菜单名等）

**源文件 `crmeb/sql/*.sql` 全部完好**（合法 UTF-8），已于 2026-09-23 按分节顺序
用源文件内容重建，并重写文件头；4 个无独立源文件的分节按 SQL 逻辑重写注释。
**结果：`EXIT=0，零 ERROR`，幂等可重跑。**

**规矩**：
1. 往该文件追加分节后，**必须在本地库完整试跑一遍**（`mysql < 02_patches_all.sql`），
   确认 `EXIT=0` 且 `grep -c '^ERROR'` 为 0，再提交。
2. 判定是否被污染：`iconv -f utf-8 -t utf-8 <file> >/dev/null` 报错 = 有非法字节；
   或看到中文变成 `???` / 引号对数量为奇数 = 已损坏。
3. 追加时用二进制方式写入（Python `open(..., 'rb')` + 定位
   `SET FOREIGN_KEY_CHECKS = 1;` 锚点插入），避免编辑器二次转码。

#### ⚠️ `stock_rework_phase1.sql` 幂等修复（同批发现）

该脚本 2 个裸 `ALTER TABLE ... ADD COLUMN`（8 列）**没有存在性判断**
（注释写"幂等"但实际不是）→ 重复执行报 `ERROR 1060 Duplicate column name`，
而 `deploy.sh` 是 `set -euo pipefail`，**会中断后续部署**。
已改为 phase3 同款模式：存储过程 `add_col_if_missing` + 逐列 `CALL`
（MySQL 5.7 无 `ADD COLUMN IF NOT EXISTS`；`DELIMITER` 在本项目 mysql 客户端下可用）。

### 🔒 上线前安全加固（2026-09-23，每次改补丁包都要复查这 5 条）

补丁包要跑在**真实业务的线上库**（`deploy.sh` 的 `DB_NAME=crmeb_java3`），不能按"本机跑通就行"交付。
以下 5 条是 2026-09-23 全量审计发现并修掉的隐患，**判定标准：幂等 + 不覆盖已有业务数据 + 不硬编码库名/id**。

| # | 隐患 | 原写法 | 修法 |
|---|---|---|---|
| 1 | **本地设置快照冲掉线上真实配置** | `local_default_settings.sql` 分节用 `REPLACE INTO eb_system_config VALUES (…304 行…)`，按主键覆盖，含 `pay_weixin_app_*`、`APP_PRIVATE_KEY`、`sms_account/token`、`tx/qn/jd` 存储密钥、`store_brokerage_*` | **已从 `02_patches_all.sql` 停用两行 REPLACE**（含 `eb_page_diy` 装修页），保留源文件备用；要执行必须单独跑并逐项核对 |
| 2 | **硬编码库名** | 3 个源脚本内 `USE \`crmeb\`;`，在拼接文件里残留 1 处 → 线上（库名 `crmeb_java3`）会打到错库或 `ERROR 1049` 中断其后全部语句 | 删除所有 `USE`，目标库由连接/命令行决定 |
| 3 | **覆盖线上订货商等级** | `stock_upgrade_conditions.sql` 4 条 `UPDATE eb_stock_level … WHERE id = 1..4` → 直接改掉线上拿货折扣 / 平级比例 / 升级门槛 | 加四条件锁：`AND name='总代' AND sort=10 AND discount=80.00`（须同时命中才算"仍是初始占位行"） |
| 4 | **凭空插入可升级的等级** | `stock.sql` 按 name 补插 `一级代理/二级代理/普通代理`；`区级订货商` 只判 name 不存在就插（`cond_self_buy=1/1000元` → 会被 `StockServiceImpl:1554` 的自动升级命中，改变线上拿货价） | 占位层级改为**仅当 `eb_stock_level` 为空**（`SET @stock_level_seed`）才播种；`区级订货商` 追加 `AND EXISTS(市级订货商)` |
| 5 | **硬编码菜单 id 改名** | `stock_changelog_product_rel.sql` / `stock_issue_batch_20260918.sql` 的 `WHERE id = 660` / `654` | 追加 `AND component='/stock/agent'` / `'/stock/setting'` |

**验收动作（必做）**：`mysql crmeb < 02_patches_all.sql` 跑两遍，要求
`EXIT=0` + `grep -c '^ERROR'` 为 0 + **`eb_stock_level` / `eb_system_config` / `eb_system_menu` 前后 diff 为空**。
只在本地跑一遍看不出副作用，必须做前后快照对比。

---

## F. 分销商等级模块（2026-09-23，原「会员返佣配置」）

- **表**：`eb_distributor_level`（独立于 `eb_system_user_level`），
  含 `name` / `grade`（权重）/ 三项返佣比例 / 8 项升级条件（每项带 `*_relation`：1=与，2=或）。
  已接入统计 + 自动升级判定。
- **后端**：`DistributorLevelController` 路由 `api/admin/distributor/level`
  （list / info/{id} / save / update/{id} / delete/{id} / use/{id}/{isShow} / recalc/{uid}）。
- **前端**：`views/distribution/distributorLevel/index.vue` + `api/distributorLevel.js`。
- **菜单**：`eb_system_menu` id=559，`name=分销商等级`，`component=/distribution/distributorLevel`；
  **`perms` 仍是 `admin:system:user:level:brokerage:*`（故意保留，避免影响角色授权）**，
  所以新 Controller 的 `@PreAuthorize` 也用这组旧 perms。
- **未删（重要）**：`SystemUserLevelBrokerage` model / Service / Dao / Request
  —— `OrderPayServiceImpl:797` 按**用户会员等级**取返佣比例算佣金，动了会破坏结算链路。
  本次只删了旧页面专用的 `SystemUserLevelBrokerageController` + `...SaveRequest`。
- 增量脚本：`crmeb/sql/distributor_level_20260923.sql` +
  `distributor_level_upgrade_20260923.sql`（已并入 `02_patches_all.sql`）。
- 等级体系整体约定与新增表/字段清单见 `local-dev/level_rules_checklist.md`。

---

## 商品「佣金设置」↔ 运营侧全局口径对照（2026-09-23 全量核对）

商品编辑页「佣金设置」**三个模块**（分销商 / 区域代理 / 团队奖）必须复用运营侧既有参数项，统一约定：
**null = 跟随全局 / 显式 0 = 该商品无此项 / 有值 = 商品级优先**；取值优先级
**等级覆盖 → 一刀切覆盖 → 全局**。

| 模块 | 运营侧页面（全局口径） | 全局参数项 | 商品级参数项 | 结算类 |
|---|---|---|---|---|
| 分销商 | 运营 → 分销商等级 `eb_distributor_level` | 自购/一级/二级返佣(%) | 自购/一级/二级（元 或 %），按等级覆盖 + 一刀切 | `OrderPayServiceImpl` |
| 区域代理 | 运营 → 区域代理设置 `agent_setting` | 代理功能开关、默认奖励比例 省/市/区(%)→ 落库 `eb_agent.ratio` | 省/市/区（元 或 %）+ 代理返佣开关 | `AgentServiceImpl` |
| ~~订货商~~ | **已移出商品页**（2026-09-23） | 拿货折扣(%)、平级规划奖励(%) 见「运营 → 订货 → 商品与库存」按商品独立设置 | 无 | `StockServiceImpl.getProductPrice` / `StockRewardServiceImpl` |
| 团队奖 | 运营 → 团队奖 `eb_system_team_level_config` | 团队极差比例(%)、平级奖比例(%)（**差额法**）、追溯层数 | 团队奖开关 + 极差/平级（元 或 %）按团队等级 | `TeamBrokerageServiceImpl` |

### ⚠️ 订货商不参与商品「佣金设置」（2026-09-23 清理）

订货商在**运营 → 订货 → 商品与库存**已有按商品独立设置（`eb_stock_price` 专用拿货价 + 层级折扣），
商品编辑页的订货商板块属重复入口，**已整体移除**：

- 前端：模板板块 + `stockLevelOptions` + `STOCK_LEVEL_KEYS` + `mergeCommissionConfig` 的 stock 段 + `stockLevelListApi` 全部删除
- 后端模型：`ProductCommissionConfig.Stock` / `StockLevel` 类与 `stock` 字段删除
- 工具类：`ProductCommissionUtil.stockLevelPeerAmountRate` / `findStockLevel` 删除
- 拿货价：`StockServiceImpl.getProductPrice`（SKU + 非 SKU 两条）移除商品级折扣覆盖，恢复
  **规格级专用价 → 商品级专用价 → 层级折扣 → 零售价**
- 奖励：`StockRewardServiceImpl` 返差价回落全局 `stock_diff_reward_status`；
  平级奖只按上级层级 `eb_stock_level.peer_rate`（不再读商品级覆盖），不再注入 `StoreProductService`
- 历史数据：旧 JSON 里的 `stock` 段被 fastjson 忽略（parse 不抛异常、值不生效），
  **任何一次保存都会自动从 JSON 中抹掉**（已用独立 harness 验证 round-trip）

**本次修复的口径偏差**
1. **团队奖极差奖**：商品级「比例」原先直接按比例算全额佣金，**绕过极差差额算法**，
   多等级叠加会超额发放。改为逐商品沿链做差额：商品级比例**替换该等级比例**后继续差额累计，
   商品级金额为该等级固定佣金（不参与累计）；链式 walk 改为按商品维度，`buildChain()` 单独抽方法。
2. **订货商商品级配置**：越级奖先下线，随后整个订货商板块移出商品页（见上）。
3. **区域代理平级/越级推荐奖**：全局页面没有这两项，商品级入口已删，
   结算侧 `assignAgentPeerLeap` / `findNearestAgentAlongSpread` 一并删除（不再可能发放）。
4. **商详佣金气泡**：`ProductServiceImpl.getPacketPriceRange` 原来只读一刀切 direct 字段，
   现按**查看者分销商等级**取一级覆盖（与结算同口径），再回落一刀切 → 旧 is_sub/SKU → 全局比例。

**商品页不再提供入口但后端仍兼容的字段**（保留 round-trip，避免改写历史金额）：
`agent.syncMode`（同总差额模式）、`agent.superiorClaim`（无下级代领）。

---

## G. 本地启动 / 打包硬规则（2026-09-23 定稿）

端口：MySQL 3306（root/123456，库 `crmeb`）｜Redis 6379（密码 123456，admin session db10、
微信 token db14、配置缓存 db7+db10）｜Admin API 8080｜Front API 8081｜Admin Web 9527。
一键：`bash local-dev/start-all.sh`（幂等，默认阻塞常驻——后台任务一退出宿主会回收整棵子进程树）；
自检 `check-all.sh`。用户侧持久运行双击 `local-dev/start-all.bat`；停服 `stop-windows.bat`（`full` 连库一起停）。

### 手工起 jar 三铁律

```bash
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"
cd /d/crmeb-java-3.0/crmeb/crmeb-admin/target && \
  "D:/env/java/jdk8u504-b01/bin/java.exe" -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 \
  -jar Crmeb-admin.jar --server.port=8080
```

1. **必须 `run_in_background=true` 且命令末尾不加 `&`**。加了 `&` 子进程被回收，日志停在
   `Refreshing ApplicationContext` / `Starting Servlet engine`，任务 5 秒后"完成"。
   `nohup` 无效；本机无 `setsid`；`cmd.exe` / `Start-Process` / `[Diagnostics.Process]::Start` /
   `schtasks` 全被安全策略拦截。起完等约 60 秒再 `netstat -ano | grep LISTENING | grep ":8080"`。
2. **必须显式 `--server.port=8080`**（front 8081 同理）。WorkBuddy 宿主会向子进程注入
   `SERVER__PORT=50592`（双下划线 = Spring 松散绑定 `server.port`），优先级**高于 application.yml**，
   而 50592 又被宿主自己占用 → 必报 `Port 50592 was already in use`，而 8080 明明空着，
   极易误判成端口冲突。命令行参数优先级最高。自检 `env | grep -i port`。
3. **必须带 `-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8`**。JDK8 在中文 Windows 上
   `file.encoding` 默认 GBK，Spring/Tomcat 用它编码响应体 → 数据库来的中文全变方块
   （前端静态文案正常，易误判成前端问题）。判定：Python `decode('utf-8')` 抛错 +
   `decode('gbk')` 正常；生效标志是响应头含 `;charset=utf-8`。各 .bat 已内置，**手工敲命令最易漏**。

### Maven 打包

```bash
cd /d/crmeb-java-3.0/crmeb && bash /d/crmeb-java-3.0/local-dev/mvnw.sh \
  -o -DskipTests -pl crmeb-common,crmeb-service,crmeb-admin,crmeb-front -am clean package
```

- 报 `找不到或无法加载主类 org.codehaus.plexus.classworlds.launcher.Launcher` 时即用此包装脚本
  （内置 `MSYS_NO_PATHCONV=1`；**不能写 `exec VAR=val cmd`**，shell 会报 not found）。
- 打包前先停 8080/8081，否则 `clean` 失败（jar 被句柄占用）。
- ⚠️ **`-pl` 绝不能省 `crmeb-front`**：front 跑旧 jar 时行为与新代码**静默不一致** ——
  曾出现旧 `matchRole()` 对未知权限类型落 `return true`，表现为"后台配了权限但人人可见、
  游客照常被拦"，极易误判成逻辑写错。**碰过 common/service 必须两端一起打。**
- **Maven 根 pom 在 `D:/crmeb-java-3.0/crmeb/`**（不是上一层），错目录报
  `Could not find the selected project in the reactor`。
- MSYS 下 `/tmp` 实映射 `C:/Users/ADMINI~1/AppData/Local/Temp`，java/python 读写临时文件
  要用 Windows 路径（`$(cygpath -w ...)`），否则"curl 写了 python 读不到"。

### 前端 dev server（9527）

```bash
export PATH="/c/Users/Administrator/.workbuddy/binaries/node/versions/22.22.2-3:/usr/bin:/bin:/c/Windows/System32:$PATH"
export NODE_OPTIONS="--openssl-legacy-provider"
cd /d/crmeb-java-3.0/admin && node node_modules/@vue/cli-service/bin/vue-cli-service.js serve --port=9527
```

17~30 秒，出现 ` DONE  Compiled successfully` 即成功。改了 .vue 想确保生效**直接重启 dev server**
（watcher 有时不写日志）。判定失败**只看 `Failed to compile` / `Module build failed` / `ERROR in`**
—— grep "error" 全是噪声（`error-page/404.vue`、`es-errors`、`[webpack.Progress]`）。

---

## H. 本次上线步骤（2026-09-23 版本）

### 一、数据库（先备份，再执行补丁）

```bash
# 1) 备份
mysqldump -uroot -p --default-character-set=utf8mb4 crmeb > crmeb_backup_$(date +%Y%m%d).sql

# 2) 执行一键补丁（幂等，含本轮全部 DDL + 数据清理）
#    库名由连接决定：线上 crmeb_java3、本地 crmeb —— 脚本内已无 USE 语句
mysql -uroot -p --default-character-set=utf8mb4 crmeb_java3 < crmeb/sql/oneclick/02_patches_all.sql
# 末尾应输出：CRMEB fix_duplicate_data done / CRMEB oneclick patches done
```

> ⚠️ 补丁包**不再包含**「本地设置快照」（`local_default_settings.sql` 分节）。
> 该分节用 `REPLACE INTO` 按主键覆盖 `eb_system_config` / `eb_page_diy`，
> 会把线上的微信支付配置、短信账号、存储密钥一起冲掉，故已停用。
> 确实需要复刻本地展示设置时**单独执行**并逐项核对：
> `mysql -uroot -p --default-character-set=utf8mb4 crmeb_java3 < crmeb/sql/local_default_settings.sql`

补丁包内含的**数据清理**（`fix_duplicate_data_20260923.sql`，已并入末尾）：
1. `eb_system_config` 同 name 重复行只保留 **id 最大（最后写入）** 的一条 ——
   与 `SystemConfigServiceImpl.get()` 的 fallback（`orderByDesc(id)` 取首条）语义一致，
   **清理不改变任何生效值**；不清理则后台保存这些配置会直接抛「配置名称存在多个」。
2. 菜单清理：`is_delte=1` 的结构错乱残留、错挂在权限占位菜单下的重复「商品与库存」、
   「商品」根菜单下隐藏的重复分组入口。
3. 商品 `commission_config` 摘除已下线的订货商（`stock`）段，全空的直接置 NULL。

### 二、后端（两端必须同时替换）

```bash
# 停服 → 构建（务必带 -pl crmeb-front）→ 启动
cd crmeb && bash ../local-dev/mvnw.sh -o -DskipTests \
  -pl crmeb-common,crmeb-service,crmeb-admin,crmeb-front -am clean package
# 启动必须带 -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8，否则库里的中文全乱码
```

### 三、前端

- admin：`cd admin && npm run build` → 将 `dist/` 部署到后台站点根目录。
- H5（`app/`）：HBuilderX 打 H5 包，注意 `app/config/app.js` 的 `HTTP_H5_URL`/domain
  指向线上域名（`api.qianxutec.com` / `app.qianxutec.com`）。

### 四、缓存与冒烟

- Redis `config_list` 无需手工刷：`get()` 命中不到会自动回查库并回写；如需强制全量重载，
  `DEL config_list` 后重启 front（`asyncConfig=true` 侧）。
- 冒烟清单：
  1. 商品编辑 → 佣金设置：只剩分销商 / 区域代理 / 团队奖 三块，无订货商板块
  2. 编辑某商品保存一次 → 商品 `commission_config` 不再出现 `stock` 段
  3. 订货商拿货价：走「订货商设置 → 拿货价 / 层级折扣」，与商品页无关
  4. 后台任意设置页保存不再报「配置名称存在多个」
  5. 商品分组权限 5 种口径（全部/仅分销商/仅区域代理/仅订货商/仅社群团队）前台表现正确

---

## I. 死代码审计工具与判定规则（2026-09-23 建立）

工具在 `local-dev/tools/`，**纯静态分析、零依赖**（只用标准库），可对整个仓库复跑：

| 脚本 | 作用 |
|---|---|
| `deadcode_scan.py` | Java：零外部引用的类 + 从未使用的 private 方法/字段；产出 JSON |
| `deadcode_scan_front.py` | 前端：从 `main.js`/`permission.js` 出发的 import 图不可达文件 |
| `deadcode_scan_dup.py` | 重复逻辑：方法体规范化后哈希一致的成组方法 |
| `deadcode_apply.py` | 按 JSON 执行删除（class 整文件删；private 成员花括号配平删；顺带清未用 import）。**默认 dry-run**，加 `--apply` 才落盘 |

完整审计结论与逐条判定理由见 **`local-dev/deadcode_audit_20260923.md`**。

### ⚠️ 三个必须保留的判定陷阱规避（改脚本时别删）

1. **注解要从 `class` 关键字位置往前取**，不能从正则匹配起点取。
   否则 `@Service` 被漏读 → `OrderServiceImpl` 这类「按接口注入」的实现类会被误判成死代码（实测踩过）。
2. **`@RestControllerAdvice` 必须单独列入框架注解白名单**（它不在 `@Component` 家族字面量里）。
   否则 `GlobalExceptionHandler` / `ResultAdvice` 会被误删，**直接搞坏全局异常处理与统一响应包装**。
3. **重复逻辑检测不能剥离字符串字面量**。
   实测 `ProductUtils.getTaobaoProductInfo` / `getTmallProductInfo` 唯一差异就在
   `item.getString("desc")` vs `getString("descUrl")`，剥离字面量后会误报为重复。

### 判定边界（哪些「看着没用」的东西不能删）

- `XxxServiceImpl`：源码无名字引用，但由 `@Service` + 按接口 `@Autowired` 装配 → **删了启动就失败**。
- `@RestController` / `@Configuration` / `@Aspect` / `@RestControllerAdvice`：容器/路由可达。
- `implements ResponseBodyAdvice / HandlerInterceptor / WebMvcConfigurer / BaseMapper / OncePerRequestFilter`：框架装配。
- Lombok（`@Data/@Getter/@Setter/@Accessors/@Builder`）类里的私有字段：getter/setter 编译期生成，**不是无用字段**。
  脚本对这类文件整文件跳过字段判定。
- 带注解的私有方法（`@PostConstruct` / `@EventListener` / `@Scheduled`）：容器回调，不是死代码。
- `admin/src/views/**/index.vue` 中内容相同的路由容器（`<router-view/>`）：每个都被各自路由引用，属正常设计。
- **`app/` 目录一律不做判定**：uni-app 用 `easycom` 按目录约定自动注册组件、页面由 `pages.json` 驱动，
  import 图分析在此不成立（会把 `manifest.json`、`vue.config.js` 都误报为不可达）。

### 验证动作（缺一不可）

```bash
# 1) 三轮复扫必须同时为 0（级联失效会在第二轮才暴露）
$PY local-dev/tools/deadcode_scan.py            # 零引用类 = 0，未用 private 成员 = 0
$PY local-dev/tools/deadcode_scan_front.py admin/src   # 不可达 = 0

# 2) 兜底：对被删类名做全仓库 grep，确认零残余引用

# 3) Java 全量重编（先 touch 全部源文件！）
cd crmeb && find . -name "*.java" -not -path "*/target/*" -exec touch {} +
bash ../local-dev/mvnw.sh -o -DskipTests -pl crmeb-common,crmeb-service,crmeb-admin,crmeb-front -am compile

# 4) 后台生产构建
cd admin && NODE_OPTIONS=--openssl-legacy-provider node node_modules/@vue/cli-service/bin/vue-cli-service.js build
```

**为什么要 `touch` 源文件**：Maven 增量编译器按「源文件时间戳 vs .class 时间戳」判断，
只删了**依赖模块**的类时，下游模块源文件没变 → 会输出 `Nothing to compile`，
等于**根本没验证**。`mvn clean` 又会被运行中的 jar 锁住，所以用 touch 代替。

**为什么不能 `rm -rf */target/classes`**：会触发宿主的安全删除拦截
（`SAFE_DELETE_BULK_CONFIRM_REQUIRED`，一次 1578 个文件超阈值），直接用 touch 方案。
