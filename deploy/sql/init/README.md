# 首次初始化 SQL

把需要在**空库第一次启动**时自动执行的 `.sql` 放到本目录（按文件名排序执行）。

建议顺序示例：

1. `01_schema.sql` — 主库结构（如 `qianxu/sql/qianxu-V5.0.sql`）
2. `02_patches.sql` — 补丁 / oneclick 补丁
3. `03_chain.sql` — 链动等业务 migration

注意：

- 仅在 MySQL 数据目录为空时执行一次；已有 `data/mysql` 后不会再跑。
- 大文件可用 `docker compose exec -T mysql mysql -uqianxu -p... qianxu < xxx.sql` 手动导入。
