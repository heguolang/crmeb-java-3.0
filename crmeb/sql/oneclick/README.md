# CRMEB SQL 部署脚本

目录：`crmeb/sql/oneclick/`

## ⭐ 部署后只需要执行这一个文件

```
crmeb/sql/oneclick/ALL_IN_ONE.sql
```

```bash
mysql -uroot -p密码 --default-character-set=utf8mb4 < ALL_IN_ONE.sql
```

执行完 **重启 jar**；若开了 Redis 配置缓存，清一次。

| 场景 | 执行什么 |
|------|----------|
| 全新部署 / 重建库 | `ALL_IN_ONE.sql`（建库 + 138 张表结构与基础数据 + 全量补丁） |
| 已有生产库，只补功能 | `02_patches_all.sql`（幂等，可重复执行，**不会**清数据） |

> ⚠️ `ALL_IN_ONE.sql` 的 PART 2 会 `DROP + 重建` 同名表，**有业务数据的库不要用**，先备份。

### ALL_IN_ONE.sql 内容（2026-09-19 生成）

| 部分 | 内容 |
|------|------|
| PART 1 | `CREATE DATABASE IF NOT EXISTS crmeb_java3`（改这一处可换库名） |
| PART 2 | 本地库整理后全量导出：138 张表结构 + 基础数据，**已清空全部日志表**（定时任务日志、异常日志、登录日志、业务变动日志） |
| PART 3 | 全量业务补丁（幂等）：团队等级、会员等级返佣、区域代理、订货商一~三期、换货、虚拟库存、线下销售、门店、隐藏运维面板、本地设置快照（配置 + 装修页）、生产域名 `http://api.qianxutec.com` |

实测：本机完整导入零报错，导入后 138 张表，层级相关菜单 654/691/692 齐全。

---

## 其它文件（备查，日常部署不用）

| 文件 | 作用 |
|------|------|
| `02_patches_all.sql` | 仅补丁合集（幂等）。已有库升级用这个 |
| `00_create_database.sql` | 仅建库语句 |
| `deploy.bat` / `deploy.sh` | 老的交互式入口（all / patch / full 三种模式） |
| `db-data/crmeb_full_data_export.sql` | 本地库全量导出源（PART 2 的来源，6MB） |

基库已并入 `ALL_IN_ONE.sql`，不再需要单独导 `crmeb/sql/Crmeb_v3.0.sql`。

---

## 默认连接（与 application-prod.yml 一致）

- 库名：`crmeb_java3`，字符集 `utf8mb4 / utf8mb4_general_ci`
- 域名：补丁末尾写入 `http://api.qianxutec.com`

## 刻意未合并进 ALL_IN_ONE 的脚本

| 脚本 | 原因 |
|------|------|
| `export_admin_settings.sql` | 会写成 `127.0.0.1` |
| `update_deploy_config.sql` | 本地开发域名 |
| `reset_default_category.sql` | 会清空分类，生产危险 |
| `sql/migration/*` | 旧版，已被根目录幂等补丁覆盖 |
