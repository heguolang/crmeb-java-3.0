#!/usr/bin/env bash
# 会员端 H5 生产构建（黔序商城 · qianxuqifu 副项目）
#
# 产物形态：index.html + static/ + assets/ + pages/（与线上现网一致）
#
# 用法（在仓库任意位置执行都行，路径自动推导）：
#   bash deploy/qianxuqifu-182/build-h5.sh
#   OUT=/tmp/xxx bash build-h5.sh        # 自定义输出目录
#   SRC=/path/to/app bash build-h5.sh    # 自定义源码目录（默认 repo/qianxu-app）
#   NO_PATCH=1 bash build-h5.sh          # 不改域名，按源码原样构建（不推荐上线）
#
# ⚠️ 四个必须踩准的点（前三条是 2026-10-02 踩过，第 4 条是 2026-10-03 同类事故）：
#   1) 必须用 HBuilderX 自带的 node18，不用系统 node22
#      （node22 编不了这套老 vue-cli 4 / node-sass）→ 所以 NODE_OPTIONS=--openssl-legacy-provider 要带上
#   2) node_modules/.bin/vue-cli-service 是坏软链 → 必须直接调
#      node_modules/@vue/cli-service/bin/vue-cli-service.js
#   3) UNI_HBUILDERX_PLUGINS 必须给，否则编译器找不到 uni 插件链
#   4) 【致命】必须把 config/app.js 的 domain / HTTP_H5_URL 切成线上域名：
#      仓库里的默认值是 127.0.0.1:8081 / 127.0.0.1:8090，忘了改就编译出一个
#      **装到线上必定连不上、但编译过程完全不报错**的包（和后台 VUE_APP_BASE_API 那次同类）。
#      脚本现在默认自动切<｜hy_place▁holder▁no▁813｜>产预审，别用 NO_PATCH=1（除非你自己改过源码里的域名）。
#
# 日志落 $LOG，构建约 3-6 分钟

set -o pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SRC=${SRC:-$ROOT/qianxu-app}
OUT=${OUT:-/tmp/qifu-h5-build}
LOG="${OUT%/}.log"

