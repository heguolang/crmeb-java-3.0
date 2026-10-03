#!/usr/bin/env bash
# 后台前端（adminqf.qianxutec.com）生产构建 + 打包（黔序商城 · qianxuqifu 副项目）
#
# 用法（在仓库任意位置执行都行，路径自动推导）：
#   bash deploy/qianxuqifu-182/build-admin.sh
#   BASE_API=... OUT=... TAR=... bash deploy/qianxuqifu-182/build-admin.sh
#
# ⚠️ 三个必须踩准的点（2026-10-03 事故复盘，别再手敲命令）：
#   1) 【最致命】VUE_APP_BASE_API 必须覆盖成 https://apiqf.qianxutec.com
#      仓库里 admin/.env.production 写的是 http://api.qianxutec.com（主项目旧域名 + http），
#      直接 `npm run build:prod` 会把错域名打进包里：https 页面发 http 请求 =
#      浏览器按混合内容拦截 → **后台直接登不进去**（接口本身全是好的，极难查）。
#   2) node 版本：本项目 vue-cli 4 + webpack 4 → **node 17 及以上**才需要
#      NODE_OPTIONS=--openssl-legacy-provider；node ≤16 千万别加，
#      加了会报 `xxx is not allowed in NODE_OPTIONS`，构建直接失败。脚本已自动判断。
#   3) 打 tar 用 COPYFILE_DISABLE=1 tar --no-xattrs，否则 macOS 扩展头会让线上解压刷
#      "Ignoring unknown extended header keyword"（无害但刷屏）。
#
# 构建完自动做「接线预检」：包里必须有 https://apiqf.qianxutec.com、且不能残留
# api.qianxutec.com；不通过就中断，避免把坏包发上线。

set -o pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SRC="$ROOT/admin"
OUT=${OUT:-$SRC/dist}
TAR=${TAR:-/tmp/qifu-admin-front.tar.gz}
BASE_API=${BASE_API:-https://apiqf.qianxutec.com}
LOG=${LOG:-/tmp/qifu-admin-build.log}

[ -d "$SRC" ] || { echo "‼️ 找不到源码目录 $SRC"; exit 1; }
cd "$SRC" || exit 1
rm -rf "$OUT"

NODE_MAJOR=$(node -v 2>/dev/null | sed 's/^v//' | cut -d. -f1)
if [ "${NODE_MAJOR:-0}" -ge 17 ]; then
  export NODE_OPTIONS=--openssl-legacy-provider
  echo "node $(node -v) → 启用 NODE_OPTIONS=--openssl-legacy-provider"
else
  echo "node $(node -v) → 不加 NODE_OPTIONS（node ≤16 加了会构建失败）"
fi

echo "---- 构建（BASE_API=$BASE_API）日志 $LOG ----"
VUE_APP_BASE_API=$BASE_API npm run build:prod -- --dest "$OUT" > "$LOG" 2>&1
RC=$?
tail -12 "$LOG"
echo "---- build exit=$RC ----"
[ $RC -ne 0 ] && exit $RC

# ---- 构建后接线预检（接口域名）----
echo "---- 接线预检（接口域名）----"
if grep -rlE '(^|[^a-z])api\.qianxutec\.com' "$OUT" >/dev/null 2>&1; then
  echo "‼️ 包里残留旧域名 api.qianxutec.com —— 已中断，禁止上线。"
  grep -rloE '(^|[^a-z])api\.qianxutec\.com' "$OUT" | head -5
  exit 1
fi
if ! grep -rl "https://apiqf.qianxutec.com" "$OUT/static" >/dev/null 2>&1; then
  echo "‼️ 包里没找到 https://apiqf.qianxutec.com —— 已中断，禁止上线。"
  exit 1
fi
echo "  ✅ 接口域名正确，且无旧域名残留"

MAIN=$(basename "$(ls "$OUT"/static/js/app.*.js | head -1)")
echo "  主包: $MAIN"

COPYFILE_DISABLE=1 tar --no-xattrs -czf "$TAR" -C "$OUT" .
SUM=$(md5sum "$TAR" 2>/dev/null | awk '{print $1}' || md5 -q "$TAR")
echo "---- 产物 ----"
echo "  dist: $OUT（$(find "$OUT" -type f | wc -l | tr -d ' ') 个文件）"
echo "  包:   $TAR"
echo "  md5:  $SUM"
