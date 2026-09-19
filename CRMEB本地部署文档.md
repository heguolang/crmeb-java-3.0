# CRMEB Java 3.0 本地部署文档（Windows）

> ⭐ **部署后数据库只需执行这一个脚本**：`crmeb/sql/oneclick/ALL_IN_ONE.sql`
> `mysql -uroot -p密码 --default-character-set=utf8mb4 < ALL_IN_ONE.sql`（建库+全量结构数据+全量补丁）
> 已有生产库只补功能时改跑 `crmeb/sql/oneclick/02_patches_all.sql`（幂等，不清数据）。

> 部署日期：2026-09-17　部署目录：`D:\crmeb-java-3.0`　代码来源：`https://github.com/heguolang/crmeb-java-3.0.git`（master @ abe4b36）

---

## 一、环境清单

| 组件 | 版本 | 安装位置 | 端口 | 凭据 |
|---|---|---|---|---|
| JDK | OpenJDK 1.8.0_504 (Temurin) | `D:\env\java\jdk8u504-b01` | — | — |
| Maven | 3.9.16 | `D:\env\apache-maven-3.9.16` | — | 本地仓库 `D:\env\maven-repo` |
| MySQL | **8.0.29**（与生产对齐） | `D:\env\mysql-8.0.29-winx64` | 3306 | root / **123456** |
| Redis | 5.0.14.1 (Windows) | `D:\env\redis` | 6379 | 密码 **123456** |
| Node.js | 22.22.2 | WorkBuddy 托管目录 | — | — |
| HBuilderX | 5.26 alpha | `E:\HBuilderX\HBuilderX` | — | — |

**版本沿革（2026-09-18 两次切换）**：本机最早用 MariaDB 10.6.28 代替 MySQL（当时 `mysqld.exe` 因缺 VC++ 运行库无法启动）；当天上午切到 MySQL 5.7.38，当晚应汪总要求**升级为 MySQL 8.0.29**：
- 服务名 `MySQL80`（自动启动），配置文件 `D:\env\mysql-8.0.29-winx64\my.ini`，数据目录 `...\data`；`lower_case_table_names=1`（8.0 只能初始化时定）；`sql_mode` 不含 8.0 已移除的 `NO_AUTO_CREATE_USER`；排序规则仍用 `utf8mb4_general_ci`（不用 8.0 默认 0900_ai_ci，与旧库一致）。JDBC 零改动：驱动 mysql-connector-java 8.0.26，URL 已带 `serverTimezone\allowPublicKeyRetrieval\useSSL=false`。
- 切换前备份：MariaDB 全量 `D:\env\backup\crmeb_mariadb_20260918.sql`（131 表）、升级 8.0 前 MySQL 5.7 全量 `D:\env\backup\crmeb_mysql57_20260918_before8.sql`（131 表，导入 8.0 无报错）。
- MariaDB 服务 `CRMEB-DB` 已停止并改为**手动启动**（数据目录 `D:\env\mariadb-10.6.28-winx64\data` 原样保留，作为回退手段；如需回退：停 MySQL80 → 启动 CRMEB-DB，两者都占用 3306，不要同时开）。
- 5.7.38 已按要求清除：服务已 remove、目录与安装包已删；客户端用 `D:\env\mysql-8.0.29-winx64\bin\mysql.exe`。
- **MariaDB 专用兼容脚本已作废并删除**：`crmeb/sql/mariadb_any_value_compat.sql`（给 MariaDB 造一个 `ANY_VALUE()` 恒等函数）在 MySQL 5.7 上**有害** —— 用户函数会遮蔽内置函数，反而偏离生产行为。该文件已从仓库删除，`crmeb/sql/oneclick/02_patches_all.sql` 里对应的补丁段也已移除。库中残留的同名函数已 `DROP FUNCTION IF EXISTS ANY_VALUE`，实测 `information_schema.ROUTINES` 中 crmeb 库 0 条自定义函数，`SELECT ANY_VALUE(id) ... GROUP BY uid` 走内置函数正常。
- MariaDB 兼容函数定义（仅回退 MariaDB 时才需要）备份在 `D:\env\backup\any_value_func_rollback.sql`，内容为：
  ```sql
  USE crmeb;
  CREATE FUNCTION IF NOT EXISTS ANY_VALUE(x LONGTEXT) RETURNS LONGTEXT
  DETERMINISTIC NO SQL
  RETURN x;
  ```

