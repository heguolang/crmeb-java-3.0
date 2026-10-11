#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TAG="${1:-3.0.0}"
cd "$ROOT"

echo "[1/4] Maven package admin + front ..."
mvn -f qianxu/pom.xml -pl qianxu-admin,qianxu-front -am -DskipTests package

echo "[2/4] Docker image qianxu-admin:${TAG} ..."
docker build -f deploy/docker/Dockerfile.admin -t "qianxu-admin:${TAG}" .

echo "[3/4] Docker image qianxu-front:${TAG} ..."
docker build -f deploy/docker/Dockerfile.front -t "qianxu-front:${TAG}" .

echo "[4/4] Docker image qianxu-admin-web:${TAG} ..."
docker build -f deploy/docker/Dockerfile.admin-web -t "qianxu-admin-web:${TAG}" .

echo
echo "OK. Images: qianxu-admin|front|admin-web:${TAG}"
echo "Next: cd deploy && cp .env.example .env && docker compose up -d"
