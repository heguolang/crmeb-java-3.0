#!/bin/bash
# 链动结算链路 e2e（第二轮：让三类奖励都落在封顶额度内）
# 关键：商品级配置优先于全局规则 → 必须临时改「商品173」而不是改规则表
# 临时口径：门槛100 / 售后0 / 封顶50%(订单100→额度50)
#           商品173: directAmount=20 / seeRatio=12 / peerRatio=10
# 拓扑：新用户 invite=104(老板,上级=102老板)
# 预期：直推 20→104；见点 12(冻结3.6+实发8.4)→104；平级 1.2(冻结0.36+实发0.84)→102
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
ADMIN=http://127.0.0.1:8080; FRONT=http://127.0.0.1:8081
SRC='X-Source: df07addc462f7f8f'
PRODUCT=173; ATTR=3412; STORE=5; MONEY=500; INVITE=104
MYSQL="D:/env/mysql-8.0.29-winx64/bin/mysql.exe"

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")
H="Authori-zation: $TOK"

echo "=== 0. 备份现状 + 临时口径 ==="
curl -s "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" > /tmp/chain_cfg_before.json
curl -s "$ADMIN/api/admin/chain/product/list?page=1&limit=50" -H "$H" -H "$SRC" > /tmp/chain_prod_before.json

curl -s -X PUT "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" -H "Content-Type: application/json" \
  -d '{"chain_after_sale_days":"0","chain_active_threshold":"100"}' >/dev/null
curl -s -X POST "$ADMIN/api/admin/chain/product/save" -H "$H" -H "$SRC" -H "Content-Type: application/json" \
  -d "{\"id\":1,\"productId\":$PRODUCT,\"directAmount\":20,\"seeRatio\":12,\"seeAmount\":0,\"peerRatio\":10,\"status\":1,\"remark\":\"文档口径自测\"}" >/dev/null

curl -s "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
print('  门槛=%s 售后=%s 封顶=%s%% 帮扶冻结=%s%%'%(d['chain_active_threshold'],d['chain_after_sale_days'],d['chain_max_brokerage_ratio'],d['chain_help_freeze_ratio']))
"
$MYSQL -uroot -p123456 -D qianxu --default-character-set=utf8mb4 -e "
SELECT CONCAT('  商品173: 直推=',direct_amount,' 见点比例=',see_ratio,'% 平级比例=',peer_ratio,'%') AS c FROM eb_chain_product WHERE product_id=$PRODUCT;" 2>&1 | grep -v "Using a password"

PHONE="188${RANDOM:0:5}${RANDOM:0:3}"
echo "=== 1. 新用户挂老板 $INVITE：$PHONE ==="
REG=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
  -d "{\"account\":\"$PHONE\",\"password\":\"123456\",\"spread_spid\":$INVITE}")
