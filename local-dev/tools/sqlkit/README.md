# sqlkit — 一键补丁生成与验收工具

`qianxu/sql/oneclick/02_patches_all.sql` 是**生成物**：由 `qianxu/sql/*.sql` 各独立脚本按
`-- ========== BEGIN: xxx.sql ==========` 分节拼接而成，线上由 `deploy.sh patch` 执行。

> 改数据请改源脚本，**不要直接改补丁包**——下次重新拼接就会覆盖。

## 标准流程

```bash
cd /d/qianxu-java-3.0
PY=/c/Users/Administrator/.workbuddy/binaries/python/versions/3.13.12/python

# 1) 从库导出快照（配置 / 菜单 / 表名+备注+行数）
D:/env/mysql-8.0.29-winx64/bin/mysql.exe -uroot -p123456 --default-character-set=utf8mb4 -N -B qianxu \
  -e "SELECT ... " > local-dev/tmp/xxx.tsv      # 见各 gen_*.py 头部注释

# 2) 生成源脚本
$PY local-dev/tools/sqlkit/gen_sync_sql.py        # → system_settings_*.sql + menu_sync_*.sql
$PY local-dev/tools/sqlkit/gen_table_comments.py  # → table_comments_*.sql
$PY local-dev/tools/sqlkit/gen_table_doc.py       # → local-dev/db_table_reference.md

# 3) 重新拼接补丁包（务必从干净备份起，避免分节重复累积）
cp local-dev/tmp/02_patches_all.bak.sql qianxu/sql/oneclick/02_patches_all.sql
$PY local-dev/tools/sqlkit/patch_bundle.py

# 4) 验收：连跑三遍，要求 EXIT=0 / ERROR=0，且前后 mysqldump 完全一致
bash local-dev/tools/sqlkit/verify_idem.sh
```

## 硬性判据

- **幂等**：`INSERT ... WHERE NOT EXISTS` 补插；`UPDATE` 必须带 `WHERE NOT (col <=> 新值)` 守卫；
  **禁止**无条件写 `update_time = NOW()`（列自带 `ON UPDATE CURRENT_TIMESTAMP`）。
- **不覆盖线上业务数据**：凭证类（微信支付、密钥、短信、存储）一律不同步。
- **不硬编码库名 / 自增 id**：定位一律走 `component` / `perms` / `name` 等业务键。
- 验收必须**连跑三遍 + 前后 diff 为空**（含 `update_time`）。

## 依赖的临时数据

`local-dev/tmp/` 被 `.gitignore` 忽略，需按各生成器头部注释重新导出：`tables.tsv`、
`local_menu.tsv`、`local_config.tsv`、`02_patches_all.bak.sql` 等。
