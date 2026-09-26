#!/usr/bin/env bash
# ==================================================================
#  线上库 qianxu_java3  →  本地库 qianxu   整库单向同步
#
#  ⛔ 单向！只允许 线上→本地。绝不允许把本地推回线上。
#     线上是生产，本地是开发，反向覆盖 = 删生产数据。
#
#  用法：
#    bash /d/qianxu-java-3.0/local-dev/sync-from-online.sh --dry-run   # 只预演，不动任何东西
#    bash /d/qianxu-java-3.0/local-dev/sync-from-online.sh --yes       # 真执行（会覆盖本地 qianxu）
#
#  执行前会自动把本地 qianxu 全量备份到 /d/env/backup/，可整库回滚。
#
#  凭据可用环境变量覆盖：
#    ONLINE_HOST / ONL_DB_PASS / LOCAL_DB_PASS / REDIS_PASS
#
#  2026-09-24 首次跑通（10.3MB 备份 / 913KB 导出 / 导入 10 秒 / 行数逐表全中）
# ==================================================================
set -u
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"

HOST="${ONLINE_HOST:-root@8.163.105.182}"
ONL_DB="${ONL_DB:-qianxu_java3}"
ONL_USER="${ONL_USER:-qianxu_java3}"
ONL_PASS="${ONL_DB_PASS:-ktXMTiAxTTyMyPKk}"
LOCAL_DB="${LOCAL_DB:-qianxu}"
MY="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"
DUMP="D:/env/mysql-8.0.29-winx64/bin/mysqldump.exe"
RCLI="D:/env/redis/redis-cli.exe"
LOCAL_DB_PASS="${LOCAL_DB_PASS:-123456}"
REDIS_PASS="${REDIS_PASS:-123456}"
REDIS_DB="${REDIS_DB:-10}"
BACKUP_DIR="D:/env/backup"

MODE=""
case "${1:-}" in
  --dry-run) MODE=dry ;;
  --yes)     MODE=yes ;;
  *) echo "用法: $0 --dry-run | --yes"; exit 2 ;;
esac

SSH_OPT="-o BatchMode=yes -o ConnectTimeout=15 -o StrictHostKeyChecking=no"

hr() { echo "------------------------------------------------------------"; }

echo "============================================================"
echo " 线上 $ONL_DB  →  本地 $LOCAL_DB     模式: $MODE"
echo "============================================================"

# ---------- 0. 连通性 ----------
hr; echo "[0/6] 连通性检查"
ssh $SSH_OPT "$HOST" "echo SSH_OK; /www/server/mysql/bin/mysql --version" 2>/dev/null | tail -2 \
  || { echo "!! SSH 不通，终止"; exit 1; }
"$MY" -uroot -p"$LOCAL_DB_PASS" -N -e "SELECT 'LOCAL_OK', VERSION();" 2>/dev/null \
  || { echo "!! 本地 MySQL 连不上，终止"; exit 1; }

# ---------- 1. 差异预览 ----------
hr; echo "[1/6] 同步前差异预览"
"$MY" -uroot -p"$LOCAL_DB_PASS" "$LOCAL_DB" -N -e \
  "SELECT CONCAT('  本地 eb_user=', (SELECT COUNT(*) FROM eb_user), '  eb_store_order=', (SELECT COUNT(*) FROM eb_store_order), '  eb_theme=', (SELECT COUNT(*) FROM eb_theme));" 2>/dev/null
ssh $SSH_OPT "$HOST" "/www/server/mysql/bin/mysql -u$ONL_USER -p$ONL_PASS $ONL_DB -N -e \"SELECT CONCAT('  线上 eb_user=', (SELECT COUNT(*) FROM eb_user), '  eb_store_order=', (SELECT COUNT(*) FROM eb_store_order), '  eb_theme=', (SELECT COUNT(*) FROM eb_theme));\"" 2>/dev/null

if [ "$MODE" = "dry" ]; then
  hr; echo "[dry-run] 到此为止，未改任何东西。"
  echo "确认无误后加 --yes 执行。"
  exit 0
fi

# ---------- 2. 备份本地 ----------
hr; echo "[2/6] 备份本地 $LOCAL_DB"
mkdir -p "$BACKUP_DIR"
TS=$(date +%Y%m%d_%H%M%S)
LOCAL_BAK="$BACKUP_DIR/${LOCAL_DB}_before_sync_$TS.sql"
"$DUMP" -uroot -p"$LOCAL_DB_PASS" --single-transaction --quick --routines --triggers --events \
  --default-character-set=utf8mb4 "$LOCAL_DB" > "$LOCAL_BAK" 2>/dev/null
[ -s "$LOCAL_BAK" ] || { echo "!! 备份失败，终止（不做任何覆盖）"; exit 1; }
echo "  备份 -> $LOCAL_BAK  ($(du -h "$LOCAL_BAK" | cut -f1))"

