#!/bin/bash
# 对 [AI自测] 拼购未支付订单补付（余额支付，payChannel=weixinh5）
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
M="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"

for UID_I in 81 82 83 84 85 86 87 88 89 90; do
  PHONE=$("$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT phone FROM eb_user WHERE uid=$UID_I" 2>/dev/null)
  ORDERNO=$("$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT order_id FROM eb_store_order WHERE uid=$UID_I AND mark LIKE '[AI自测]%' AND paid=0 ORDER BY id DESC LIMIT 1" 2>/dev/null)
  [ -z "$ORDERNO" ] && { echo "uid=$UID_I 无未付订单"; continue; }
  TK=$(curl -s -X POST "$FRONT/api/front/login" -H "Content-Type: application/json" -d "{\"account\":\"$PHONE\",\"password\":\"123456\"}" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
  PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: $TK" -H "Content-Type: application/json" \
    -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}")
  echo "uid=$UID_I order=$ORDERNO -> $(echo $PAY | head -c 150)"
done