---

## 二、项目结构

```
D:\crmeb-java-3.0
├─ crmeb/                    后端（Spring Boot 2.2.6 + MyBatis-Plus 3.3.1）
│  ├─ crmeb-admin/           后台管理 API（:8080）→ target/Crmeb-admin.jar
│  ├─ crmeb-front/           会员端 API（:8081）   → target/Crmeb-front.jar
│  ├─ crmeb-common/
│  ├─ crmeb-service/
│  ├─ sql/                   建表与补丁脚本
│  └─ crmebimage/            上传图片资源（531 个文件）
├─ admin/                    管理后台前端（Vue2 + element-ui，:9527）
├─ app/                      会员端 uni-app（H5，:8090，HBuilderX 运行）
├─ db-data/                  数据导出包（含全量业务数据 + 图片）
└─ local-dev/                本地脚本目录
   ├─ start-windows.bat      一键启动（本次新增）
   ├─ stop-windows.bat       一键停止（本次新增）
   └─ start.sh / stop.sh     macOS 版本（原有）
```

---

## 三、部署步骤（复现用）

### 1. 获取代码

```bash
# 网络不稳时优先用 ZIP 包（本次实际做法）
curl -L -o D:\crmeb-master.zip https://codeload.github.com/heguolang/crmeb-java-3.0/zip/refs/heads/master
tar -xf D:\crmeb-master.zip -C D:\
mv D:\crmeb-java-3.0-master D:\crmeb-java-3.0
```

### 2. 初始化数据库（按顺序，前 13 步为建表/补丁，第 14 步为业务数据）

```bash
set MYSQL=D:\env\mysql-8.0.29-winx64\bin\mysql.exe -uroot -p123456 --default-character-set=utf8mb4 crmeb
%MYSQL% < crmeb\sql\Crmeb_v3.0.sql
%MYSQL% < crmeb\sql\add_missing_team_level_tables.sql
%MYSQL% < crmeb\sql\add_missing_columns.sql
%MYSQL% < crmeb\sql\upgrade_team_level_direct.sql
%MYSQL% < crmeb\sql\add_login_notice_config.sql
%MYSQL% < crmeb\sql\add_user_update_password_menu.sql
%MYSQL% < crmeb\sql\add_admin_log_menu_and_table.sql
%MYSQL% < crmeb\sql\reset_default_category.sql
%MYSQL% < crmeb\sql\update_copyright_company_name.sql
%MYSQL% < crmeb\sql\agent.sql
%MYSQL% < crmeb\sql\stock.sql
%MYSQL% < crmeb\sql\update_deploy_config.sql
%MYSQL% < crmeb\sql\export_admin_settings.sql
%MYSQL% < db-data\crmeb_full_data_export.sql
```

> **不要再执行 `mariadb_any_value_compat.sql`**（该文件已删除）。它原本是 MariaDB 时代的兼容补丁，作用是给 MariaDB 造一个 `ANY_VALUE()` 恒等函数。现库为 MySQL 5.7，`ANY_VALUE()` 是内置函数，再建同名用户函数会遮蔽内置函数。若从 MariaDB 迁库过来发现残留，清理一次即可：
> ```sql
> DROP FUNCTION IF EXISTS ANY_VALUE;
> ```

导入后：**131 张表**、用户 10 条、后台设置/菜单/站点配置均完整。生产/空库环境可直接用一键脚本 `crmeb\sql\oneclick\deploy.bat`（生产同款，其合并补丁里对应的 MariaDB 兼容段已同步移除）。

### 3. 图片资源

