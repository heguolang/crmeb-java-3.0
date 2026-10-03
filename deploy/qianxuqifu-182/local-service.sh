#!/bin/bash
# ============================================================
# 黔序自用副库(qianxuqifu) · 本地 macOS 服务管理
#
#   源码目录 : 从脚本位置自动推导（repo/qianxu），可用 R= 覆盖
#   MySQL    : 127.0.0.1:3306 / qianxu_java3（账号口令用 LOCAL_DB_USER/LOCAL_DB_PASS，默认 root/root）
#   Redis    : 127.0.0.1:6379, 密码 123456
#              主库 db12（admin+front **共用**）/ 副库 db9（微信 accessToken 共用）
#   端口     : admin 18080 / front 18081
#
# ⚠ 与线上 182 同源的两条铁律：
#   ① admin 与 front 的 --spring.redis.database 必须相同。
#      prod profile 下 front 的 asyncConfig=true（读 Redis 缓存）、admin=false（直查库），
#      靠「后台保存时刷 Redis」补洞 —— 分成两个库就把补洞切断，
#      表现为「后台改配置、会员端一直读旧值」，且不报任何错。
#   ② 本机 Redis 是默认配置只有 16 个库（db0–15），库号别越界；
#      主项目 tech.qianxu 占用 db10/13/14，本项目避开。
#
# 用法: bash local-service.sh {start|stop|restart|status}
# ============================================================
set -uo pipefail

# 源码目录：从脚本位置自动推导；本机 MySQL / 本地口令可用环境变量覆盖
#   （本地弱口令不再写死在脚本里：LOCAL_DB_USER / LOCAL_DB_PASS，默认 root/root）
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
R=${R:-$ROOT/qianxu}
# JVM：优先系统默认 java，没装 JDK 时回退到本机 zulu8（可用 JAVA=/path/to/java 覆盖）
JAVA=${JAVA:-$(command -v java || echo /Users/qianxu/.jdks/zulu8.96.0.205-ca-jdk8.0.504-macosx_aarch64/Contents/Home/bin/java)}
LOCAL_DB_USER=${LOCAL_DB_USER:-root}
LOCAL_DB_PASS=${LOCAL_DB_PASS:-root}
DBURL=${DBURL:-'jdbc:mysql://127.0.0.1:3306/qianxu_java3?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true'}
REDIS_DB=12
REDIS_SECOND_DB=9

pid_on_port() { lsof -nP -iTCP:"$1" -sTCP:LISTEN -t 2>/dev/null | head -1; }

start_one() {
  local name=$1 port=$2 jar=$3
  local pid; pid=$(pid_on_port "$port")
  if [ -n "$pid" ]; then echo "[$name] 已在运行 (port=$port pid=$pid)，跳过"; return 0; fi
  # ⚠ 必须 start_new_session（等价 setsid）：否则在 Agent/CI 等非交互环境里，
  #   父 shell 退出时子进程会被整组回收 —— 表现是「启动日志有 Started，进程却没了」。
  #   macOS 默认没有 setsid 命令，故借 python 起进程。
  /usr/bin/python3 - "$JAVA" "$R/$jar" "$port" "$DBURL" "$REDIS_DB" "$REDIS_SECOND_DB" \
      "/tmp/qifu_${name}_${port}.log" "$R/" "$LOCAL_DB_USER" "$LOCAL_DB_PASS" <<'PY'
import subprocess, sys
java, jar, port, url, db, db2, log, img, dbuser, dbpass = sys.argv[1:11]
cmd = [java, "-Dfile.encoding=UTF-8", "-jar", jar,
       "--server.port=" + port,
       "--spring.datasource.url=" + url,
       "--spring.datasource.username=" + dbuser,
       "--spring.datasource.password=" + dbpass,
       "--qianxu.imagePath=" + img,
       "--spring.redis.database=" + db,
       "--spring.redis.second.database=" + db2]
with open(log, "w") as f:
    p = subprocess.Popen(cmd, stdout=f, stderr=subprocess.STDOUT, start_new_session=True)
print("  pid=%d" % p.pid)
PY
  echo "[$name] 已启动 (port=$port) → /tmp/qifu_${name}_${port}.log"
}

stop_one() {
  local name=$1 port=$2 pid
  pid=$(pid_on_port "$port")
  if [ -z "$pid" ]; then echo "[$name] 未在运行 (port=$port)"; return 0; fi
  echo "[$name] 停止 pid=$pid (port=$port)"
  kill "$pid"
  for _ in $(seq 1 30); do
    [ -z "$(pid_on_port "$port")" ] && { echo "[$name] 已停止"; return 0; }
    sleep 1
  done
  echo "[$name] 30s 未退出，强制 kill -9 pid=$pid"
  kill -9 "$pid" 2>/dev/null
}

status_one() {
  local pid; pid=$(pid_on_port "$2")
  if [ -n "$pid" ]; then echo "[$1] UP   port=$2 pid=$pid"; else echo "[$1] DOWN port=$2"; fi
}

case "${1:-status}" in
  start)
    start_one admin 18080 qianxu-admin/target/Qianxu-admin.jar
    sleep 2
    start_one front 18081 qianxu-front/target/Qianxu-front.jar
    ;;
  stop)
    stop_one admin 18080
    stop_one front 18081
    ;;
  restart)
    "$0" stop; sleep 2; "$0" start
    ;;
  status)
    status_one admin 18080
    status_one front 18081
    ;;
  *)
    echo "用法: $0 {start|stop|restart|status}"; exit 1;;
esac
