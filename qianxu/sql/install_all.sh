#!/usr/bin/env bash
# ============================================================
# 一键建库 + 按正确顺序导入两份 SQL（黔序商城）
#
# 为什么要有这个脚本：
#   00_full_install.sql 冻结于 2026-09-30，2026-10 之后新增的业务表
#   （eb_svc_contract / eb_svc_demand / eb_svc_platform / eb_svc_invoice /
#    eb_sms_record / eb_sms_template）只在 01_increment.sql 里。
#   两个脚本手工导时漏掉第二个，表现是「后台菜单都在、点进去报表不存在」。
#   另外 00 里的库名默认是主项目的 qianxu_java3，副库要换成 qianxuqifu。
#   这里统一处理掉，避免下游踩坑。
#
# 用法：
#   DB=qianxuqifu bash qianxu/sql/install_all.sh
#   DB=qianxuqifu MYSQL_USER=root MYSQL_PASS='xxx' bash qianxu/sql/install_all.sh
#   DB=qianxuqifu SKIP_CREATE_DB=1 bash qianxu/sql/install_all.sh   # 库已存在时
#   MYSQL_BIN=/opt/homebrew/opt/mysql@8.0/bin/mysql ...             # 客户端不在 PATH 时
#
# 注意：00 会 DROP + 重建同名表 —— 有业务数据的库【禁止】跑（用 01_increment.sql）。
# ============================================================
set -uo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FULL="$HERE/00_full_install.sql"
INC="$HERE/01_increment.sql"

DB=${DB:-qianxuqifu}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PASS=${MYSQL_PASS:-}
SKIP_CREATE_DB=${SKIP_CREATE_DB:-0}
# 2026-10-03：brew 装的 MySQL 客户端不在 PATH 里（宝塔 / Windows 常见路径也各不相同），
# 找不到 mysql 就直接退出太让人摸不着头脑，所以允许显式指定并给出常见路径提示。
MYSQL_BIN=${MYSQL_BIN:-mysql}

if ! command -v "$MYSQL_BIN" >/dev/null 2>&1; then
  echo "‼️ 找不到 mysql 客户端：$MYSQL_BIN"
  echo "   指定完整路径即可，例如："
  echo "   MYSQL_BIN=/opt/homebrew/opt/mysql@8.0/bin/mysql bash $0"
  echo "   MYSQL_BIN=/www/server/mysql/bin/mysql    bash $0   # 宝塔"
  exit 1
fi

[ -f "$FULL" ] || { echo "‼️ 缺少 $FULL"; exit 1; }
[ -f "$INC"  ] || { echo "‼️ 缺少 $INC";  exit 1; }

if [ -z "$MYSQL_PASS" ]; then
  read -rsp "MySQL $MYSQL_USER 密码（直接回车表示空密码）: " MYSQL_PASS; echo
fi

MYSQL=("$MYSQL_BIN" -u"$MYSQL_USER")
[ -n "$MYSQL_PASS" ] && MYSQL+=(-p"$MYSQL_PASS")
MYSQL+=(--default-character-set=utf8mb4 --max_allowed_packet=512M)

# 探一下能不能连上（不指定库）
if ! "${MYSQL[@]}" -e "SELECT 1" >/dev/null 2>&1; then
  echo "‼️ 连不上 MySQL（用户 $MYSQL_USER），检查账号/密码/端口"
  exit 1
fi

# ---- 1) 建库（默认建，SKIP_CREATE_DB=1 时跳过）----
if [ "$SKIP_CREATE_DB" != "1" ]; then
  echo "---- 建库 $DB ----"
  "${MYSQL[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$DB\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci" || exit 1
else
  echo "---- 跳过建库，直接用已有的 $DB ----"
fi

# ---- 2) 全量：把库名从 qianxu_java3 换成目标库（只动 PART1 那两条语句）----
TMP_FULL="${TMPDIR:-/tmp}/00_full_install.$$.sql"
trap 'rm -f "$TMP_FULL"' EXIT
sed "s/\`qianxu_java3\`/\`$DB\`/" "$FULL" > "$TMP_FULL"

echo "---- 导入 00_full_install.sql ----"
"${MYSQL[@]}" "$DB" < "$TMP_FULL"
RC=$?
echo "   exit=$RC"
[ $RC -ne 0 ] && { echo "‼️ 全量导入失败，已中止（增量还没跑，库处于半初始化状态）"; exit $RC; }

# ---- 3) 增量：补 2026-10 之后的新表 / 菜单 / 配置 ----
echo "---- 导入 01_increment.sql（幂等，可重复跑）----"
"${MYSQL[@]}" "$DB" < "$INC"
RC=$?
echo "   exit=$RC"
[ $RC -ne 0 ] && { echo "‼️ 增量导入失败，已中止"; exit $RC; }

# ---- 4) 关键表校验 ----
echo "---- 关键表校验 ----"
MISS=0
for t in eb_svc_contract eb_svc_demand eb_svc_platform eb_svc_invoice eb_sms_record eb_system_menu eb_user; do
  N=$("${MYSQL[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB' AND TABLE_NAME='$t'")
  if [ "$N" = "1" ]; then echo "  ✅ $t"; else echo "  ❌ $t 不存在"; MISS=1; fi
done
TOTAL=$("${MYSQL[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DB'")
echo "---- 完成：库 $DB 共 $TOTAL 张表 ----"
[ $MISS -ne 0 ] && { echo "‼️ 有关键表缺失，别直接上线"; exit 1; }

cat <<TIP

下一步：
  1) 起服务：DB_PASS='真实口令' bash deploy/qianxuqifu-182/qifu-service.sh start
  2) 清一次 Redis 配置缓存（后台 → 维护 → 缓存清除），重新登录后台加载新菜单
TIP
