#!/bin/bash
# 拼购端到端自测：A 推荐人 + B1..B10 余额支付参团，满10人开奖
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
ADMIN=http://127.0.0.1:8080
M="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"

# 后台 token
TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" -d '{"account":"admin","pwd":"123456"}' | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo "admin token: $TOK"

# 1. 注册 A + B1..B10（B 的 spread_spid 全部指向 A）
declare -A TOKENS
declare -A UIDS
for i in 00 01 02 03 04 05 06 07 08 09 10; do
  P="166000200$i"
  SPID=0
  [ "$i" != "00" ] && SPID=${UIDS[00]}
  RES=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
    -d "{\"account\":\"$P\",\"password\":\"123456\",\"spread_spid\":$SPID}")
  UID_I=$(echo "$RES" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
  TK=$(echo "$RES" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
  UIDS[$i]=$UID_I
  TOKENS[$i]=$TK
  echo "member $P -> uid=$UID_I token=${TK:0:8}..."
done

# 2. 充值每人 200 元
for i in 00 01 02 03 04 05 06 07 08 09 10; do
  curl -s "$ADMIN/api/admin/user/operate/founds?uid=${UIDS[$i]}&integralType=1&integralValue=0&moneyType=1&moneyValue=200" -H "Authori-zation: $TOK" > /dev/null
done
echo "charged 200 each"

# 3. B01..B10 依次参团：预下单 → 创建订单 → 余额支付
for i in 01 02 03 04 05 06 07 08 09 10; do
  P="166000200$i"
  PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "Authori-zation: ${TOKENS[$i]}" -H "Content-Type: application/json" \
    -d '{"preOrderType":"buyNow","orderDetails":[{"productId":172,"attrValueId":3411,"productNum":1,"shoppingCartId":0}]}')
  PRENO=$(echo "$PRE" | grep -o '"preOrderNo":"[^"]*' | sed 's/"preOrderNo":"//')
  if [ -z "$PRENO" ]; then echo "B$i preOrder FAIL: $PRE"; continue; fi
  ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "Authori-zation: ${TOKENS[$i]}" -H "Content-Type: application/json" \
    -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":5,\"realName\":\"拼购自测\",\"phone\":\"$P\",\"useIntegral\":false,\"mark\":\"[AI自测]拼购端到端自测\"}")
  ORDERNO=$(echo "$ORD" | grep -o '"orderNo":"[^"]*' | sed 's/"orderNo":"//')
  if [ -z "$ORDERNO" ]; then echo "B$i create FAIL: $ORD"; continue; fi
  PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: ${TOKENS[$i]}" -H "Content-Type: application/json" \
    -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"from\":\"weixinh5\"}")
  echo "B$i order=$ORDERNO pay: $(echo $PAY | head -c 120)"
done

# 4. 查库核对
echo "=== pingou_team ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT * FROM eb_pingou_team;" 2>/dev/null
echo "=== pingou_join (前12列) ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT id,team_id,uid,order_no,pay_price,spread_uid,is_win,reward_status,refund_price,subsidy_price,share_rate,share_price,share_status FROM eb_pingou_join;" 2>/dev/null
echo "=== 用户资金：A=推荐人 B1..B10 ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT uid,phone,now_money,brokerage_price FROM eb_user WHERE phone LIKE '166000200%' ORDER BY uid;" 2>/dev/null
echo "=== 佣金记录（推荐人分享奖励 + 未中奖补贴）==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT uid,title,price,balance,mark FROM eb_user_brokerage_record ORDER BY id DESC LIMIT 15;" 2>/dev/null
