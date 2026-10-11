#!/bin/bash
# ==================================================================
#  2+1 链动 端到端自测
#  拓扑：A -> (B1,B2) 直推满员升老板
#        A -> (C1,C2) 溢出滑落到 B1/B2，C1 记为 A 的帮扶对象
#        B1 -> D1     使 B1 直推满 2 升老板（产生 2 代见单 + 平级）
#        C1 -> (E1,E2) 使 C1 升老板 -> 触发 A 的帮扶奖
#  全部走真实接口：注册 / 后台充值 / 预下单 / 下单 / 余额支付
# ==================================================================
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
ADMIN=http://127.0.0.1:8080
M="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"
PRODUCT=173
ATTR=3412
STORE=5
MARK='[AI自测]链动2+1端到端'
MONEY=500

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo "admin token: ${TOK:0:10}..."

declare -A UIDS TOKENS

register() {   # $1=槽位  $2=手机号  $3=推荐人uid
  local KEY=$1 PHONE=$2 SPID=$3
  local RES=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
    -d "{\"account\":\"$PHONE\",\"password\":\"123456\",\"spread_spid\":$SPID}")
  local U=$(echo "$RES" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
  local T=$(echo "$RES" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
  if [ -z "$U" ]; then echo "  [$KEY] 注册失败: $(echo $RES | head -c 200)"; return 1; fi
  UIDS[$KEY]=$U; TOKENS[$KEY]=$T
  echo "  [$KEY] phone=$PHONE uid=$U spread_spid=$SPID"
}

charge() {     # $1=槽位
  local KEY=$1
  local R=$(curl -s "$ADMIN/api/admin/user/operate/founds?uid=${UIDS[$KEY]}&integralType=1&integralValue=0&moneyType=1&moneyValue=$MONEY" \
    -H "Authori-zation: $TOK")
  echo "  [$KEY] 充值$MONEY -> $(echo "$R" | head -c 80)"
}

buy() {        # $1=槽位
  local KEY=$1
  local PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "Authori-zation: ${TOKENS[$KEY]}" -H "Content-Type: application/json" \
    -d "{\"preOrderType\":\"buyNow\",\"orderDetails\":[{\"productId\":$PRODUCT,\"attrValueId\":$ATTR,\"productNum\":1,\"shoppingCartId\":0}]}")
  local PRENO=$(echo "$PRE" | grep -o '"preOrderNo":"[^"]*' | sed 's/"preOrderNo":"//')
  if [ -z "$PRENO" ]; then echo "  [$KEY] 预下单失败: $(echo $PRE | head -c 200)"; return 1; fi
  local ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "Authori-zation: ${TOKENS[$KEY]}" -H "Content-Type: application/json" \
    -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":$STORE,\"realName\":\"链动自测\",\"phone\":\"${UIDS[$KEY]}\",\"useIntegral\":false,\"mark\":\"$MARK\"}")
  local ORDERNO=$(echo "$ORD" | grep -o '"orderNo":"[^"]*' | sed 's/"orderNo":"//')
  if [ -z "$ORDERNO" ]; then echo "  [$KEY] 下单失败: $(echo $ORD | head -c 200)"; return 1; fi
  local PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: ${TOKENS[$KEY]}" -H "Content-Type: application/json" \
    -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}")
  local PCODE=$(echo "$PAY" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)
  echo "  [$KEY] order=$ORDERNO pay_code=$PCODE"
  [ "$PCODE" != "200" ] && echo "      支付响应: $(echo $PAY | head -c 200)"
  return 0
}

echo "=== [1/5] 注册 A（链首） ==="
register A 16600040001 0 || exit 1
echo "=== [2/5] A 充值并购买礼包（成为代理） ==="
charge A; buy A
echo "=== [3/5] A 直推 B1、B2（每人一单） ==="
register B1 16600040002 ${UIDS[A]}; charge B1; buy B1
register B2 16600040003 ${UIDS[A]}; charge B2; buy B2
echo "=== [4/5] A 继续推 C1、C2（应滑落到 B1/B2 下） ==="
register C1 16600040004 ${UIDS[A]}; charge C1; buy C1
register C2 16600040005 ${UIDS[A]}; charge C2; buy C2
echo "=== [5/5] B1 推 D1；C1 推 E1、E2 ==="
register D1 16600040006 ${UIDS[B1]}; charge D1; buy D1
register E1 16600040007 ${UIDS[C1]}; charge E1; buy E1
register E2 16600040008 ${UIDS[C1]}; charge E2; buy E2

echo
echo "=== 拓扑落库结果 ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e \
 "SELECT u.uid,u.phone,u.spread_uid 邀请人,u.chain_invite_uid 链动邀请,u.chain_parent_id 安置上级,u.chain_identity 身份(0无1代理2老板),u.now_money 余额,u.brokerage_price 佣金 FROM eb_user u WHERE u.phone LIKE '166000400%' ORDER BY u.uid;" 2>/dev/null
