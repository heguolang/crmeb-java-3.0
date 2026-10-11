#!/bin/bash
# 补跑帮扶链路：A(102) 已是老板，再推 F1 触发滑落+记帮扶对象，F1 推 2 人升老板 -> A 拿帮扶奖
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
ADMIN=http://127.0.0.1:8080
MARK='[AI自测]链动2+1帮扶'
PRODUCT=173; ATTR=3412; STORE=5; MONEY=500

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' | grep -o '"token":"[^"]*' | sed 's/"token":"//')

declare -A UIDS TOKENS
register() {
  local RES=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
    -d "{\"account\":\"$2\",\"password\":\"123456\",\"spread_spid\":$3}")
  UIDS[$1]=$(echo "$RES" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
  TOKENS[$1]=$(echo "$RES" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
  echo "  [$1] $2 uid=${UIDS[$1]} 推荐=$3"
}
charge() {
  curl -s "$ADMIN/api/admin/user/operate/founds?uid=${UIDS[$1]}&integralType=1&integralValue=0&moneyType=1&moneyValue=$MONEY" -H "Authori-zation: $TOK" >/dev/null
}
buy() {
  local PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "Authori-zation: ${TOKENS[$1]}" -H "Content-Type: application/json" \
    -d "{\"preOrderType\":\"buyNow\",\"orderDetails\":[{\"productId\":$PRODUCT,\"attrValueId\":$ATTR,\"productNum\":1,\"shoppingCartId\":0}]}")
  local PRENO=$(echo "$PRE" | grep -o '"preOrderNo":"[^"]*' | sed 's/"preOrderNo":"//')
  local ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "Authori-zation: ${TOKENS[$1]}" -H "Content-Type: application/json" \
    -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":$STORE,\"realName\":\"链动自测\",\"phone\":\"${UIDS[$1]}\",\"useIntegral\":false,\"mark\":\"$MARK\"}")
  local ORDERNO=$(echo "$ORD" | grep -o '"orderNo":"[^"]*' | sed 's/"orderNo":"//')
  local PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: ${TOKENS[$1]}" -H "Content-Type: application/json" \
    -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}")
  echo "  [$1] order=$ORDERNO pay_code=$(echo "$PAY" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)"
}

echo "=== A(102) 推 F1 -> 应滑落并记为帮扶对象 ==="
register F1 16600040009 102; charge F1; buy F1
sleep 3
echo "=== F1 推 G1、G2 -> F1 升老板 -> A 拿帮扶奖 ==="
register G1 16600040010 ${UIDS[F1]}; charge G1; buy G1
register G2 16600040011 ${UIDS[F1]}; charge G2; buy G2
echo "完成，等支付任务结算后查库"
