# 增量 SQL（一键更新自动执行）

把**已有库**的升级补丁放到本目录，文件名建议：

```text
YYYYMMDD_简短说明.sql
例如：20261012_add_xxx_column.sql
```

`./deploy/scripts/server-update.sh` 会在重启容器前：

1. 读取 `deploy/.env` 里的库名/密码  
2. 按文件名排序执行**尚未执行过**的 `.sql`  
3. 记入表 `qx_sql_migration`（避免重复执行）

## 写法要求（必须幂等）

- `CREATE TABLE IF NOT EXISTS ...`
- 加列用 `information_schema` 判断后再 `ALTER`
- `INSERT ... WHERE NOT EXISTS` / `INSERT IGNORE`

**不要**把 `00_full_install.sql`（会 DROP 表）放进本目录。

## 手工只跑 SQL（不构建镜像）

```bash
cd /opt/qianxu-docker/crmeb-java-3.0
./deploy/scripts/apply-sql-migrations.sh
```

## 跳过 SQL

```bash
./deploy/scripts/server-update.sh --skip-sql
```