# 线上域名（可在命令行覆盖）
APP_API_BASE=${APP_API_BASE:-https://apiqf.qianxutec.com}
APP_H5_URL=${APP_H5_URL:-https://appqf.qianxutec.com}

HBX=/Applications/HBuilderX.app/Contents/HBuilderX
CLI="$HBX/plugins/uniapp-cli"
NODE="$HBX/plugins/node18/node"
PY_BIN="${PY_BIN:-$(command -v python3)}"

[ -d "$SRC" ] || { echo "‼️ 找不到会员端源码 $SRC（可 SRC=... 指定）"; exit 1; }
[ -x "$NODE" ] || { echo "‼️ 找不到 HBuilderX 自带 node18：$NODE（本脚本依赖 HBuilderX 环境）"; exit 1; }
echo "---- 源码 $SRC ----"

# ---- 接线预检（ref / 组件方法）----
# 2026-10-02 事故：模板漏 ref="sealPad" 但脚本里调 this.$refs.sealPad → 点「确认签署」必炸，
# 编译与热编译都不报错，只有客户点下去才暴露。这里做机械卡点，拦住整类问题。
WIRE_CHECK="$HOME/.workbuddy/skills/qianxu-build-deploy/tools/h5_wiring_check.py"
if [ -f "$WIRE_CHECK" ] && [ "${WIRING_SKIP:-0}" != "1" ]; then
  echo "---- 接线预检（ref / 组件方法）----"
  if ! "$PY_BIN" "$WIRE_CHECK" "$SRC"; then
    echo "‼️ 接线检查未通过 —— 已中断构建，避免把坏包发上线。"
    echo "   （确认可忽略时用 WIRING_SKIP=1 再跑一次）"
    exit 1
  fi
fi

# ---- 切到线上域名：复制到临时目录改，不动源码 ----
BUILD_SRC="$SRC"
if [ "${NO_PATCH:-0}" != "1" ]; then
  BUILD_SRC="${TMPDIR:-/tmp}/qifu-h5-src-$(date +%s)"
  echo "---- 复制源码到 $BUILD_SRC 并切到线上域名 ----"
  rm -rf "$BUILD_SRC" && mkdir -p "$BUILD_SRC"
  (cd "$SRC" && COPYFILE_DISABLE=1 tar --no-xattrs -cf - \
      --exclude=node_modules --exclude=unpackage --exclude=.hbuilderx --exclude=.git .) |
      (cd "$BUILD_SRC" && tar -xf -)
  "$PY_BIN" - "$BUILD_SRC/config/app.js" "$APP_API_BASE" "$APP_H5_URL" <<'PY'
# -*- coding: utf-8 -*-
import re, sys, io
p, api, h5 = sys.argv[1], sys.argv[2], sys.argv[3]
lines = io.open(p, encoding='utf-8').read().split('\n')
out, hit = [], 0
for ln in lines:
    s = ln.strip()
    if s.startswith('//'):
        out.append(ln); continue
    new = ln
    new = re.sub(r"^(\s*let\s+domain\s*=\s*)'[^']*'", r"\1'%s'" % api, new)
    # 注意：只替换引号内的值，不追加逗号 —— 原行末尾本来就带逗号，补一个会变成 ,, 语法错误
    new = re.sub(r"(HTTP_H5_URL\s*:\s*)'[^']*'", r"\1'%s'" % h5, new)
    if new != ln:
        hit += 1
    out.append(new)
io.open(p, 'w', encoding='utf-8').write('\n'.join(out))
print('  修改 %d 行' % hit)
if hit < 2:
    print('‼️ app.js 里没找到 domain / HTTP_H5_URL，无法确认接线，请检查文件结构'); sys.exit(1)
PY
  [ $? -ne 0 ] && exit 1
  grep -nE "^\s*let domain|HTTP_H5_URL:" "$BUILD_SRC/config/app.js" | head -4
  # 补丁后做一次语法校验：避免像 2026-10-03 那次补丁多补一个逗号，构建出来的包直接是坏的
  "$NODE" --check "$BUILD_SRC/config/app.js" || {
    echo "‼️ app.js 切换域名后语法校验未通过，已中断。"
    exit 1
  }
  echo "  ✅ app.js 语法校验通过"
fi

rm -rf "$OUT" && mkdir -p "$OUT"

cd "$CLI" || exit 1
env \
  VUE_CLI_CONTEXT="$CLI" \
  UNI_CLI_CONTEXT="$CLI" \
  UNI_HBUILDERX_PLUGINS="$HBX/plugins" \
  UNI_INPUT_DIR="$BUILD_SRC" \
  UNI_OUTPUT_DIR="$OUT" \
  UNI_PLATFORM=h5 \
  NODE_ENV=production \
  NODE_OPTIONS=--openssl-legacy-provider \
  "$NODE" --max-old-space-size=5120 --no-warnings \
  "$CLI/node_modules/@vue/cli-service/bin/vue-cli-service.js" uni-build \
  > "$LOG" 2>&1

RC=$?
echo "---- build exit=$RC ----"
tail -30 "$LOG"
[ $RC -ne 0 ] && exit $RC

# ---- 构建后接线预检（接口域名）----
echo "---- 接线预检（接口域名）----"
if grep -rl "127.0.0.1" "$OUT" >/dev/null 2>&1; then
  echo "‼️ 产物里残留 127.0.0.1 —— 装上线必定连不上，已判定为坏包。"
  grep -rlo "127.0.0.1" "$OUT" | head -5
  exit 1
fi
if grep -rlE '(^|[^a-z])api\.qianxutec\.com' "$OUT" >/dev/null 2>&1; then
  echo "‼️ 产物里残留主项目旧域名 api.qianxutec.com —— 已判定为坏包。"
  grep -rloE '(^|[^a-z])api\.qianxutec\.com' "$OUT" | head -5
  exit 1
fi
if ! grep -rl "apiqf.qianxutec.com" "$OUT" >/dev/null 2>&1; then
  echo "‼️ 产物里没找到 $APP_API_BASE —— 已判定为坏包。"
  exit 1
fi
echo "  ✅ 接口域名正确，无 localhost / 旧域名残留"

echo "---- 产物 ----"
ls -la "$OUT"
exit 0