```bash
xcopy /E /Y /I db-data\crmeb_image\crmebimage\*  crmeb\crmebimage\
```

对应 `application.yml` 的 `crmeb.imagePath: D:/crmeb-java-3.0/crmeb/`（注意斜杠结尾）。

### 4. 配置修改（已改好，仅列出差异）

`crmeb/crmeb-admin/src/main/resources/application.yml` 与 `crmeb/crmeb-front/src/main/resources/application.yml`：

| 配置项 | 原值 | 现值 |
|---|---|---|
| `crmeb.imagePath` | `/Users/qianxu/.../crmeb/`（macOS 路径） | `D:/crmeb-java-3.0/crmeb/` |
| `spring.datasource.password` | `root` | `123456` |
| `crmeb.demoSite` | `true`（手机号掩码成 139\*\*\*\*0004） | `false`（显示完整手机号，两个模块均已改） |

> `demoSite` 仅影响手机号掩码。当前用启动参数 `--crmeb.demoSite=false` 覆盖（`start-windows.bat` 内置），源码配置也已改为 false，重新打包后自动生效。若要对访客隐藏完整手机号，把该值改回 `true` 重启即可。

Redis 保持 `127.0.0.1:6379`、密码 `123456`、库 `10`；两个 jar 均如此。

### 5. 启动 Redis

```bash
D:\env\redis\redis-server.exe --port 6379 --requirepass 123456 --maxmemory 512mb
```

### 6. 后端打包与启动

```bash
set JAVA_HOME=D:\env\java\jdk8u504-b01
cd D:\crmeb-java-3.0\crmeb
D:\env\apache-maven-3.9.16\bin\mvn.cmd package -DskipTests -Dmaven.repo.local=D:/env/maven-repo

java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar crmeb\crmeb-admin\target\Crmeb-admin.jar --server.port=8080
java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar crmeb\crmeb-front\target\Crmeb-front.jar --server.port=8081
```

> 启动必须显式带 `--server.port`：环境里存在 `SERVER_PORT` 变量会被 Spring Boot 的松散绑定拾取，导致端口被改成随机值（本次曾变成 63510 并报端口占用）。原 macOS 脚本 `local-dev/start.sh` 里也是用 `unset SERVER_PORT` 规避的。
>
> **必须带 `-Dfile.encoding=UTF-8`**：中文 Windows 上 Java 8 的默认编码是 GBK，JSON 序列化会输出 GBK 字节，浏览器按 UTF-8 解析后接口返回的中文全变成"�"方块。原作者在 macOS（默认 UTF-8）下不会遇到。详见第六节踩坑第 0 条。

### 7. 管理后台前端

```bash
cd D:\crmeb-java-3.0\admin
npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
set NODE_OPTIONS=--openssl-legacy-provider      # Node 22 + webpack 4 必需
npm run dev -- --port=9527
```

首次编译约 5 分钟，成功后访问 http://127.0.0.1:9527 。

### 8. 会员端 H5（HBuilderX）

```bash
E:\HBuilderX\HBuilderX\cli.exe project open --path D:\crmeb-java-3.0\app
E:\HBuilderX\HBuilderX\cli.exe launch web --project D:\crmeb-java-3.0\app --browser Chrome
```

- 接口地址：`app/config/app.js` → `http://127.0.0.1:8081`（无需修改）
- H5 端口：`app/manifest.json` → `h5.devServer.port = 8090`
- 若报 scss 编译错误，需先在 HBuilderX 安装 `compile-node-sass` 插件：`cli.exe installPlugin --name compile-node-sass`

---

## 四、服务地址

| 服务 | 地址 | 说明 |
|---|---|---|
| 管理后台 | http://127.0.0.1:9527 | 账号 **admin / 123456** |
| 后台 API | http://127.0.0.1:8080 | 接口文档 http://127.0.0.1:8080/doc.html |
| 会员端 API | http://127.0.0.1:8081 | H5 / 小程序调用 |
| 会员端 H5 | http://127.0.0.1:8090 | HBuilderX 运行 |

