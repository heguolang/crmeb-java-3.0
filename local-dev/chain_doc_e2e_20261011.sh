#!/bin/bash
# 链动结算链路 e2e（对齐文档口径后）
# 场景：新用户挂在老板 104 名下（104 的上级 102 也是老板）→ 下单支付
# 预期：直推奖¥100→104；见点奖=实付×seeRatio(12%)→104；帮扶冻结30%；
#       平级奖=见点×peerRatio(10%)→102（独立发放）；单笔封顶 50%
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
ADMIN=http://127.0.0.1:8080; FRONT=http://127.0.0.1:8081
SRC='X-Source: df07addc462f7f8f'
PRODUCT=173; ATTR=3412; STORE=5; MONEY=500
INVITE=104

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")
H="Authori-zation: $TOK"

echo "=== 0. 临时：售后保护期=0（支付即结算）+ 激活门槛=100（自测商品只卖100）==="
curl -s -X PUT "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" -H "Content-Type: application/json" \
  -d '{"chain_after_sale_days":"0","chain_active_threshold":"100"}' >/dev/null
curl -s "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
print('  after_sale_days =',d['chain_after_sale_days'],'| active_threshold =',d['chain_active_threshold'])
print('  单笔封顶% =',d['chain_max_brokerage_ratio'],'| 帮扶冻结% =',d['chain_help_freeze_ratio'])
print('  规则 =',[(r['type'],str(r['amount']),str(r['ratio'])) for r in d['rules']])
"
curl -s "$ADMIN/api/admin/chain/product/list?page=1&limit=3" -H "$H" -H "$SRC" | python -c "
import sys,json
r=json.load(sys.stdin)['data']['list'][0]
print('  产品%s pid=%s 直推=%s 见点比例=%s%% 平级=%s%%'%(r['id'],r['productId'],r['directAmount'],r.get('seeRatio'),r['peerRatio']))
"

PHONE="188${RANDOM:0:5}${RANDOM:0:3}"
echo "=== 1. 注册新用户挂老板 $INVITE 名下：$PHONE ==="
REG=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
  -d "{\"account\":\"$PHONE\",\"password\":\"123456\",\"spread_spid\":$INVITE}")
RUID=$(echo "$REG" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
CUST=$(echo "$REG" | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
echo "  uid=$RUID"
[ -z "$RUID" ] && { echo "注册失败: $REG"; exit 1; }

echo "=== 2. 充值 $MONEY 并下单支付 ==="
curl -s "$ADMIN/api/admin/user/operate/founds?uid=$RUID&integralType=1&integralValue=0&moneyType=1&moneyValue=$MONEY" -H "$H" -H "$SRC" >/dev/null
PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "$CUST" -H "Content-Type: application/json" \
  -d "{\"preOrderType\":\"buyNow\",\"orderDetails\":[{\"productId\":$PRODUCT,\"attrValueId\":$ATTR,\"productNum\":1,\"shoppingCartId\":0}]}")
PRENO=$(echo "$PRE" | grep -o '"preOrderNo":"[^"]*' | sed 's/"preOrderNo":"//')
ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "$CUST" -H "Content-Type: application/json" \
  -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":$STORE,\"realName\":\"链动口径自测\",\"phone\":\"$PHONE\",\"useIntegral\":false,\"mark\":\"[AI自测]链动文档口径\"}")
ORDERNO=$(echo "$ORD" | grep -o '"orderNo":"[^"]*' | sed 's/"orderNo":"//')
echo "  orderNo=$ORDERNO"
curl -s -X POST "$FRONT/api/front/pay/payment" -H "$CUST" -H "Content-Type: application/json" \
  -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}" >/dev/null
echo "  已发起余额支付，等异步结算 40s ..."
sleep 40

echo "=== 3. 订单链动流水（产生了哪些奖）==="
cd /d/qianxu-java-3.0
"D:/env/mysql-8.0.29-winx64/bin/mysql.exe" -uroot -p123456 -D qianxu --default-character-set=utf8mb4 -e "
SELECT order_id, user_id, source_uid, reward_type, amount, status, mark
  FROM eb_chain_commission WHERE order_id='$ORDERNO' ORDER BY id;
SELECT '--- 本单已发分润合计 vs 50% 封顶 ---' AS i;
SELECT c.order_id, SUM(c.amount) AS chain_total, o.pay_price, ROUND(o.pay_price*50/100,2) AS cap_50pct
  FROM eb_chain_commission c
  JOIN eb_store_order o ON o.order_id = c.order_id COLLATE utf8mb4_general_ci
 WHERE c.order_id='$ORDERNO' AND c.status<>5 GROUP BY c.order_id, o.pay_price;
SELECT '--- 买家身份/关系 ---' AS i;
SELECT uid, chain_identity, chain_parent_id, chain_invite_uid FROM eb_user WHERE uid=$RUID;
SELECT '--- 受益人余额（102/104）---' AS i;
SELECT uid, brokerage_price FROM eb_user WHERE uid IN (102,104,$RUID);
SELECT '--- 关系变更日志 ---' AS i;
SELECT id,user_id,from_parent_id,to_parent_id,reason FROM eb_chain_slide_log ORDER BY id DESC LIMIT 4;
SELECT '--- 关系表（买家/104）---' AS i;
SELECT user_id,parent_id,invite_uid,path_snapshot,level_depth FROM eb_chain_relation WHERE user_id IN ($RUID,104);
" 2>&1 | grep -v "Using a password"

echo "=== 4. 恢复：售后保护期=7 + 激活门槛=399 ==="
curl -s -X PUT "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" -H "Content-Type: application/json" \
  -d '{"chain_after_sale_days":"7","chain_active_threshold":"399"}' >/dev/null
curl -s "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
print('  after_sale_days =',d['chain_after_sale_days'],'| active_threshold =',d['chain_active_threshold'])
"
echo "NEWUID=$RUID ORDERNO=$ORDERNO"
