#!/bin/bash
# 自查：活动按等级「独立设置」覆盖默认比例
# 建活动(shareTiers=[{levelId:1,...,rewardValue:8.88}]) → B1..B10 余额支付参团 → 满员开奖
# 期望：推荐人 A 每笔分享奖 8.88（100×8.88%），共 88.80（若走默认 5% 则是 5.00/50.00，即判失败）
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
ADMIN=http://127.0.0.1:8080
M="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"
MARK="[AI自测]等级独立设置8.88"
TS=$(date +%Y%m%d_%H%M%S)
SNAP="/d/qianxu-java-3.0/local-dev/logs/tier_verify_snap_$TS.txt"

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" -d '{"account":"admin","pwd":"123456"}' | grep -o '"token":"[^"]*' | sed 's/"token":"//')

# 1. 建活动（A级独立设置 8.88%）
RES=$(curl -s -X POST "$ADMIN/api/admin/pingou/activity/save" -H "Authori-zation: $TOK" -H "Content-Type: application/json" \
  -d '{"name":"[AI自测]等级独立设置8.88","image":"","productId":172,"productName":"自测商品","productPrice":100.00,"startTime":"2026-09-28 00:00:00","endTime":"2026-10-31 23:59:59","totalNum":10,"winNum":2,"refundSubsidy":0,"shareTiers":[{"levelId":1,"minDirectNum":1,"rewardType":1,"rewardValue":8.88}],"limitNum":0,"isShow":false,"sort":0,"status":1}')
echo "create activity: $RES"
AID=$("$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT id FROM eb_pingou_activity WHERE name='[AI自测]等级独立设置8.88' AND is_del=0 ORDER BY id DESC LIMIT 1" 2>/dev/null)
echo "activity id=$AID"
[ -z "$AID" ] && { echo "活动创建失败，退出"; exit 1; }

# 2. 快照（回滚依据）
{
  echo "== snapshot $TS =="
  echo "-- activity: $AID"
  "$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT uid,now_money,brokerage_price FROM eb_user WHERE phone LIKE '166000200%' ORDER BY uid" 2>/dev/null
  echo "-- stock attr 3411:"
  "$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT id,stock FROM eb_store_product_attr_value WHERE id=3411" 2>/dev/null
  echo "-- MAX ids:"
  "$M" -uroot -p123456 -h127.0.0.1 -N qianxu -e "SELECT (SELECT MAX(id) FROM eb_store_order),(SELECT MAX(id) FROM eb_user_bill),(SELECT MAX(id) FROM eb_user_brokerage_record),(SELECT MAX(id) FROM eb_pingou_join),(SELECT MAX(id) FROM eb_pingou_team)" 2>/dev/null
} > "$SNAP"
echo "snapshot -> $SNAP"; cat "$SNAP"

# 3. 登录 A + B1..B10
declare -A TOKENS UIDS
for i in 00 01 02 03 04 05 06 07 08 09 10; do
  P="166000200$i"
  RES=$(curl -s -X POST "$FRONT/api/front/login" -H "Content-Type: application/json" -d "{\"account\":\"$P\",\"password\":\"123456\"}")
  UIDS[$i]=$(echo "$RES" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
  TOKENS[$i]=$(echo "$RES" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
  echo "login $P uid=${UIDS[$i]}"
done

# 4. B01..B10 参团
for i in 01 02 03 04 05 06 07 08 09 10; do
  PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "Authori-zation: ${TOKENS[$i]}" -H "Content-Type: application/json" \
    -d '{"preOrderType":"buyNow","orderDetails":[{"productId":172,"attrValueId":3411,"productNum":1,"shoppingCartId":0}]}')
  PRENO=$(echo "$PRE" | grep -o '"preOrderNo":"[^"]*' | sed 's/"preOrderNo":"//')
  if [ -z "$PRENO" ]; then echo "B$i preOrder FAIL: $(echo $PRE | head -c 120)"; continue; fi
  ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "Authori-zation: ${TOKENS[$i]}" -H "Content-Type: application/json" \
    -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":5,\"realName\":\"拼购自测\",\"phone\":\"166000200$i\",\"useIntegral\":false,\"mark\":\"$MARK\"}")
  ORDERNO=$(echo "$ORD" | grep -o '"orderNo":"[^"]*' | sed 's/"orderNo":"//')
  if [ -z "$ORDERNO" ]; then echo "B$i create FAIL: $(echo $ORD | head -c 120)"; continue; fi
  PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: ${TOKENS[$i]}" -H "Content-Type: application/json" \
    -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}")
  echo "B$i order=$ORDERNO pay: $(echo $PAY | head -c 100)"
done

echo "等待开奖/异步发奖 75s..."
sleep 75

echo "=== team ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT id,activity_id,team_no,join_num,status FROM eb_pingou_team WHERE activity_id=$AID" 2>/dev/null
echo "=== join: 期望每行 share_price=8.88（独立设置覆盖默认5%） ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT id,uid,is_win,refund_price,subsidy_price,share_price,share_status FROM eb_pingou_join WHERE activity_id=$AID" 2>/dev/null
echo "=== 推荐人 A(uid=${UIDS[00]}) 佣金：期望 +88.80（10笔×8.88） ==="
"$M" -uroot -p123456 -h127.0.0.1 --default-character-set=utf8mb4 qianxu -e "SELECT uid,title,price,mark FROM eb_user_brokerage_record WHERE mark='$MARK' ORDER BY id" 2>/dev/null
echo "=== front detail 返回的有效阶梯（应含合并结果） ==="
curl -s "$FRONT/api/front/pingou/detail/$AID" | head -c 600
echo
echo "ACTIVITY_ID=$AID"; echo "SNAP=$SNAP"