# ---------- 3. 导出台线上 ----------
hr; echo "[3/6] 导出线上 $ONL_DB"
ssh $SSH_OPT "$HOST" "/www/server/mysql/bin/mysqldump -u$ONL_USER -p$ONL_PASS \
  --single-transaction --quick --routines --triggers --events \
  --default-character-set=utf8mb4 --set-gtid-purged=OFF --column-statistics=0 \
  --no-tablespaces --add-drop-table $ONL_DB 2>/dev/null | gzip > /tmp/wb_sync.sql.gz; \
  echo \"  远端导出: \$(du -h /tmp/wb_sync.sql.gz | cut -f1)  表数: \$(zcat /tmp/wb_sync.sql.gz | grep -c '^DROP TABLE IF EXISTS')\"" 2>/dev/null

scp $SSH_OPT "$HOST:/tmp/wb_sync.sql.gz" "$BACKUP_DIR/" 2>/dev/null || { echo "!! scp 失败"; exit 1; }
gunzip -c "$BACKUP_DIR/wb_sync.sql.gz" > "$BACKUP_DIR/${ONL_DB}_sync.sql"
echo "  解压 -> $BACKUP_DIR/${ONL_DB}_sync.sql  ($(wc -l < "$BACKUP_DIR/${ONL_DB}_sync.sql") 行)"

# ---------- 4. 导入 ----------
hr; echo "[4/6] 导入到本地 $LOCAL_DB （覆盖）"
"$MY" -uroot -p"$LOCAL_DB_PASS" --default-character-set=utf8mb4 "$LOCAL_DB" \
  < "$BACKUP_DIR/${ONL_DB}_sync.sql" 2>/tmp/wb_import_err.txt
RC=$?
echo "  导入退出码=$RC （非 0 请查 /tmp/wb_import_err.txt）"
[ $RC -eq 0 ] || { echo "!! 导入异常，本地数据可能不完整，可用 $LOCAL_BAK 回滚"; exit 1; }

# 清掉线上没有的本地遗留表（只删 _bak_ 前缀，防止误伤）
LEFTOVER=$("$MY" -uroot -p"$LOCAL_DB_PASS" -N -e \
  "SELECT table_name FROM information_schema.tables WHERE table_schema='$LOCAL_DB' AND table_name LIKE '%\_bak%';" 2>/dev/null | tr -d '\r')
if [ -n "$LEFTOVER" ]; then
  echo "  清理本地遗留备份表: $(echo "$LEFTOVER" | tr '\n' ' ')"
  echo "$LEFTOVER" | sed "s/^/DROP TABLE IF EXISTS \`/;s/$/\`;/" > /tmp/wb_drop.sql
  "$MY" -uroot -p"$LOCAL_DB_PASS" "$LOCAL_DB" < /tmp/wb_drop.sql 2>/dev/null
fi

# ---------- 5. 配置本地化（唯一允许的差异）----------
hr; echo "[5/6] 配置本地化（否则本地会去请求线上）"
"$MY" -uroot -p"$LOCAL_DB_PASS" "$LOCAL_DB" -e "
  UPDATE eb_system_config SET value='http://127.0.0.1:8080' WHERE name='api_url';
  UPDATE eb_system_config SET value='http://127.0.0.1:8081' WHERE name='front_api_url';
  UPDATE eb_system_config SET value='http://127.0.0.1:8090' WHERE name='site_url';
" 2>/dev/null
echo "  api_url / front_api_url / site_url -> 本地"
echo "  七牛 localUploadUrl / qnUploadUrl 保持；NOTIFY 保持线上（支付宝回调）"

# ---------- 6. 清缓存 + 验证 ----------
hr; echo "[6/6] 清 Redis db$REDIS_DB 旧 token，并验证"
for p in 'TOKEN_USER:*' 'TOKEN:ADMIN:*' 'FRONT_USER_TOKEN:*' 'config_list'; do
  n=$("$RCLI" -a "$REDIS_PASS" -n "$REDIS_DB" --raw KEYS "$p" 2>/dev/null | tr -d '\r' | grep -c .)
  if [ "${n:-0}" -gt 0 ]; then
    "$RCLI" -a "$REDIS_PASS" -n "$REDIS_DB" --raw KEYS "$p" 2>/dev/null | tr -d '\r' \
      | xargs -r "$RCLI" -a "$REDIS_PASS" -n "$REDIS_DB" DEL >/dev/null 2>&1
    echo "  删除 $p : $n 个"
  fi
done

echo "  表数: $("$MY" -uroot -p"$LOCAL_DB_PASS" -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$LOCAL_DB';" 2>/dev/null)"
curl -s -o /dev/null -m 15 "http://127.0.0.1:8081/api/front/index" -w "  front /api/front/index  HTTP=%{http_code}\n"
curl -s -o /dev/null -m 15 "http://127.0.0.1:8081/api/front/theme_info/home" -w "  front theme_info/home    HTTP=%{http_code}\n"
curl -s -m 15 -X POST "http://127.0.0.1:8080/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' -o /tmp/wb_login.json -w "  admin 登录              HTTP=%{http_code}\n"

hr
echo "完成。回滚用: $LOCAL_BAK"
echo "（本地服务无需重启 —— 两端配置都是直读库）"
