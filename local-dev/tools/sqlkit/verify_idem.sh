#!/bin/bash
# 本地连跑 02_patches_all.sql 并逐次导出全库数据，用于幂等性验收
set -u
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"
MYSQL="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"
DUMP="D:/env/mysql-8.0.29-winx64/bin/mysqldump.exe"
DB=crmeb
PATCH=/d/qianxu-java-3.0/qianxu/sql/oneclick/02_patches_all.sql
TMP=/d/qianxu-java-3.0/local-dev/tmp

dump() {  # $1 = 标签
  "$DUMP" -uroot -p123456 --default-character-set=utf8mb4 --skip-dump-date \
      --no-create-info --order-by-primary --skip-comments "$DB" 2>/dev/null > "$TMP/dump_$1.sql"
  "$DUMP" -uroot -p123456 --default-character-set=utf8mb4 --skip-dump-date \
      --no-data --skip-comments "$DB" 2>/dev/null > "$TMP/schema_$1.sql"
  echo "  dump_$1.sql  $(wc -c < "$TMP/dump_$1.sql") 字节 / schema_$1.sql $(wc -c < "$TMP/schema_$1.sql") 字节"
}

echo "[0] 补丁前基线"
dump base

for i in 1 2 3; do
  echo "[$i] 执行 02_patches_all.sql"
  "$MYSQL" -uroot -p123456 --default-character-set=utf8mb4 "$DB" < "$PATCH" > "$TMP/run_b$i.out" 2> "$TMP/run_b$i.err"
  rc=$?
  errs=$(grep -c "ERROR" "$TMP/run_b$i.err" || true)
  echo "  EXIT=$rc  ERROR=$errs"
  if [ "$errs" != "0" ]; then grep -m5 "ERROR" "$TMP/run_b$i.err"; fi
  dump "b$i"
done

echo
echo "=== 幂等判定：dump_b1 vs dump_b2 vs dump_b3 ==="
if diff -q "$TMP/dump_b1.sql" "$TMP/dump_b2.sql" >/dev/null && diff -q "$TMP/dump_b2.sql" "$TMP/dump_b3.sql" >/dev/null; then
  echo "  ✅ 数据三份完全一致（含 update_time）"
else
  echo "  ❌ 数据存在差异"
  diff "$TMP/dump_b1.sql" "$TMP/dump_b2.sql" | head -20
fi
echo "=== 幂等判定：表结构（含表备注） ==="
if diff -q "$TMP/schema_b1.sql" "$TMP/schema_b2.sql" >/dev/null && diff -q "$TMP/schema_b2.sql" "$TMP/schema_b3.sql" >/dev/null; then
  echo "  ✅ 表结构/表备注三份完全一致"
else
  echo "  ❌ 表结构存在差异"
  diff "$TMP/schema_b1.sql" "$TMP/schema_b2.sql" | head -20
fi
echo
echo "=== 补丁前 vs 补丁后（本次变更量） ==="
diff "$TMP/dump_base.sql" "$TMP/dump_b1.sql" | grep -c '^[<>]' || true
echo "  表结构差异行数: $(diff "$TMP/schema_base.sql" "$TMP/schema_b1.sql" | grep -c '^[<>]' || true)"
