package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.config.QianxuConfig;
import com.qxkj.common.constants.BrokerageRecordConstants;
import com.qxkj.common.constants.ChainConstants;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.constants.UploadConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.chain.*;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.order.StoreOrderInfo;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.ChainRelationUpdateRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.ChainIdentityResponse;
import com.qxkj.common.response.ChainTeamNodeResponse;
import com.qxkj.common.response.UserSpreadBannerResponse;
import com.qxkj.common.utils.ExportUtil;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.common.vo.ChainCommissionExcelVo;
import com.qxkj.service.dao.*;
import com.qxkj.common.model.product.StoreProduct;
import com.qxkj.service.service.ChainService;
import com.qxkj.service.service.StoreOrderInfoService;
import com.qxkj.service.service.StoreOrderService;
import com.qxkj.service.service.StoreProductService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.SystemGroupDataService;
import com.qxkj.service.service.UserBrokerageRecordService;
import com.qxkj.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChainServiceImpl implements ChainService {

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private StoreOrderInfoService storeOrderInfoService;
    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;
    @Autowired
    private ChainIdentityDao chainIdentityDao;
    @Autowired
    private ChainRelationDao chainRelationDao;
    @Autowired
    private ChainRewardRuleDao chainRewardRuleDao;
    @Autowired
    private ChainCommissionDao chainCommissionDao;
    @Autowired
    private ChainSlideLogDao chainSlideLogDao;
    @Autowired
    private ChainAuditLogDao chainAuditLogDao;
    @Autowired
    private SystemGroupDataService systemGroupDataService;
    @Autowired
    private StoreOrderService storeOrderService;
    @Autowired
    private QianxuConfig qianxuConfig;
    @Autowired
    private ChainPhase2Helper chainPhase2Helper;
    @Autowired
    private ChainProductDao chainProductDao;
    @Autowired
    private StoreProductService storeProductService;

    @Override
    public boolean isEnabled() {
        return "1".equals(cfg(ChainConstants.CONFIG_FUNC_STATUS, "0"));
    }

    @Override
    public boolean isChainGiftOrder(StoreOrder storeOrder) {
        if (!isEnabled() || ObjectUtil.isNull(storeOrder)) {
            return false;
        }
        Set<Integer> giftIds = giftProductIds();
        if (giftIds.isEmpty()) {
            return false;
        }
        List<StoreOrderInfo> infos = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
        if (CollUtil.isEmpty(infos)) {
            return false;
        }
        for (StoreOrderInfo info : infos) {
            if (giftIds.contains(info.getProductId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldSkipNormalBrokerage(StoreOrder storeOrder) {
        if (!isChainGiftOrder(storeOrder)) {
            return false;
        }
        return "1".equals(cfg(ChainConstants.CONFIG_SKIP_NORMAL_BROKERAGE, "1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindParent(Integer userId, Integer inviteUid) {
        if (!isEnabled() || ObjectUtil.isNull(userId) || ObjectUtil.isNull(inviteUid) || inviteUid <= 0) {
            return false;
        }
        if (userId.equals(inviteUid)) {
            throw new QianxuException("不能绑定自己为上级");
        }
        User user = userService.getById(userId);
        User invite = userService.getById(inviteUid);
        if (ObjectUtil.isNull(user) || ObjectUtil.isNull(invite)) {
            throw new QianxuException("用户不存在");
        }
        if (ObjectUtil.defaultIfNull(user.getChainParentId(), 0) > 0) {
            return true;
        }
        // 防成环：邀请人安置链上不能已有自己
        if (isAncestor(inviteUid, userId)) {
            throw new QianxuException("不能形成循环关系");
        }

        // 安置位（走人留人）：推荐人已是老板 → 挂推荐人；否则挂推荐人的上级（直推"留给"上级）
        Integer placeParent = resolvePlacementParent(inviteUid, invite);
        String path = buildPath(placeParent, userId);

        user.setChainInviteUid(inviteUid);
        user.setChainParentId(placeParent);
        user.setUpdateTime(DateUtil.date());
        userService.updateById(user);

        ChainRelation relation = chainRelationDao.selectOne(Wrappers.<ChainRelation>lambdaQuery()
                .eq(ChainRelation::getUserId, userId).last("limit 1"));
        if (ObjectUtil.isNull(relation)) {
            relation = new ChainRelation();
            relation.setUserId(userId);
            relation.setCreateTime(DateUtil.date());
        }
        relation.setParentId(placeParent);
        relation.setInviteUid(inviteUid);
        relation.setLevelDepth(pathDepth(path));
        relation.setPathSnapshot(path);
        relation.setUpdateTime(DateUtil.date());
        if (ObjectUtil.isNull(relation.getId())) {
            chainRelationDao.insert(relation);
        } else {
            chainRelationDao.updateById(relation);
        }

        // 关系变更留痕（原来挂推荐人、按走人留人改挂推荐人上级）
        if (!placeParent.equals(inviteUid)) {
            ChainSlideLog slideLog = new ChainSlideLog();
            slideLog.setUserId(userId);
            slideLog.setFromParentId(inviteUid);
            slideLog.setToParentId(placeParent);
            slideLog.setReason("走人留人：推荐人未升老板，直推留给其上级");
            slideLog.setCreatedAt(DateUtil.date());
            chainSlideLogDao.insert(slideLog);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserBrokerageRecord> onOrderPaid(StoreOrder storeOrder) {
        List<UserBrokerageRecord> records = new ArrayList<>();
        if (!isEnabled() || !isChainGiftOrder(storeOrder)) {
            return records;
        }
        // 配了售后保护期：支付时只标记，等保护期满由定时任务结算
        if (afterSaleDays() > 0) {
            markChainGiftOrder(storeOrder);
            return records;
        }
        return settleChainOrder(storeOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserBrokerageRecord> settleChainOrder(StoreOrder storeOrder) {
        List<UserBrokerageRecord> records = new ArrayList<>();
        if (!isEnabled() || ObjectUtil.isNull(storeOrder)) {
            return records;
        }
        Integer uid = storeOrder.getUid();
        User buyer = userService.getById(uid);
        if (ObjectUtil.isNull(buyer)) {
            return records;
        }
        // 有效订单门槛：实付金额需 ≥ 激活门槛价
        BigDecimal threshold = activeThreshold();
        if (threshold.compareTo(BigDecimal.ZERO) > 0 && ObjectUtil
                .defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO).compareTo(threshold) < 0) {
            markChainGiftOrder(storeOrder);
            log.info("chain order below threshold orderId={} pay={} threshold={}",
                    storeOrder.getOrderId(), storeOrder.getPayPrice(), threshold);
            return records;
        }

        // 若尚未绑定且有推广关系，尝试用 spread 做邀请人
        if (ObjectUtil.defaultIfNull(buyer.getChainParentId(), 0) <= 0
                && ObjectUtil.defaultIfNull(buyer.getSpreadUid(), 0) > 0) {
            try {
                bindParent(uid, buyer.getSpreadUid());
                buyer = userService.getById(uid);
            } catch (Exception e) {
                log.warn("chain auto bind on pay fail uid={}", uid, e);
            }
        }

        // 标记身份订单
        markChainGiftOrder(storeOrder);

        // 支付成功即成为代理（身份）
        upgradeToAgent(buyer, "购买代理礼包支付成功");
        buyer = userService.getById(uid);

        boolean effectiveOnPay = SysConfigConstants.CREDIT_TIMING_ON_PAY.equals(
                cfg(ChainConstants.CONFIG_EFFECTIVE_TIMING, SysConfigConstants.CREDIT_TIMING_ON_PAY));
        if (effectiveOnPay) {
            markEffectiveAndMaybeUpgradeBoss(buyer);
        }

        // 分佣：支付时生成记录；到账时机另控
        records.addAll(assignChainRewards(storeOrder, buyer));
        if (CollUtil.isNotEmpty(records)) {
            Set<Integer> notifyUids = records.stream().map(UserBrokerageRecord::getUid).collect(Collectors.toSet());
            for (Integer nUid : notifyUids) {
                chainPhase2Helper.pushNotify(nUid, ChainConstants.NOTIFY_COMMISSION, "链动佣金入账",
                        "订单" + storeOrder.getOrderId() + "产生链动奖励，请到链动中心查看");
            }
        }
        // 新人礼包单可能推进其邀请人所属老板的帮扶解锁
        tryUnlockHelpRelated(buyer);
        return records;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processAfterSaleOrders() {
        if (!isEnabled()) {
            return 0;
        }
        int days = afterSaleDays();
        if (days <= 0) {
            return 0;
        }
        Date deadline = DateUtil.offsetDay(DateUtil.date(), -days);
        List<StoreOrder> orders = storeOrderService.list(Wrappers.<StoreOrder>lambdaQuery()
                .eq(StoreOrder::getPaid, true)
                .like(StoreOrder::getRemark, "CHAIN_GIFT")
                .notLike(StoreOrder::getRemark, "CHAIN_SETTLED")
                .le(StoreOrder::getPayTime, deadline)
                .orderByAsc(StoreOrder::getId));
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }
        int n = 0;
        for (StoreOrder o : orders) {
            try {
                settleChainOrder(o);
                markChainSettled(o);
                n++;
            } catch (Exception e) {
                log.warn("chain after-sale settle fail orderId={}", o.getOrderId(), e);
            }
        }
        return n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onOrderComplete(StoreOrder storeOrder) {
        if (!isEnabled() || !isChainGiftOrder(storeOrder)) {
            return;
        }
        User buyer = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(buyer)) {
            return;
        }
        boolean effectiveOnComplete = SysConfigConstants.CREDIT_TIMING_ON_COMPLETE.equals(
                cfg(ChainConstants.CONFIG_EFFECTIVE_TIMING, SysConfigConstants.CREDIT_TIMING_ON_PAY));
        if (effectiveOnComplete) {
            if (ObjectUtil.defaultIfNull(buyer.getChainIdentity(), 0) < ChainConstants.IDENTITY_AGENT) {
                upgradeToAgent(buyer, "购买代理礼包订单完成");
                buyer = userService.getById(buyer.getUid());
            }
            markEffectiveAndMaybeUpgradeBoss(buyer);
        }
        // 完成时补生成佣金（若支付时未生成——正常支付已生成；此处主要防漏）
        List<ChainCommission> exists = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getOrderId, storeOrder.getOrderId()).last("limit 1"));
        if (CollUtil.isEmpty(exists)) {
            List<UserBrokerageRecord> created = assignChainRewards(storeOrder, buyer);
            if (CollUtil.isNotEmpty(created)) {
                created.forEach(r -> r.setLinkId(storeOrder.getOrderId()));
                userBrokerageRecordService.saveBatch(created);
            }
        }
        tryUnlockHelpRelated(buyer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onOrderRefund(StoreOrder storeOrder) {
        if (!isEnabled() || ObjectUtil.isNull(storeOrder)) {
            return;
        }
        List<ChainCommission> list = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getOrderId, storeOrder.getOrderId()));
        for (ChainCommission c : list) {
            int st = ObjectUtil.defaultIfNull(c.getStatus(), 0);
            if (st == ChainConstants.COMMISSION_INVALID || st == ChainConstants.COMMISSION_CLAWBACK) {
                continue;
            }
            // 已结算：主流程会扣回余额；此处若余额不足则记待追回
            if (st == ChainConstants.COMMISSION_SETTLED) {
                User u = userService.getById(c.getUserId());
                BigDecimal bal = ObjectUtil.isNotNull(u)
                        ? ObjectUtil.defaultIfNull(u.getBrokeragePrice(), BigDecimal.ZERO) : BigDecimal.ZERO;
                if (bal.compareTo(ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO)) < 0) {
                    c.setStatus(ChainConstants.COMMISSION_CLAWBACK);
                    c.setMark(StrUtil.blankToDefault(c.getMark(), "") + " | 退款待追回");
                    c.setUpdateTime(DateUtil.date());
                    chainCommissionDao.updateById(c);
                    continue;
                }
            }
            c.setStatus(ChainConstants.COMMISSION_INVALID);
            c.setUpdateTime(DateUtil.date());
            chainCommissionDao.updateById(c);
        }
        if (!isChainGiftOrder(storeOrder)) {
            return;
        }
        if (!"1".equals(cfg(ChainConstants.CONFIG_IDENTITY_DOWNGRADE, "0"))) {
            return;
        }
        User buyer = userService.getById(storeOrder.getUid());
        if (ObjectUtil.isNull(buyer)) {
            return;
        }
        // 简单降级：若无其它有效礼包订单，降为无身份
        buyer.setChainIdentity(ChainConstants.IDENTITY_NONE);
        buyer.setUpdateTime(DateUtil.date());
        userService.updateById(buyer);
        ChainIdentity identity = new ChainIdentity();
        identity.setUserId(buyer.getUid());
        identity.setIdentityType(ChainConstants.IDENTITY_NONE);
        identity.setStatus(0);
        identity.setUpgradeTime(DateUtil.date());
        identity.setRemark("礼包退款降级");
        identity.setCreateTime(DateUtil.date());
        identity.setUpdateTime(DateUtil.date());
        chainIdentityDao.insert(identity);
    }

    @Override
    public ChainIdentityResponse myIdentity(Integer uid) {
        ChainIdentityResponse resp = new ChainIdentityResponse();
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            return resp;
        }
        int identity = ObjectUtil.defaultIfNull(user.getChainIdentity(), 0);
        resp.setIdentity(identity);
        resp.setIdentityName(identityName(identity));
        resp.setChainParentId(ObjectUtil.defaultIfNull(user.getChainParentId(), 0));
        resp.setChainInviteUid(ObjectUtil.defaultIfNull(user.getChainInviteUid(), 0));
        int need = intCfg(ChainConstants.CONFIG_DIRECT_CAPACITY, 2);
        int effective = countEffectiveDirects(uid);
        resp.setEffectiveDirectCount(effective);
        resp.setBossNeedCount(need);
        resp.setBossRemainCount(Math.max(0, need - effective));
        boolean helpUnlocked = identity >= ChainConstants.IDENTITY_BOSS
                && ObjectUtil.defaultIfNull(user.getChainHelpUnlocked(), 0) == 1;
        resp.setHelpUnlocked(helpUnlocked);
        int seatOrders = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10);
        int selfOrders = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20);
        resp.setHelpUnlockNeedOrders(seatOrders);
        resp.setHelpUnlockNeedTeamOrders(selfOrders);
        resp.setHelpNeedSeatCount(need);
        resp.setHelpUnlockedSeatCount(countHelpQualifiedSeats(uid, need));
        resp.setSelfTeamOrders(countTeamGiftOrders(uid));
        resp.setKeptSeats(buildKeptSeats(uid, need, seatOrders));

        // 团队概况：团队人数 / 已升老板数 / 直推人数
        Set<Integer> team = collectTeamUids(uid);
        resp.setTeamSize(team.size());
        resp.setTeamBossCount(team.isEmpty() ? 0 : Math.toIntExact(userService.count(Wrappers.<User>lambdaQuery()
                .in(User::getUid, team)
                .ge(User::getChainIdentity, ChainConstants.IDENTITY_BOSS))));
        resp.setDirectCount(ObjectUtil.defaultIfNull(userService.count(Wrappers.<User>lambdaQuery()
                .eq(User::getChainInviteUid, uid)), 0L).intValue());

        Map<String, Object> sum = commissionSummary(uid);
        resp.setTodayAmount((BigDecimal) sum.get("today"));
        resp.setTotalAmount((BigDecimal) sum.get("total"));
        resp.setFreezeAmount((BigDecimal) sum.get("freeze"));
        resp.setHelpFundAmount((BigDecimal) sum.get("helpFund"));
        resp.setWithdrawable((BigDecimal) sum.get("withdrawable"));
        return resp;
    }

    /** 留人座位明细：升老板前的前 N 名直推（走人留人里的「留人」） */
    private List<ChainIdentityResponse.KeptSeat> buildKeptSeats(Integer bossUid, int needSeats, int seatOrders) {
        List<User> seats = listKeptSeats(bossUid, needSeats);
        List<ChainIdentityResponse.KeptSeat> list = new ArrayList<>();
        for (User seat : seats) {
            ChainIdentityResponse.KeptSeat item = new ChainIdentityResponse.KeptSeat();
            item.setUid(seat.getUid());
            item.setNickname(seat.getNickname());
            item.setAvatar(seat.getAvatar());
            int sid = ObjectUtil.defaultIfNull(seat.getChainIdentity(), 0);
            item.setIdentity(sid);
            item.setIdentityName(identityName(sid));
            int orders = countTeamGiftOrders(seat.getUid());
            item.setTeamOrders(orders);
            boolean upgraded = sid >= ChainConstants.IDENTITY_BOSS;
            item.setUpgraded(upgraded);
            item.setQualified(upgraded && orders >= seatOrders);
            list.add(item);
        }
        return list;
    }

    @Override
    public ChainTeamNodeResponse myTeam(Integer uid, Integer depth) {
        int maxDepth = ObjectUtil.defaultIfNull(depth, 3);
        if (maxDepth < 1) {
            maxDepth = 1;
        }
        if (maxDepth > 5) {
            maxDepth = 5;
        }
        User root = userService.getById(uid);
        ChainTeamNodeResponse node = toNode(root);
        fillChildren(node, 1, maxDepth);
        return node;
    }

    @Override
    public Map<String, Object> commissionSummary(Integer uid) {
        Map<String, Object> map = new HashMap<>();
        Date todayStart = DateUtil.beginOfDay(DateUtil.date());
        List<ChainCommission> all = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getUserId, uid)
                .ne(ChainCommission::getStatus, ChainConstants.COMMISSION_INVALID));
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal today = BigDecimal.ZERO;
        BigDecimal helpFund = BigDecimal.ZERO;
        BigDecimal freeze = BigDecimal.ZERO;
        for (ChainCommission c : all) {
            BigDecimal amt = ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO);
            total = total.add(amt);
            if (c.getCreateTime() != null && !c.getCreateTime().before(todayStart)) {
                today = today.add(amt);
            }
            if (ObjectUtil.defaultIfNull(c.getStatus(), 0) == ChainConstants.COMMISSION_HELP_FUND) {
                // 帮扶基金：解锁前始终冻结，且不进入佣金钱包
                helpFund = helpFund.add(amt);
            }
        }
        // 冻结金额 = 帮扶基金（链动流水 status=帮扶基金，未解锁部分）
        //          + 佣金钱包流水仍处「冻结」状态的链动佣金
        // 说明：链动流水的 status 不随解冻回写，故钱包侧直接按 uid + 链动佣金层级查询，
        //      不依赖 chain_commission.brokerage_record_id（支付链路生成时钱包记录id尚未落库）。
        freeze = freeze.add(helpFund);
        List<UserBrokerageRecord> frozen = userBrokerageRecordService.list(Wrappers.<UserBrokerageRecord>lambdaQuery()
                .eq(UserBrokerageRecord::getUid, uid)
                .in(UserBrokerageRecord::getBrokerageLevel,
                        ChainConstants.BROKERAGE_LEVEL_DIRECT,
                        ChainConstants.BROKERAGE_LEVEL_SEE,
                        ChainConstants.BROKERAGE_LEVEL_PEER,
                        ChainConstants.BROKERAGE_LEVEL_HELP,
                        ChainConstants.BROKERAGE_LEVEL_DIVIDEND)
                .in(UserBrokerageRecord::getStatus,
                        BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE,
                        BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_FROZEN));
        for (UserBrokerageRecord r : frozen) {
            freeze = freeze.add(ObjectUtil.defaultIfNull(r.getPrice(), BigDecimal.ZERO));
        }
        User u = userService.getById(uid);
        BigDecimal withdrawable = ObjectUtil.isNotNull(u)
                ? ObjectUtil.defaultIfNull(u.getBrokeragePrice(), BigDecimal.ZERO) : BigDecimal.ZERO;
        map.put("today", today);
        map.put("total", total);
        map.put("freeze", freeze);
        map.put("helpFund", helpFund);
        map.put("withdrawable", withdrawable);
        return map;
    }

    @Override
    public CommonPage<ChainCommission> commissionPage(Integer uid, String type, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<ChainCommission> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChainCommission::getUserId, uid);
        if (StrUtil.isNotBlank(type)) {
            if ("help".equals(type)) {
                // 帮扶基金：见点/平级奖被冻结的那部分 + 后续解冻入账
                lqw.and(w -> w.eq(ChainCommission::getRewardType, ChainConstants.REWARD_SEE_HELP)
                        .or().eq(ChainCommission::getRewardType, ChainConstants.REWARD_PEER_HELP));
            } else if (ChainConstants.REWARD_SEE.equals(type) || ChainConstants.REWARD_PEER.equals(type)) {
                lqw.eq(ChainCommission::getRewardType, type);
            } else {
                lqw.eq(ChainCommission::getRewardType, type);
            }
        }
        lqw.orderByDesc(ChainCommission::getId);
        return CommonPage.restPage(new PageInfo<>(chainCommissionDao.selectList(lqw)));
    }

    @Override
    public Map<String, Object> promotePoster(Integer uid) {
        Map<String, Object> map = new HashMap<>();
        List<UserSpreadBannerResponse> banners = systemGroupDataService.getListByGid(
                Constants.GROUP_DATA_ID_SPREAD_BANNER_LIST, UserSpreadBannerResponse.class);
        String site = cfg(Constants.CONFIG_KEY_SITE_URL, "");
        if (StrUtil.isBlank(site)) {
            site = cfg(SysConfigConstants.CONFIG_KEY_SITE_URL, "");
        }
        String inviteUrl = StrUtil.removeSuffix(site, "/") + "/?spread=" + uid + "&" + ChainConstants.SHARE_QUERY_FLAG;
        map.put("banners", banners);
        map.put("uid", uid);
        map.put("inviteUrl", inviteUrl);
        map.put("chainFlag", true);
        return map;
    }

    @Override
    public String rulesContent() {
        String content = cfg(ChainConstants.CONFIG_RULES_CONTENT, "");
        if (StrUtil.isNotBlank(content)) {
            return content;
        }
        int capacity = intCfg(ChainConstants.CONFIG_DIRECT_CAPACITY, 2);
        int unlockN = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10);
        int unlockM = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20);
        BigDecimal freeze = helpFreezeRatio();
        return "【2+1链动规则】\n"
                + "1. 购买链动礼包（实付达门槛）即成为" + identityName(ChainConstants.IDENTITY_AGENT) + "，享受直推奖。\n"
                + "2. 直推满 " + capacity + " 名有效" + identityName(ChainConstants.IDENTITY_AGENT)
                + " 即晋升" + identityName(ChainConstants.IDENTITY_BOSS) + "，享受直推奖、见点奖、平级奖。\n"
                + "3. 走人留人：晋升" + identityName(ChainConstants.IDENTITY_BOSS) + "后本人脱离原上级团队独立发展；"
                + "升级前已有直推中，前 " + capacity + " 名继续留在原上级名下，第 " + (capacity + 1) + " 名起随本人走。\n"
                + "4. 见点奖与平级奖的 " + freeze.stripTrailingZeros().toPlainString()
                + "% 会先进入帮扶基金冻结；当留人的每位" + identityName(ChainConstants.IDENTITY_AGENT)
                + "均晋升" + identityName(ChainConstants.IDENTITY_BOSS) + "、且各自团队有效订单 ≥ " + unlockN
                + "、本人团队有效订单 ≥ " + unlockM + " 时全部解冻，之后不再冻结。\n"
                + "5. 单笔订单的链动分润总额不超过订单实付的 50%。\n"
                + "6. 具体比例与金额以后台实际配置为准。";
    }

    @Override
    public Map<String, Object> rulesMeta() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("funcStatus", cfg(ChainConstants.CONFIG_FUNC_STATUS, "0"));
        map.put("agentName", identityName(ChainConstants.IDENTITY_AGENT));
        map.put("bossName", identityName(ChainConstants.IDENTITY_BOSS));
        map.put("directCapacity", intCfg(ChainConstants.CONFIG_DIRECT_CAPACITY, 2));
        map.put("activeThreshold", activeThreshold());
        map.put("afterSaleDays", afterSaleDays());
        map.put("maxBrokerageRatio", maxBrokerageRatio());
        map.put("helpFreezeRatio", helpFreezeRatio());
        map.put("helpUnlockN", intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10));
        map.put("helpUnlockM", intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20));
        map.put("leaveTeamEnabled", "1".equals(cfg(ChainConstants.CONFIG_LEAVE_TEAM_ENABLED, "1")));
        map.put("directAmount", ruleAmount(findRule(ChainConstants.REWARD_DIRECT, 1), null));
        map.put("seeRatio", seeRatio());
        map.put("peerRatio", peerRatio());
        map.put("giftProductIds", cfg(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, ""));
        return map;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> map = new LinkedHashMap<>();
        // ① 基础设置
        map.put(ChainConstants.CONFIG_FUNC_STATUS, cfg(ChainConstants.CONFIG_FUNC_STATUS, "0"));
        map.put(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, cfg(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, ""));
        map.put(ChainConstants.CONFIG_ACTIVE_THRESHOLD, cfg(ChainConstants.CONFIG_ACTIVE_THRESHOLD, "0"));
        map.put(ChainConstants.CONFIG_AFTER_SALE_DAYS, cfg(ChainConstants.CONFIG_AFTER_SALE_DAYS, "0"));
        map.put(ChainConstants.CONFIG_EFFECTIVE_TIMING, cfg(ChainConstants.CONFIG_EFFECTIVE_TIMING, "1"));
        map.put(ChainConstants.CONFIG_CREDIT_TIMING, cfg(ChainConstants.CONFIG_CREDIT_TIMING, "1"));
        map.put(ChainConstants.CONFIG_FREEZE_DAYS, cfg(ChainConstants.CONFIG_FREEZE_DAYS, "1"));
        map.put(ChainConstants.CONFIG_SKIP_NORMAL_BROKERAGE, cfg(ChainConstants.CONFIG_SKIP_NORMAL_BROKERAGE, "1"));
        // ② 身份与升级
        map.put(ChainConstants.CONFIG_LEVEL_AGENT_NAME, cfg(ChainConstants.CONFIG_LEVEL_AGENT_NAME, "代理"));
        map.put(ChainConstants.CONFIG_LEVEL_BOSS_NAME, cfg(ChainConstants.CONFIG_LEVEL_BOSS_NAME, "老板"));
        map.put(ChainConstants.CONFIG_DIRECT_CAPACITY, cfg(ChainConstants.CONFIG_DIRECT_CAPACITY, "2"));
        map.put(ChainConstants.CONFIG_LEAVE_TEAM_ENABLED, cfg(ChainConstants.CONFIG_LEAVE_TEAM_ENABLED, "1"));
        map.put(ChainConstants.CONFIG_IDENTITY_DOWNGRADE, cfg(ChainConstants.CONFIG_IDENTITY_DOWNGRADE, "0"));
        // ③ 奖励设置
        map.put(ChainConstants.CONFIG_MAX_BROKERAGE_RATIO, cfg(ChainConstants.CONFIG_MAX_BROKERAGE_RATIO, "50"));
        // ④ 帮扶基金
        map.put(ChainConstants.CONFIG_HELP_FREEZE_RATIO, cfg(ChainConstants.CONFIG_HELP_FREEZE_RATIO, "30"));
        map.put(ChainConstants.CONFIG_HELP_UNLOCK_N, cfg(ChainConstants.CONFIG_HELP_UNLOCK_N, "10"));
        map.put(ChainConstants.CONFIG_HELP_UNLOCK_M, cfg(ChainConstants.CONFIG_HELP_UNLOCK_M, "20"));
        map.put(ChainConstants.CONFIG_HELP_RELEASE_MODE,
                cfg(ChainConstants.CONFIG_HELP_RELEASE_MODE, ChainConstants.HELP_RELEASE_ONCE));
        map.put(ChainConstants.CONFIG_RULES_CONTENT, cfg(ChainConstants.CONFIG_RULES_CONTENT, ""));
        chainPhase2Helper.putPhase2Config(map);
        map.put("rules", listRules());
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(Map<String, String> config) {
        if (CollUtil.isEmpty(config)) {
            return;
        }
        for (Map.Entry<String, String> e : config.entrySet()) {
            if (e.getKey() == null || e.getKey().startsWith("rules")) {
                continue;
            }
            systemConfigService.updateOrSaveValueByName(e.getKey(), e.getValue());
        }
    }

    @Override
    public List<ChainRewardRule> listRules() {
        return chainRewardRuleDao.selectList(Wrappers.<ChainRewardRule>lambdaQuery()
                .orderByAsc(ChainRewardRule::getType).orderByAsc(ChainRewardRule::getGenLevel));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRules(List<ChainRewardRule> rules) {
        if (CollUtil.isEmpty(rules)) {
            return;
        }
        for (ChainRewardRule rule : rules) {
            rule.setUpdateTime(DateUtil.date());
            if (ObjectUtil.isNull(rule.getId())) {
                rule.setCreateTime(DateUtil.date());
                chainRewardRuleDao.insert(rule);
            } else {
                chainRewardRuleDao.updateById(rule);
            }
        }
    }

    @Override
    public CommonPage<ChainProduct> chainProductPage(String keywords, Integer status, PageParamRequest pageParam) {
        PageHelper.startPage(pageParam.getPage(), pageParam.getLimit());
        LambdaQueryWrapper<ChainProduct> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(status)) {
            lqw.eq(ChainProduct::getStatus, status);
        }
        boolean kwIsId = false;
        if (StrUtil.isNotBlank(keywords)) {
            String kw = keywords.trim();
            if (kw.matches("\\d+")) {
                lqw.eq(ChainProduct::getProductId, Integer.parseInt(kw));
                kwIsId = true;
            }
        }
        lqw.orderByDesc(ChainProduct::getSort).orderByDesc(ChainProduct::getId);
        List<ChainProduct> list = chainProductDao.selectList(lqw);
        PageInfo<ChainProduct> pageInfo = new PageInfo<>(list);
        for (ChainProduct p : list) {
            fillProductInfo(p);
        }
        if (StrUtil.isNotBlank(keywords) && !kwIsId) {
            String kw = keywords.trim();
            list = list.stream()
                    .filter(p -> StrUtil.containsIgnoreCase(StrUtil.blankToDefault(p.getStoreName(), ""), kw))
                    .collect(Collectors.toList());
            pageInfo.setList(list);
            pageInfo.setTotal(list.size());
        }
        return CommonPage.restPage(pageInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChainProduct(ChainProduct product) {
        if (ObjectUtil.isNull(product) || ObjectUtil.isNull(product.getProductId()) || product.getProductId() <= 0) {
            throw new QianxuException("请选择商品");
        }
        StoreProduct sp = loadStoreProductBrief(product.getProductId());
        if (ObjectUtil.isNull(sp)) {
            throw new QianxuException("商品不存在");
        }
        if (ObjectUtil.isNull(product.getDirectAmount())) {
            product.setDirectAmount(new BigDecimal("100"));
        }
        if (ObjectUtil.isNull(product.getSeeRatio())) {
            product.setSeeRatio(new BigDecimal("10"));
        }
        if (ObjectUtil.isNull(product.getPeerRatio())) {
            product.setPeerRatio(new BigDecimal("10"));
        }
        if (ObjectUtil.isNull(product.getStatus())) {
            product.setStatus(1);
        }
        if (ObjectUtil.isNull(product.getSort())) {
            product.setSort(0);
        }
        product.setUpdateTime(DateUtil.date());
        ChainProduct exists = chainProductDao.selectOne(Wrappers.<ChainProduct>lambdaQuery()
                .eq(ChainProduct::getProductId, product.getProductId()).last("limit 1"));
        if (ObjectUtil.isNotNull(exists)) {
            product.setId(exists.getId());
            chainProductDao.updateById(product);
        } else if (ObjectUtil.isNotNull(product.getId())) {
            chainProductDao.updateById(product);
        } else {
            product.setCreateTime(DateUtil.date());
            chainProductDao.insert(product);
        }
        // 同步到全局礼包ID配置，兼容旧逻辑
        syncGiftProductIdsConfig();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChainProduct(Integer id) {
        if (ObjectUtil.isNull(id)) {
            return;
        }
        chainProductDao.deleteById(id);
        syncGiftProductIdsConfig();
    }

    @Override
    public CommonPage<Map<String, Object>> adminUserPage(String keywords, Integer identity, Integer inviterUid, PageParamRequest pageParam) {
        PageHelper.startPage(pageParam.getPage(), pageParam.getLimit());
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(identity)) {
            lqw.eq(User::getChainIdentity, identity);
        } else {
            lqw.and(w -> w.gt(User::getChainIdentity, 0).or().gt(User::getChainParentId, 0));
        }
        if (ObjectUtil.isNotNull(inviterUid) && inviterUid > 0) {
            lqw.eq(User::getSpreadUid, inviterUid);
        }
        if (StrUtil.isNotBlank(keywords)) {
            lqw.and(w -> w.like(User::getNickname, keywords).or().like(User::getPhone, keywords)
                    .or().eq(User::getUid, keywords));
        }
        lqw.orderByDesc(User::getUid);
        List<User> users = userService.list(lqw);
        PageInfo<User> pageInfo = new PageInfo<>(users);

        // 批量取上级 / 邀请人资料（昵称、手机、身份）
        Set<Integer> relatedUids = new HashSet<>();
        for (User u : users) {
            if (ObjectUtil.isNotNull(u.getChainParentId()) && u.getChainParentId() > 0) {
                relatedUids.add(u.getChainParentId());
            }
            if (ObjectUtil.isNotNull(u.getChainInviteUid()) && u.getChainInviteUid() > 0) {
                relatedUids.add(u.getChainInviteUid());
            }
        }
        Map<Integer, User> relatedMap = relatedUids.isEmpty() ? Collections.emptyMap()
                : userService.list(Wrappers.<User>lambdaQuery().in(User::getUid, relatedUids)).stream()
                        .collect(Collectors.toMap(User::getUid, u -> u));

        // 批量取升级时间（成为代理 / 成为老板，各取最近一次）
        Map<Integer, Date> agentTimeMap = new HashMap<>();
        Map<Integer, Date> bossTimeMap = new HashMap<>();
        Set<Integer> pageUids = new HashSet<>();
        for (User u : users) {
            pageUids.add(u.getUid());
        }
        if (!pageUids.isEmpty()) {
            List<ChainIdentity> identityLogs = chainIdentityDao.selectList(Wrappers.<ChainIdentity>lambdaQuery()
                    .in(ChainIdentity::getUserId, pageUids)
                    .in(ChainIdentity::getIdentityType, 1, 2)
                    .eq(ChainIdentity::getStatus, 1)
                    .orderByDesc(ChainIdentity::getUpgradeTime));
            for (ChainIdentity ci : identityLogs) {
                if (ci.getIdentityType() != null && ci.getIdentityType() == 1) {
                    agentTimeMap.putIfAbsent(ci.getUserId(), ci.getUpgradeTime());
                } else if (ci.getIdentityType() != null && ci.getIdentityType() == 2) {
                    bossTimeMap.putIfAbsent(ci.getUserId(), ci.getUpgradeTime());
                }
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> row = new HashMap<>();
            row.put("uid", u.getUid());
            row.put("nickname", u.getNickname());
            row.put("phone", u.getPhone());
            row.put("avatar", u.getAvatar());
            row.put("identity", u.getChainIdentity());
            row.put("identityName", identityName(ObjectUtil.defaultIfNull(u.getChainIdentity(), 0)));
            row.put("agentTime", agentTimeMap.get(u.getUid()));
            row.put("bossTime", bossTimeMap.get(u.getUid()));
            row.put("chainParentId", u.getChainParentId());
            row.put("chainInviteUid", u.getChainInviteUid());
            row.put("effectiveDirectCount", countEffectiveDirects(u.getUid()));
            User parent = ObjectUtil.isNotNull(u.getChainParentId()) && u.getChainParentId() > 0
                    ? relatedMap.get(u.getChainParentId()) : null;
            if (ObjectUtil.isNotNull(parent)) {
                row.put("parentNickname", parent.getNickname());
                row.put("parentPhone", parent.getPhone());
                row.put("parentIdentityName", identityName(ObjectUtil.defaultIfNull(parent.getChainIdentity(), 0)));
            }
            User inviter = ObjectUtil.isNotNull(u.getChainInviteUid()) && u.getChainInviteUid() > 0
                    ? relatedMap.get(u.getChainInviteUid()) : null;
            if (ObjectUtil.isNotNull(inviter)) {
                row.put("inviteNickname", inviter.getNickname());
                row.put("invitePhone", inviter.getPhone());
                row.put("inviteIdentityName", identityName(ObjectUtil.defaultIfNull(inviter.getChainIdentity(), 0)));
            }
            rows.add(row);
        }
        return CommonPage.restPage(CommonPage.copyPageInfo(pageInfo, rows));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateRelation(ChainRelationUpdateRequest request, String operator) {
        User user = userService.getById(request.getUserId());
        if (ObjectUtil.isNull(user)) {
            throw new QianxuException("用户不存在");
        }
        Integer oldParent = ObjectUtil.defaultIfNull(user.getChainParentId(), 0);
        Integer newParent = ObjectUtil.defaultIfNull(request.getParentId(), 0);
        if (newParent > 0) {
            if (newParent.equals(user.getUid()) || isAncestor(newParent, user.getUid())) {
                throw new QianxuException("不能形成循环关系");
            }
            if (ObjectUtil.isNull(userService.getById(newParent))) {
                throw new QianxuException("上级不存在");
            }
        }
        user.setChainParentId(newParent);
        user.setUpdateTime(DateUtil.date());
        userService.updateById(user);

        ChainRelation relation = chainRelationDao.selectOne(Wrappers.<ChainRelation>lambdaQuery()
                .eq(ChainRelation::getUserId, user.getUid()).last("limit 1"));
        if (ObjectUtil.isNull(relation)) {
            relation = new ChainRelation();
            relation.setUserId(user.getUid());
            relation.setInviteUid(ObjectUtil.defaultIfNull(user.getChainInviteUid(), 0));
            relation.setCreateTime(DateUtil.date());
        }
        relation.setParentId(newParent);
        relation.setPathSnapshot(buildPath(newParent, user.getUid()));
        relation.setLevelDepth(pathDepth(relation.getPathSnapshot()));
        relation.setUpdateTime(DateUtil.date());
        if (ObjectUtil.isNull(relation.getId())) {
            chainRelationDao.insert(relation);
        } else {
            chainRelationDao.updateById(relation);
        }

        ChainAuditLog audit = new ChainAuditLog();
        audit.setOperator(StrUtil.blankToDefault(operator, "admin"));
        audit.setUserId(user.getUid());
        audit.setBeforeVal(String.valueOf(oldParent));
        audit.setAfterVal(String.valueOf(newParent));
        audit.setReason(StrUtil.blankToDefault(request.getReason(), "后台调整"));
        audit.setCreateTime(DateUtil.date());
        chainAuditLogDao.insert(audit);
    }

    @Override
    public CommonPage<ChainSlideLog> slideLogPage(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<ChainSlideLog> list = chainSlideLogDao.selectList(
                Wrappers.<ChainSlideLog>lambdaQuery().orderByDesc(ChainSlideLog::getId));
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public CommonPage<ChainCommission> adminCommissionPage(String orderId, String type, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<ChainCommission> lqw = Wrappers.lambdaQuery();
        if (StrUtil.isNotBlank(orderId)) {
            lqw.eq(ChainCommission::getOrderId, orderId);
        }
        if (StrUtil.isNotBlank(type)) {
            if ("help".equals(type)) {
                // 帮扶基金：冻结记录（见点/平级奖各自冻结一笔）+ 历史「帮扶奖」遗留类型
                lqw.and(w -> w.eq(ChainCommission::getRewardType, ChainConstants.REWARD_SEE_HELP)
                        .or().eq(ChainCommission::getRewardType, ChainConstants.REWARD_PEER_HELP)
                        .or().eq(ChainCommission::getRewardType, "help"));
            } else {
                lqw.eq(ChainCommission::getRewardType, type);
            }
        }
        lqw.orderByDesc(ChainCommission::getId);
        return CommonPage.restPage(new PageInfo<>(chainCommissionDao.selectList(lqw)));
    }

    @Override
    public String exportCommission(String orderId, String type) {
        PageParamRequest page = new PageParamRequest();
        page.setPage(Constants.DEFAULT_PAGE);
        page.setLimit(Constants.EXPORT_MAX_LIMIT);
        CommonPage<ChainCommission> data = adminCommissionPage(orderId, type, page);
        if (CollUtil.isEmpty(data.getList())) {
            throw new QianxuException("没有可导出的数据!");
        }
        List<ChainCommissionExcelVo> voList = new ArrayList<>();
        for (ChainCommission c : data.getList()) {
            ChainCommissionExcelVo vo = new ChainCommissionExcelVo();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setSourceUid(c.getSourceUid());
            vo.setOrderId(c.getOrderId());
            vo.setRewardType(c.getRewardType());
            vo.setAmount(ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO).toPlainString());
            vo.setStatus(String.valueOf(c.getStatus()));
            vo.setMark(c.getMark());
            vo.setCreateTime(c.getCreateTime() == null ? "" : DateUtil.formatDateTime(c.getCreateTime()));
            voList.add(vo);
        }
        ExportUtil.setUpload(qianxuConfig.getImagePath(), Constants.UPLOAD_MODEL_PATH_EXCEL, UploadConstants.DOWNLOAD_FILE_KEYWORD);
        String fileName = "链动佣金".concat(QianxuDateUtil.nowDateTime(Constants.DATE_TIME_FORMAT_NUM))
                .concat(QianxuUtil.randomCount(111111111, 999999999).toString()).concat(".xlsx");
        LinkedHashMap<String, String> aliasMap = new LinkedHashMap<>();
        aliasMap.put("id", "ID");
        aliasMap.put("userId", "用户UID");
        aliasMap.put("sourceUid", "来源UID");
        aliasMap.put("orderId", "订单号");
        aliasMap.put("rewardType", "奖励类型");
        aliasMap.put("amount", "金额");
        aliasMap.put("status", "状态");
        aliasMap.put("mark", "备注");
        aliasMap.put("createTime", "时间");
        return ExportUtil.exportExecl(fileName, "链动佣金导出", voList, aliasMap);
    }

    @Override
    public CommonPage<ChainAuditLog> auditLogPage(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<ChainAuditLog> list = chainAuditLogDao.selectList(
                Wrappers.<ChainAuditLog>lambdaQuery().orderByDesc(ChainAuditLog::getId));
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Map<String, Object> userChainDetail(Integer uid) {
        Map<String, Object> map = new HashMap<>();
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            throw new QianxuException("用户不存在");
        }
        map.put("uid", user.getUid());
        map.put("nickname", user.getNickname());
        map.put("identity", user.getChainIdentity());
        map.put("identityName", identityName(ObjectUtil.defaultIfNull(user.getChainIdentity(), 0)));
        map.put("chainParentId", user.getChainParentId());
        map.put("chainInviteUid", user.getChainInviteUid());
        map.put("effectiveDirectCount", countEffectiveDirects(uid));
        map.put("team", myTeam(uid, 3));
        map.put("commissionSummary", commissionSummary(uid));
        // 帮扶基金冻结流水（新机制：冻结池即链动流水中 status=帮扶基金 的记录）
        int needSeats = intCfg(ChainConstants.CONFIG_DIRECT_CAPACITY, 2);
        map.put("helpUnlocked", ObjectUtil.defaultIfNull(user.getChainHelpUnlocked(), 0) == 1);
        map.put("helpQualifiedSeats", countHelpQualifiedSeats(uid, needSeats));
        map.put("helpNeedSeats", needSeats);
        map.put("helpUnlockNeedOrders", intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10));
        map.put("helpUnlockNeedTeamOrders", intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20));
        map.put("helpFundRecords", chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getUserId, uid)
                .eq(ChainCommission::getStatus, ChainConstants.COMMISSION_HELP_FUND)
                .orderByDesc(ChainCommission::getId).last("limit 20")));
        return map;
    }

    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> map = new HashMap<>();
        long agentCount = userService.count(Wrappers.<User>lambdaQuery().eq(User::getChainIdentity, ChainConstants.IDENTITY_AGENT));
        long bossCount = userService.count(Wrappers.<User>lambdaQuery().eq(User::getChainIdentity, ChainConstants.IDENTITY_BOSS));
        map.put("agentCount", agentCount);
        map.put("bossCount", bossCount);
        Date today = DateUtil.beginOfDay(DateUtil.date());
        map.put("todayNew", chainIdentityDao.selectCount(Wrappers.<ChainIdentity>lambdaQuery()
                .ge(ChainIdentity::getCreateTime, today).eq(ChainIdentity::getStatus, 1)));
        List<ChainCommission> settled = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .in(ChainCommission::getStatus, ChainConstants.COMMISSION_SETTLED, ChainConstants.COMMISSION_FROZEN, ChainConstants.COMMISSION_PENDING));
        BigDecimal total = settled.stream().map(c -> ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        map.put("commissionTotal", total);
        // 团队层级分布：按安置路径深度粗分
        Map<String, Long> depthDist = new LinkedHashMap<>();
        depthDist.put("depth1", 0L);
        depthDist.put("depth2", 0L);
        depthDist.put("depth3Plus", 0L);
        List<ChainRelation> relations = chainRelationDao.selectList(Wrappers.lambdaQuery());
        for (ChainRelation r : relations) {
            int d = ObjectUtil.defaultIfNull(r.getLevelDepth(), pathDepth(r.getPathSnapshot()));
            if (d <= 1) {
                depthDist.put("depth1", depthDist.get("depth1") + 1);
            } else if (d == 2) {
                depthDist.put("depth2", depthDist.get("depth2") + 1);
            } else {
                depthDist.put("depth3Plus", depthDist.get("depth3Plus") + 1);
            }
        }
        map.put("teamDepthDist", depthDist);
        // 帮扶基金：冻结中总额 + 尚未解锁的老板数
        List<ChainCommission> helpFunds = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getStatus, ChainConstants.COMMISSION_HELP_FUND)
                .select(ChainCommission::getAmount));
        BigDecimal helpFundTotal = helpFunds.stream()
                .map(c -> ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        map.put("helpFundTotal", helpFundTotal);
        map.put("helpLockedBossCount", userService.count(Wrappers.<User>lambdaQuery()
                .ge(User::getChainIdentity, ChainConstants.IDENTITY_BOSS)
                .eq(User::getChainHelpUnlocked, 0)));
        chainPhase2Helper.enrichDashboard(map);
        return map;
    }

    @Override
    public CommonPage<ChainNotifyLog> notifyPage(Integer uid, PageParamRequest page) {
        return chainPhase2Helper.notifyPage(uid, page);
    }

    @Override
    public void markNotifyRead(Integer uid, Integer id) {
        chainPhase2Helper.markNotifyRead(uid, id);
    }

    @Override
    public int unreadNotifyCount(Integer uid) {
        return chainPhase2Helper.unreadNotifyCount(uid);
    }

    @Override
    public int runDividend(String period) {
        return chainPhase2Helper.runDividend(period);
    }

    @Override
    public CommonPage<ChainDividendRecord> dividendPage(String period, PageParamRequest page) {
        return chainPhase2Helper.dividendPage(period, page);
    }

    // ----------------- internal -----------------

    private void markChainGiftOrder(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder) || ObjectUtil.isNull(storeOrder.getId())) {
            return;
        }
        String remark = StrUtil.blankToDefault(storeOrder.getRemark(), "");
        if (remark.contains("[CHAIN_GIFT]")) {
            return;
        }
        StoreOrder upd = new StoreOrder();
        upd.setId(storeOrder.getId());
        upd.setRemark(StrUtil.isBlank(remark) ? "[CHAIN_GIFT]" : remark + " [CHAIN_GIFT]");
        storeOrderService.updateById(upd);
        storeOrder.setRemark(upd.getRemark());
    }

    /** 售后保护期满、已结算标记 */
    private void markChainSettled(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder) || ObjectUtil.isNull(storeOrder.getId())) {
            return;
        }
        String remark = StrUtil.blankToDefault(storeOrder.getRemark(), "");
        if (remark.contains("[CHAIN_SETTLED]")) {
            return;
        }
        StoreOrder upd = new StoreOrder();
        upd.setId(storeOrder.getId());
        upd.setRemark(StrUtil.isBlank(remark) ? "[CHAIN_SETTLED]" : remark + " [CHAIN_SETTLED]");
        storeOrderService.updateById(upd);
        storeOrder.setRemark(upd.getRemark());
    }

    /** 激活门槛价（元），0=不限制 */
    private BigDecimal activeThreshold() {
        return decimalCfg(ChainConstants.CONFIG_ACTIVE_THRESHOLD, BigDecimal.ZERO);
    }

    /** 售后保护期（天），0=支付即生效 */
    private int afterSaleDays() {
        return Math.max(0, intCfg(ChainConstants.CONFIG_AFTER_SALE_DAYS, 0));
    }

    /** 单笔分润封顶比例（%，订单实付），默认 50 */
    private BigDecimal maxBrokerageRatio() {
        BigDecimal r = decimalCfg(ChainConstants.CONFIG_MAX_BROKERAGE_RATIO, new BigDecimal("50"));
        if (r.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return r;
    }

    private List<UserBrokerageRecord> assignChainRewards(StoreOrder storeOrder, User buyer) {
        List<UserBrokerageRecord> records = new ArrayList<>();
        if (ObjectUtil.isNull(buyer)) {
            return records;
        }
        Integer freezeDays = intCfg(ChainConstants.CONFIG_FREEZE_DAYS, 1);
        boolean creditOnPay = SysConfigConstants.CREDIT_TIMING_ON_PAY.equals(
                cfg(ChainConstants.CONFIG_CREDIT_TIMING, SysConfigConstants.CREDIT_TIMING_ON_PAY));
        // 订单内链动产品奖励（优先于全局规则）
        ChainProduct chainProduct = resolveOrderChainProduct(storeOrder);
        BigDecimal payPrice = ObjectUtil.defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO);

        // 单笔分润封顶：不超过订单实付的 N%
        BigDecimal capRatio = maxBrokerageRatio();
        BigDecimal cap = capRatio.compareTo(BigDecimal.ZERO) > 0
                ? payPrice.multiply(capRatio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;

        // 1 直推奖（1 级）：推荐人
        Integer inviteUid = ObjectUtil.defaultIfNull(buyer.getChainInviteUid(), 0);
        if (inviteUid <= 0) {
            inviteUid = ObjectUtil.defaultIfNull(buyer.getSpreadUid(), 0);
        }
        if (inviteUid > 0) {
            User invite = userService.getById(inviteUid);
            if (ObjectUtil.isNotNull(invite) && ObjectUtil.defaultIfNull(invite.getChainIdentity(), 0) >= ChainConstants.IDENTITY_AGENT) {
                BigDecimal amount = capAmount(resolveDirectAmount(chainProduct, payPrice), cap, paid);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    UserBrokerageRecord r = buildBrokerage(inviteUid, amount, ChainConstants.BROKERAGE_LEVEL_DIRECT,
                            ChainConstants.TITLE_DIRECT, freezeDays, creditOnPay,
                            StrUtil.format("直推用户{}购买礼包", buyer.getUid()));
                    if (saveCommissionMirror(storeOrder.getOrderId(), inviteUid, buyer.getUid(),
                            ChainConstants.REWARD_DIRECT, amount, r, creditOnPay)) {
                        records.add(r);
                        paid = paid.add(amount);
                    }
                }
            }
        }

        // 2 见点奖（仅直属上级老板，1 级）+ 平级奖（1 级，独立发放给上级老板）
        BigDecimal peerRatio = resolvePeerRatio(chainProduct);
        BigDecimal helpRatio = helpFreezeRatio();
        Integer parentUid = ObjectUtil.defaultIfNull(buyer.getChainParentId(), 0);
        if (parentUid > 0) {
            User parent = userService.getById(parentUid);
            if (ObjectUtil.isNotNull(parent)
                    && ObjectUtil.defaultIfNull(parent.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS) {
                // 见点奖基数：订单实付 × 比例（商品可覆盖）
                BigDecimal rawSee = resolveSeeAmount(chainProduct, 1, payPrice);
                // 平级奖基数 = 下级见点奖 × 比例（文档 3.3，独立发放，不扣减下级见点奖）
                BigDecimal rawPeer = BigDecimal.ZERO;
                Integer upperId = ObjectUtil.defaultIfNull(parent.getChainParentId(), 0);
                boolean upperIsBoss = false;
                if (upperId > 0 && peerRatio.compareTo(BigDecimal.ZERO) > 0) {
                    User upper = userService.getById(upperId);
                    upperIsBoss = ObjectUtil.isNotNull(upper)
                            && ObjectUtil.defaultIfNull(upper.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS;
                    if (upperIsBoss) {
                        rawPeer = rawSee.multiply(peerRatio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    }
                }
                // 见点奖：按比例冻结进帮扶基金，余下计入佣金
                BigDecimal seeAmount = capAmount(rawSee, cap, paid);
                if (seeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal helpPart = freezePart(seeAmount, helpRatio);
                    BigDecimal paySee = seeAmount.subtract(helpPart);
                    if (paySee.compareTo(BigDecimal.ZERO) > 0) {
                        UserBrokerageRecord r = buildBrokerage(parent.getUid(), paySee, ChainConstants.BROKERAGE_LEVEL_SEE,
                                ChainConstants.TITLE_SEE, freezeDays, creditOnPay,
                                StrUtil.format("见点奖，来源用户{}", buyer.getUid()));
                        if (saveCommissionMirror(storeOrder.getOrderId(), parent.getUid(), buyer.getUid(),
                                ChainConstants.REWARD_SEE, paySee, r, creditOnPay)) {
                            records.add(r);
                            paid = paid.add(paySee);
                        }
                    }
                    if (helpPart.compareTo(BigDecimal.ZERO) > 0) {
                        saveHelpFundMirror(storeOrder.getOrderId(), parent.getUid(), buyer.getUid(),
                                ChainConstants.REWARD_SEE_HELP, helpPart,
                                StrUtil.format("见点奖冻结{}%进帮扶基金，来源用户{}",
                                        helpRatio.stripTrailingZeros().toPlainString(), buyer.getUid()));
                    }
                }
                // 平级奖 → 上级老板
                if (upperIsBoss && rawPeer.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal peerAmount = capAmount(rawPeer, cap, paid);
                    if (peerAmount.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal peerHelp = freezePart(peerAmount, helpRatio);
                        BigDecimal payPeer = peerAmount.subtract(peerHelp);
                        if (payPeer.compareTo(BigDecimal.ZERO) > 0) {
                            UserBrokerageRecord pr = buildBrokerage(upperId, payPeer, ChainConstants.BROKERAGE_LEVEL_PEER,
                                    ChainConstants.TITLE_PEER, freezeDays, creditOnPay,
                                    StrUtil.format("平级奖，来自老板{}见点", parent.getUid()));
                            if (saveCommissionMirror(storeOrder.getOrderId(), upperId, buyer.getUid(),
                                    ChainConstants.REWARD_PEER, payPeer, pr, creditOnPay)) {
                                records.add(pr);
                                paid = paid.add(payPeer);
                            }
                        }
                        if (peerHelp.compareTo(BigDecimal.ZERO) > 0) {
                            saveHelpFundMirror(storeOrder.getOrderId(), upperId, buyer.getUid(),
                                    ChainConstants.REWARD_PEER_HELP, peerHelp,
                                    StrUtil.format("平级奖冻结{}%进帮扶基金，来源用户{}",
                                            helpRatio.stripTrailingZeros().toPlainString(), buyer.getUid()));
                        }
                    }
                }
            }
        }
        return records;
    }

    /** 封顶裁剪：本单已发 + 本次 不得超过 cap */
    private BigDecimal capAmount(BigDecimal amount, BigDecimal cap, BigDecimal paid) {
        if (cap.compareTo(BigDecimal.ZERO) <= 0) {
            return amount;
        }
        BigDecimal remain = cap.subtract(paid);
        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.min(remain);
    }

    /** 按比例冻结进帮扶基金 */
    private BigDecimal freezePart(BigDecimal amount, BigDecimal ratio) {
        if (ratio.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal part = amount.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return part.min(amount);
    }

    private boolean saveCommissionMirror(String orderId, Integer uid, Integer sourceUid, String rewardType,
                                         BigDecimal amount, UserBrokerageRecord brokerage, boolean creditOnPay) {
        Integer exists = chainCommissionDao.selectCount(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getOrderId, orderId)
                .eq(ChainCommission::getRewardType, rewardType)
                .eq(ChainCommission::getUserId, uid));
        if (exists != null && exists > 0) {
            return false;
        }
        ChainCommission c = new ChainCommission();
        c.setUserId(uid);
        c.setSourceUid(sourceUid);
        c.setOrderId(orderId);
        // 多代见单/平级用 see1/peer1 保证 order+type+user 唯一
        c.setRewardType(rewardType);
        c.setAmount(amount);
        boolean settledNow = BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE.equals(brokerage.getStatus());
        c.setStatus(settledNow ? ChainConstants.COMMISSION_SETTLED : ChainConstants.COMMISSION_PENDING);
        c.setMark(brokerage.getMark());
        c.setCreateTime(DateUtil.date());
        c.setUpdateTime(DateUtil.date());
        try {
            chainCommissionDao.insert(c);
        } catch (Exception e) {
            log.warn("chain commission duplicate orderId={} type={} uid={}", orderId, rewardType, uid);
            return false;
        }
        return true;
    }

    /** 见单帮扶基金：不入佣金钱包，待解锁后释放 */
    private void saveHelpFundMirror(String orderId, Integer uid, Integer sourceUid, String rewardType,
                                    BigDecimal amount, String mark) {
        Integer exists = chainCommissionDao.selectCount(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getOrderId, orderId)
                .eq(ChainCommission::getRewardType, rewardType)
                .eq(ChainCommission::getUserId, uid));
        if (exists != null && exists > 0) {
            return;
        }
        ChainCommission c = new ChainCommission();
        c.setUserId(uid);
        c.setSourceUid(sourceUid);
        c.setOrderId(orderId);
        c.setRewardType(rewardType);
        c.setAmount(amount);
        c.setStatus(ChainConstants.COMMISSION_HELP_FUND);
        c.setBrokerageRecordId(0);
        c.setMark(mark);
        c.setCreateTime(DateUtil.date());
        c.setUpdateTime(DateUtil.date());
        try {
            chainCommissionDao.insert(c);
        } catch (Exception e) {
            log.warn("chain help fund duplicate orderId={} type={} uid={}", orderId, rewardType, uid);
        }
    }

    /** 帮扶冻结比例%（文档 3.3 默认 30） */
    private BigDecimal helpFreezeRatio() {
        return decimalCfg(ChainConstants.CONFIG_HELP_FREEZE_RATIO, new BigDecimal("30"));
    }

    private void tryUnlockHelpRelated(User buyer) {
        if (ObjectUtil.isNull(buyer)) {
            return;
        }
        Set<Integer> bosses = new LinkedHashSet<>();
        int parentId = ObjectUtil.defaultIfNull(buyer.getChainParentId(), 0);
        if (parentId > 0) {
            bosses.add(parentId);
        }
        int inviteUid = ObjectUtil.defaultIfNull(buyer.getChainInviteUid(), 0);
        if (inviteUid > 0) {
            bosses.add(inviteUid);
            User agent = userService.getById(inviteUid);
            if (ObjectUtil.isNotNull(agent)) {
                int bossUid = ObjectUtil.defaultIfNull(agent.getChainParentId(), 0);
                if (bossUid > 0) {
                    bosses.add(bossUid);
                }
            }
        }
        for (Integer bossUid : bosses) {
            if (bossUid == null || bossUid <= 0) {
                continue;
            }
            User boss = userService.getById(bossUid);
            if (ObjectUtil.isNull(boss)
                    || ObjectUtil.defaultIfNull(boss.getChainIdentity(), 0) < ChainConstants.IDENTITY_BOSS) {
                continue;
            }
            if (ObjectUtil.defaultIfNull(boss.getChainHelpUnlocked(), 0) == 1) {
                // 已解冻：逐笔释放模式下，每来一笔团队订单释放一部分
                if (ChainConstants.HELP_RELEASE_RATIO.equals(
                        cfg(ChainConstants.CONFIG_HELP_RELEASE_MODE, ChainConstants.HELP_RELEASE_ONCE))) {
                    releaseHelpFund(bossUid, intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20));
                }
            } else {
                tryUnlockHelp(bossUid);
            }
        }
    }

    /**
     * 帮扶基金解冻（文档 3.4）：
     * 老板留下的 N 个直推均已晋升老板，且各自团队有效订单数 ≥ N、该老板自身团队有效订单数 ≥ M。
     */
    private void tryUnlockHelp(Integer bossUid) {
        if (bossUid == null || bossUid <= 0) {
            return;
        }
        User boss = userService.getById(bossUid);
        if (ObjectUtil.isNull(boss)
                || ObjectUtil.defaultIfNull(boss.getChainIdentity(), 0) < ChainConstants.IDENTITY_BOSS) {
            return;
        }
        if (ObjectUtil.defaultIfNull(boss.getChainHelpUnlocked(), 0) == 1) {
            return;
        }
        if (helpFreezeRatio().compareTo(BigDecimal.ZERO) <= 0) {
            markHelpUnlocked(bossUid);
            return;
        }
        int needSeats = intCfg(ChainConstants.CONFIG_DIRECT_CAPACITY, 2);
        int seatOrders = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10);
        int selfOrders = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20);
        List<User> seats = listKeptSeats(bossUid, needSeats);
        if (seats.size() < needSeats) {
            return;
        }
        for (User seat : seats) {
            // 留人的代理必须均已晋升老板
            if (ObjectUtil.defaultIfNull(seat.getChainIdentity(), 0) < ChainConstants.IDENTITY_BOSS) {
                return;
            }
            if (countTeamGiftOrders(seat.getUid()) < seatOrders) {
                return;
            }
        }
        if (countTeamGiftOrders(bossUid) < selfOrders) {
            return;
        }
        markHelpUnlocked(bossUid);
        BigDecimal released = releaseHelpFund(bossUid, 1);
        chainPhase2Helper.pushNotify(bossUid, ChainConstants.NOTIFY_HELP, "帮扶基金已解冻",
                "留人的" + needSeats + "个代理均已晋老板且团队达标，帮扶基金"
                        + released.setScale(2, RoundingMode.HALF_UP) + "元已解冻");
    }

    private void markHelpUnlocked(Integer bossUid) {
        User boss = userService.getById(bossUid);
        if (ObjectUtil.isNull(boss)) {
            return;
        }
        boss.setChainHelpUnlocked(1);
        boss.setUpdateTime(DateUtil.date());
        userService.updateById(boss);
    }

    /** 老板留人的前 N 个直推（推荐关系，按加入先后） */
    private List<User> listKeptSeats(Integer bossUid, int needSeats) {
        List<User> directs = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getChainInviteUid, bossUid)
                .ge(User::getChainIdentity, ChainConstants.IDENTITY_AGENT)
                .orderByAsc(User::getUid));
        if (CollUtil.isEmpty(directs)) {
            return Collections.emptyList();
        }
        return directs.size() <= needSeats ? directs : directs.subList(0, needSeats);
    }

    /** 留人座位中「已达标」的数量：本人已晋升老板 且 团队有效订单 ≥ N */
    private int countHelpQualifiedSeats(Integer bossUid, int needSeats) {
        int seatOrders = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10);
        List<User> seats = listKeptSeats(bossUid, needSeats);
        if (CollUtil.isEmpty(seats)) {
            return 0;
        }
        int qualified = 0;
        for (User seat : seats) {
            if (ObjectUtil.defaultIfNull(seat.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS
                    && countTeamGiftOrders(seat.getUid()) >= seatOrders) {
                qualified++;
            }
        }
        return qualified;
    }

    /** 该用户整条团队（含本人）已支付的链动礼包单数 */
    private int countTeamGiftOrders(Integer rootUid) {
        Set<Integer> team = collectTeamUids(rootUid);
        if (CollUtil.isEmpty(team)) {
            return 0;
        }
        Integer cnt = storeOrderService.count(Wrappers.<StoreOrder>lambdaQuery()
                .in(StoreOrder::getUid, team)
                .eq(StoreOrder::getPaid, true)
                .like(StoreOrder::getRemark, "CHAIN_GIFT"));
        return cnt == null ? 0 : cnt;
    }

    /** 沿安置链收集团队 UID（含本人，最多 20 层） */
    private Set<Integer> collectTeamUids(Integer rootUid) {
        Set<Integer> all = new LinkedHashSet<>();
        all.add(rootUid);
        List<Integer> current = new ArrayList<>();
        current.add(rootUid);
        int depth = 0;
        while (!current.isEmpty() && depth++ < 20) {
            List<User> children = userService.list(Wrappers.<User>lambdaQuery()
                    .in(User::getChainParentId, current)
                    .select(User::getUid));
            List<Integer> next = new ArrayList<>();
            for (User c : children) {
                if (all.add(c.getUid())) {
                    next.add(c.getUid());
                }
            }
            current = next;
        }
        return all;
    }

    /**
     * 解冻帮扶基金 → 写入佣金流水并可提现。
     * divisor &lt;= 1：一次性全额解冻；divisor &gt; 1：按冻结池的 1/divisor 逐笔释放。
     */
    private BigDecimal releaseHelpFund(Integer bossUid, int divisor) {
        List<ChainCommission> funds = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getUserId, bossUid)
                .eq(ChainCommission::getStatus, ChainConstants.COMMISSION_HELP_FUND)
                .orderByAsc(ChainCommission::getId));
        if (CollUtil.isEmpty(funds)) {
            return BigDecimal.ZERO;
        }
        BigDecimal pool = BigDecimal.ZERO;
        for (ChainCommission f : funds) {
            pool = pool.add(ObjectUtil.defaultIfNull(f.getAmount(), BigDecimal.ZERO));
        }
        BigDecimal quota = divisor <= 1 ? pool : pool.divide(new BigDecimal(divisor), 2, RoundingMode.HALF_UP);
        int freezeDays = intCfg(ChainConstants.CONFIG_FREEZE_DAYS, 1);
        boolean creditOnPay = true;
        BigDecimal total = BigDecimal.ZERO;
        for (ChainCommission fund : funds) {
            if (quota.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal amount = ObjectUtil.defaultIfNull(fund.getAmount(), BigDecimal.ZERO);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                fund.setStatus(ChainConstants.COMMISSION_SETTLED);
                fund.setUpdateTime(DateUtil.date());
                chainCommissionDao.updateById(fund);
                continue;
            }
            boolean partial = amount.compareTo(quota) > 0;
            BigDecimal releaseAmount = partial ? quota : amount;
            UserBrokerageRecord r = buildBrokerage(bossUid, releaseAmount, ChainConstants.BROKERAGE_LEVEL_HELP,
                    ChainConstants.TITLE_SEE_HELP, freezeDays, creditOnPay,
                    StrUtil.blankToDefault(fund.getMark(), "") + " | 帮扶基金解冻");
            r.setLinkId(fund.getOrderId());
            userBrokerageRecordService.save(r);
            if (partial) {
                ChainCommission rest = new ChainCommission();
                rest.setUserId(fund.getUserId());
                rest.setSourceUid(fund.getSourceUid());
                rest.setOrderId(fund.getOrderId());
                rest.setRewardType(fund.getRewardType());
                rest.setAmount(amount.subtract(releaseAmount));
                rest.setStatus(ChainConstants.COMMISSION_HELP_FUND);
                rest.setBrokerageRecordId(0);
                rest.setMark(fund.getMark());
                rest.setCreateTime(DateUtil.date());
                rest.setUpdateTime(DateUtil.date());
                chainCommissionDao.insert(rest);
            }
            fund.setBrokerageRecordId(ObjectUtil.defaultIfNull(r.getId(), 0));
            boolean settledNow = BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE.equals(r.getStatus());
            fund.setStatus(settledNow ? ChainConstants.COMMISSION_SETTLED : ChainConstants.COMMISSION_PENDING);
            fund.setUpdateTime(DateUtil.date());
            chainCommissionDao.updateById(fund);
            if (settledNow) {
                User boss = userService.getById(bossUid);
                if (ObjectUtil.isNotNull(boss)) {
                    userService.operationBrokerage(bossUid, releaseAmount,
                            ObjectUtil.defaultIfNull(boss.getBrokeragePrice(), BigDecimal.ZERO), "add");
                }
            }
            total = total.add(releaseAmount);
            quota = quota.subtract(releaseAmount);
        }
        return total;
    }

    private UserBrokerageRecord buildBrokerage(Integer uid, BigDecimal amount, int level, String title,
                                               int freezeDays, boolean creditOnPay, String mark) {
        UserBrokerageRecord r = new UserBrokerageRecord();
        r.setUid(uid);
        r.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        r.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        r.setTitle(title);
        r.setPrice(amount);
        r.setMark(mark);
        r.setBrokerageLevel(level);
        r.setCreateTime(DateUtil.date());
        // 有冻结期：先 CREATE，走现有冻结/解冻任务后才可提现
        if (freezeDays > 0) {
            r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
            r.setFrozenTime(freezeDays);
        } else if (creditOnPay) {
            r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
            r.setFrozenTime(0);
        } else {
            r.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
            r.setFrozenTime(0);
        }
        return r;
    }

    private void upgradeToAgent(User user, String remark) {
        if (ObjectUtil.defaultIfNull(user.getChainIdentity(), 0) >= ChainConstants.IDENTITY_AGENT) {
            return;
        }
        user.setChainIdentity(ChainConstants.IDENTITY_AGENT);
        user.setUpdateTime(DateUtil.date());
        userService.updateById(user);
        ChainIdentity identity = new ChainIdentity();
        identity.setUserId(user.getUid());
        identity.setIdentityType(ChainConstants.IDENTITY_AGENT);
        identity.setUpgradeTime(DateUtil.date());
        identity.setStatus(1);
        identity.setRemark(remark);
        identity.setCreateTime(DateUtil.date());
        identity.setUpdateTime(DateUtil.date());
        chainIdentityDao.insert(identity);
        chainPhase2Helper.pushNotify(user.getUid(), ChainConstants.NOTIFY_UPGRADE, "成为代理",
                "恭喜您成为链动代理，" + StrUtil.blankToDefault(remark, ""));
    }

    private void markEffectiveAndMaybeUpgradeBoss(User agentUser) {
        // 通知安置上级重算直推有效数
        Integer parentId = ObjectUtil.defaultIfNull(agentUser.getChainParentId(), 0);
        if (parentId > 0) {
            tryUpgradeBoss(parentId);
        }
        // 本人若已是代理且直推满员也可升
        tryUpgradeBoss(agentUser.getUid());
    }

    private void tryUpgradeBoss(Integer uid) {
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            return;
        }
        if (ObjectUtil.defaultIfNull(user.getChainIdentity(), 0) < ChainConstants.IDENTITY_AGENT) {
            return;
        }
        if (ObjectUtil.defaultIfNull(user.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS) {
            // 已是老板，检测帮扶完成（本人升老板时给帮扶人发奖——在升老板分支处理）
            return;
        }
        int need = intCfg(ChainConstants.CONFIG_DIRECT_CAPACITY, 2);
        int effective = countEffectiveDirects(uid);
        if (effective < need) {
            return;
        }
        Integer oldParentId = ObjectUtil.defaultIfNull(user.getChainParentId(), 0);
        user.setChainIdentity(ChainConstants.IDENTITY_BOSS);
        // 新晋老板：帮扶未解锁，见单按比例进帮扶基金
        user.setChainHelpUnlocked(0);
        // 走人（文档 3.2 / 5.2）：升老板后本人脱离原上级团队 → parent_id 置空，
        // 原上级名下不再产生来自本分支的见点奖；此前挂靠在前 N 个直推保持不变（留人）。
        boolean leaveTeam = "1".equals(cfg(ChainConstants.CONFIG_LEAVE_TEAM_ENABLED, "1"));
        if (leaveTeam && oldParentId > 0) {
            user.setChainParentId(null);
        }
        user.setUpdateTime(DateUtil.date());
        userService.updateById(user);
        ChainIdentity identity = new ChainIdentity();
        identity.setUserId(uid);
        identity.setIdentityType(ChainConstants.IDENTITY_BOSS);
        identity.setUpgradeTime(DateUtil.date());
        identity.setStatus(1);
        identity.setRemark("直推有效" + identityName(ChainConstants.IDENTITY_AGENT) + "满" + need + "人晋升"
                + identityName(ChainConstants.IDENTITY_BOSS));
        identity.setCreateTime(DateUtil.date());
        identity.setUpdateTime(DateUtil.date());
        chainIdentityDao.insert(identity);

        // 关系快照留痕：本体脱离原上级（走人）
        if (leaveTeam && oldParentId > 0) {
            ChainRelation relation = chainRelationDao.selectOne(Wrappers.<ChainRelation>lambdaQuery()
                    .eq(ChainRelation::getUserId, uid).last("limit 1"));
            if (ObjectUtil.isNotNull(relation)) {
                relation.setParentId(null);
                relation.setPathSnapshot("/" + uid + "/");
                relation.setLevelDepth(1);
                relation.setUpdateTime(DateUtil.date());
                chainRelationDao.updateById(relation);
            }
            ChainSlideLog leaveLog = new ChainSlideLog();
            leaveLog.setUserId(uid);
            leaveLog.setFromParentId(oldParentId);
            leaveLog.setToParentId(0);
            leaveLog.setReason("走人留人：晋升" + identityName(ChainConstants.IDENTITY_BOSS)
                    + "，本人脱离原上级团队（前 " + need + " 名直推继续留在原上级名下）");
            leaveLog.setCreatedAt(DateUtil.date());
            chainSlideLogDao.insert(leaveLog);
        }
        int unlockN = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_N, 10);
        int unlockM = intCfg(ChainConstants.CONFIG_HELP_UNLOCK_M, 20);
        chainPhase2Helper.pushNotify(uid, ChainConstants.NOTIFY_UPGRADE, "晋升" + identityName(ChainConstants.IDENTITY_BOSS),
                "恭喜晋升" + identityName(ChainConstants.IDENTITY_BOSS) + "！留人的 " + need + " 名"
                        + identityName(ChainConstants.IDENTITY_AGENT) + "均晋升" + identityName(ChainConstants.IDENTITY_BOSS)
                        + "、且各自团队有效订单 ≥ " + unlockN + "、本人团队 ≥ " + unlockM + " 后，帮扶基金解锁");

        // 若留下的代理此前已达标，当场尝试解锁
        tryUnlockHelp(uid);
    }

    private int countEffectiveDirects(Integer parentUid) {
        List<User> directs = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getChainParentId, parentUid)
                .ge(User::getChainIdentity, ChainConstants.IDENTITY_AGENT));
        if (CollUtil.isEmpty(directs)) {
            return 0;
        }
        // 有效性：身份>=代理即视为有效；若配置为完成才有效，依赖升代理时机已按配置控制
        return directs.size();
    }

    /**
     * 安置位（走人留人机制）：
     * <ul>
     *   <li>关闭走人留人 → 直接挂推荐人</li>
     *   <li>推荐人已是老板 → 挂推荐人本人（老板独立发展）</li>
     *   <li>推荐人还不是老板 → 挂到推荐人的上级（其直推"留给"上级，为上级贡献见点奖）；
     *       推荐人无上级时挂推荐人</li>
     * </ul>
     */
    private Integer resolvePlacementParent(Integer inviteUid, User invite) {
        if (!"1".equals(cfg(ChainConstants.CONFIG_LEAVE_TEAM_ENABLED, "1"))) {
            return inviteUid;
        }
        if (ObjectUtil.isNull(invite)) {
            return inviteUid;
        }
        if (ObjectUtil.defaultIfNull(invite.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS) {
            return inviteUid;
        }
        Integer upper = ObjectUtil.defaultIfNull(invite.getChainParentId(), 0);
        return upper > 0 ? upper : inviteUid;
    }

    private boolean isAncestor(Integer maybeAncestor, Integer userId) {
        Integer cursor = maybeAncestor;
        int guard = 0;
        while (cursor != null && cursor > 0 && guard++ < 50) {
            if (cursor.equals(userId)) {
                return true;
            }
            User u = userService.getById(cursor);
            if (ObjectUtil.isNull(u)) {
                break;
            }
            cursor = ObjectUtil.defaultIfNull(u.getChainParentId(), 0);
        }
        return false;
    }

    private String buildPath(Integer parentId, Integer userId) {
        if (parentId == null || parentId <= 0) {
            return "/" + userId + "/";
        }
        User parent = userService.getById(parentId);
        ChainRelation pr = chainRelationDao.selectOne(Wrappers.<ChainRelation>lambdaQuery()
                .eq(ChainRelation::getUserId, parentId).last("limit 1"));
        String base = ObjectUtil.isNotNull(pr) && StrUtil.isNotBlank(pr.getPathSnapshot())
                ? pr.getPathSnapshot()
                : (ObjectUtil.isNotNull(parent) ? "/" + parentId + "/" : "/");
        if (!base.endsWith("/")) {
            base = base + "/";
        }
        return base + userId + "/";
    }

    private int pathDepth(String path) {
        if (StrUtil.isBlank(path)) {
            return 0;
        }
        return (int) Arrays.stream(path.split("/")).filter(StrUtil::isNotBlank).count();
    }

    private void fillChildren(ChainTeamNodeResponse node, int depth, int maxDepth) {
        if (depth > maxDepth || node.getUid() == null) {
            return;
        }
        List<User> children = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getChainParentId, node.getUid()).orderByAsc(User::getUid));
        for (User child : children) {
            ChainTeamNodeResponse childNode = toNode(child);
            ChainRelation rel = chainRelationDao.selectOne(Wrappers.<ChainRelation>lambdaQuery()
                    .eq(ChainRelation::getUserId, child.getUid()).last("limit 1"));
            childNode.setIsHelp(ObjectUtil.isNotNull(rel) && ObjectUtil.defaultIfNull(rel.getIsHelp(), 0) == 1);
            fillChildren(childNode, depth + 1, maxDepth);
            node.getChildren().add(childNode);
        }
    }

    private ChainTeamNodeResponse toNode(User user) {
        ChainTeamNodeResponse node = new ChainTeamNodeResponse();
        if (ObjectUtil.isNull(user)) {
            return node;
        }
        node.setUid(user.getUid());
        node.setNickname(user.getNickname());
        node.setAvatar(user.getAvatar());
        int identity = ObjectUtil.defaultIfNull(user.getChainIdentity(), 0);
        node.setIdentity(identity);
        node.setIdentityName(identityName(identity));
        node.setIsHelp(false);
        return node;
    }

    /** 级别显示名（后台可自定义，如把「代理/老板」改成「VIP/合伙人」） */
    private String identityName(int identity) {
        if (identity >= ChainConstants.IDENTITY_BOSS) {
            return StrUtil.blankToDefault(cfg(ChainConstants.CONFIG_LEVEL_BOSS_NAME, ""), "老板");
        }
        if (identity >= ChainConstants.IDENTITY_AGENT) {
            return StrUtil.blankToDefault(cfg(ChainConstants.CONFIG_LEVEL_AGENT_NAME, ""), "代理");
        }
        return "游客";
    }

    private Set<Integer> giftProductIds() {
        Set<Integer> set = new HashSet<>();
        String raw = cfg(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, "");
        if (StrUtil.isNotBlank(raw)) {
            for (String p : raw.split(",")) {
                if (StrUtil.isBlank(p)) {
                    continue;
                }
                try {
                    set.add(Integer.parseInt(p.trim()));
                } catch (Exception ignore) {
                }
            }
        }
        // 启用中的链动产品
        List<ChainProduct> products = chainProductDao.selectList(Wrappers.<ChainProduct>lambdaQuery()
                .eq(ChainProduct::getStatus, 1)
                .select(ChainProduct::getProductId));
        if (CollUtil.isNotEmpty(products)) {
            for (ChainProduct p : products) {
                if (ObjectUtil.isNotNull(p.getProductId())) {
                    set.add(p.getProductId());
                }
            }
        }
        return set;
    }

    private void fillProductInfo(ChainProduct p) {
        if (ObjectUtil.isNull(p) || ObjectUtil.isNull(p.getProductId())) {
            return;
        }
        StoreProduct sp = loadStoreProductBrief(p.getProductId());
        if (ObjectUtil.isNull(sp)) {
            return;
        }
        p.setStoreName(sp.getStoreName());
        p.setImage(sp.getImage());
        p.setPrice(sp.getPrice());
    }

    /** 只查必要字段，避免本地/旧库商品表缺列导致 getById 失败 */
    private StoreProduct loadStoreProductBrief(Integer productId) {
        if (ObjectUtil.isNull(productId)) {
            return null;
        }
        return storeProductService.getOne(Wrappers.<StoreProduct>lambdaQuery()
                .select(StoreProduct::getId, StoreProduct::getStoreName, StoreProduct::getImage, StoreProduct::getPrice)
                .eq(StoreProduct::getId, productId)
                .last("limit 1"));
    }

    private void syncGiftProductIdsConfig() {
        List<ChainProduct> products = chainProductDao.selectList(Wrappers.<ChainProduct>lambdaQuery()
                .eq(ChainProduct::getStatus, 1)
                .select(ChainProduct::getProductId)
                .orderByAsc(ChainProduct::getProductId));
        String ids = CollUtil.isEmpty(products) ? "" : products.stream()
                .map(ChainProduct::getProductId)
                .filter(ObjectUtil::isNotNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        systemConfigService.updateOrSaveValueByName(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, ids);
    }

    /** 订单中第一个启用的链动产品 */
    private ChainProduct resolveOrderChainProduct(StoreOrder storeOrder) {
        if (ObjectUtil.isNull(storeOrder)) {
            return null;
        }
        List<StoreOrderInfo> infos = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
        if (CollUtil.isEmpty(infos)) {
            return null;
        }
        for (StoreOrderInfo info : infos) {
            if (ObjectUtil.isNull(info.getProductId())) {
                continue;
            }
            ChainProduct cp = chainProductDao.selectOne(Wrappers.<ChainProduct>lambdaQuery()
                    .eq(ChainProduct::getProductId, info.getProductId())
                    .eq(ChainProduct::getStatus, 1)
                    .last("limit 1"));
            if (ObjectUtil.isNotNull(cp)) {
                return cp;
            }
        }
        return null;
    }

    private BigDecimal resolveDirectAmount(ChainProduct product, BigDecimal payPrice) {
        if (ObjectUtil.isNotNull(product) && ObjectUtil.isNotNull(product.getDirectAmount())
                && product.getDirectAmount().compareTo(BigDecimal.ZERO) > 0) {
            return product.getDirectAmount();
        }
        return ruleAmount(findRule(ChainConstants.REWARD_DIRECT, 1), payPrice);
    }

    private BigDecimal resolveSeeAmount(ChainProduct product, int gen, BigDecimal payPrice) {
        if (ObjectUtil.isNotNull(product)) {
            // 商品自定义：优先比例（文档口径：见点奖 = 订单实付 × 比例%）
            if (ObjectUtil.isNotNull(product.getSeeRatio()) && product.getSeeRatio().compareTo(BigDecimal.ZERO) > 0
                    && ObjectUtil.isNotNull(payPrice)) {
                return payPrice.multiply(product.getSeeRatio())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
            // 兼容旧数据：仍支持按固定金额覆盖
            if (ObjectUtil.isNotNull(product.getSeeAmount()) && product.getSeeAmount().compareTo(BigDecimal.ZERO) > 0) {
                return product.getSeeAmount();
            }
        }
        return ruleAmount(findRule(ChainConstants.REWARD_SEE, gen), payPrice);
    }

    private BigDecimal resolvePeerRatio(ChainProduct product) {
        if (ObjectUtil.isNotNull(product) && ObjectUtil.isNotNull(product.getPeerRatio())
                && product.getPeerRatio().compareTo(BigDecimal.ZERO) > 0) {
            return product.getPeerRatio();
        }
        return peerRatio();
    }

    private ChainRewardRule findRule(String type, int genLevel) {
        ChainRewardRule rule = chainRewardRuleDao.selectOne(Wrappers.<ChainRewardRule>lambdaQuery()
                .eq(ChainRewardRule::getType, type)
                .eq(ChainRewardRule::getGenLevel, genLevel)
                .eq(ChainRewardRule::getStatus, 1)
                .last("limit 1"));
        if (ObjectUtil.isNull(rule) && ChainConstants.REWARD_SEE.equals(type)) {
            rule = chainRewardRuleDao.selectOne(Wrappers.<ChainRewardRule>lambdaQuery()
                    .eq(ChainRewardRule::getType, type)
                    .eq(ChainRewardRule::getStatus, 1)
                    .orderByDesc(ChainRewardRule::getGenLevel)
                    .last("limit 1"));
        }
        if (ObjectUtil.isNull(rule)) {
            rule = chainRewardRuleDao.selectOne(Wrappers.<ChainRewardRule>lambdaQuery()
                    .eq(ChainRewardRule::getType, type)
                    .eq(ChainRewardRule::getStatus, 1)
                    .last("limit 1"));
        }
        return rule;
    }

    private BigDecimal ruleAmount(ChainRewardRule rule, BigDecimal payPrice) {
        if (ObjectUtil.isNull(rule)) {
            return BigDecimal.ZERO;
        }
        if (ObjectUtil.isNotNull(rule.getAmount()) && rule.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return rule.getAmount();
        }
        if (ObjectUtil.isNotNull(rule.getRatio()) && rule.getRatio().compareTo(BigDecimal.ZERO) > 0
                && ObjectUtil.isNotNull(payPrice)) {
            return payPrice.multiply(rule.getRatio()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal peerRatio() {
        ChainRewardRule rule = findRule(ChainConstants.REWARD_PEER, 1);
        if (ObjectUtil.isNull(rule) || ObjectUtil.isNull(rule.getRatio())) {
            return BigDecimal.ZERO;
        }
        return rule.getRatio();
    }

    /** 见点奖比例%（会员端规则页展示，取自奖励规则表） */
    private BigDecimal seeRatio() {
        ChainRewardRule rule = findRule(ChainConstants.REWARD_SEE, 1);
        if (ObjectUtil.isNull(rule) || ObjectUtil.isNull(rule.getRatio())) {
            return BigDecimal.ZERO;
        }
        return rule.getRatio();
    }

    private String cfg(String key, String def) {
        String v = systemConfigService.getValueByKey(key);
        return StrUtil.isBlank(v) ? def : v;
    }

    private int intCfg(String key, int def) {
        try {
            return Integer.parseInt(cfg(key, String.valueOf(def)));
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal decimalCfg(String key, BigDecimal def) {
        try {
            String v = cfg(key, null);
            if (StrUtil.isBlank(v)) {
                return def;
            }
            return new BigDecimal(v.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
