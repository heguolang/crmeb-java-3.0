package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.constants.BrokerageRecordConstants;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.order.StoreOrderInfo;
import com.qxkj.common.model.pingou.PingouActivity;
import com.qxkj.common.model.pingou.PingouJoin;
import com.qxkj.common.model.pingou.PingouTeam;
import com.qxkj.common.model.product.StoreProduct;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBill;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.PingouActivityRequest;
import com.qxkj.common.request.PingouSearchRequest;
import com.qxkj.common.request.PingouTeamSearchRequest;
import com.qxkj.common.response.PingouFrontDetailResponse;
import com.qxkj.common.response.PingouJoinUserResponse;
import com.qxkj.common.response.PingouTeamListResponse;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.dao.PingouActivityDao;
import com.qxkj.service.dao.PingouJoinDao;
import com.qxkj.service.dao.PingouTeamDao;
import com.qxkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拼购活动 Service 实现
 *
 * 玩法：购买拼购活动商品（正常下单链路，余额/微信/支付宝均可）即自动参团；
 * 满成团人数即开奖，随机抽中奖人数获得商品（订单正常发货）；
 * 未中奖者本金全额退回余额账户，并发放活动约定的补贴佣金到佣金账户；
 * 参与者的直接推荐人按直推人数获得参与金额 5‰ ~ 10‰ 的分享奖励。
 */
@Service
public class PingouServiceImpl extends ServiceImpl<PingouActivityDao, PingouActivity> implements PingouService {

    private static final Logger logger = LoggerFactory.getLogger(PingouServiceImpl.class);

    /** 分享奖励发放状态：未发放 */
    private static final int SHARE_STATUS_NONE = 0;
    /** 分享奖励发放状态：已发放 */
    private static final int SHARE_STATUS_DONE = 1;
    /** 分享奖励发放状态：无推荐人，无需发放 */
    private static final int SHARE_STATUS_NO_SPREAD = 2;

    @Autowired
    private PingouActivityDao activityDao;

    @Autowired
    private PingouTeamDao teamDao;

    @Autowired
    private PingouJoinDao joinDao;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBillService userBillService;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private StoreOrderInfoService storeOrderInfoService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // ============================================================
    // 后台：活动管理
    // ============================================================