登录接口验证（已实测返回 200 与 token）：

```bash
curl -X POST http://127.0.0.1:8080/api/admin/login \
     -H "Content-Type: application/json" \
     -d '{"account":"admin","pwd":"123456"}'
```

---

## 五、一键启停脚本

```bash
D:\crmeb-java-3.0\local-dev\start-windows.bat     # 依次拉起 MySQL80(服务)/Redis/8080/8081/9527
D:\crmeb-java-3.0\local-dev\stop-windows.bat      # 停止两个 jar + 前端
D:\crmeb-java-3.0\local-dev\stop-windows.bat full # 连带停止 Redis 与 MySQL80 服务（net stop，需管理员）
```

脚本已内置 `set SERVER_PORT=` 与 `NODE_OPTIONS=--openssl-legacy-provider`。数据库改为 MySQL 5.7 后，`start-windows.bat` 不再直接拉进程，而是检测 3306 → 未监听时 `net start MySQL80`（服务为自动启动，正常开机即在跑）；`stop-windows.bat full` 用 `net stop MySQL80` 优雅停库，**不再直接 kill 数据库进程**。

---

## 六、踩坑记录

0. **接口中文全部显示"�"方块（前后端都乱）——真正的根因**：中文 Windows 上 Java 8 默认编码是 GBK，后端 JSON 序列化走了 JVM 平台默认编码，接口输出的中文是 GBK 字节（如 `realName` = `\xb3\xac\xbc\xb6\xb9\xdc\xc0\xed\xd4\xb1`），浏览器按 UTF-8 解析就全是替换符。特征：编译进前端的静态文案正常、**所有接口数据里的中文全乱**。修复：启动命令加 `-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8`（`start-windows.bat` 已内置），重启后实测登录接口与首页接口均返回合法 UTF-8（"超级管理员"、"春节快乐"等）。
   附带的字体加固（非本次乱码主因，但建议保留）：项目大量使用 macOS 字体 `PingFang SC` 与仅含拉丁字形的 D-DIN-PRO（`.regular`/`.semiBold` 类），已在 `admin/src/assets/fonts/font.css`、`app/static/fonts/font.css` 追加 `Microsoft YaHei / SimHei / Hiragino Sans GB` 回退，并在 `app/App.vue` 增加 view/text 全局中文兜底。另外本机未装 Chrome 只有 Edge，已用 `cli.exe config set --key browser.chrome.path` 将 HBuilderX 运行浏览器指向 Edge；若在 WorkBuddy 内嵌预览窗口里仍见方框，属预览内核缺中文字体，用系统浏览器访问即可。

0.5. **后台登录后提示"服务器数据异常，请联系管理员"【已解决】**：当时的库是 MariaDB 10.6，没有 MySQL 5.7 的内置函数 `ANY_VALUE()`，首页统计接口 `/admin/statistics/home/*` 全部报 `FUNCTION crmeb.ANY_VALUE does not exist`。**2026-09-18 切换到 MySQL（5.7 → 8.0.29）后从根上消失**（内置函数天然可用），`/admin/statistics/trade/data` 实测 200。原先的兼容脚本 `mariadb_any_value_compat.sql` 已删除，切勿在 MySQL 上重建同名函数，详见第一节「版本沿革」。

1. **MySQL 5.7 `mysqld.exe` 静默退出【已解决】**：根因是缺 VC++ 运行库（`vcruntime140.dll` / `vcruntime140_1.dll` / `msvcp140.dll`），当时临时用 MariaDB 10.6 顶上。现运行库已补齐，**2026-09-18 已正式切回 MySQL**（先 5.7.38、当晚升级 8.0.29），与生产环境（宝塔 MySQL）一致。
2. **`mvnw.cmd` 无法下载 wrapper**：直连 `repo.maven.apache.org` 失败，改用本机 Maven + 阿里云镜像（已配置在 `D:\env\apache-maven-3.9.16\conf\settings.xml`）。
3. **端口被环境变量覆盖**：必须 `--server.port` 显式指定或清除 `SERVER_PORT`。
4. **Redis 需要密码**：项目配置 `password: 123456`，启动时加 `--requirepass 123456`；早期用过的 `redis-lite`（Node 实现）不支持 Lua 脚本，CRMEB 不可用，已替换为真实 Redis。
5. **npm 安装极慢/中断**：配置 npmmirror 源；中断后重新执行 `npm install` 可续传，切勿在解包中途强杀（会留下半解包的模块，表现为 `Cannot find module`）。
6. **HBuilderX 首次运行 uni-app**：需下载编译工具与 `compile-node-sass` 插件，等待完成即可。

