# QIANXU SQL 脚本（2026-09-30 整理）

**目录里只有两份正式脚本，按名字选、不会拿错：**

| 文件 | 什么时候用 | 会不会动业务数据 |
|------|-----------|----------------|
| `00_full_install.sql` | **全新部署 / 测试环境重建** | ⚠️ 会 **DROP + 重建**同名表，有业务数据的库**禁止**执行 |
| `01_increment.sql` | **已有库升级补功能**（生产/本地都用它） | 不会。只补表结构 / 配置 / 菜单 / 定时任务 |

## ⚠️ 全新部署必须两份都跑

`00_full_install.sql` 冻结于 2026-09-30，**不含** 2026-10 之后新增的表：
`eb_svc_contract`、`eb_svc_demand`、`eb_svc_platform`、`eb_svc_invoice`、
`eb_sms_record`、`eb_sms_template`（这些只在 `01_increment.sql` 里）。

只导 00 就上线 → 后台菜单看着都在，点进去报「表不存在」。

**推荐一条命令搞定**（自动建库、自动把库名从主项目的 `qianxu_java3` 换成目标库、按序导两份、导完校验关键表）：

```bash
DB=qianxuqifu bash qianxu/sql/install_all.sh
# 库已存在：DB=qianxuqifu SKIP_CREATE_DB=1 bash qianxu/sql/install_all.sh
```

## 执行方式（手工导）

```bash
# 全新部署（默认会 CREATE DATABASE qianxu_java3 并 USE —— 副库请改成 qianxuqifu）
mysql -uroot -p密码 --default-character-set=utf8mb4 --max_allowed_packet=512M < 00_full_install.sql

# 已有库升级（务必指定目标库名）
mysql -uroot -p密码 --default-character-set=utf8mb4 qianxuqifu < 01_increment.sql
```

线上（宝塔）用命令行导入，**不要用网页版 phpMyAdmin**（大文件会截断）。

## 执行后必做

1. 重启 `Qianxu-admin.jar`、`Qianxu-front.jar`
2. 清一次 Redis 配置缓存：
   - 后台 → 维护 → 缓存清除
   - 或 `POST /api/admin/system/config/clear/cache`
3. 重新登录后台（菜单有缓存）

## 🔒 铁律：新增补丁必须【同步】并入两份

原因：只并入一份的话，全量新建的库会和增量升级的库**对不上**——
2026-09-30 就踩过：拼购 / 全球分红的补丁只提交了独立文件没并入，
导致代码最新版一查就报「服务器数据异常」（`Unknown column 'dividend_level_id'` / `Table 'eb_dividend_level' doesn't exist`）。

**现状（2026-10-03 盘点）**：2026-10 的合同 / 服务需求 / 签约渠道 / 发票 / 短信这几批补丁
**只并入了 `01_increment.sql`，尚未回填 `00_full_install.sql`**，所以「只导 00」这条路现在是断的。
回填工作量大且要重跑全量校验，暂时由 `install_all.sh`（两份都导 + 导完校验关键表）兜底。
新写补丁时请照上面铁律**两份都追加**，别再扩大这个缺口。

追加位置：**两份文件末尾 `SET FOREIGN_KEY_CHECKS = 1;` 之前**，用统一分节标记：

```sql
-- ========== BEGIN: 你的脚本名.sql ==========
（补丁内容，必须是幂等写法：CREATE TABLE IF NOT EXISTS / information_schema 判断 + PREPARE / INSERT ... WHERE NOT EXISTS）
-- ========== END: 你的脚本名.sql ==========
```

写完用下面两条验证（都应输出 0 行差异）：

```bash
# 1) 幂等：连跑两遍，结构不变
mysqldump --no-data --skip-dump-date 库名 | sed 's/AUTO_INCREMENT=[0-9]*//' > a.sql
mysql -uroot -p 库名 < 01_increment.sql
mysqldump --no-data --skip-dump-date 库名 | sed 's/AUTO_INCREMENT=[0-9]*//' > b.sql
diff a.sql b.sql

# 2) 两份脚本对齐：全量建出的库 vs 增量打过的库，表/列零差异
```

## `_archive/` 是什么

2026-09-30 之前的 70 个零散 SQL 和旧目录（`oneclick/`、`migration/`）全部移到这里，**仅供追溯，不再单独执行**。

旧名字对照：

| 旧 | 新 |
|----|----|
| `oneclick/ALL_IN_ONE.sql` | `00_full_install.sql` |
| `oneclick/02_patches_all.sql` | `01_increment.sql` |
| `oneclick/03_schema_increment_20260920.sql` | 不再单独维护，结构部分已在 `01_increment.sql` 内 |
| `sql/*.sql`（70 个） | 已并入上面两份；一次性修复脚本（如 `fix_online_2026xxxx`）不并入 |

> 需要彻底删除 `_archive/` 的话直接删目录即可，正式脚本不依赖它。
