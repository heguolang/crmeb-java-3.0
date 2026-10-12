#!/usr/bin/env bash
# 执行 deploy/sql/migrate/*.sql 中尚未应用的补丁（记录在 qx_sql_migration）
# 用法：
#   ./deploy/scripts/apply-sql-migrations.sh
#   ./deploy/scripts/apply-sql-migrations.sh --dry-run

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
DEPLOY="$ROOT/deploy"
MIGRATE_DIR="$DEPLOY/sql/migrate"
DRY_RUN=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=1; shift ;;
    -h|--help)
      sed -n '2,8p' "$0"
      exit 0
      ;;
    *) echo "未知参数: $1" >&2; exit 1 ;;
  esac
done

if [[ ! -f "$DEPLOY/.env" ]]; then
  echo "缺少 $DEPLOY/.env" >&2
  exit 1
fi

while IFS= read -r line || [[ -n "$line" ]]; do
  [[ "$line" =~ ^[[:space:]]*# ]] && continue
  [[ "$line" =~ ^[[:space:]]*$ ]] && continue
  if [[ "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
    key="${BASH_REMATCH[1]}"
    val="${BASH_REMATCH[2]}"
    val="${val%\"}"; val="${val#\"}"
    val="${val%\'}"; val="${val#\'}"
    export "$key=$val"
  fi
done < "$DEPLOY/.env"

MYSQL_DATABASE="${MYSQL_DATABASE:-qianxu}"
MYSQL_USER="${MYSQL_USER:-qianxu}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:?请在 .env 设置 MYSQL_PASSWORD}"

COMPOSE=(docker compose -f "$DEPLOY/docker-compose.yml")
if [[ -f "$DEPLOY/docker-compose.baota.yml" ]]; then
  COMPOSE+=(-f "$DEPLOY/docker-compose.baota.yml")
fi

cd "$DEPLOY"
if ! "${COMPOSE[@]}" ps --format '{{.Name}} {{.Status}}' 2>/dev/null | grep -q 'mysql.*Up'; then
  echo "[sql] 启动 mysql ..."
  "${COMPOSE[@]}" up -d mysql
  sleep 15
fi

mysql_exec() {
  "${COMPOSE[@]}" exec -T mysql mysql \
    -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
    --default-character-set=utf8mb4 \
    "$MYSQL_DATABASE" "$@"
}

echo "[sql] 库=$MYSQL_DATABASE 用户=$MYSQL_USER"
echo "[sql] 确保迁移记录表存在 ..."
mysql_exec <<'SQL'
CREATE TABLE IF NOT EXISTS qx_sql_migration (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  filename VARCHAR(255) NOT NULL,
  checksum CHAR(64) NOT NULL DEFAULT '',
  applied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_filename (filename)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Docker一键更新SQL迁移记录';
SQL

shopt -s nullglob
files=("$MIGRATE_DIR"/*.sql)
if [[ ${#files[@]} -eq 0 ]]; then
  echo "[sql] $MIGRATE_DIR 下无 .sql，跳过"
  exit 0
fi

IFS=$'\n' sorted=($(printf '%s\n' "${files[@]}" | sort))
pending=0
applied=0

for f in "${sorted[@]}"; do
  base="$(basename "$f")"
  exists="$(mysql_exec -N -e "SELECT COUNT(*) FROM qx_sql_migration WHERE filename='${base//\'/\\\'}'" | tr -d '\r' | tail -1)"
  if [[ "$exists" != "0" ]]; then
    echo "[sql] skip (已执行): $base"
    continue
  fi
  pending=$((pending + 1))
  echo "[sql] apply: $base"
  if [[ "$DRY_RUN" -eq 1 ]]; then
    continue
  fi
  sum="$(sha256sum "$f" | awk '{print $1}')"
  if ! mysql_exec < "$f"; then
    echo "[sql] 失败: $base （已中断，未写入迁移记录）" >&2
    exit 1
  fi
  mysql_exec -e "INSERT INTO qx_sql_migration(filename, checksum) VALUES ('${base//\'/\\\'}', '$sum')"
  applied=$((applied + 1))
  echo "[sql] ok: $base"
done

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "[sql] dry-run 结束，待执行 $pending 个文件"
else
  echo "[sql] 完成：新执行 $applied 个，目录共 ${#sorted[@]} 个 .sql"
fi
