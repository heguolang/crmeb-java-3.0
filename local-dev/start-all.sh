#!/usr/bin/env bash
# ==================================================================
#  QIANXU Java 3.0 - start ALL local services with ONE command
#    Redis 6379 | Admin API 8080 | Front API 8081 | Admin Web 9527
#
#  Usage:  bash /d/qianxu-java-3.0/local-dev/start-all.sh
#          bash /d/qianxu-java-3.0/local-dev/start-all.sh --detach
#  Idempotent: any port already LISTENING is skipped.
#
#  Default mode keeps this shell ALIVE on purpose: when launched as a
#  background task, the host reaps child processes as soon as the task
#  exits - so we block forever to keep the services running.
#  --detach returns immediately (use when you run it by hand and accept
#  that the services may be reaped later).
#
#  Then check with:  bash /d/qianxu-java-3.0/local-dev/check-all.sh
# ==================================================================
set -u
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"

KEEP=1
case "${1:-}" in
  --detach|--nowait) KEEP=0 ;;
esac

JAVA="D:/env/java/jdk8u504-b01/bin/java.exe"
NODE_DIR="/c/Users/Administrator/.workbuddy/binaries/node/versions/22.22.2-3"
PROJ=/d/qianxu-java-3.0
LOGS=$PROJ/local-dev/logs
mkdir -p "$LOGS"

port_up() { netstat -ano 2>/dev/null | grep LISTENING | grep -q ":$1 "; }

echo "==================================================="
echo " QIANXU start-all (6379 / 8080 / 8081 / 9527)"
echo "==================================================="

# ---------- Redis 6379 ----------
if port_up 6379; then echo "[6379] Redis      -> already up"; else
  (cd /d/env/redis && ./redis-server.exe --port 6379 --requirepass 123456 --maxmemory 512mb > "$LOGS/redis.log" 2>&1 &)
  echo "[6379] Redis      -> starting"
fi

# ---------- Admin API 8080 ----------
if port_up 8080; then echo "[8080] Admin API  -> already up"; else
  (cd "$PROJ/qianxu/qianxu-admin/target" && "$JAVA" -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 \
     -jar Qianxu-admin.jar --server.port=8080 > "$LOGS/admin-8080.log" 2>&1 &)
  echo "[8080] Admin API  -> starting (~60s)"
fi

# ---------- Front API 8081 ----------
if port_up 8081; then echo "[8081] Front API  -> already up"; else
  (cd "$PROJ/qianxu/qianxu-front/target" && "$JAVA" -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 \
     -jar Qianxu-front.jar --server.port=8081 > "$LOGS/front-8081.log" 2>&1 &)
  echo "[8081] Front API  -> starting (~60s)"
fi

# ---------- Admin Web 9527 ----------
if port_up 9527; then echo "[9527] Admin Web  -> already up"; else
  (cd "$PROJ/admin" && PATH="$NODE_DIR:$PATH" NODE_OPTIONS=--openssl-legacy-provider \
     "$NODE_DIR/node.exe" node_modules/@vue/cli-service/bin/vue-cli-service.js serve --port=9527 \
     > "$LOGS/admin-web.log" 2>&1 &)
  echo "[9527] Admin Web  -> starting (webpack ~25s)"
fi

echo
echo "Launched. Admin API needs ~60s, Web needs ~25s."
echo "Check with:  bash $PROJ/local-dev/check-all.sh"

if [ "$KEEP" -eq 1 ]; then
  echo
  echo "[keep-alive] staying alive so services are not reaped (Ctrl+C to detach)..."
  while true; do sleep 30; done
fi
