#!/bin/bash
# ============================================================
# QIANXU Java 3.0 —— 数据库全量覆盖 + 图片合并 + 全量业务数据还原
#
# 用法：
#   bash local-dev/rebuild-db.sh                  # 全量覆盖（自动备份）
#   bash local-dev/rebuild-db.sh --no-backup      # 跳过备份
#   bash local-dev/rebuild-db.sh --with-migration # 额外跑 qianxu/sql/migration/*
#
# ⚠️ 会 DROP 整个 crmeb 库！默认执行前会自动备份。
#
# 执行顺序严格对齐上游官方文档 db-data/导入说明.txt 的 14 步，
# 之后追加第 15 步：把 db-data/qianxu_image 的图片合并进 qianxu/crmebimage。
#
# 【为什么不跑 qianxu/sql/migration/*】
#   官方 14 步里没有它们。add_missing_team_level_tables.sql +
#   add_missing_columns.sql 已覆盖这些 migration 建的表和列，
#   export_admin_settings.sql 又导出了全套菜单。
#   再跑 migration 反而会踩坑：
#     migration/member_level_config.sql 与 user_team_level.sql 是
#     【无守卫】的 ALTER ... ADD COLUMN，列已存在即 ERROR 1060，
#     且会中断文件内后续语句（表就建不出来了）。
#   如确需执行，用 --with-migration，脚本会把它们排到 add_missing_columns 之前。
#
# 【reset_default_category.sql（官方第 8 步）】
#   它是破坏性的：删掉全部 type=1 产品分类、把所有商品改挂到单一
#   「默认分类」、清空 eb_store_product_cate。官方放在第 8 步是为了给
#   「只有种子数据」的库一个干净基线，第 14 步会用真实业务数据把分类还原。
#   本脚本按官方顺序原样执行。
# ============================================================
set -u

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SQLDIR="$PROJECT_DIR/qianxu/sql"
DBDATA="$PROJECT_DIR/db-data"
MYSQL_BIN="/opt/homebrew/opt/mysql@8.0/bin"
MYSQL="$MYSQL_BIN/mysql"
DUMP="$MYSQL_BIN/mysqldump"
SOCKET="/tmp/mysql.sock"
DB="crmeb"
DBUSER="root"
DBPASS="root"
IMAGE_ROOT="$PROJECT_DIR/qianxu"          # crmeb.imagePath 指向这里
IMAGE_DIR="$IMAGE_ROOT/crmebimage"

DO_BACKUP=1
WITH_MIGRATION=0
for a in "$@"; do
  case "$a" in
    --no-backup)      DO_BACKUP=0 ;;
    --with-migration) WITH_MIGRATION=1 ;;
  esac
done

die() { echo "❌ $*" >&2; exit 1; }

[ -x "$MYSQL" ] || die "找不到 mysql: $MYSQL"
[ -d "$SQLDIR" ] || die "找不到 SQL 目录: $SQLDIR"

run_sql() {
  "$MYSQL" -u"$DBUSER" -p"$DBPASS" --socket="$SOCKET" "$DB" \
    --default-character-set=utf8mb4 -e "SET SESSION sql_mode=''; source $1;" 2>&1 \
    | grep -v "Using a password"
}

# ---------- 0. 备份 ----------
if [ "$DO_BACKUP" = "1" ]; then
  BK="/Users/qianxu/WorkBuddy/java/_backup_qianxu_dbfull_$(date +%Y%m%d_%H%M%S)"
  echo "==> [0/4] 备份现有库到 $BK"
  mkdir -p "$BK"
  "$MYSQL" -u"$DBUSER" -p"$DBPASS" --socket="$SOCKET" "$DB" -e "
    SELECT '表数' t, COUNT(*) n FROM information_schema.tables WHERE table_schema='$DB'
    UNION ALL SELECT '系统菜单', COUNT(*) FROM eb_system_menu
    UNION ALL SELECT '系统配置', COUNT(*) FROM eb_system_config
    UNION ALL SELECT '用户',     COUNT(*) FROM eb_user
    UNION ALL SELECT '订单',     COUNT(*) FROM eb_store_order;" 2>&1 \
    | grep -v "Using a password" > "$BK/baseline_before.txt"
  "$DUMP" -u"$DBUSER" -p"$DBPASS" --socket="$SOCKET" \
    --single-transaction --routines --triggers "$DB" \
    > "$BK/qianxu_before.sql" 2>/dev/null
  echo "    DB 备份: $(ls -lh "$BK/qianxu_before.sql" | awk '{print $5}')"
  if [ -d "$IMAGE_DIR" ]; then
    tar -czf "$BK/crmebimage_before.tar.gz" -C "$IMAGE_ROOT" crmebimage 2>/dev/null
    echo "    图片备份: $(ls -lh "$BK/crmebimage_before.tar.gz" 2>/dev/null | awk '{print $5}')"
  fi
