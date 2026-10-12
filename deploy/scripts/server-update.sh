#!/usr/bin/env bash
# 服务器一键更新：git pull → 打包 JAR → 打 Docker 镜像 → 滚动重启
# 用法：
#   cd /opt/qianxu-docker/crmeb-java-3.0
#   chmod +x deploy/scripts/server-update.sh
#   ./deploy/scripts/server-update.sh              # 自动生成版本号
#   ./deploy/scripts/server-update.sh 3.0.1        # 指定版本号
#   ./deploy/scripts/server-update.sh --skip-pull  # 不拉 git（仅本地已有代码）
#   ./deploy/scripts/server-update.sh --official   # 用官方基础镜像（默认国内 DaoCloud）
#
# 更新范围：Java admin/front + 后台 Vue（admin-web）
# 不含：H5/小程序（qianxu-app，仍需单独发到 /www/wwwroot/app...）

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
DEPLOY="$ROOT/deploy"
TAG=""
DO_PULL=1
USE_CHINA=1

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-pull) DO_PULL=0; shift ;;
    --official)  USE_CHINA=0; shift ;;
    --china)     USE_CHINA=1; shift ;;
    -h|--help)
      sed -n '2,14p' "$0"
      exit 0
      ;;
    -*)
      echo "未知参数: $1" >&2
      exit 1
      ;;
    *)
      TAG="$1"
      shift
      ;;
  esac
done

cd "$ROOT"

if [[ ! -f "$DEPLOY/.env" ]]; then
  echo "缺少 $DEPLOY/.env，请先配置后再更新" >&2
  exit 1
fi

COMPOSE=(docker compose -f "$DEPLOY/docker-compose.yml")
if [[ -f "$DEPLOY/docker-compose.baota.yml" ]]; then
  COMPOSE+=(-f "$DEPLOY/docker-compose.baota.yml")
  echo "[info] 使用宝塔端口映射: docker-compose.baota.yml"
fi

echo "========== 1/5 Git =========="
if [[ "$DO_PULL" -eq 1 ]]; then
  git fetch --all --prune
  BRANCH="$(git rev-parse --abbrev-ref HEAD)"
  echo "分支: $BRANCH"
  git pull --ff-only origin "$BRANCH" || git pull --ff-only
else
  echo "跳过 git pull (--skip-pull)"
fi

SHORT_SHA="$(git rev-parse --short HEAD)"
if [[ -z "$TAG" ]]; then
  TAG="3.0.$(date +%Y%m%d%H%M)-${SHORT_SHA}"
fi
echo "镜像 TAG: $TAG  (commit $SHORT_SHA)"

echo "========== 2/5 Maven 打包 =========="
if command -v mvn >/dev/null 2>&1; then
  MVN=(mvn)
elif [[ -x "$ROOT/qianxu/mvnw" ]]; then
  MVN=("$ROOT/qianxu/mvnw")
else
  echo "未找到 mvn / mvnw" >&2
  exit 1
fi
"${MVN[@]}" -f qianxu/pom.xml -pl qianxu-admin,qianxu-front -am -DskipTests package

echo "========== 3/5 构建 Docker 镜像 =========="
if [[ "$USE_CHINA" -eq 1 ]]; then
  echo "基础镜像: 国内加速 (*.china Dockerfile)"
  docker build -f deploy/docker/Dockerfile.admin.china -t "qianxu-admin:${TAG}" .
  docker build -f deploy/docker/Dockerfile.front.china -t "qianxu-front:${TAG}" .
else
  docker build -f deploy/docker/Dockerfile.admin -t "qianxu-admin:${TAG}" .
  docker build -f deploy/docker/Dockerfile.front -t "qianxu-front:${TAG}" .
fi
docker build -f deploy/docker/Dockerfile.admin-web -t "qianxu-admin-web:${TAG}" .

# 同时打 latest，方便排查
docker tag "qianxu-admin:${TAG}" qianxu-admin:latest
docker tag "qianxu-front:${TAG}" qianxu-front:latest
docker tag "qianxu-admin-web:${TAG}" qianxu-admin-web:latest

echo "========== 4/5 写入 .env IMAGE_TAG =========="
if grep -q '^IMAGE_TAG=' "$DEPLOY/.env"; then
  sed -i "s/^IMAGE_TAG=.*/IMAGE_TAG=${TAG}/" "$DEPLOY/.env"
else
  echo "IMAGE_TAG=${TAG}" >> "$DEPLOY/.env"
fi
grep '^IMAGE_TAG=' "$DEPLOY/.env"

echo "========== 5/5 重启容器 =========="
cd "$DEPLOY"
"${COMPOSE[@]}" up -d
echo
"${COMPOSE[@]}" ps
echo
echo "等待 admin 启动..."
sleep 25
"${COMPOSE[@]}" logs --tail=25 admin || true
echo
echo "自检:"
curl -s -o /dev/null -w "  admin :21700 -> %{http_code}\n" http://127.0.0.1:21700/api/admin/getLoginPic || true
curl -s -o /dev/null -w "  front :21710 -> %{http_code}\n" http://127.0.0.1:21710/api/front/index || true
curl -s -o /dev/null -w "  nginx :18080 -> %{http_code}\n" http://127.0.0.1:18080/ || true
echo
echo "完成。版本 IMAGE_TAG=${TAG}"
echo "若有 SQL 升级，请另行导入后再执行: ${COMPOSE[*]} restart admin front"
echo "H5 未包含在此脚本内，需单独发布到站点目录。"
