# 黔序商城 · SQL 补丁标准（给开发 / AI 共用）

> 使用方式：对 AI 说  
> **「按 `deploy/sql/SQL_PATCH_STANDARD.md` 生成 SQL」**  
> 或  
> **「严格遵循项目 SQL 补丁标准」**

本文约定：**已有库的增量升级**如何写、放哪、如何被服务器自动执行。

---

## 1. 文件放哪

| 用途 | 路径 | 是否被一键更新自动执行 |
|------|------|------------------------|
| **增量补丁（默认）** | `deploy/sql/migrate/YYYYMMDD_说明.sql` | ✅ 是（未执行过的会自动跑） |
| 总增量合集（建议同步追加） | `qianxu/sql/01_increment.sql` | ❌ 否（不每次全量重跑） |
| 全新装库全量脚本 | `qianxu/sql/00_full_install.sql` | ❌ 否；**禁止**放进 migrate |

一键更新命令：`./deploy/scripts/server-update.sh`  
只跑 SQL：`./deploy/scripts/apply-sql-migrations.sh`  
跳过 SQL：`./deploy/scripts/server-update.sh --skip-sql`

---

## 2. 命名

```text
YYYYMMDD_英文或拼音短说明.sql
```

示例：

- `20261012_add_eb_xxx_column.sql`
- `20261015_chain_menu_fix.sql`

规则：

- 按**文件名排序**执行，必须带日期前缀
- **已在任意环境执行过的文件：禁止改名、禁止改内容指望重跑**
- 需要修正时：新建下一个日期的 `YYYYMMDD_fix_xxx.sql`

---

## 3. 幂等（强制）

同一文件执行任意多次，结果一致且不报错。

允许：

- `CREATE TABLE IF NOT EXISTS ...`
- 加列前查 `information_schema.COLUMNS`，不存在再 `ALTER`（可用 PREPARE）
- `INSERT ... SELECT ... WHERE NOT EXISTS (...)`
- `INSERT IGNORE ...`

禁止放入 migrate：

- 无条件 `DROP TABLE` / `TRUNCATE`
- 清空业务数据的 `DELETE`
- `00_full_install.sql` 或任何会重建/清空库的全量脚本
- 不可重复的「一次性修脏数据」（除非用户明确要求**仅手工执行**，且不要放进 migrate）

字符集与引擎：`utf8mb4` + `InnoDB`。

---

## 4. 与 `01_increment.sql` 同步（推荐）

长期维护时，migrate 里的补丁内容应**同等追加**到 `qianxu/sql/01_increment.sql`，位置在文件末尾 `SET FOREIGN_KEY_CHECKS = 1;` **之前**，并用标记包裹：

```sql
-- ========== BEGIN: 20261012_add_eb_xxx_column.sql ==========
-- （与 deploy/sql/migrate 中同名文件内容一致，须幂等）
-- ========== END: 20261012_add_eb_xxx_column.sql ==========
```

避免「只导 00 / 只跑旧增量」的环境和「只跑 migrate」的环境结构不一致。

---

## 5. AI / 开发输出清单

生成补丁时必须做到：

1. 在 `deploy/sql/migrate/` 写出完整 `.sql` 文件（路径正确）
2. 文件名符合 `YYYYMMDD_说明.sql`
3. 全文幂等；有中文注释说明改了什么
4. 如用户未禁止，同步追加到 `qianxu/sql/01_increment.sql`（带 BEGIN/END 标记）
5. 简短说明：涉及哪些表/配置；是否已同步 01_increment

不要：

- 只把 SQL 贴在对话里却不落盘到 migrate
- 把全量安装脚本放进 migrate
- 修改已经执行过的旧 migrate 文件内容

---

## 6. 服务器侧行为（背景）

- 执行记录表：`qx_sql_migration`（库内自动创建）
- 已记录过的 `filename` 会 skip，不会重复执行
- 首次引入本机制时：migrate 目录为空则什么也不跑，安全

---

## 7. 最小示例（加列）

```sql
-- 20261012_add_demo_flag.sql
-- 给 eb_user 增加 demo_flag，幂等

SET NAMES utf8mb4;

SET @db := DATABASE();
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'eb_user' AND COLUMN_NAME = 'demo_flag'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE eb_user ADD COLUMN demo_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT ''演示标记'' AFTER uid',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
```

---

**一句话：升级补丁 = 幂等小文件 + `deploy/sql/migrate/日期_说明.sql` +（建议）同步 `01_increment.sql`；已执行过的文件只追加新文件、不改旧文件。**
