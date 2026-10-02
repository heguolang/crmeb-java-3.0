#!/bin/bash
# ============================================================
# 黔序商城 · 自用副库(qianxuqifu) 服务管理脚本
#   站点目录 : /www/wwwroot/apiqf.qianxutec.com
#   数据库   : MySQL qianxuqifu (用户 qianxuqifu)
#   Redis    : db12(admin+front 共用) / db14(微信共用)  ⚠ 必须共用同一 db，否则后台改配置前台读不到缓存
#   端口     : admin 21800 / front 21810
#
# ⚠ 本脚本只按「端口」精确取 PID，绝不使用 pkill -f / killall 之类宽泛匹配，
#   以免误杀同机上并存的 api.qianxutec.com(21700/21710)、admin.xml168.cn(20400/20410)。
# ============================================================
set -uo pipefail

D=/www/wwwroot/apiqf.qianxutec.com
ADMIN_JAR=$D/Qianxu-admin.jar
FRONT_JAR=$D/Qianxu-front.jar
DB_URL='jdbc:mysql://127.0.0.1:3306/qianxuqifu?characterEncoding=utf-8&useSSL=false&serverTimeZone=GMT+8'
DB_USER=qianxuqifu
DB_PASS='__SET_MYSQL_PASSWORD__'   # 入库脱敏：真实口令见线上该文件（chmod 700 root）
JAVA=$(command -v java || echo /usr/bin/java)
# 同机还有 main(1g+1g) 与 xml168(768m+768m)，本套限堆，避免把机器内存吃满
JVM_OPTS=(-server -Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss512k
          -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/)

pid_on_port() {
  ss -lntp 2>/dev/null | grep -E "LISTEN.*:${1}\b" | grep -oE 'pid=[0-9]+' | head -1 | cut -d= -f2
}

start_one() {
  local name=$1 port=$2 jar=$3; shift 3
  local pid; pid=$(pid_on_port "$port")
  if [ -n "$pid" ]; then echo "[$name] 已在运行 (port=$port pid=$pid)，跳过"; return 0; fi
  mkdir -p "$D/qianxu_log"
  cd "$D" || return 1
  setsid nohup "$JAVA" "${JVM_OPTS[@]}" -jar "$jar" \
      --spring.profiles.active=prod \
      --server.port="$port" \
      --spring.datasource.url="$DB_URL" \
      --spring.datasource.username="$DB_USER" \
      --spring.datasource.password="$DB_PASS" \
      --qianxu.imagePath="$D/" \
      "$@" \
      > "$D/qianxu_log/${name}_console.log" 2>&1 &
  echo "[$name] 已启动 (port=$port, pid=$!)"
}

stop_one() {
  local name=$1 port=$2 pid
  pid=$(pid_on_port "$port")
  if [ -z "$pid" ]; then echo "[$name] 未在运行 (port=$port)"; return 0; fi
  echo "[$name] 停止 pid=$pid (port=$port)"
  kill "$pid"
  for i in $(seq 1 30); do
    [ -z "$(pid_on_port "$port")" ] && { echo "[$name] 已停止"; return 0; }
    sleep 1
  done
  echo "[$name] 30s 未退出，强制 kill -9 pid=$pid"
  kill -9 "$pid" 2>/dev/null
}

status_one() {
  local name=$1 port=$2 pid
  pid=$(pid_on_port "$port")
  if [ -n "$pid" ]; then echo "[$name] UP   port=$port pid=$pid"; else echo "[$name] DOWN port=$port"; fi
}

case "${1:-status}" in
  start)
    start_one admin 21800 "$ADMIN_JAR" --spring.redis.database=12 --spring.redis.second.database=14
    sleep 2
    start_one front 21810 "$FRONT_JAR" --spring.redis.database=12 --spring.redis.second.database=14
    ;;
  stop)
    stop_one admin 21800
    stop_one front 21810
    ;;
  restart)
    "$0" stop; sleep 2; "$0" start
    ;;
  status)
    status_one admin 21800
    status_one front 21810
    ;;
  *)
    echo "用法: $0 {start|stop|restart|status}"; exit 1;;
esac
