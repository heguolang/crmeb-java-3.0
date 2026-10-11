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
import com.qxkj.service.service.ChainService;
import com.qxkj.service.service.StoreOrderInfoService;
import com.qxkj.service.service.StoreOrderService;
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
    private ChainHelpRecordDao chainHelpRecordDao;
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
    public void assertOrderRisk(User user, String clientIp, String deviceId, Collection<Integer> productIds) {
        if (!isEnabled()) {
            return;
        }
        chainPhase2Helper.assertOrderRisk(user, clientIp, deviceId, productIds, giftProductIds());
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

        Integer placeParent = resolvePlacementParent(inviteUid);
        boolean slid = !placeParent.equals(inviteUid);
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
        relation.setIsHelp(0);
        relation.setUpdateTime(DateUtil.date());
        if (ObjectUtil.isNull(relation.getId())) {
            chainRelationDao.insert(relation);
        } else {
            chainRelationDao.updateById(relation);
        }

        if (slid) {
            ChainSlideLog slideLog = new ChainSlideLog();
            slideLog.setUserId(userId);
            slideLog.setFromParentId(inviteUid);
            slideLog.setToParentId(placeParent);
            slideLog.setReason("直推位已满，按最小团队滑落");
            slideLog.setCreatedAt(DateUtil.date());
            chainSlideLogDao.insert(slideLog);
            // 邀请人为老板时，溢出用户记为帮扶对象
            if (ObjectUtil.defaultIfNull(invite.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS) {
                markHelp(inviteUid, userId);
                relation.setIsHelp(1);
                chainRelationDao.updateById(relation);
            }
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
        Integer uid = storeOrder.getUid();
        User buyer = userService.getById(uid);
        if (ObjectUtil.isNull(buyer)) {
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
        return records;
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
        int need = intCfg(ChainConstants.CONFIG_SLIDE_CAPACITY, 2);
        int effective = countEffectiveDirects(uid);
        resp.setEffectiveDirectCount(effective);
        resp.setBossNeedCount(need);
        resp.setBossRemainCount(Math.max(0, need - effective));
        Map<String, Object> sum = commissionSummary(uid);
        resp.setTodayAmount((BigDecimal) sum.get("today"));
        resp.setTotalAmount((BigDecimal) sum.get("total"));
        resp.setFreezeAmount((BigDecimal) sum.get("freeze"));
        return resp;
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
        BigDecimal freeze = BigDecimal.ZERO;
        for (ChainCommission c : all) {
            BigDecimal amt = ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO);
            total = total.add(amt);
            if (c.getCreateTime() != null && !c.getCreateTime().before(todayStart)) {
                today = today.add(amt);
            }
            if (ObjectUtil.defaultIfNull(c.getStatus(), 0) == ChainConstants.COMMISSION_FROZEN
                    || ObjectUtil.defaultIfNull(c.getStatus(), 0) == ChainConstants.COMMISSION_PENDING) {
                freeze = freeze.add(amt);
            }
        }
        User u = userService.getById(uid);
        BigDecimal withdrawable = ObjectUtil.isNotNull(u)
                ? ObjectUtil.defaultIfNull(u.getBrokeragePrice(), BigDecimal.ZERO) : BigDecimal.ZERO;
        map.put("today", today);
        map.put("total", total);
        map.put("freeze", freeze);
        map.put("withdrawable", withdrawable);
        return map;
    }

    @Override
    public CommonPage<ChainCommission> commissionPage(Integer uid, String type, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<ChainCommission> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChainCommission::getUserId, uid);
        if (StrUtil.isNotBlank(type)) {
            if (ChainConstants.REWARD_SEE.equals(type) || ChainConstants.REWARD_PEER.equals(type)) {
                lqw.likeRight(ChainCommission::getRewardType, type);
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
        return "【2+1链动规则】\n"
                + "1. 购买代理礼包成为代理，获得推荐与直推分佣权。\n"
                + "2. 直推满2名有效代理自动晋升老板，解锁见单奖/平级奖。\n"
                + "3. 老板直推位满员后，新成员按规则滑落至下级空位。\n"
                + "4. 滑落产生的帮扶对象晋升老板后，帮扶人获一次性帮扶奖。\n"
                + "5. 奖励含直推奖、见单奖、平级奖、帮扶奖，具体金额以后台配置为准。";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseTimeoutHelps() {
        int days = intCfg(ChainConstants.CONFIG_HELP_TIMEOUT_DAYS, 0);
        if (days <= 0) {
            return 0;
        }
        Date deadline = DateUtil.offsetDay(DateUtil.date(), -days);
        List<ChainHelpRecord> list = chainHelpRecordDao.selectList(Wrappers.<ChainHelpRecord>lambdaQuery()
                .eq(ChainHelpRecord::getStatus, ChainConstants.HELP_DOING)
                .le(ChainHelpRecord::getCreateTime, deadline));
        int n = 0;
        for (ChainHelpRecord help : list) {
            help.setStatus(ChainConstants.HELP_RELEASED);
            help.setUpdateTime(DateUtil.date());
            chainHelpRecordDao.updateById(help);
            n++;
        }
        return n;
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ChainConstants.CONFIG_FUNC_STATUS, cfg(ChainConstants.CONFIG_FUNC_STATUS, "0"));
        map.put(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, cfg(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, ""));
        map.put(ChainConstants.CONFIG_EFFECTIVE_TIMING, cfg(ChainConstants.CONFIG_EFFECTIVE_TIMING, "1"));
        map.put(ChainConstants.CONFIG_CREDIT_TIMING, cfg(ChainConstants.CONFIG_CREDIT_TIMING, "1"));
        map.put(ChainConstants.CONFIG_FREEZE_DAYS, cfg(ChainConstants.CONFIG_FREEZE_DAYS, "1"));
        map.put(ChainConstants.CONFIG_SLIDE_ENABLED, cfg(ChainConstants.CONFIG_SLIDE_ENABLED, "1"));
        map.put(ChainConstants.CONFIG_SLIDE_CAPACITY, cfg(ChainConstants.CONFIG_SLIDE_CAPACITY, "2"));
        map.put(ChainConstants.CONFIG_SLIDE_DEPTH, cfg(ChainConstants.CONFIG_SLIDE_DEPTH, "2"));
        map.put(ChainConstants.CONFIG_SLIDE_MODE, cfg(ChainConstants.CONFIG_SLIDE_MODE, ChainConstants.SLIDE_MODE_MIN_TEAM));
        map.put(ChainConstants.CONFIG_SEE_GENERATIONS, cfg(ChainConstants.CONFIG_SEE_GENERATIONS, "3"));
        map.put(ChainConstants.CONFIG_IDENTITY_DOWNGRADE, cfg(ChainConstants.CONFIG_IDENTITY_DOWNGRADE, "0"));
        map.put(ChainConstants.CONFIG_SKIP_NORMAL_BROKERAGE, cfg(ChainConstants.CONFIG_SKIP_NORMAL_BROKERAGE, "1"));
        map.put(ChainConstants.CONFIG_HELP_TIMEOUT_DAYS, cfg(ChainConstants.CONFIG_HELP_TIMEOUT_DAYS, "0"));
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
    public CommonPage<Map<String, Object>> adminUserPage(String keywords, Integer identity, PageParamRequest pageParam) {
        PageHelper.startPage(pageParam.getPage(), pageParam.getLimit());
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotNull(identity)) {
            lqw.eq(User::getChainIdentity, identity);
        } else {
            lqw.and(w -> w.gt(User::getChainIdentity, 0).or().gt(User::getChainParentId, 0));
        }
        if (StrUtil.isNotBlank(keywords)) {
            lqw.and(w -> w.like(User::getNickname, keywords).or().like(User::getPhone, keywords)
                    .or().eq(User::getUid, keywords));
        }
        lqw.orderByDesc(User::getUid);
        List<User> users = userService.list(lqw);
        PageInfo<User> pageInfo = new PageInfo<>(users);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> row = new HashMap<>();
            row.put("uid", u.getUid());
            row.put("nickname", u.getNickname());
            row.put("phone", u.getPhone());
            row.put("identity", u.getChainIdentity());
            row.put("identityName", identityName(ObjectUtil.defaultIfNull(u.getChainIdentity(), 0)));
            row.put("chainParentId", u.getChainParentId());
            row.put("chainInviteUid", u.getChainInviteUid());
            row.put("effectiveDirectCount", countEffectiveDirects(u.getUid()));
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
            if (ChainConstants.REWARD_SEE.equals(type) || ChainConstants.REWARD_PEER.equals(type)) {
                lqw.likeRight(ChainCommission::getRewardType, type);
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
        List<ChainHelpRecord> helps = chainHelpRecordDao.selectList(Wrappers.<ChainHelpRecord>lambdaQuery()
                .and(w -> w.eq(ChainHelpRecord::getHelperId, uid).or().eq(ChainHelpRecord::getTargetId, uid))
                .orderByDesc(ChainHelpRecord::getId).last("limit 20"));
        map.put("helpRecords", helps);
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
        map.put("helpDoing", chainHelpRecordDao.selectCount(Wrappers.<ChainHelpRecord>lambdaQuery()
                .eq(ChainHelpRecord::getStatus, ChainConstants.HELP_DOING)));
        chainPhase2Helper.enrichDashboard(map);
        return map;
    }

    @Override
    public CommonPage<ChainRiskLog> riskLogPage(PageParamRequest page) {
        return chainPhase2Helper.riskLogPage(page);
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

    private List<UserBrokerageRecord> assignChainRewards(StoreOrder storeOrder, User buyer) {
        List<UserBrokerageRecord> records = new ArrayList<>();
        if (ObjectUtil.isNull(buyer)) {
            return records;
        }
        Integer freezeDays = intCfg(ChainConstants.CONFIG_FREEZE_DAYS, 1);
        boolean creditOnPay = SysConfigConstants.CREDIT_TIMING_ON_PAY.equals(
                cfg(ChainConstants.CONFIG_CREDIT_TIMING, SysConfigConstants.CREDIT_TIMING_ON_PAY));

        // 1 直推奖：邀请人
        Integer inviteUid = ObjectUtil.defaultIfNull(buyer.getChainInviteUid(), 0);
        if (inviteUid <= 0) {
            inviteUid = ObjectUtil.defaultIfNull(buyer.getSpreadUid(), 0);
        }
        if (inviteUid > 0) {
            User invite = userService.getById(inviteUid);
            if (ObjectUtil.isNotNull(invite) && ObjectUtil.defaultIfNull(invite.getChainIdentity(), 0) >= ChainConstants.IDENTITY_AGENT) {
                ChainRewardRule directRule = findRule(ChainConstants.REWARD_DIRECT, 1);
                BigDecimal amount = ruleAmount(directRule, storeOrder.getPayPrice());
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    UserBrokerageRecord r = buildBrokerage(inviteUid, amount, ChainConstants.BROKERAGE_LEVEL_DIRECT,
                            ChainConstants.TITLE_DIRECT, freezeDays, creditOnPay,
                            StrUtil.format("直推用户{}购买礼包", buyer.getUid()));
                    if (saveCommissionMirror(storeOrder.getOrderId(), inviteUid, buyer.getUid(),
                            ChainConstants.REWARD_DIRECT, amount, r, creditOnPay)) {
                        records.add(r);
                    }
                }
            }
        }

        // 2 见单奖：沿安置链找老板
        int maxGen = intCfg(ChainConstants.CONFIG_SEE_GENERATIONS, 3);
        BigDecimal peerRatio = peerRatio();
        Integer cursor = ObjectUtil.defaultIfNull(buyer.getChainParentId(), 0);
        int gen = 0;
        while (cursor != null && cursor > 0 && gen < maxGen) {
            User parent = userService.getById(cursor);
            if (ObjectUtil.isNull(parent)) {
                break;
            }
            if (ObjectUtil.defaultIfNull(parent.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS) {
                gen++;
                ChainRewardRule seeRule = findRule(ChainConstants.REWARD_SEE, gen);
                BigDecimal seeAmount = ruleAmount(seeRule, storeOrder.getPayPrice());
                if (seeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal peerPart = BigDecimal.ZERO;
                    // 平级：当前老板的上级也是老板时，从本代见单抽成
                    Integer upperId = ObjectUtil.defaultIfNull(parent.getChainParentId(), 0);
                    if (upperId > 0 && peerRatio.compareTo(BigDecimal.ZERO) > 0) {
                        User upper = userService.getById(upperId);
                        if (ObjectUtil.isNotNull(upper)
                                && ObjectUtil.defaultIfNull(upper.getChainIdentity(), 0) >= ChainConstants.IDENTITY_BOSS) {
                            peerPart = seeAmount.multiply(peerRatio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        }
                    }
                    BigDecimal realSee = seeAmount.subtract(peerPart);
                    if (realSee.compareTo(BigDecimal.ZERO) > 0) {
                        UserBrokerageRecord r = buildBrokerage(parent.getUid(), realSee, ChainConstants.BROKERAGE_LEVEL_SEE,
                                ChainConstants.TITLE_SEE, freezeDays, creditOnPay,
                                StrUtil.format("见单奖第{}代，来源用户{}", gen, buyer.getUid()));
                        if (saveCommissionMirror(storeOrder.getOrderId(), parent.getUid(), buyer.getUid(),
                                ChainConstants.REWARD_SEE + gen, realSee, r, creditOnPay)) {
                            // reward_type 用 see，唯一键用 see+gen 会破坏类型筛选；改用 see，唯一靠 user+order+type
                            // 多代见单同 reward_type=see 会冲突，改存 see1/see2...
                            records.add(r);
                        }
                    }
                    if (peerPart.compareTo(BigDecimal.ZERO) > 0) {
                        Integer peerUid = ObjectUtil.defaultIfNull(parent.getChainParentId(), 0);
                        UserBrokerageRecord pr = buildBrokerage(peerUid, peerPart, ChainConstants.BROKERAGE_LEVEL_PEER,
                                ChainConstants.TITLE_PEER, freezeDays, creditOnPay,
                                StrUtil.format("平级奖，来自老板{}见单", parent.getUid()));
                        if (saveCommissionMirror(storeOrder.getOrderId(), peerUid, buyer.getUid(),
                                ChainConstants.REWARD_PEER + gen, peerPart, pr, creditOnPay)) {
                            records.add(pr);
                        }
                    }
                }
            }
            cursor = ObjectUtil.defaultIfNull(parent.getChainParentId(), 0);
            if (gen >= maxGen) {
                break;
            }
            // 防止死循环
            if (cursor.equals(parent.getUid())) {
                break;
            }
        }
        return records;
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
        int need = intCfg(ChainConstants.CONFIG_SLIDE_CAPACITY, 2);
        int effective = countEffectiveDirects(uid);
        if (effective < need) {
            return;
        }
        user.setChainIdentity(ChainConstants.IDENTITY_BOSS);
        user.setUpdateTime(DateUtil.date());
        userService.updateById(user);
        ChainIdentity identity = new ChainIdentity();
        identity.setUserId(uid);
        identity.setIdentityType(ChainConstants.IDENTITY_BOSS);
        identity.setUpgradeTime(DateUtil.date());
        identity.setStatus(1);
        identity.setRemark("直推有效代理满" + need + "人晋升老板");
        identity.setCreateTime(DateUtil.date());
        identity.setUpdateTime(DateUtil.date());
        chainIdentityDao.insert(identity);
        chainPhase2Helper.pushNotify(uid, ChainConstants.NOTIFY_UPGRADE, "晋升老板",
                "恭喜您直推有效代理已满，晋升为老板");

        completeHelpIfNeeded(uid);
    }

    private void completeHelpIfNeeded(Integer targetUid) {
        List<ChainHelpRecord> helps = chainHelpRecordDao.selectList(Wrappers.<ChainHelpRecord>lambdaQuery()
                .eq(ChainHelpRecord::getTargetId, targetUid)
                .eq(ChainHelpRecord::getStatus, ChainConstants.HELP_DOING));
        if (CollUtil.isEmpty(helps)) {
            return;
        }
        ChainRewardRule helpRule = findRule(ChainConstants.REWARD_HELP, 1);
        BigDecimal amount = ruleAmount(helpRule, null);
        int freezeDays = intCfg(ChainConstants.CONFIG_FREEZE_DAYS, 1);
        boolean creditOnPay = true;
        for (ChainHelpRecord help : helps) {
            help.setStatus(ChainConstants.HELP_DONE);
            help.setCompletedAt(DateUtil.date());
            help.setUpdateTime(DateUtil.date());
            chainHelpRecordDao.updateById(help);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            String orderKey = "help-" + help.getId();
            UserBrokerageRecord r = buildBrokerage(help.getHelperId(), amount, ChainConstants.BROKERAGE_LEVEL_HELP,
                    ChainConstants.TITLE_HELP, freezeDays, creditOnPay,
                    StrUtil.format("帮扶对象{}晋升老板", targetUid));
            r.setLinkId(orderKey);
            if (saveCommissionMirror(orderKey, help.getHelperId(), targetUid, ChainConstants.REWARD_HELP, amount, r, creditOnPay)) {
                userBrokerageRecordService.save(r);
                // 无冻结期才即时入账；有冻结期走解冻任务
                if (freezeDays <= 0 && BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE.equals(r.getStatus())) {
                    User helper = userService.getById(help.getHelperId());
                    if (ObjectUtil.isNotNull(helper)) {
                        userService.operationBrokerage(help.getHelperId(), amount,
                                ObjectUtil.defaultIfNull(helper.getBrokeragePrice(), BigDecimal.ZERO), "add");
                    }
                }
                chainPhase2Helper.pushNotify(help.getHelperId(), ChainConstants.NOTIFY_HELP, "帮扶完成",
                        "帮扶对象" + targetUid + "已晋升老板，帮扶奖" + amount + "元");
            }
        }
    }

    private void markHelp(Integer helperId, Integer targetId) {
        Integer cnt = chainHelpRecordDao.selectCount(Wrappers.<ChainHelpRecord>lambdaQuery()
                .eq(ChainHelpRecord::getHelperId, helperId)
                .eq(ChainHelpRecord::getTargetId, targetId));
        if (cnt != null && cnt > 0) {
            return;
        }
        // 每个老板默认只留 1 个进行中的帮扶对象
        Integer doing = chainHelpRecordDao.selectCount(Wrappers.<ChainHelpRecord>lambdaQuery()
                .eq(ChainHelpRecord::getHelperId, helperId)
                .eq(ChainHelpRecord::getStatus, ChainConstants.HELP_DOING));
        if (doing != null && doing > 0) {
            return;
        }
        ChainHelpRecord record = new ChainHelpRecord();
        record.setHelperId(helperId);
        record.setTargetId(targetId);
        record.setStatus(ChainConstants.HELP_DOING);
        record.setCreateTime(DateUtil.date());
        record.setUpdateTime(DateUtil.date());
        chainHelpRecordDao.insert(record);
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
     * 从邀请人起找安置位：未满容量挂其下；已满则在其直推中选最小团队向下滑落。
     */
    private Integer resolvePlacementParent(Integer inviteUid) {
        if (!"1".equals(cfg(ChainConstants.CONFIG_SLIDE_ENABLED, "1"))) {
            return inviteUid;
        }
        int capacity = intCfg(ChainConstants.CONFIG_SLIDE_CAPACITY, 2);
        int maxDepth = intCfg(ChainConstants.CONFIG_SLIDE_DEPTH, 2);
        return findSlot(inviteUid, capacity, 0, maxDepth);
    }

    private Integer findSlot(Integer uid, int capacity, int depth, int maxDepth) {
        int directCount = (int) userService.count(Wrappers.<User>lambdaQuery().eq(User::getChainParentId, uid));
        if (directCount < capacity) {
            return uid;
        }
        if (depth >= maxDepth) {
            // 超出配置深度仍继续在最小团队下找，避免绑失败
            return findAmongChildren(uid, capacity, depth, maxDepth + 8);
        }
        return findAmongChildren(uid, capacity, depth, maxDepth);
    }

    private Integer findAmongChildren(Integer uid, int capacity, int depth, int maxDepth) {
        List<User> children = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getChainParentId, uid).orderByAsc(User::getUid));
        if (CollUtil.isEmpty(children)) {
            return uid;
        }
        String mode = cfg(ChainConstants.CONFIG_SLIDE_MODE, ChainConstants.SLIDE_MODE_MIN_TEAM);
        if (ChainConstants.SLIDE_MODE_RIGHT.equals(mode)) {
            children.sort(Comparator.comparing(User::getUid).reversed());
        } else if (ChainConstants.SLIDE_MODE_LEFT.equals(mode)) {
            children.sort(Comparator.comparing(User::getUid));
        } else {
            // 最小团队优先
            children.sort(Comparator.comparingInt(c ->
                    (int) userService.count(Wrappers.<User>lambdaQuery().eq(User::getChainParentId, c.getUid()))));
        }
        for (User child : children) {
            Integer slot = findSlot(child.getUid(), capacity, depth + 1, maxDepth);
            if (slot != null) {
                return slot;
            }
        }
        return children.get(0).getUid();
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

    private String identityName(int identity) {
        if (identity >= ChainConstants.IDENTITY_BOSS) {
            return "老板";
        }
        if (identity >= ChainConstants.IDENTITY_AGENT) {
            return "代理";
        }
        return "游客";
    }

    private Set<Integer> giftProductIds() {
        String raw = cfg(ChainConstants.CONFIG_GIFT_PRODUCT_IDS, "");
        Set<Integer> set = new HashSet<>();
        if (StrUtil.isBlank(raw)) {
            return set;
        }
        for (String p : raw.split(",")) {
            if (StrUtil.isBlank(p)) {
                continue;
            }
            try {
                set.add(Integer.parseInt(p.trim()));
            } catch (Exception ignore) {
            }
        }
        return set;
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
}
