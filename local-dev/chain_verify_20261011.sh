#!/bin/bash
# 链动后台+会员端接口回归（本轮改造后）
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"; export MSYS_NO_PATHCONV=1
ADMIN=http://127.0.0.1:8080
FRONT=http://127.0.0.1:8081
SRC='X-Source: df07addc462f7f8f'

TOK=$(curl -s -X POST "$ADMIN/api/admin/login" -H "Content-Type: application/json" \
  -d '{"account":"admin","pwd":"123456"}' | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])")
echo "admin token: ${TOK:0:16}..."
H="Authori-zation: $TOK"

say(){ echo; echo "===== $1 ====="; }

say "1. 链动配置 GET /admin/chain/config"
curl -s "$ADMIN/api/admin/chain/config" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
keys=['chain_func_status','chain_gift_product_ids','chain_active_threshold','chain_after_sale_days',
'chain_effective_timing','chain_credit_timing','chain_freeze_days','chain_skip_normal_brokerage',
'chain_level_agent_name','chain_level_boss_name','chain_direct_capacity','chain_leave_team_enabled',
'chain_identity_downgrade','chain_max_brokerage_ratio','chain_help_freeze_ratio','chain_help_unlock_n',
'chain_help_unlock_m','chain_help_release_mode','chain_notify_status','chain_dividend_status','chain_dividend_ratio','chain_dividend_min_team']
for k in keys: print(' ',k,'=',d.get(k))
print('  rules =',[(r['type'],r['genLevel'],str(r['amount']),str(r['ratio']),r['status']) for r in d.get('rules',[])])
"

say "2. 看板 GET /admin/chain/dashboard"
curl -s "$ADMIN/api/admin/chain/dashboard" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
for k in ['agentCount','bossCount','todayNew','commissionTotal','todayCommission','clawbackCount','helpFundTotal','helpLockedBossCount']:
    print(' ',k,'=',d.get(k))
print('  teamDepthDist =',d.get('teamDepthDist'))
print('  last7Days len =',len(d.get('last7Days') or []))
print('  riskToday present?', 'riskToday' in d)
print('  helpDoing present?', 'helpDoing' in d)
"

say "3. 链动产品 GET /admin/chain/product/list"
curl -s "$ADMIN/api/admin/chain/product/list?page=1&limit=10" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
for r in d['list']:
    print(' id=%s pid=%s direct=%s seeRatio=%s seeAmount=%s peer=%s status=%s name=%s'%(
      r['id'],r['productId'],r['directAmount'],r.get('seeRatio'),r.get('seeAmount'),r['peerRatio'],r['status'],r.get('storeName')))
print(' total=',d['total'])
"

say "4. 链动用户 GET /admin/chain/users"
curl -s "$ADMIN/api/admin/chain/users?page=1&limit=5" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
for r in d['list'][:5]:
    print(' uid=%s nick=%s identity=%s(%s) parent=%s invite=%s direct=%s'%(r['uid'],r.get('nickname'),r.get('identity'),r.get('identityName'),r.get('chainParentId'),r.get('chainInviteUid'),r.get('effectiveDirectCount')))
print(' total=',d['total'])
"

say "5. 链动佣金 GET /admin/chain/commission"
for t in "" direct see peer help; do
  n=$(curl -s "$ADMIN/api/admin/chain/commission?page=1&limit=3&type=$t" -H "$H" -H "$SRC" | python -c "import sys,json;print(json.load(sys.stdin)['data']['total'])")
  echo "  type='${t:-全部}' -> total=$n"
done

say "6. 关系变更 GET /admin/chain/slide-logs + /audit-logs"
curl -s "$ADMIN/api/admin/chain/slide-logs?page=1&limit=5" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']; print('  slide total=',d['total'])
for r in d['list'][:3]: print('   ',r['id'],r['userId'],r.get('fromParentId'),'->',r.get('toParentId'),str(r.get('reason'))[:60])
"
curl -s "$ADMIN/api/admin/chain/audit-logs?page=1&limit=5" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']; print('  audit total=',d['total'])
for r in d['list'][:3]: print('   ',r['id'],r.get('operator'),r['userId'],r.get('beforeVal'),'->',r.get('afterVal'),r.get('reason'))
"

say "7. 团队分红 GET /admin/chain/dividend"
curl -s "$ADMIN/api/admin/chain/dividend?page=1&limit=5" -H "$H" -H "$SRC" | python -c "
import sys,json
d=json.load(sys.stdin)['data']; print('  dividend total=',d['total'])
"

# ---------- 会员端 ----------
PHONE="199${RANDOM:0:5}${RANDOM:0:3}"
R=$(curl -s -X POST "$FRONT/api/front/register" -H "Content-Type: application/json" \
  -d "{\"account\":\"$PHONE\",\"password\":\"123456\",\"spread_spid\":102}")
CU=$(echo "$R" | python -c "import sys,json;print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
say "8. 会员端 GET /api/front/chain/my-identity (新用户 $PHONE)"
curl -s "$FRONT/api/front/chain/my-identity" -H "Authori-zation: $CU" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
for k in ['identity','identityName','effectiveDirectCount','bossNeedCount','bossRemainCount','helpUnlocked','helpUnlockNeedOrders','helpUnlockNeedTeamOrders','helpNeedSeatCount','helpUnlockedSeatCount','selfTeamOrders','teamSize','teamBossCount','directCount','todayAmount','totalAmount','freezeAmount','withdrawable','helpFundAmount']:
    print(' ',k,'=',d.get(k))
print('  keptSeats =',d.get('keptSeats'))
"

say "9. 会员端 GET /api/front/chain/rules"
curl -s "$FRONT/api/front/chain/rules" -H "Authori-zation: $CU" | python -c "
import sys,json
d=json.load(sys.stdin)['data']
for k in ['enabled','funcStatus','agentName','bossName','directCapacity','activeThreshold','afterSaleDays','maxBrokerageRatio','helpFreezeRatio','helpUnlockN','helpUnlockM','leaveTeamEnabled','directAmount','seeRatio','peerRatio','giftProductIds']:
    print(' ',k,'=',d.get(k))
print('  content len =',len(d.get('content') or ''))
print('  content head =',(d.get('content') or '')[:120].replace(chr(10),' | '))
"

say "10. 会员端 GET /api/front/chain/commission-list / commission-summary / my-team"
curl -s "$FRONT/api/front/chain/commission-list?page=1&limit=3" -H "Authori-zation: $CU" | python -c "
import sys,json
d=json.load(sys.stdin)['data']; print('  commission-list total=',d['total'])
"
curl -s "$FRONT/api/front/chain/commission-summary" -H "Authori-zation: $CU" | python -c "
import sys,json
print('  summary =',json.load(sys.stdin)['data'])
"
curl -s "$FRONT/api/front/chain/my-team?depth=3" -H "Authori-zation: $CU" | python -c "
import sys,json
d=json.load(sys.stdin)['data']; print('  my-team root uid=',d.get('uid'),'children=',len(d.get('children') or []))
"
echo; echo "DONE."