---

## 六点五、订货商模块（二次开发，2026-09-17）

自定义开发的批发订货模块，代码位于 `crmeb/crmeb-*` 的 `com.zbkj.*.stock` 包，管理后台入口"订货管理"，会员端接口前缀 `api/front/stock`。

### 层级与升级
- 5 级层级（sort 越小层级越高）：分公司(40%) / 全国(50%) / 省级(60%) / 市级(70%) / 区级(80%)，折扣为拿货价百分比。
- 升级条件存于 `eb_stock_level`：自购消费(cond_self_buy/self_buy_amount)、直推订单业绩(cond_direct/direct_order_amount)、团队伞下业绩(cond_team/team_amount)、购买指定商品(cond_product/upgrade_product_ids)，组合方式 condition_logic（0=或 1=与），另含平级奖比例 peer_rate。
- 触发时机：上级审核通过（auditOrder）与确认收款（confirmPay）后自动调用 `StockServiceImpl.checkAndUpgrade(uid)`。
- 初始化/迁移 SQL：`crmeb/sql/stock_upgrade_conditions.sql`（重建库后需重新执行）。

### 后台管理入口（2026-09-17 补齐 UI）
- **层级与升级条件**：订货 → 订货代理 → 「层级设置」按钮 → 弹窗内每行「升级条件」按钮，可编辑四项条件开关与阈值、或/与组合方式、平级奖比例（保存到 eb_stock_level）。
- **规则开关**：订货 → 奖励规则（/stock/setting）→ 订单上级审核 / **上级代理发货（stock_parent_deliver）** / **上级无库存等待时长（stock_up_search_hours，默认 12 小时）** / 差价奖励 / **阶梯业绩奖励（周期：月度/季度/年度）**。原「平级奖励比例与代数」全局配置已取消（平级奖比例改在层级设置里按层级配置）。
- 注意：这两页读取/保存走 `/admin/stock/level/*` 与 `/admin/stock/setting/*` 接口，新配置键已加入后台 StockController 的白名单数组；若再新增配置键需同步修改 getSetting/saveSetting 两处 keys。

### 商品加入制（2026-09-18 调整）
- 商品与库存页**不再默认展示全部商品**：需点「添加商品」从商城商品中选择加入（`eb_stock_product_rel`）后才会出现在订货模块，再设置拿货价/库存；可「移除」。
- 会员端订货商品中心同样只显示已加入的商品。
- 接口：`/admin/stock/product/selectList`（可选商品）、`/product/add`（批量加入）、`/product/remove`。

### 订货商管理 与 变更记录（2026-09-18 调整）
- 「订货代理」菜单改名「订货商管理」；操作列按钮统一为胶囊样式、每行 3 个（商品与库存页同样式）。
- 新增菜单「订货商变更记录」（/stock/changelog）：记录订货商 新增/层级变更/上级变更/状态变更/删除，含自动升级（备注"满足升级条件自动升级"），表 `eb_stock_change_log`。
- 「提现管理」菜单已从数据库菜单删除（eb_system_menu id 655/670 置 is_delte=1），页面文件已删除。
- 迁移 SQL：`crmeb/sql/stock_changelog_product_rel.sql`（重建库后需重新执行）。

