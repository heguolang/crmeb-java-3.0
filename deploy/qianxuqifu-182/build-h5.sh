#!/usr/bin/env bash
# 会员端 H5 生产构建（黔序商城 · qianxuqifu 副项目）
#
# 用途：把 qianxuqifu-app 编译成 appqf.qianxutec.com 可直接托管的静态产物。
# 产物形态：index.html + static/ + assets/ + pages/（与线上现网一致）
#
# 用法：
#   bash build-h5.sh                # 输出到 /tmp/qifu-h5-build
#   OUT=/tmp/xxx bash build-h5.sh   # 自定义输出目录
#
# ⚠️ 三个必须踩准的点（2026-10-02 踩过）：
#   1) 必须用 HBuilderX 自带的 node18，不用系统 node22
#      （node22 编不了这套老 vue-cli 4 / node-sass）
#   2) node_modules/.bin/vue-cli-service 是坏软链 → 必须直接调
#      node_modules/@vue/cli-service/bin/vue-cli-service.js
#   3) UNI_HBUILDERX_PLUGINS 必须给，否则编译器找不到 uni 插件链
#
# 日志同时落 $OUT/../build-h5.log，便于观察卡在哪一步（构建约 3-6 分钟）

set -o pipefail

HBX=/Applications/HBuilderX.app/Contents/HBuilderX
CLI="$HBX/plugins/uniapp-cli"
NODE="$HBX/plugins/node18/node"
SRC=/Users/qianxu/WorkBuddy/qianxuqifu/qianxuqifu-app
OUT=${OUT:-/tmp/qifu-h5-build}
LOG="${OUT%/}.log"

# ---- 构建前接线预检（ref / 组件方法）----
# 2026-10-02 事故：模板漏 ref="sealPad" 但脚本里调 this.$refs.sealPad → 点「确认签署」必炸，
# 编译与热编译都不报错，只有客户点下去才暴露。这里做机械卡点，拦住整类问题。
WIRE_CHECK="$HOME/.workbuddy/skills/qianxu-build-deploy/tools/h5_wiring_check.py"
PY_BIN="${PY_BIN:-$HOME/.workbuddy/binaries/python/versions/3.13.12/bin/python3}"
if [ -f "$WIRE_CHECK" ] && [ "${WIRING_SKIP:-0}" != "1" ]; then
  echo "---- 接线预检（ref / 组件方法）----"
  if ! "$PY_BIN" "$WIRE_CHECK" "$SRC"; then
    echo "‼️ 接线检查未通过 —— 已中断构建，避免把坏包发上线。"
    echo "   （确认可忽略时用 WIRING_SKIP=1 再跑一次）"
    exit 1
  fi
fi

rm -rf "$OUT" && mkdir -p "$OUT"

cd "$CLI" || exit 1
env \
  VUE_CLI_CONTEXT="$CLI" \
  UNI_CLI_CONTEXT="$CLI" \
  UNI_HBUILDERX_PLUGINS="$HBX/plugins" \
  UNI_INPUT_DIR="$SRC" \
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
echo "---- 产物 ----"
ls -la "$OUT"
exit $RC
