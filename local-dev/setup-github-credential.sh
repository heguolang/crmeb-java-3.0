#!/bin/bash
# 配置 GitHub 推送凭据（token 只写入本机钥匙串，不经过任何对话/日志）
#
# 说明：GitHub 早已禁用「密码」做 git 操作，推送只能用一个 Personal Access Token（PAT）。
#       Token 在你的「终端」里输入、不回显，脚本只把它写进 macOS 钥匙串，
#       既不落成明文文件，也不会出现在对话记录里。
#
# 用法：在「终端」里执行
#     bash /Users/qianxu/WorkBuddy/java/qianxu-java-3.0/local-dev/setup-github-credential.sh
#
# 跑完会做一次真实验证，并生成 local-dev/push-to-github.sh —— 之后直接跑它就能推。

set -u

REPO="/Users/qianxu/WorkBuddy/java/qianxu-java-3.0"
GH_REPO="heguolang/qianxu-java-3.0"
DIRECT_HOST="github.com"
DIRECT_URL="https://${DIRECT_HOST}/${GH_REPO}.git"
MIRROR_HOST="ghfast.top"
MIRROR_URL="https://${MIRROR_HOST}/https://github.com/${GH_REPO}.git"

echo "==> 配置 GitHub 推送凭据"
echo "    token 只写入本机钥匙串：不显示在屏幕上、不落成明文文件、不进对话记录"
echo ""

DEFAULT_USER="$(git -C "$REPO" config --local user.name 2>/dev/null || echo '')"
printf "GitHub 用户名 [%s]: " "$DEFAULT_USER"
read -r GH_USER
GH_USER="${GH_USER:-$DEFAULT_USER}"
if [ -z "$GH_USER" ]; then
  echo "!! 用户名不能为空" >&2
  exit 1
fi

printf "Personal Access Token（输入时不回显）: "
stty -echo
read -r GH_TOKEN
stty echo
echo ""

if [ -z "$GH_TOKEN" ]; then
  echo "!! token 不能为空" >&2
  exit 1
fi

echo "==> 校验 token ..."
API_USER="$(curl -s -m 15 -H "Authorization: Bearer $GH_TOKEN" \
  -H "Accept: application/vnd.github+json" https://api.github.com/user \
  | sed -n 's/.*"login"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

if [ -z "$API_USER" ]; then
  echo "!! token 校验失败：api.github.com 没返回用户信息" >&2
  echo "   请确认 token 未过期；classic token 勾 repo，fine-grained token 需 Contents: Read and write" >&2
  exit 1
fi
echo "    校验通过，身份：$API_USER"

echo "==> 写入 macOS 钥匙串（host: $DIRECT_HOST）"
security add-internet-password -a "$GH_USER" -s "$DIRECT_HOST" -w "$GH_TOKEN" -U 2>/dev/null \
  && echo "    已写入" \
  || { echo "!! 钥匙串写入失败" >&2; exit 1; }

echo "==> 让本仓库使用钥匙串凭据"
git -C "$REPO" config --local credential.helper osxkeychain
echo "    已设置 credential.helper=osxkeychain"

echo "==> 生成推送脚本 local-dev/push-to-github.sh"
cat > "$REPO/local-dev/push-to-github.sh" <<PUSH
#!/bin/bash
# 自动生成：推送 master 到 GitHub
# 优先直连 github.com；直连失败时自动降级到 ghfast.top 镜像。
set -u
REPO="$REPO"
GH_USER="${GH_USER}"
COMMON=(-c http.proxy= -c https.proxy= -c http.lowSpeedLimit=1000 -c http.lowSpeedTime=60)

if git -C "\$REPO" "\${COMMON[@]}" ls-remote \\
     "https://\${GH_USER}@${DIRECT_HOST}/${GH_REPO}.git" HEAD >/dev/null 2>&1; then
  TARGET="https://\${GH_USER}@${DIRECT_HOST}/${GH_REPO}.git"
  echo "==> 直连 github.com 推送"
else
  TARGET="https://\${GH_USER}@${MIRROR_HOST}/https://github.com/${GH_REPO}.git"
  echo "==> 直连不通，改用 ghfast.top 镜像推送"
fi

git -C "\$REPO" "\${COMMON[@]}" push "\$TARGET" master
echo "==> 推送完成"
git -C "\$REPO" log --oneline -1
PUSH
chmod +x "$REPO/local-dev/push-to-github.sh"
echo "    已生成"

echo "==> 真实验证（对私有仓库做 ls-remote，能通过就说明凭据可用）"
if git -C "$REPO" -c http.proxy= -c https.proxy= \
     ls-remote "https://${GH_USER}@${DIRECT_HOST}/${GH_REPO}.git" HEAD >/dev/null 2>&1; then
  echo "    ✅ 验证通过（直连 github.com 可用）"
  echo ""
  echo "全部就绪，接下来直接跑："
  echo "  bash $REPO/local-dev/push-to-github.sh"
elif git -C "$REPO" -c http.proxy= -c https.proxy= \
     ls-remote "https://${GH_USER}@${MIRROR_HOST}/https://github.com/${GH_REPO}.git" HEAD >/dev/null 2>&1; then
  echo "    ✅ 验证通过（走 ghfast.top 镜像）"
  echo ""
  echo "全部就绪，接下来直接跑："
  echo "  bash $REPO/local-dev/push-to-github.sh"
else
  echo "    ❌ 验证失败" >&2
  echo "   常见原因：token 权限不足（需 repo / Contents: Read and write）、token 已过期、用户名填错" >&2
  exit 1
fi