fi

# ---------- 1. DROP + CREATE ----------
echo "==> [1/4] 重建空库 $DB"
"$MYSQL" -u"$DBUSER" -p"$DBPASS" --socket="$SOCKET" -e "
  DROP DATABASE IF EXISTS \`$DB\`;
  CREATE DATABASE \`$DB\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" \
  2>&1 | grep -v "Using a password"
echo "    已重建"

# ---------- 2. 官方 14 步 ----------
STEPS=(
  "$SQLDIR/Qianxu_v3.0.sql"                        # 1  全量建表
  "$SQLDIR/add_missing_team_level_tables.sql"     # 2  补齐 5 张缺失表
  "$SQLDIR/add_missing_columns.sql"               # 3  补齐缺失字段
  "$SQLDIR/upgrade_team_level_direct.sql"         # 4  团队等级链式关系
  "$SQLDIR/add_login_notice_config.sql"           # 5
  "$SQLDIR/add_user_update_password_menu.sql"     # 6
  "$SQLDIR/add_admin_log_menu_and_table.sql"      # 7
  "$SQLDIR/reset_default_category.sql"            # 8  ⚠️破坏性，见文件头
  "$SQLDIR/update_copyright_company_name.sql"     # 9
  "$SQLDIR/agent.sql"                             # 10 区域代理
  "$SQLDIR/stock.sql"                             # 11 订货系统
  "$SQLDIR/update_deploy_config.sql"              # 12 本地域名/图片根地址
  "$SQLDIR/export_admin_settings.sql"             # 13 后台设置+菜单
  "$DBDATA/qianxu_full_data_export.sql"            # 14 全量业务数据（带行数保护）
)
LABELS=("1/14 全量建表" "2/14 补表" "3/14 补字段" "4/14 团队等级" "5/14 登录提示" \
        "6/14 改密码菜单" "7/14 日志管理" "8/14 默认分类" "9/14 版权" "10/14 区域代理" \
        "11/14 订货系统" "12/14 部署配置" "13/14 后台设置" "14/14 业务数据")

if [ "$WITH_MIGRATION" = "1" ]; then
  MIG=("$SQLDIR/migration/member_level_config.sql"
       "$SQLDIR/migration/system_user_level_brokerage.sql"
       "$SQLDIR/migration/system_team_level.sql"
       "$SQLDIR/migration/user_team_level.sql"
       "$SQLDIR/migration/team_brokerage_record_menu.sql"
       "$SQLDIR/migration/register_default_promoter_level.sql"
       "$SQLDIR/migration/credit_timing_config.sql"
       "$SQLDIR/migration/user_grade_brokerage_menu.sql")
  STEPS=("${STEPS[@]:0:2}" "${MIG[@]}" "${STEPS[@]:2}")
  LABELS=("extra-mig" "extra-mig" "extra-mig" "extra-mig" "extra-mig" \
          "extra-mig" "extra-mig" "extra-mig" "${LABELS[@]}")
fi

echo "==> [2/4] 按官方顺序导入（共 ${#STEPS[@]} 个脚本）"
FAIL=0; i=0
for S in "${STEPS[@]}"; do
  NAME="$(basename "$S")"
  LABEL="${LABELS[$i]:-?}"
  if [ ! -f "$S" ]; then
    echo "  [跳过] $LABEL · $NAME —— 文件不存在"; i=$((i+1)); continue
  fi
  T0=$(date +%s)
  OUT="$(run_sql "$S")"
  T1=$(date +%s)
  if echo "$OUT" | grep -qiE "ERROR [0-9]+"; then
    echo "  [失败] $LABEL · $NAME  ($((T1-T0))s)"
    echo "$OUT" | grep -iE "ERROR [0-9]+" | head -3 | sed 's/^/         /'
    FAIL=$((FAIL+1))
  else
    echo "  [OK ] $LABEL · $NAME  ($((T1-T0))s)"
  fi
  i=$((i+1))
done

# ---------- 3. 图片合并 ----------
echo "==> [3/4] 合并图片到 $IMAGE_DIR"
IMG_SRC="$DBDATA/qianxu_image/crmebimage"
if [ -d "$IMG_SRC" ]; then
  BEFORE=$(find "$IMAGE_DIR" -type f 2>/dev/null | wc -l | tr -d ' ')
  mkdir -p "$IMAGE_DIR"
  # rsync 增量补齐；不加 --delete，避免删掉本地已有图片
  if command -v rsync >/dev/null 2>&1; then
    rsync -a "$IMG_SRC/" "$IMAGE_DIR/" 2>&1 | tail -3
  else
    cp -R "$IMG_SRC/." "$IMAGE_DIR/" 2>/dev/null
  fi
  AFTER=$(find "$IMAGE_DIR" -type f 2>/dev/null | wc -l | tr -d ' ')
  SRC_N=$(find "$IMG_SRC" -type f 2>/dev/null | wc -l | tr -d ' ')
  echo "    源包 ${SRC_N} 个文件；目标目录 ${BEFORE} -> ${AFTER} 个文件"
else
  echo "    跳过：$IMG_SRC 不存在"
fi

# ---------- 4. 校验 ----------
echo ""
echo "==> [4/4] 校验"
"$MYSQL" -u"$DBUSER" -p"$DBPASS" --socket="$SOCKET" "$DB" -e "
SELECT '表数' AS 指标, COUNT(*) AS 值 FROM information_schema.tables WHERE table_schema='$DB'
UNION ALL SELECT '系统配置', COUNT(*) FROM eb_system_config
UNION ALL SELECT '系统菜单', COUNT(*) FROM eb_system_menu
UNION ALL SELECT '商品',     COUNT(*) FROM eb_store_product
UNION ALL SELECT '商品分类(type=1)', COUNT(*) FROM eb_category WHERE type=1
UNION ALL SELECT '用户',     COUNT(*) FROM eb_user
UNION ALL SELECT '收货地址', COUNT(*) FROM eb_user_address
UNION ALL SELECT '订单',     COUNT(*) FROM eb_store_order
UNION ALL SELECT '订单明细', COUNT(*) FROM eb_store_order_info
UNION ALL SELECT '订货代理', COUNT(*) FROM eb_stock_agent
UNION ALL SELECT '区域代理', COUNT(*) FROM eb_agent;" 2>&1 | grep -v "Using a password"

echo ""
echo "==> 关键结构与配置"
"$MYSQL" -u"$DBUSER" -p"$DBPASS" --socket="$SOCKET" "$DB" -e "
SELECT 'eb_system_team_level 列数(应 23)' AS 检查项, COUNT(*) AS 值
  FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$DB' AND TABLE_NAME='eb_system_team_level'
UNION ALL SELECT 'eb_stock_* 表数(应 11)', COUNT(*) FROM information_schema.tables
  WHERE TABLE_SCHEMA='$DB' AND TABLE_NAME LIKE 'eb_stock%'
UNION ALL SELECT 'api_url 指向 8080', COUNT(*) FROM eb_system_config
  WHERE name='api_url' AND value='http://127.0.0.1:8080'
UNION ALL SELECT 'localUploadUrl 指向 8080', COUNT(*) FROM eb_system_config
  WHERE name='localUploadUrl' AND value='http://127.0.0.1:8080'
UNION ALL SELECT '支付开关值干净(1)', COUNT(*) FROM eb_system_config
  WHERE name='pay_weixin_open' AND value='1';" 2>&1 | grep -v "Using a password"

if [ "$FAIL" -gt 0 ]; then
  echo ""
  echo "❌ 有 $FAIL 个脚本执行失败"
  exit 1
fi
echo ""
echo "✅ 全量覆盖完成，所有脚本零 ERROR"
