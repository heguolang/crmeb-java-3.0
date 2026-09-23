#!/usr/bin/env bash
# ==================================================================
#  CRMEB - health check for all local services
#  Usage: bash /d/crmeb-java-3.0/local-dev/check-all.sh
# ==================================================================
set -u
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"

echo "---------------- PORT ----------------"
for p in 3306 6379 8080 8081 9527; do
  if netstat -ano 2>/dev/null | grep LISTENING | grep -q ":$p "; then
    echo "  $p : UP"
  else
    echo "  $p : DOWN"
  fi
done

echo "---------------- HTTP ----------------"
code() { curl -s -o /dev/null -m 8 -w "%{http_code}" "$1" 2>/dev/null; }
echo "  admin api  /admin/distributor/level/list : $(code 'http://127.0.0.1:8080/api/admin/distributor/level/list?page=1&limit=1')  (401/200 both OK)"
echo "  front api  /api/front/index              : $(code 'http://127.0.0.1:8081/api/front/index')  (expect 200)"
echo "  admin web  :9527                         : $(code 'http://127.0.0.1:9527/')  (expect 200)"

echo "---------------- REDIS CONFIG ----------------"
for db in 7 10; do
  v=$("D:/env/redis/redis-cli.exe" -a 123456 --no-auth-warning -n $db HGET config_list localUploadUrl 2>/dev/null | tr -d '\r')
  echo "  db$db localUploadUrl = ${v:-<empty>}"
done
