#!/bin/bash
# 链动自测订单补付：按 uid 升序逐单余额支付（顺序影响奖励结算，不可乱序）
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
M="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"

for U in "$@"; do
  PHONE=$("$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT phone FROM eb_user WHERE uid=$U" 2>/dev/null | tr -d '\r')
  ORDERNO=$("$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e \
    "SELECT order_id FROM eb_store_order WHERE uid=$U AND mark='[AI自测]链动2+1端到端' AND paid=0 ORDER BY id DESC LIMIT 1" 2>/dev/null | tr -d '\r')
  [ -z "$ORDERNO" ] && { echo "uid=$U 无未付订单，跳过"; continue; }
  TK=$(curl -s -X POST "$FRONT/api/front/login" -H "Content-Type: application/json" \
    -d "{\"account\":\"$PHONE\",\"password\":\"123456\"}" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
  PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: $TK" -H "Content-Type: application/json" \
    -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}")
  CODE=$(echo "$PAY" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)
  echo "uid=$U order=$ORDERNO pay_code=$CODE"
  [ "$CODE" != "200" ] && echo "   -> $(echo $PAY | head -c 180)"
done
