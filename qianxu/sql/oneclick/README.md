# QIANXU SQL 部署脚本

目录：`qianxu/sql/oneclick/`

## ⭐ 测试环境请用这个（推荐）

```
qianxu/sql/oneclick/ALL_IN_ONE_TEST.sql
```

相对原版 `ALL_IN_ONE.sql` 已整理：

1. 全库统一 `utf8mb4_general_ci`（避免 Quartz 外键 ERROR 3780）
2. 主题等数据中的 `http://127.0.0.1:8080` → `http://api.qianxutec.com`
3. 去掉 `qrtz_*` 含二进制 `JOB_DATA` 的 INSERT（避免宝塔/网页导入截断）；表结构保留，启动后按 `eb_schedule_job` 注册
4. 末尾再次强制域名 + `sys_switch_*` 去重 + 重建干净 Quartz 空表

### 导入方式（务必命令行，不要用宝塔网页导）

```bash
# 1) 建议先备份 / 空库重建
mysql -uroot -p -e "DROP DATABASE IF EXISTS qianxu_java3; CREATE DATABASE qianxu_java3 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2) 导入（在 oneclick 目录下）
mysql -uroot -p密码 --default-character-set=utf8mb4 --max_allowed_packet=512M < ALL_IN_ONE_TEST.sql
```

### 导入后

1. 重启 **admin**、**front** jar  
2. Redis：`redis-cli -a 密码 -n 8 FLUSHDB`  
3. 无痕打开 H5，确认图片为 `http://api.qianxutec.com/qianxuimage/...`

> ⚠️ 会 `DROP + 重建` 同名表，**有要保留的业务数据不要用**。

重新从 `ALL_IN_ONE.sql` 生成测试版：

```bash
node build_test_sql.js
```

---

## 其它文件

| 文件 | 何时用 |
|------|--------|
| `ALL_IN_ONE_TEST.sql` | **测试库全新部署 / 可清空重建（推荐）** |
| `ALL_IN_ONE.sql` | 原始合集（含本地域名残留与 Quartz 二进制，网页导入易失败） |
| `02_patches_all.sql` | **已有库只补功能**（幂等，不清业务数据） |
| `03_schema_increment_20260920.sql` | **线上只补「表结构 / 字段」**（幂等、无任何数据语句，最快最安全） |
| `00_create_database.sql` | 仅建库 |
| `build_test_sql.js` | 从 `ALL_IN_ONE.sql` 生成 `ALL_IN_ONE_TEST.sql` |

| 场景 | 执行什么 |
|------|----------|
| 测试环境重建 | `ALL_IN_ONE_TEST.sql` |
| 生产已有数据只升级补丁（结构+菜单+设置） | `02_patches_all.sql` |
| 生产只想补表结构/字段（不要菜单设置） | `03_schema_increment_20260920.sql` |

> `03_schema_increment_20260920.sql` 由 `02_patches_all.sql` 自动抽取结构类 DDL 生成：
> 29 张自建表（`CREATE TABLE IF NOT EXISTS`）+ 71 处字段变更（先查 `information_schema` 再 `ALTER`），
> 不含任何 `INSERT`/`UPDATE`。已在本机库执行两遍，`mysqldump --no-data` 前后 diff 无差异（幂等已验证）。

---

## 默认连接（与 application-prod.yml 一致）

- 库名：`qianxu_java3`，字符集 `utf8mb4 / utf8mb4_general_ci`
- 域名：`http://api.qianxutec.com`