### 库存与向上匹配（规则 7）
- 上级可用库存 = 上级历史已付款采购数量 − 已供应给直接下级的数量（按商品计）。
- 下单时若直接上级库存不足：订单**不进入正常审核流**，状态仍为待审核但记录 `up_search_time`（等待起点）、`up_search_num=0`；等待 `stock_up_search_hours`（默认 12 小时）后，在下级**下次下单或查询我的订单时**懒触发 `processUpSearchOrders`：沿上级链向上找第一个有货的更高级上级，把代理改挂过去并释放订单；上级链均无货则挂到总部（总部走云仓库存）。审核开关关闭的订单释放时补扣云仓库存。
- 等待期内上级不可审核该订单；上级审核通过时若自己库存不足会被拦截。

### 上级发货开关
- 系统配置 `stock_parent_deliver`：0=总部（后台）发货（默认）；1=上级代理在会员端发货。
- =1 时后台 `sendOrder` 对有上级的订单拦截，需上级调用会员端接口 `POST api/front/stock/order/parentSend` 填快递发货。

### 阶梯业绩奖励（2026-09-18 改造，替代原"级差"模式）
- 不再是级差（按级别差额）模式，改为**阶梯业绩一次性奖励**：订货商统计周期内团队业绩，达到某档阶梯（`eb_stock_ladder`）即按该档规则发放一次性奖励，所有订货商规则一致。
- 每档奖励**二选一**：固定金额 `reward`（>0 时优先）或 团队业绩 × 比例 `rate`%（固定金额为 0 时按比例）。区间匹配 min ≤ 业绩 < max（max=0 表示不限）。
- 结算周期由系统配置 `stock_ladder_cycle` 决定（1=月度 / 2=季度 / 3=年度）；定时任务 `StockLadderSettleTask`（crmeb-admin）在每月 1 日 01:00 自动结算上一周期，也可在后台奖励规则页手动指定周期与月份结算（接口 `/admin/stock/reward/monthlySettle`，参数 type + month）。
- 初始化/迁移 SQL：`crmeb/sql/stock_ladder_period_reward.sql`（重建库后需重新执行）。

### 平级奖
- 全局配置（stock_peer_status/rate/generations）已**取消不使用**；平级奖比例改为按层级配置：`eb_stock_level.peer_rate`，在「订货代理 → 层级设置」中编辑。
- 结算规则（`StockRewardServiceImpl.calcPeerReward`）：订单订货商的**直接上级若与订货商同级**，按该层级的 peer_rate 比例拿平级奖。

### 奖金并入佣金（2026-09-17 调整）
- 订货奖金（差价/阶梯业绩/平级）入账时**同步计入用户佣金余额**（`user.brokerage_price`）并写一条已完成状态的 `eb_user_brokerage_record`（标题"订货奖金"，不冻结），阶梯业绩周期结算同样处理（备注"阶梯业绩结算|周期"）。
- 会员端奖金中心不再有独立提现入口/提现记录，「申请提现」按钮直接跳转系统统一佣金提现页 `/pages/users/user_cash/index`，走 CRMEB 原生佣金提现（user_extract）逻辑。
- 管理后台「订货 → 提现管理」菜单已移除（路由 stock.js 中删除，页面文件保留）；会员端的 stock/withdraw 接口保留但前端不再调用。

---

## 七、数据备份

```bash
D:\env\mysql-8.0.29-winx64\bin\mysqldump.exe -uroot -p123456 --default-character-set=utf8mb4 --single-transaction --routines --triggers --events --databases crmeb > D:\env\backup\crmeb_backup.sql
```

> 历史备份均可回退：MariaDB 全量 `D:\env\backup\crmeb_mariadb_20260918.sql`、MySQL 5.7 全量 `D:\env\backup\crmeb_mysql57_20260918_before8.sql`；MariaDB 服务 `CRMEB-DB` 与数据目录 `D:\env\mariadb-10.6.28-winx64\data` 原样保留。

图片一并备份 `crmeb\crmebimage\` 目录。`db-data\` 目录是上一台机器（macOS）导出的完整数据包，含导入顺序说明，可作为还原参考。