RUID=$(echo "$REG" | grep -o '"uid":[0-9]*' | head -1 | cut -d: -f2)
CUST=$(echo "$REG" | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
CUSTH="Authori-zation: $CUST"
echo "  uid=$RUID token=${CUST:0:8}..."
[ -z "$RUID" ] && { echo "注册失败: $REG"; exit 1; }

echo "=== 2. 充值+下单+支付 ==="
curl -s "$ADMIN/api/admin/user/operate/founds?uid=$RUID&integralType=1&integralValue=0&moneyType=1&moneyValue=$MONEY" -H "$H" -H "$SRC" >/dev/null
PRE=$(curl -s -X POST "$FRONT/api/front/order/pre/order" -H "$CUSTH" -H "Content-Type: application/json" \
  -d "{\"preOrderType\":\"buyNow\",\"orderDetails\":[{\"productId\":$PRODUCT,\"attrValueId\":$ATTR,\"productNum\":1,\"shoppingCartId\":0}]}")
PRENO=$(echo "$PRE" | python -c "import sys,json;print(json.load(sys.stdin)['data']['preOrderNo'])")
ORD=$(curl -s -X POST "$FRONT/api/front/order/create" -H "$CUSTH" -H "Content-Type: application/json" \
  -d "{\"preOrderNo\":\"$PRENO\",\"shippingType\":2,\"storeId\":$STORE,\"realName\":\"AI-test\",\"phone\":\"$PHONE\",\"useIntegral\":false,\"mark\":\"AI-chain-doc-test\"}")
ORDERNO=$(echo "$ORD" | python -c "import sys,json;print(json.load(sys.stdin)['data']['orderNo'])" 2>/dev/null)
echo "  orderNo=$ORDERNO"
[ -z "$ORDERNO" ] && { echo "下单失败: $ORD"; exit 1; }
curl -s -X POST "$FRONT/api/front/pay/payment" -H "Authori-zation: $CUST" -H "Content-Type: application/json" \
  -d "{\"orderNo\":\"$ORDERNO\",\"payType\":\"yue\",\"payChannel\":\"weixinh5\",\"from\":\"weixinh5\",\"scene\":0}" >/dev/null
echo "  已支付，等异步结算 40s ..."; sleep 40

echo "=== 3. 结果 ==="
cd /d/qianxu-java-3.0
$MYSQL -uroot -p123456 -D qianxu --default-character-set=utf8mb4 -e "
SELECT '--- 链动流水 eb_chain_commission ---' AS i;
SELECT user_id,source_uid,reward_type,amount,status,brokerage_record_id FROM eb_chain_commission WHERE order_id='$ORDERNO' ORDER BY id;
SELECT '--- 本单合计 vs 封顶 ---' AS i;
SELECT SUM(amount) AS used, o.pay_price, ROUND(o.pay_price*50/100,2) AS cap
  FROM eb_chain_commission c JOIN eb_store_order o ON o.order_id = c.order_id COLLATE utf8mb4_general_ci
 WHERE c.order_id='$ORDERNO' GROUP BY o.pay_price;
SELECT '--- 佣金钱包流水 eb_user_brokerage_record ---' AS i;
SELECT uid,link_id,brokerage_level,title,break_money,status FROM eb_user_brokerage_record WHERE link_id='$ORDERNO' ORDER BY id;
SELECT '--- 帮扶基金冻结 eb_chain_help_record ---' AS i;
SELECT * FROM eb_chain_help_record WHERE order_id='$ORDERNO';
SELECT '--- 买家身份/归属 ---' AS i;
SELECT uid,chain_identity,chain_parent_id,chain_invite_uid,spread_uid FROM eb_user WHERE uid=$RUID;
SELECT '--- 受益人佣金余额 102/104 ---' AS i;
SELECT uid,chain_identity,brokerage_price,chain_help_unlocked FROM eb_user WHERE uid IN (102,104);
" 2>&1 | grep -v "Using a password"

echo "=== 4. 还原口径（商品直推=100 / 售后=7 / 门槛=399）==="
curl -s -X POST "$ADMIN/api/admin/chain/product/save" -H "$H" -H "$SRC" -H "Content-Type: application/json" \
  -d "{\"id\":1,\"productId\":$PRODUCT,\"directAmount\":100,\"seeRatio\":12,\"seeAmount\":0,\"peerRatio\":10,\"status\":1,\"remark\":\"文档口径自测\"}" >/dev/null
curl -s -X PUT "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" -H "Content-Type: application/json" \
  -d '{"chain_after_sale_days":"7","chain_active_threshold":"399"}' >/dev/null
curl -s "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
print('  门槛=%s 售后=%s'%(d['chain_active_threshold'],d['chain_after_sale_days']))
"
$MYSQL -uroot -p123456 -D qianxu --default-character-set=utf8mb4 -e "
SELECT CONCAT('  商品173: 直推=',direct_amount,' 见点比例=',see_ratio,'% 平级比例=',peer_ratio,'%') AS c FROM eb_chain_product WHERE product_id=$PRODUCT;" 2>&1 | grep -v "Using a password"
echo "NEWUID=$RUID ORDERNO=$ORDERNO"
