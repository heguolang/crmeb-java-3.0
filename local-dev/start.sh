#!/bin/bash
# CRMEB Java 3.0 本地一键启动脚本
# 启动顺序：MySQL -> Redis -> 后台API(8080) -> 前台API(8081) -> 管理端前端(9527)
set -u

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="$HOME/.jdks/zulu8.96.0.205-ca-jdk8.0.504-macosx_aarch64/Contents/Home"
MYSQL_BIN="/opt/homebrew/opt/mysql@8.0/bin"
LOG_DIR="$PROJECT_DIR/local-dev/logs"
mkdir -p "$LOG_DIR"

# 避免环境变量 SERVER_PORT 覆盖 Spring 配置
unset SERVER_PORT

port_open() { (echo > /dev/tcp/127.0.0.1/$1) >/dev/null 2>&1; }

echo "==> [1/5] 检查 MySQL (3306)"
if port_open 3306; then
  echo "    MySQL 已在运行"
else
  nohup $MYSQL_BIN/mysqld --datadir=/opt/homebrew/var/mysql8 --socket=/tmp/mysql.sock --port=3306 --mysqlx=0 \
    > "$LOG_DIR/mysql.log" 2>&1 &
  for i in $(seq 1 30); do port_open 3306 && break; sleep 1; done
  echo "    MySQL 已启动"
fi

echo "==> [2/5] 检查 Redis (6379)"
if port_open 6379; then
  echo "    Redis 已在运行"
else
  nohup redis-server --port 6379 --requirepass 123456 --daemonize no --appendonly no \
    > "$LOG_DIR/redis.log" 2>&1 &
  for i in $(seq 1 15); do port_open 6379 && break; sleep 1; done
  echo "    Redis 已启动"
fi

echo "==> [3/5] 启动后台 API (8080)"
if port_open 8080; then
  echo "    8080 已被占用，跳过"
else
  nohup "$JAVA_HOME/bin/java" -jar "$PROJECT_DIR/crmeb/crmeb-admin/target/Crmeb-admin.jar" \
    --server.port=8080 --crmeb.imagePath="$PROJECT_DIR/crmeb/" \
    > "$LOG_DIR/crmeb-admin.log" 2>&1 &
fi

echo "==> [4/5] 启动前台 API (8081)"
if port_open 8081; then
  echo "    8081 已被占用，跳过"
else
  nohup "$JAVA_HOME/bin/java" -jar "$PROJECT_DIR/crmeb/crmeb-front/target/Crmeb-front.jar" \
    --server.port=8081 --crmeb.imagePath="$PROJECT_DIR/crmeb/" \
    > "$LOG_DIR/crmeb-front.log" 2>&1 &
fi

echo "==> [5/5] 启动管理端前端 (9527)"
if port_open 9527; then
  echo "    9527 已被占用，跳过"
else
  export NVM_DIR="$HOME/.nvm"
  [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh" && nvm use 16 >/dev/null
  nohup npm --prefix "$PROJECT_DIR/admin" run dev -- --port=9527 \
    > "$LOG_DIR/admin-web.log" 2>&1 &
fi

echo ""
echo "说明：会员端 H5 由 HBuilderX 运行（端口 8083），请双击 run-hbuilderx.command 启动"

echo ""
echo "等待服务启动（约 60-90 秒）..."
for i in $(seq 1 90); do
  if port_open 8080 && port_open 9527; then break; fi
  sleep 2
done

echo ""
echo "================ 服务地址 ================"
echo "管理后台前端 : http://127.0.0.1:9527   (账号 admin / 123456)"
echo "会员端 H5    : http://127.0.0.1:8083   (HBuilderX 运行，见 run-hbuilderx.command)"
echo "后台接口     : http://127.0.0.1:8080   接口文档 http://127.0.0.1:8080/doc.html"
echo "前台接口     : http://127.0.0.1:8081   (会员端调用此接口)"
echo "MySQL        : 127.0.0.1:3306  root/root  库名 crmeb"
echo "Redis        : 127.0.0.1:6379  密码 123456"
echo "日志目录     : $LOG_DIR"
echo "=========================================="