    @Override
    public List<PingouActivity> getAdminList(PingouSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<PingouActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PingouActivity::getIsDel, false);
        if (ObjectUtil.isNotNull(request)) {
            if (StrUtil.isNotBlank(request.getKeywords())) {
                String kw = request.getKeywords();
                wrapper.and(w -> w.like(PingouActivity::getName, kw).or().like(PingouActivity::getProductName, kw));
            }
            if (ObjectUtil.isNotNull(request.getStatus())) {
                wrapper.eq(PingouActivity::getStatus, request.getStatus());
            }
        }
        wrapper.orderByDesc(PingouActivity::getSort).orderByDesc(PingouActivity::getId);
        return activityDao.selectList(wrapper);
    }

    @Override
    public boolean saveActivity(PingouActivityRequest request) {
        validateRequest(request);
        StoreProduct product = storeProductService.getById(request.getProductId());
        if (ObjectUtil.isNull(product) || product.getIsDel()) {
            throw new QianxuException("拼购商品不存在或已删除");
        }
        PingouActivity activity = request.toEntity();
        activity.setProductName(product.getStoreName());
        activity.setProductPrice(product.getPrice());
        activity.setIsDel(false);
        return save(activity);
    }

    @Override
    public boolean updateActivity(PingouActivityRequest request) {
        if (ObjectUtil.isNull(request.getId())) {
            throw new QianxuException("活动ID不能为空");
        }
        validateRequest(request);
        PingouActivity exist = getById(request.getId());
        if (ObjectUtil.isNull(exist) || Boolean.TRUE.equals(exist.getIsDel())) {
            throw new QianxuException("活动不存在");
        }
        StoreProduct product = storeProductService.getById(request.getProductId());
        if (ObjectUtil.isNull(product) || product.getIsDel()) {
            throw new QianxuException("拼购商品不存在或已删除");
        }
        PingouActivity activity = request.toEntity();
        activity.setProductName(product.getStoreName());
        activity.setProductPrice(product.getPrice());
        activity.setIsDel(false);
        return updateById(activity);
    }

    private void validateRequest(PingouActivityRequest request) {
        if (request.getTotalNum() == null || request.getTotalNum() < 2) {
            throw new QianxuException("成团人数至少为2人");
        }
        if (request.getWinNum() == null || request.getWinNum() < 1) {
            throw new QianxuException("中奖人数至少为1人");
        }
        if (request.getWinNum() >= request.getTotalNum()) {
            throw new QianxuException("中奖人数必须小于成团人数");
        }
        if (request.getStartTime() != null && request.getEndTime() != null
                && !request.getEndTime().after(request.getStartTime())) {
            throw new QianxuException("活动结束时间必须晚于开始时间");
        }
    }

    @Override
    public boolean deleteActivity(Integer id) {
        PingouActivity activity = getById(id);
        if (ObjectUtil.isNull(activity) || Boolean.TRUE.equals(activity.getIsDel())) {
            return false;
        }
        activity.setIsDel(true);
        activity.setStatus(false);
        return updateById(activity);
    }

    @Override
    public boolean updateActivityStatus(Integer id, boolean status) {
        PingouActivity activity = getById(id);
        if (ObjectUtil.isNull(activity) || Boolean.TRUE.equals(activity.getIsDel())) {
            return false;
        }
        activity.setStatus(status);
        return updateById(activity);
    }

    // ============================================================
    // 后台：团记录
    // ============================================================

    @Override
    public List<PingouTeamListResponse> getAdminTeamList(PingouTeamSearchRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<PingouTeam> wrapper = new LambdaQueryWrapper<>();
        if (ObjectUtil.isNotNull(request)) {
            if (ObjectUtil.isNotNull(request.getActivityId())) {
                wrapper.eq(PingouTeam::getActivityId, request.getActivityId());
            }
            if (ObjectUtil.isNotNull(request.getStatus())) {
                wrapper.eq(PingouTeam::getStatus, request.getStatus());
            }
        }
        wrapper.orderByDesc(PingouTeam::getId);
        List<PingouTeam> teams = teamDao.selectList(wrapper);
        return buildTeamResponses(teams, true);
    }

    @Override
    public List<PingouTeamListResponse> getTeamDetail(Integer teamId) {
        PingouTeam team = teamDao.selectById(teamId);
        if (ObjectUtil.isNull(team)) {
            throw new QianxuException("团记录不存在");
        }
        return buildTeamResponses(Collections.singletonList(team), true);
    }

    // ============================================================
    // 会员端
    // ============================================================

    @Override
    public List<PingouActivity> getFrontList(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<PingouActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PingouActivity::getIsDel, false)
                .eq(PingouActivity::getStatus, true)
                .eq(PingouActivity::getIsShow, true)
                .orderByDesc(PingouActivity::getSort).orderByDesc(PingouActivity::getId);
        return activityDao.selectList(wrapper);
    }

    @Override
    public PingouFrontDetailResponse getFrontDetail(Integer id) {
        PingouActivity activity = getValidActivity(id);
        PingouFrontDetailResponse response = new PingouFrontDetailResponse();
        response.setId(activity.getId());
        response.setName(activity.getName());
        response.setImage(activity.getImage());
        response.setProductId(activity.getProductId());
        response.setProductName(activity.getProductName());
        response.setProductPrice(activity.getProductPrice());
        response.setStartTime(formatDate(activity.getStartTime()));
        response.setEndTime(formatDate(activity.getEndTime()));
        response.setTotalNum(activity.getTotalNum());
        response.setWinNum(activity.getWinNum());
        response.setRefundSubsidy(activity.getRefundSubsidy());
        response.setShareRateLow(activity.getShareRateLow());
        response.setShareRateHigh(activity.getShareRateHigh());
        response.setShareRateThreshold(activity.getShareRateThreshold());
        response.setLimitNum(activity.getLimitNum());

        // 累计开团次数
        Integer teamCount = teamDao.selectCount(new LambdaQueryWrapper<PingouTeam>()
                .eq(PingouTeam::getActivityId, id));
        response.setTeamCount(teamCount == null ? 0 : teamCount.intValue());

        // 当前进行中的团
        PingouTeam openTeam = getOpenTeam(id);
        if (ObjectUtil.isNotNull(openTeam)) {
            response.setCurrentTeamId(openTeam.getId());
            response.setCurrentTeamNo(openTeam.getTeamNo());
            response.setCurrentJoinNum(openTeam.getJoinNum());
            response.setCurrentRemainNum(Math.max(0, activity.getTotalNum() - openTeam.getJoinNum()));
        }

        // 最近已开奖团（最多两期，含中奖情况）
        List<PingouTeam> recent = teamDao.selectList(new LambdaQueryWrapper<PingouTeam>()
                .eq(PingouTeam::getActivityId, id)
                .eq(PingouTeam::getStatus, PingouTeam.STATUS_FINISH)
                .orderByDesc(PingouTeam::getId)
                .last(" limit 2"));
        response.setRecentTeams(buildTeamResponses(recent, true));
        return response;
    }

    @Override
    public List<PingouTeamListResponse> getFrontTeamList(Integer activityId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<PingouTeam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PingouTeam::getActivityId, activityId).orderByDesc(PingouTeam::getId);
        return buildTeamResponses(teamDao.selectList(wrapper), true);
    }

    @Override
    public PageInfo<PingouJoin> getMyJoins(Integer uid, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<PingouJoin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PingouJoin::getUid, uid).orderByDesc(PingouJoin::getId);
        List<PingouJoin> list = joinDao.selectList(wrapper);
        return new PageInfo<>(list);
    }

    // ============================================================
    // 核心：支付成功参团 / 成团开奖 / 兜底
    // ============================================================

    @Override
    public void onPaySuccess(StoreOrder storeOrder) {
        doJoin(storeOrder);
    }

    /**
     * 参团主流程（事务内执行）：
     * 订单商品命中启用中的拼购活动 → 写参与记录 → 团计数 → 满员开奖
     */
    private void doJoin(StoreOrder storeOrder) {
        // 幂等：该订单已登记参团则跳过
        Integer joined = joinDao.selectCount(new LambdaQueryWrapper<PingouJoin>()
                .eq(PingouJoin::getOrderId, storeOrder.getId()));
        if (joined != null && joined > 0) {
            return;
        }

        // 订单商品行命中拼购活动商品（一个订单只处理第一个命中的拼购商品）
        List<StoreOrderInfo> orderInfoList = storeOrderInfoService.getListByOrderNo(storeOrder.getOrderId());
        if (CollUtil.isEmpty(orderInfoList)) {
            return;
        }
        StoreOrderInfo hitInfo = null;
        PingouActivity hitActivity = null;
        for (StoreOrderInfo orderInfo : orderInfoList) {
            PingouActivity activity = findValidActivityByProduct(orderInfo.getProductId());
            if (ObjectUtil.isNotNull(activity)) {
                hitInfo = orderInfo;
                hitActivity = activity;
                break;
            }
        }
        if (ObjectUtil.isNull(hitActivity) || ObjectUtil.isNull(hitInfo)) {
            return; // 非拼购商品订单，静默跳过
        }

        // 限购校验：超出限购次数按普通订单处理，不参与拼购
        if (ObjectUtil.defaultIfNull(hitActivity.getLimitNum(), 0) > 0) {
            Integer myCount = joinDao.selectCount(new LambdaQueryWrapper<PingouJoin>()
                    .eq(PingouJoin::getActivityId, hitActivity.getId())
                    .eq(PingouJoin::getUid, storeOrder.getUid()));
            if (myCount != null && myCount >= hitActivity.getLimitNum()) {
                logger.info("用户参与拼购已达限购次数，按普通订单处理，uid={}, activityId={}", storeOrder.getUid(), hitActivity.getId());
                return;
            }
        }

        // 参与金额 = 命中商品行实付金额（单价 × 数量）
        BigDecimal payPrice = ObjectUtil.defaultIfNull(hitInfo.getPrice(), BigDecimal.ZERO)
                .multiply(new BigDecimal(ObjectUtil.defaultIfNull(hitInfo.getPayNum(), 1)));

        // 获取或创建进行中的团
        PingouTeam team = getOpenTeam(hitActivity.getId());
        if (ObjectUtil.isNull(team)) {
            team = createOpenTeam(hitActivity);
        }

        // 写参与记录
        User buyer = userService.getById(storeOrder.getUid());
        PingouJoin join = new PingouJoin();
        join.setTeamId(team.getId());
        join.setActivityId(hitActivity.getId());
        join.setUid(storeOrder.getUid());
        join.setOrderId(storeOrder.getId());
        join.setOrderNo(storeOrder.getOrderId());
        join.setProductId(hitActivity.getProductId());
        join.setPayPrice(payPrice);
        join.setSpreadUid(ObjectUtil.defaultIfNull(buyer == null ? null : buyer.getSpreadUid(), 0));
        join.setIsWin(0);
        join.setRewardStatus(0);
        join.setRefundPrice(BigDecimal.ZERO);
        join.setSubsidyPrice(BigDecimal.ZERO);
        join.setShareRate(BigDecimal.ZERO);
        join.setSharePrice(BigDecimal.ZERO);
        join.setShareStatus(SHARE_STATUS_NONE);
        join.setCreateTime(DateUtil.date());
        if (joinDao.insert(join) <= 0) {
            throw new QianxuException("拼购参与记录写入失败");
        }

        // 分享奖励：发给自己直接推荐人（支付成功即发，比例按推荐人直推参与人数）
        grantShareReward(hitActivity, join);

        // 团计数
        boolean counted = teamDao.update(null, new UpdateWrapper<PingouTeam>()
                .setSql("join_num = join_num + 1")
                .eq("id", team.getId())
                .eq("status", PingouTeam.STATUS_OPEN)) > 0;
        if (!counted) {
            throw new QianxuException("拼团计数失败，团可能刚满员，请稍后重试");
        }

        // 满员开奖
        PingouTeam latest = teamDao.selectById(team.getId());
        if (latest.getJoinNum() >= latest.getTotalNum()) {
            drawTeam(latest);
        }
    }

    /**
     * 分享奖励：参与者支付成功后，其直接推荐人获得 参与金额 × 比例。
     * 比例规则：推荐人直推参与本活动人数（含本次）未达阈值享低比例，达到阈值享高比例。
     */
    private void grantShareReward(PingouActivity activity, PingouJoin join) {
        Integer spreadUid = join.getSpreadUid();
        if (ObjectUtil.isNull(spreadUid) || spreadUid < 1 || spreadUid.equals(join.getUid())) {
            join.setShareStatus(SHARE_STATUS_NO_SPREAD);
            joinDao.updateById(join);
            return;
        }
        // 推荐人直推人数（含本次）
        Integer spreadCount = joinDao.selectCount(new LambdaQueryWrapper<PingouJoin>()
                .eq(PingouJoin::getActivityId, activity.getId())
                .eq(PingouJoin::getSpreadUid, spreadUid));
        int count = (spreadCount == null ? 0 : spreadCount.intValue());
        BigDecimal rate = count >= ObjectUtil.defaultIfNull(activity.getShareRateThreshold(), 10)
                ? ObjectUtil.defaultIfNull(activity.getShareRateHigh(), BigDecimal.TEN)
                : ObjectUtil.defaultIfNull(activity.getShareRateLow(), new BigDecimal("5"));
        BigDecimal sharePrice = join.getPayPrice().multiply(rate)
                .divide(new BigDecimal("1000"), 2, RoundingMode.DOWN);
        join.setShareRate(rate);
        if (sharePrice.compareTo(BigDecimal.ZERO) <= 0) {
            join.setShareStatus(SHARE_STATUS_NO_SPREAD);
            joinDao.updateById(join);
            return;
        }
        grantBrokerage(spreadUid, sharePrice,
                BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_PINGOU_SHARE,
                StrUtil.format("拼购分享奖励：被推荐人参与「{}」，按{}‰奖励{}", activity.getName(), rate, sharePrice),
                join.getOrderNo());
        join.setSharePrice(sharePrice);
        join.setShareStatus(SHARE_STATUS_DONE);
        joinDao.updateById(join);
    }

    /**
     * 成团开奖：随机抽取中奖者；未中奖者退本金到余额 + 补贴佣金；团置为已完成
     */
    private void drawTeam(PingouTeam team) {
        List<PingouJoin> joins = joinDao.selectList(new LambdaQueryWrapper<PingouJoin>()
                .eq(PingouJoin::getTeamId, team.getId()));
        if (CollUtil.isEmpty(joins) || joins.size() < team.getTotalNum()) {
            return;
        }
        PingouActivity activity = getById(team.getActivityId());
        if (ObjectUtil.isNull(activity)) {
            logger.error("拼购开奖失败：活动不存在，teamId={}, activityId={}", team.getId(), team.getActivityId());
            return;
        }

        // 随机抽中奖者
        Collections.shuffle(joins, new Random());
        int winNum = Math.min(ObjectUtil.defaultIfNull(team.getWinNum(), 0), joins.size());
        Set<Integer> winnerJoinIds = new HashSet<>();
        for (int i = 0; i < winNum; i++) {
            winnerJoinIds.add(joins.get(i).getId());
        }

        // 未中奖者：退本金 + 补贴佣金
        for (PingouJoin join : joins) {
            boolean isWin = winnerJoinIds.contains(join.getId());
            join.setIsWin(isWin ? 1 : 0);
            if (isWin) {
                joinDao.updateById(join);
                continue;
            }
            refundAndSubsidy(activity, join);
        }

        team.setStatus(PingouTeam.STATUS_FINISH);
        team.setWinTime(DateUtil.date());
        teamDao.updateById(team);
        logger.info("拼购团开奖完成：teamId={}, activityId={}, teamNo={}, 参团{}人, 中奖{}人",
                team.getId(), team.getActivityId(), team.getTeamNo(), joins.size(), winNum);
    }

    /**
     * 未中奖处理：本金全额退回余额账户 + 活动约定补贴佣金到佣金账户
     * 幂等：以 join.reward_status 为准（CAS 更新），失败留给兜底任务重试
     */
    private void refundAndSubsidy(PingouActivity activity, PingouJoin join) {
        // CAS 抢占发放权，防止并发重复发放
        int updated = joinDao.update(null, new UpdateWrapper<PingouJoin>()
                .setSql("reward_status = 1")
                .eq("id", join.getId())
                .eq("reward_status", 0)) > 0 ? 1 : 0;
        if (updated == 0) {
            return;
        }

        // 1. 本金退回余额账户
        BigDecimal refundPrice = ObjectUtil.defaultIfNull(join.getPayPrice(), BigDecimal.ZERO);
        if (refundPrice.compareTo(BigDecimal.ZERO) > 0) {
            User user = userService.getById(join.getUid());
            if (ObjectUtil.isNull(user)) {
                throw new QianxuException("拼购退款失败，用户不存在，uid=" + join.getUid());
            }
            Boolean refundOk = userService.operationNowMoney(join.getUid(), refundPrice,
                    ObjectUtil.defaultIfNull(user.getNowMoney(), BigDecimal.ZERO), "add");
            if (!Boolean.TRUE.equals(refundOk)) {
                throw new QianxuException("拼购退款到余额失败，uid=" + join.getUid());
            }
            UserBill refundBill = new UserBill();
            refundBill.setUid(join.getUid());
            refundBill.setLinkId(join.getOrderNo());
            refundBill.setTitle("拼购未中奖退款");
            refundBill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
            refundBill.setType(Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND);
            refundBill.setNumber(refundPrice);
            refundBill.setBalance(ObjectUtil.defaultIfNull(user.getNowMoney(), BigDecimal.ZERO).add(refundPrice));
            refundBill.setMark(StrUtil.format("拼购「{}」未中奖，本金{}元退回余额账户", activity.getName(), refundPrice));
            refundBill.setPm(1);
            userBillService.save(refundBill);
        }

        // 2. 补贴佣金进佣金账户
        BigDecimal subsidy = ObjectUtil.defaultIfNull(activity.getRefundSubsidy(), BigDecimal.ZERO);
        if (subsidy.compareTo(BigDecimal.ZERO) > 0) {
            grantBrokerage(join.getUid(), subsidy,
                    BrokerageRecordConstants.BROKERAGE_RECORD_TITLE_PINGOU_SUBSIDY,
                    StrUtil.format("拼购「{}」未中奖补贴，退本金{}元至余额，另补贴佣金{}元",
                            activity.getName(), refundPrice, subsidy),
                    join.getOrderNo());
        }

        join.setRefundPrice(refundPrice);
        join.setSubsidyPrice(subsidy);
        joinDao.updateById(join);
    }

    /**
     * 发放佣金到佣金账户（乐观锁，失败抛异常由事务回滚/兜底任务重试）
     */
    private void grantBrokerage(Integer uid, BigDecimal price, String title, String mark, String linkId) {
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            throw new QianxuException("佣金发放失败，用户不存在，uid=" + uid);
        }
        Boolean addOk = userService.operationBrokerage(uid, price,
                ObjectUtil.defaultIfNull(user.getBrokeragePrice(), BigDecimal.ZERO), "add");
        if (!Boolean.TRUE.equals(addOk)) {
            throw new QianxuException("佣金发放失败，uid=" + uid);
        }
        UserBrokerageRecord record = new UserBrokerageRecord();
        record.setUid(uid);
        record.setLinkId(linkId);
        record.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        record.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        record.setTitle(title);
        record.setPrice(price);
        record.setBalance(ObjectUtil.defaultIfNull(user.getBrokeragePrice(), BigDecimal.ZERO).add(price));
        record.setMark(mark);
        record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
        record.setFrozenTime(0);
        record.setCreateTime(QianxuDateUtil.nowDateTime());
        userBrokerageRecordService.save(record);

        // 资金明细（佣金账户）
        UserBill bill = new UserBill();
        bill.setUid(uid);
        bill.setLinkId(linkId);
        bill.setTitle(title);
        bill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
        bill.setType(Constants.USER_BILL_TYPE_BROKERAGE);
        bill.setNumber(price);
        bill.setBalance(ObjectUtil.defaultIfNull(user.getBrokeragePrice(), BigDecimal.ZERO).add(price));
        bill.setMark(mark);
        bill.setPm(1);
        userBillService.save(bill);
    }

    @Override
    public void checkAndDrawTeams() {
        // 1. 满员未开奖的团 → 开奖
        List<PingouTeam> needDrawTeams = teamDao.selectList(new LambdaQueryWrapper<PingouTeam>()
                .eq(PingouTeam::getStatus, PingouTeam.STATUS_OPEN)
                .apply("join_num >= total_num"));
        for (PingouTeam team : needDrawTeams) {
            try {
                transactionTemplate.execute(e -> {
                    PingouTeam latest = teamDao.selectById(team.getId());
                    if (latest.getStatus() == PingouTeam.STATUS_OPEN && latest.getJoinNum() >= latest.getTotalNum()) {
                        drawTeam(latest);
                    }
                    return Boolean.TRUE;
                });
            } catch (Exception e) {
                logger.error("拼购兜底开奖失败，teamId={}, 原因: {}", team.getId(), e.getMessage(), e);
            }
        }

        // 2. 已开奖团中未中奖奖励未发放的 → 补发
        List<PingouJoin> pendingJoins = joinDao.selectList(new LambdaQueryWrapper<PingouJoin>()
                .eq(PingouJoin::getIsWin, 0)
                .eq(PingouJoin::getRewardStatus, 0)
                .inSql(PingouJoin::getTeamId,
                        "SELECT id FROM eb_pingou_team WHERE status = 1"));
        for (PingouJoin join : pendingJoins) {
            try {
                transactionTemplate.execute(e -> {
                    PingouActivity activity = getById(join.getActivityId());
                    if (ObjectUtil.isNotNull(activity)) {
                        refundAndSubsidy(activity, join);
                    }
                    return Boolean.TRUE;
                });
            } catch (Exception e) {
                logger.error("拼购兜底补发奖励失败，joinId={}, 原因: {}", join.getId(), e.getMessage(), e);
            }
        }
    }

    // ============================================================
    // 私有工具
    // ============================================================

    /**
     * 查询启用中且在有效期内的活动（按商品）
     */
    private PingouActivity findValidActivityByProduct(Integer productId) {
        if (ObjectUtil.isNull(productId)) {
            return null;
        }
        List<PingouActivity> list = activityDao.selectList(new LambdaQueryWrapper<PingouActivity>()
                .eq(PingouActivity::getProductId, productId)
                .eq(PingouActivity::getStatus, true)
                .eq(PingouActivity::getIsDel, false)
                .orderByDesc(PingouActivity::getSort));
        Date now = DateUtil.date();
        for (PingouActivity activity : list) {
            if (activity.getStartTime() != null && now.before(activity.getStartTime())) {
                continue;
            }
            if (activity.getEndTime() != null && now.after(activity.getEndTime())) {
                continue;
            }
            return activity;
        }
        return null;
    }

    private PingouActivity getValidActivity(Integer id) {
        PingouActivity activity = getById(id);
        if (ObjectUtil.isNull(activity) || Boolean.TRUE.equals(activity.getIsDel()) || !Boolean.TRUE.equals(activity.getStatus())) {
            throw new QianxuException("拼购活动不存在或已下架");
        }
        return activity;
    }

    /**
     * 查询活动当前进行中的团
     */
    private PingouTeam getOpenTeam(Integer activityId) {
        List<PingouTeam> teams = teamDao.selectList(new LambdaQueryWrapper<PingouTeam>()
                .eq(PingouTeam::getActivityId, activityId)
                .eq(PingouTeam::getStatus, PingouTeam.STATUS_OPEN)
                .orderByDesc(PingouTeam::getId)
                .last(" limit 1"));
        return CollUtil.isEmpty(teams) ? null : teams.get(0);
    }

    /**
     * 创建新一期团（唯一索引 uk_activity_open 防并发重复）
     */
    private PingouTeam createOpenTeam(PingouActivity activity) {
        Integer teamCount = teamDao.selectCount(new LambdaQueryWrapper<PingouTeam>()
                .eq(PingouTeam::getActivityId, activity.getId()));
        PingouTeam team = new PingouTeam();
        team.setActivityId(activity.getId());
        team.setTeamNo((teamCount == null ? 0 : teamCount.intValue()) + 1);
        team.setJoinNum(0);
        team.setTotalNum(activity.getTotalNum());
        team.setWinNum(activity.getWinNum());
        team.setStatus(PingouTeam.STATUS_OPEN);
        team.setOpenTime(DateUtil.date());
        team.setCreateTime(DateUtil.date());
        teamDao.insert(team);
        return team;
    }

    /**
     * 组装团记录响应（含活动信息与成员头像/中奖情况）
     */
    private List<PingouTeamListResponse> buildTeamResponses(List<PingouTeam> teams, boolean withJoins) {
        if (CollUtil.isEmpty(teams)) {
            return new ArrayList<>();
        }
        Map<Integer, PingouActivity> activityMap = new HashMap<>();
        List<Integer> activityIds = teams.stream().map(PingouTeam::getActivityId).distinct().collect(Collectors.toList());
        if (CollUtil.isNotEmpty(activityIds)) {
            activityDao.selectBatchIds(activityIds).forEach(a -> activityMap.put(a.getId(), a));
        }
        List<PingouTeamListResponse> result = new ArrayList<>();
        for (PingouTeam team : teams) {
            PingouTeamListResponse response = new PingouTeamListResponse();
            response.setId(team.getId());
            response.setActivityId(team.getActivityId());
            response.setTeamNo(team.getTeamNo());
            response.setJoinNum(team.getJoinNum());
            response.setTotalNum(team.getTotalNum());
            response.setWinNum(team.getWinNum());
            response.setRemainNum(Math.max(0, team.getTotalNum() - team.getJoinNum()));
            response.setStatus(team.getStatus());
            response.setOpenTime(formatDate(team.getOpenTime()));
            response.setWinTime(formatDate(team.getWinTime()));
            PingouActivity activity = activityMap.get(team.getActivityId());
            if (ObjectUtil.isNotNull(activity)) {
                response.setActivityName(activity.getName());
                response.setProductName(activity.getProductName());
                response.setImage(activity.getImage());
            }
            if (withJoins) {
                List<PingouJoin> joins = joinDao.selectList(new LambdaQueryWrapper<PingouJoin>()
                        .eq(PingouJoin::getTeamId, team.getId())
                        .orderByAsc(PingouJoin::getId));
                List<PingouJoinUserResponse> joinUsers = new ArrayList<>();
                if (CollUtil.isNotEmpty(joins)) {
                    Map<Integer, User> userMap = new HashMap<>();
                    List<Integer> uids = joins.stream().map(PingouJoin::getUid).distinct().collect(Collectors.toList());
                    userService.listByIds(uids).forEach(u -> userMap.put(u.getUid(), u));
                    for (PingouJoin join : joins) {
                        PingouJoinUserResponse joinUser = new PingouJoinUserResponse();
                        joinUser.setId(join.getId());
                        joinUser.setUid(join.getUid());
                        joinUser.setIsWin(join.getIsWin());
                        joinUser.setPayPrice(join.getPayPrice());
                        joinUser.setCreateTime(formatDate(join.getCreateTime()));
                        User user = userMap.get(join.getUid());
                        if (ObjectUtil.isNotNull(user)) {
                            joinUser.setNickname(user.getNickname());
                            joinUser.setAvatar(user.getAvatar());
                        }
                        joinUsers.add(joinUser);
                    }
                }
                response.setJoins(joinUsers);
            }
            result.add(response);
        }
        return result;
    }

    private String formatDate(Date date) {
        return ObjectUtil.isNull(date) ? null : DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
    }
}
