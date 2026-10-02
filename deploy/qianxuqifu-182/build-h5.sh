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
