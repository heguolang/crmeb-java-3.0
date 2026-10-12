# 增量 SQL（一键更新自动执行）

**写补丁前请先读（给人或 AI）：**

👉 [../SQL_PATCH_STANDARD.md](../SQL_PATCH_STANDARD.md)

对 AI 可直接说：

> 按 `deploy/sql/SQL_PATCH_STANDARD.md` 生成 SQL

本目录只放 `YYYYMMDD_说明.sql`。  
`./deploy/scripts/server-update.sh` 会自动执行尚未跑过的文件（记录在 `qx_sql_migration`）。
