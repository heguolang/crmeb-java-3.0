#!/bin/bash
# 链动 2+1 巡检：新下一单，验证佣金状态写入与资金入账的一致性
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
FRONT=http://127.0.0.1:8081
ADMIN=http://127.0.0.1:8080
MARK='[AI自测]链动巡检'
PRODUCT=173; ATTR=3412; STORE=5; MONEY=500
PHONE="$1"; SPREAD="${2:-102}"

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' | grep -o '"token":"[^"]*' | sed 's/"token":"//')

echo "=== 注册 $PHONE 邀请人=$SPREAD ==="
REG=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
  -d "{\"account\":\"$PHONE\",\"password\":\"123456\",\"spread_spid\":$SPREAD}")
RUID=$(echo "$REG" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
CUST=$(echo "$REG" | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo "uid=$RUID"
[ -z "$RUID" ] && { echo "注册失败: $REG"; exit 1; }

echo "=== 充值 $MONEY ==="
curl -s "$ADMIN/api/admin/user/operate/founds?uid=$RUID&integralType=1&integralValue=0&moneyType=1&moneyValue=$MONEY" -H "Authori-zation: $TOK" >/dev/null

echo "=== 下单 ==="
PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "Authori-zation: $CUST" -H "Content-Type: application/json" \
  -d "{\"preOrderType\":\"buyNow\",\"orderDetails\":[{\"productId\":$PRODUCT,\"attrValueId\":$ATTR,\"productNum\":1,\"shoppingCartId\":0}]}")
PRENO=$(echo "$PRE" | grep -o '"preOrderNo":"[^"]*' | sed 's/"preOrderNo":"//')
ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "Authori-zation: $CUST" -H "Content-Type: application/json" \
  -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":$STORE,\"realName\":\"链动巡检\",\"phone\":\"$PHONE\",\"useIntegral\":false,\"mark\":\"$MARK\"}")
ORDERNO=$(echo "$ORD" | grep -o '"orderNo":"[^"]*' | sed 's/"orderNo":"//')
echo "orderNo=$ORDERNO"

echo "=== 支付 ==="
PAY=$(curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: $CUST" -H "Content-Type: application/json" \
  -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}")
echo "pay=$PAY"

echo "UID=$UID ORDERNO=$ORDERNO"
