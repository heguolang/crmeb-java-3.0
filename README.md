<div align="center">

# 黔序科技商城系统

</div>

> **仓库说明**  
> 本仓库为 **黔序科技商城系统 完整版**（`qianxu` 后端 + `admin` 管理端 + `app` 移动端 一体）。  
> 本版额外含：会员等级返佣、团队等级/团队奖、注册默认推广员、订货商实体库存等能力。  
> 系统基于 Apache-2.0 协议的开源电商系统二次开发，遵循原协议保留其版权声明；**黔序商城系统软件 V1.0 已登记软件著作权（登记号 2025SR2146980，贵州黔序科技有限公司）**。

<div align="center">
    <a href="LICENSE">
        <img src="https://img.shields.io/badge/License-apache2.0-green.svg?style=flat" />
    </a>
</div>

---

### 📖 项目介绍

黔序科技商城系统，是贵州黔序科技有限公司基于开源电商解决方案构建的多端一体化商城平台。系统采用 Java + Vue + uni-app 技术栈，前后端分离，借助 uni-app 跨端能力覆盖小程序、公众号、H5、APP 多端，数据统一、业务同步。

在开源底座之上，本系统针对订货分销业务做了深度定制：

- **订货商体系**：订货商管理、实体库存（订单完成入库、下级付款出库的防超卖口径）、库存记录流水
- **分销体系**：分销商等级、推广业绩与佣金台账、上级关系链、注册默认分销等级
- **团队奖励**：团队等级、团队奖、会员等级返佣

---

### ✨ 系统优势

- **成熟稳定的架构**：后端 SpringBoot + SpringSecurity，前端 Vue + ElementUI（PC管理端）与 uni-app（移动端），多端并行开发，结构清晰。
- **规范的接口设计**：提供规范的 RESTful API 与数据结构，接口复用率高，逻辑层次清晰，便于二次开发与系统集成。
- **数据与统计**：集成 ECharts 实现多维数据看板，支持订单、用户、资金等多维度统计分析。
- **精细权限控制**：基于 Spring Security 实现角色权限控制，可精确到按钮级别。
- **高可用设计**：内置 Redis 队列，削峰解耦，提升系统并发能力与稳定性。

---

### 🛠️ 技术栈与运行环境

| 类别 | 技术 |
|------|------|
| 后端框架 | SpringBoot 2.2.6.RELEASE |
| ORM | Mybatis Plus 3.3.1 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 构建工具 | Maven 3.6.1 |
| PC管理端 | Vue 2.x + Element UI 2.13 |
| 移动端 | uni-app（H5 + 公众号 + 微信小程序） |
| 开发工具 | HBuilderX（移动端） |
| 运行环境 | Java 1.8、Node 16（实测 v16.20.2 / npm 8；node ≥17 构建前端需加 `NODE_OPTIONS=--openssl-legacy-provider`，node ≤16 加了会构建失败） |

---

### 📦 项目结构

```
qianxu-mall
├── admin/          PC端管理端（Vue + ElementUI）
├── qianxu-app/     移动商城（UniApp标准开发，支持H5 + 微信小程序）
├── qianxu/         后端API服务（Java SpringBoot + MybatisPlus，mvnw 可直接离线构建）
├── db-data/        初始图片资源与说明
├── qianxu/sql/     00_full_install.sql（全新安装）+ 01_increment.sql（增量，幂等可重复执行）
├── deploy/         部署脚本与 nginx 配置（含 qianxuqifu-182 自用副库全套）
└── 接口文档/       部署后也可在线查看 Swagger 文档
```

---

### 🚀 下游部署速查（clone 后照这个顺序走）

**1. 建库 + 导数据**

```bash
DB=qianxuqifu bash qianxu/sql/install_all.sh
```

它会自动建库、按正确顺序导 `00_full_install.sql` + `01_increment.sql`、导完校验关键表。
**两份都要导**：`00` 冻结于 2026-09-30，合同 / 服务需求 / 签约渠道 / 发票 / 短信这几批表只在 `01` 里，
只导 `00` 的结果是「后台菜单都在、点进去报表不存在」。手工导的话：

```bash
mysql -uroot -p qianxuqifu < qianxu/sql/00_full_install.sql
mysql -uroot -p qianxuqifu < qianxu/sql/01_increment.sql   # 幂等，可重复执行
```

**2. 改配置（三处不改就一定会踩坑）**

| 位置 | 默认值（仓库内） | 上线必须改成 |
|---|---|---|
| `admin` 构建 | `admin/.env.production` = `http://api.qianxutec.com` | 你自己的后台 API 域名，且**必须 https**（http 会被浏览器按混合内容拦截，页面接口全挂但接口本身是好的，极难查） |
| `qianxu-app` 构建 | `qianxu-app/config/app.js` = `127.0.0.1:8081` / `HTTP_H5_URL` = `127.0.0.1:8090` | 线上 API / H5 域名（忘了改 = 编译出一个装上线必定连不上的包，编译过程完全不报错） |
| 后端启动 | `application-prod.yml` 里 `password: __SET_MYSQL_PASSWORD__` | 真实口令用命令行传入，不要写进文件 |

> 两个构建脚本已内置「接线预检」，域名不对会直接判坏包并中断，别绕过：
> `bash deploy/qianxuqifu-182/build-admin.sh`（后台）、`bash deploy/qianxuqifu-182/build-h5.sh`（H5）。

**3. 构建后端**

```bash
cd qianxu && ./mvnw clean package -DskipTests   # wrapper 已入库，离线可用（首次会下载 Maven 发行包）
# 产物：qianxu/qianxu-admin/target/Qianxu-admin.jar、qianxu/qianxu-front/target/Qianxu-front.jar
```

**4. 启动**

```bash
DB_PASS='真实口令' bash deploy/qianxuqifu-182/qifu-service.sh start
```

脚本用命令行覆盖 `url / username / password / port / imagePath / redis.database`，
优先级高于 `application-prod.yml`；端口 admin 21800 / front 21810，Redis 主库 db12（**admin 与 front 必须共用同一 db**，分库会导致「后台改配置前台不生效」）。

**平台限制**：H5 构建脚本依赖 HBuilderX 自带的 node18（`vue-cli 4 + node-sass`，系统 node22 编不了），
目前只在 macOS 验证过；Linux/Windows 需自行替换 `build-h5.sh` 里的 `HBX / NODE / CLI` 三个路径。

---

### 📚 参考资料

以下为上游开源项目 QIANXU 的文档资源，二次开发时可参考：

| 文档 | 链接 |
|------|------|
| 使用文档 | 内部资料（已移除外部链接） |
| 数据字典 | 内部资料（已移除外部链接） |
| 二开文档 | 内部资料（已移除外部链接） |

---

### 📄 开源协议

本项目基于 [Apache License 2.0](LICENSE) 发布，源自 QIANXU Java 3.0 开源版本。依照协议要求，源文件中的原始版权声明予以保留。

© 2026 贵州黔序科技有限公司
