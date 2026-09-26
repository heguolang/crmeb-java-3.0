#!/usr/bin/env bash
# ============================================================
# QIANXU Java 3.0 一键导入数据库（Linux / 宝塔）
# 用法：
#   chmod +x deploy.sh
#   ./deploy.sh            # 全新：基库 + 补丁
#   ./deploy.sh patch      # 仅补丁（已有库）
#   ./deploy.sh full       # 基库 + 补丁 + 演示数据
# ============================================================
set -euo pipefail

# ------ 请按服务器修改 ------
MYSQL_BIN="${MYSQL_BIN:-mysql}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-crmeb_java3}"
MYSQL_PWD="${MYSQL_PWD:-ktXMTiAxTTyMyPKk}"
DB_NAME="${DB_NAME:-crmeb_java3}"
MODE="${1:-all}"
# ----------------------------

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SQL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
BASE_SQL="$SQL_DIR/Qianxu_v3.0.sql"
PATCH_SQL="$SCRIPT_DIR/02_patches_all.sql"
FULL_DATA="$ROOT_DIR/db-data/qianxu_full_data_export.sql"

MYSQL_CMD=("$MYSQL_BIN" -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PWD" --default-character-set=utf8mb4)

echo "[QIANXU] host=$MYSQL_HOST db=$DB_NAME mode=$MODE"

run_sql_file() {
  local file="$1"
  echo ">>> importing: $file"
  "${MYSQL_CMD[@]}" "$DB_NAME" < "$file"
}

if [[ "$MODE" != "patch" ]]; then
  echo "[1/3] create database if not exists..."
  "${MYSQL_CMD[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

  echo "[2/3] import base Qianxu_v3.0.sql (may take a few minutes)..."
  [[ -f "$BASE_SQL" ]] || { echo "missing $BASE_SQL"; exit 1; }
  run_sql_file "$BASE_SQL"
fi

echo "[patch] import 02_patches_all.sql ..."
[[ -f "$PATCH_SQL" ]] || { echo "missing $PATCH_SQL"; exit 1; }
run_sql_file "$PATCH_SQL"

if [[ "$MODE" == "full" ]]; then
  echo "[optional] import demo business data..."
  if [[ -f "$FULL_DATA" ]]; then
    run_sql_file "$FULL_DATA"
  else
    echo "warn: $FULL_DATA not found, skip"
  fi
fi

echo "========== DONE =========="
echo "domain set to: http://api.qianxutec.com"
echo "please restart jars and clear redis config cache if enabled"
