package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.system.DistributorLevel;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserDistributorLevel;
import com.qxkj.common.model.user.UserDistributorLevelStat;
import com.qxkj.common.request.DistributorLevelRequest;
import com.qxkj.service.dao.DistributorLevelDao;
import com.qxkj.service.dao.LevelStatOrderLogDao;
import com.qxkj.service.dao.UserDistributorLevelDao;
import com.qxkj.service.dao.UserDistributorLevelStatDao;
import com.qxkj.service.service.DistributorLevelService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分销商等级 Service 实现
 */
@Slf4j
@Service
public class DistributorLevelServiceImpl extends ServiceImpl<DistributorLevelDao, DistributorLevel>
        implements DistributorLevelService {

    /** 幂等流水模块名 */
    private static final String MODULE = "DISTRIBUTOR";

    @Resource
    private DistributorLevelDao dao;

    @Resource
    private UserDistributorLevelStatDao statDao;

    @Resource
    private UserDistributorLevelDao recordDao;

    @Resource
    private LevelStatOrderLogDao logDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public List<DistributorLevel> getList() {
        LambdaQueryWrapper<DistributorLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(DistributorLevel::getIsDel, false);
        lqw.orderByAsc(DistributorLevel::getGrade);
        lqw.orderByAsc(DistributorLevel::getId);
        return dao.selectList(lqw);
    }

    @Override
    public DistributorLevel getLevelInfo(Integer id) {
        DistributorLevel level = getExistLevel(id);
        return level;
    }

    @Override
    public Boolean saveLevel(DistributorLevelRequest request) {
        checkNameUnique(request.getName(), null);
        checkGradeUnique(request.getGrade(), null);
        checkCondition(request);

        DistributorLevel level = buildEntity(request);
        level.setIsDel(false);
        level.setCreateTime(DateUtil.date());
        level.setUpdateTime(DateUtil.date());
        return save(level);
    }

    @Override
    public Boolean updateLevel(Integer id, DistributorLevelRequest request) {
        DistributorLevel exist = getExistLevel(id);
        checkNameUnique(request.getName(), id);
        checkGradeUnique(request.getGrade(), id);
        checkCondition(request);

        DistributorLevel level = buildEntity(request);
        level.setId(exist.getId());
        level.setIsDel(false);
        level.setCreateTime(exist.getCreateTime());
        level.setUpdateTime(DateUtil.date());
        return updateById(level);
    }

    @Override
    public Boolean deleteLevel(Integer id) {
        getExistLevel(id);
        LambdaUpdateWrapper<DistributorLevel> luw = Wrappers.lambdaUpdate();
        luw.set(DistributorLevel::getIsDel, true);
        luw.set(DistributorLevel::getUpdateTime, DateUtil.date());
        luw.eq(DistributorLevel::getId, id);
        return update(luw);
    }

    @Override
    public Boolean updateShow(Integer id, Boolean isShow) {
        getExistLevel(id);
        LambdaUpdateWrapper<DistributorLevel> luw = Wrappers.lambdaUpdate();
        luw.set(DistributorLevel::getIsShow, isShow);
        luw.set(DistributorLevel::getUpdateTime, DateUtil.date());
        luw.eq(DistributorLevel::getId, id);
        return update(luw);
    }

    /**
     * 取未删除的等级，不存在则抛业务异常
     */
    private DistributorLevel getExistLevel(Integer id) {
        if (ObjectUtil.isNull(id) || id <= 0) {
            throw new QianxuException("分销商等级不存在");
        }
        LambdaQueryWrapper<DistributorLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(DistributorLevel::getId, id);
        lqw.eq(DistributorLevel::getIsDel, false);
        lqw.last(" limit 1");
        DistributorLevel level = dao.selectOne(lqw);
        if (ObjectUtil.isNull(level)) {
            throw new QianxuException("分销商等级不存在");
        }
        return level;
    }

    /**
     * 等级名称唯一校验
     */
    private void checkNameUnique(String name, Integer excludeId) {
        LambdaQueryWrapper<DistributorLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(DistributorLevel::getName, name);
        lqw.eq(DistributorLevel::getIsDel, false);
        if (ObjectUtil.isNotNull(excludeId)) {
            lqw.ne(DistributorLevel::getId, excludeId);
        }
        if (dao.selectCount(lqw) > 0) {
            throw new QianxuException("等级名称已存在，请更换");
        }
    }

    /**
     * 等级权重唯一校验
     */
    private void checkGradeUnique(Integer grade, Integer excludeId) {
        LambdaQueryWrapper<DistributorLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(DistributorLevel::getGrade, grade);
        lqw.eq(DistributorLevel::getIsDel, false);
        if (ObjectUtil.isNotNull(excludeId)) {
            lqw.ne(DistributorLevel::getId, excludeId);
        }
        if (dao.selectCount(lqw) > 0) {
            throw new QianxuException("等级权重已存在，请更换");
        }
    }

    /**
     * 升级条件跨字段校验
     */
    private void checkCondition(DistributorLevelRequest request) {
        Integer directLevelCount = ObjectUtil.defaultIfNull(request.getDirectLevelCount(), 0);
        Integer directLevelId = ObjectUtil.defaultIfNull(request.getDirectLevelId(), 0);
        if (directLevelCount > 0 && directLevelId <= 0) {
            throw new QianxuException("直推指定等级人数大于0时，必须选择指定的分销商等级");
        }
    }

    /**
     * 请求对象转实体，统一补默认值
     */
    private DistributorLevel buildEntity(DistributorLevelRequest request) {
        DistributorLevel level = new DistributorLevel();
        level.setName(request.getName());
        level.setGrade(request.getGrade());
        level.setSelfBrokerageRate(ObjectUtil.defaultIfNull(request.getSelfBrokerageRate(), 0));
        level.setBrokerageRateOne(ObjectUtil.defaultIfNull(request.getBrokerageRateOne(), 0));
        level.setBrokerageRateTwo(ObjectUtil.defaultIfNull(request.getBrokerageRateTwo(), 0));

        level.setDirectUserCount(ObjectUtil.defaultIfNull(request.getDirectUserCount(), 0));
        level.setDirectUserRelation(ObjectUtil.defaultIfNull(request.getDirectUserRelation(), 1));
        level.setTeamUserCount(ObjectUtil.defaultIfNull(request.getTeamUserCount(), 0));
        level.setTeamUserRelation(ObjectUtil.defaultIfNull(request.getTeamUserRelation(), 1));
        level.setDirectLevelId(ObjectUtil.defaultIfNull(request.getDirectLevelId(), 0));
        level.setDirectLevelCount(ObjectUtil.defaultIfNull(request.getDirectLevelCount(), 0));
        level.setDirectLevelRelation(ObjectUtil.defaultIfNull(request.getDirectLevelRelation(), 1));

        level.setTotalConsumeAmount(ObjectUtil.defaultIfNull(request.getTotalConsumeAmount(), BigDecimal.ZERO));
        level.setTotalConsumeRelation(ObjectUtil.defaultIfNull(request.getTotalConsumeRelation(), 1));
        level.setTotalRechargeAmount(ObjectUtil.defaultIfNull(request.getTotalRechargeAmount(), BigDecimal.ZERO));
        level.setTotalRechargeRelation(ObjectUtil.defaultIfNull(request.getTotalRechargeRelation(), 1));
        level.setTeamProductAmount(ObjectUtil.defaultIfNull(request.getTeamProductAmount(), BigDecimal.ZERO));
        level.setTeamProductRelation(ObjectUtil.defaultIfNull(request.getTeamProductRelation(), 1));
        level.setDirectConsumeAmount(ObjectUtil.defaultIfNull(request.getDirectConsumeAmount(), BigDecimal.ZERO));
        level.setDirectConsumeRelation(ObjectUtil.defaultIfNull(request.getDirectConsumeRelation(), 1));
        level.setDirectUserConsumeAmount(ObjectUtil.defaultIfNull(request.getDirectUserConsumeAmount(), BigDecimal.ZERO));
        level.setDirectUserConsumeRelation(ObjectUtil.defaultIfNull(request.getDirectUserConsumeRelation(), 1));

        // 下单指定商品：ID 列表存逗号分隔字符串，空列表=未启用
        List<Integer> orderProductIds = request.getOrderProductIds() == null
                ? new ArrayList<>()
                : request.getOrderProductIds().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        level.setOrderProductIds(orderProductIds.isEmpty() ? "" : orderProductIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        level.setOrderProductRelation(ObjectUtil.defaultIfNull(request.getOrderProductRelation(), 1));
        level.setOrderProductMode(ObjectUtil.defaultIfNull(request.getOrderProductMode(), 1));

        level.setIsShow(ObjectUtil.defaultIfNull(request.getIsShow(), true));
        return level;
    }

    // ==================== 统计与自动升级 ====================

    @Override
    public List<DistributorLevel> getUsableList() {
        LambdaQueryWrapper<DistributorLevel> lqw = Wrappers.lambdaQuery();
        lqw.eq(DistributorLevel::getIsDel, false);
        lqw.orderByAsc(DistributorLevel::getGrade);
        lqw.orderByAsc(DistributorLevel::getId);
        return dao.selectList(lqw);
    }

    @Override
    public Boolean processOnOrderPaid(StoreOrder storeOrder) {
        if (!isEnabled() || ObjectUtil.isNull(storeOrder) || !Boolean.TRUE.equals(storeOrder.getPaid())) {
            return Boolean.TRUE;
        }
        BigDecimal amount = ObjectUtil.defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Boolean.TRUE;
        }
        final BigDecimal finalAmount = amount;
        return transactionTemplate.execute(e -> {
            if (!claimOnce(storeOrder.getOrderId(), "PAID")) {
                return Boolean.TRUE;
            }
            List<Integer> affected = applyStats(storeOrder.getUid(), finalAmount);
            syncDistributorLevels(affected);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean processOnOrderComplete(StoreOrder storeOrder) {
        if (!isEnabled() || ObjectUtil.isNull(storeOrder)) {
            return Boolean.TRUE;
        }
        if (!Integer.valueOf(3).equals(storeOrder.getStatus())) {
            return Boolean.TRUE;
        }
        // 统计口径统一为「支付成功」，完成时不再累加金额，仅重试一次升级判定
        return transactionTemplate.execute(e -> {
            if (!claimOnce(storeOrder.getOrderId(), "COMPLETE")) {
                return Boolean.TRUE;
            }
            List<Integer> affected = collectAffectedUids(storeOrder.getUid());
            syncDistributorLevels(affected);
            return Boolean.TRUE;
        });
    }

    @Override
    public Boolean rollbackOnRefund(StoreOrder storeOrder) {
        if (!isEnabled() || ObjectUtil.isNull(storeOrder)) {
            return Boolean.TRUE;
        }
        if (!Integer.valueOf(2).equals(storeOrder.getRefundStatus())) {
            return Boolean.TRUE;
        }
        BigDecimal amount = ObjectUtil.defaultIfNull(storeOrder.getPayPrice(), BigDecimal.ZERO);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Boolean.TRUE;
        }
        final BigDecimal rollbackAmount = amount.negate();
        return transactionTemplate.execute(e -> {
            if (!claimOnce(storeOrder.getOrderId(), "REFUND")) {
                return Boolean.TRUE;
            }
            List<Integer> affected = applyStats(storeOrder.getUid(), rollbackAmount);
            // 只升不降：退款只回退统计，不降级（与团队等级策略一致）
            syncDistributorLevels(affected);
            return Boolean.TRUE;
        });
    }

    @Override
    public DistributorLevel resolveMatchedLevel(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        List<DistributorLevel> list = getUsableList();
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        UserDistributorLevelStat stat = getOrInitStat(user.getUid());
        return list.stream()
                .filter(level -> meetsCondition(level, stat))
                .max((a, b) -> ObjectUtil.defaultIfNull(a.getGrade(), 0) - ObjectUtil.defaultIfNull(b.getGrade(), 0))
                .orElse(null);
    }

    @Override
    public Boolean syncDistributorLevels(List<Integer> uids) {
        if (CollUtil.isEmpty(uids)) {
            return Boolean.TRUE;
        }
        for (Integer uid : uids.stream().distinct().collect(Collectors.toList())) {
            User user = userService.getById(uid);
            if (ObjectUtil.isNull(user)) {
                continue;
            }
            DistributorLevel matched = resolveMatchedLevel(user);
            Integer matchedLevelId = ObjectUtil.isNull(matched) ? 0 : matched.getId();
            Integer currentLevelId = ObjectUtil.defaultIfNull(user.getDistributorLevelId(), 0);
            if (currentLevelId.equals(matchedLevelId)) {
                continue;
            }
            // 只升不降：订单流程不得清空或降低已有等级，避免覆盖管理员手动设置
            int currentGrade = 0;
            if (currentLevelId > 0) {
                DistributorLevel currentLevel = dao.selectById(currentLevelId);
                currentGrade = ObjectUtil.isNull(currentLevel) ? 0 : ObjectUtil.defaultIfNull(currentLevel.getGrade(), 0);
            }
            int matchedGrade = ObjectUtil.isNull(matched) ? 0 : ObjectUtil.defaultIfNull(matched.getGrade(), 0);
            if (matchedGrade <= currentGrade) {
                continue;
            }
            User updateUser = new User();
            updateUser.setUid(uid);
            updateUser.setDistributorLevelId(matchedLevelId);
            updateUser.setUpdateTime(DateUtil.date());
            userService.updateById(updateUser);

            UserDistributorLevel record = new UserDistributorLevel();
            record.setUid(uid);
            record.setLevelId(matchedLevelId);
            record.setGrade(matchedGrade);
            record.setStatus(true);
            record.setIsDel(false);
            record.setMark(ObjectUtil.isNull(matched) ? "分销商等级清空" : "分销商等级升级：" + matched.getName());
            record.setCreateTime(DateUtil.date());
            record.setUpdateTime(DateUtil.date());
            recordDao.insert(record);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean recalcUser(Integer uid) {
        if (ObjectUtil.isNull(uid) || uid <= 0) {
            throw new QianxuException("参数错误");
        }
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            throw new QianxuException("用户不存在");
        }
        return transactionTemplate.execute(e -> {
            UserDistributorLevelStat stat = getOrInitStat(uid);
            stat.setTotalConsumeAmount(statDao.sumSelfOrderAmount(uid));
            stat.setDirectConsumeAmount(statDao.sumDirectOrderAmount(uid));
            stat.setDirectUserConsumeAmount(statDao.sumDirectUserOrderAmount(uid));
            stat.setTeamProductAmount(statDao.sumTeamOrderAmount(uid));
            stat.setUpdateTime(DateUtil.date());
            statDao.updateById(stat);
            syncDistributorLevels(CollUtil.newArrayList(uid));
            return Boolean.TRUE;
        });
    }

    @Override
    public Integer getBrokerageRate(Integer uid, Integer brokerageLevel) {
        // 默认关闭：保持原有按会员等级取返佣的行为不变
        if (!"1".equals(systemConfigService.getValueByKey("distributor_level_brokerage_enabled"))) {
            return null;
        }
        if (ObjectUtil.isNull(uid) || uid <= 0 || ObjectUtil.isNull(brokerageLevel)) {
            return null;
        }
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        Integer levelId = ObjectUtil.defaultIfNull(user.getDistributorLevelId(), 0);
        if (levelId <= 0) {
            return null;
        }
        DistributorLevel level = dao.selectById(levelId);
        if (ObjectUtil.isNull(level) || Boolean.TRUE.equals(level.getIsDel())) {
            return null;
        }
        if (Integer.valueOf(0).equals(brokerageLevel)) {
            return ObjectUtil.defaultIfNull(level.getSelfBrokerageRate(), 0);
        }
        if (Integer.valueOf(1).equals(brokerageLevel)) {
            return ObjectUtil.defaultIfNull(level.getBrokerageRateOne(), 0);
        }
        if (Integer.valueOf(2).equals(brokerageLevel)) {
            return ObjectUtil.defaultIfNull(level.getBrokerageRateTwo(), 0);
        }
        return null;
    }

    // ==================== 私有方法 ====================

    /**
     * 本次统计是否已处理过。首次处理返回 true 并落一条流水（事务回滚则流水一并回滚）。
     */
    private boolean claimOnce(String orderNo, String scene) {
        if (StrUtil.isBlank(orderNo)) {
            // 无单号无法去重，按未处理放行（保持原行为）
            return true;
        }
        return logDao.insertIgnore(orderNo, MODULE, scene) > 0;
    }

    /**
     * 累加统计：本人消费额 / 直推消费额 / 直推会员消费额 / 团队消费额。
     * delta 为负即回退，字段下限 0。
     */
    private List<Integer> applyStats(Integer buyerUid, BigDecimal delta) {
        Set<Integer> affected = new HashSet<>();
        if (ObjectUtil.isNull(buyerUid) || buyerUid <= 0) {
            return new ArrayList<>(affected);
        }
        affected.add(buyerUid);
        getOrInitStat(buyerUid);
        statDao.incrColumn(buyerUid, "total_consume_amount", delta);

        User buyer = userService.getById(buyerUid);
        if (ObjectUtil.isNull(buyer) || ObjectUtil.defaultIfNull(buyer.getSpreadUid(), 0) <= 0) {
            return new ArrayList<>(affected);
        }
        Integer directUid = buyer.getSpreadUid();
        affected.add(directUid);
        getOrInitStat(directUid);
        statDao.incrColumn(directUid, "direct_consume_amount", delta);
        if (ObjectUtil.defaultIfNull(buyer.getLevel(), 0) > 0) {
            statDao.incrColumn(directUid, "direct_user_consume_amount", delta);
        }

        // 团队：沿推荐链向上，受最大层数限制
        Integer currentUid = directUid;
        Set<Integer> visited = new HashSet<>();
        int maxDepth = getMaxDepth();
        int depth = 0;
        while (ObjectUtil.isNotNull(currentUid) && currentUid > 0) {
            if (!visited.add(currentUid)) {
                break;
            }
            depth++;
            if (maxDepth > 0 && depth > maxDepth) {
                break;
            }
            getOrInitStat(currentUid);
            statDao.incrColumn(currentUid, "team_product_amount", delta);
            User parent = userService.getById(currentUid);
            if (ObjectUtil.isNull(parent)) {
                break;
            }
            currentUid = parent.getSpreadUid();
        }
        affected.addAll(visited);
        return new ArrayList<>(affected);
    }

    /**
     * 只取受影响的 uid 链（不累加金额），用于订单完成时的补充判定
     */
    private List<Integer> collectAffectedUids(Integer buyerUid) {
        Set<Integer> affected = new HashSet<>();
        if (ObjectUtil.isNull(buyerUid) || buyerUid <= 0) {
            return new ArrayList<>(affected);
        }
        affected.add(buyerUid);
        Integer currentUid = buyerUid;
        Set<Integer> visited = new HashSet<>();
        int maxDepth = getMaxDepth();
        int depth = 0;
        while (true) {
            User user = userService.getById(currentUid);
            if (ObjectUtil.isNull(user) || ObjectUtil.defaultIfNull(user.getSpreadUid(), 0) <= 0) {
                break;
            }
            currentUid = user.getSpreadUid();
            if (!visited.add(currentUid)) {
                break;
            }
            depth++;
            if (maxDepth > 0 && depth > maxDepth) {
                break;
            }
            affected.add(currentUid);
        }
        return new ArrayList<>(affected);
    }

    /**
     * 判断用户是否满足该分销商等级的升级条件。
     *
     * <p>八个条件按展示顺序链式组合：
     * <pre>直推会员人数 [directUserRelation] 团队会员人数 [teamUserRelation] 直推指定等级
     * [directLevelRelation] 累计商城总消费额 [totalConsumeRelation] 总充值额 [totalRechargeRelation]
     * 团队商品总消费额 [teamProductRelation] 直推商城消费总额 [directConsumeRelation] 直推会员商城消费总额</pre>
     * 关系值 1=与 2=或（该行条件与下一条件之间的连接）。门槛为 0 的条件视为自动满足。
     * 最后一个条件的 directUserConsumeRelation 不参与组合（其后无条件）。
     * 全部条件门槛均为 0 的等级不参与自动升级（视为未配置，需管理员手动指定）。
     */
    private boolean meetsCondition(DistributorLevel level, UserDistributorLevelStat stat) {
        if (ObjectUtil.isNull(level) || ObjectUtil.isNull(stat)) {
            return false;
        }
        Integer uid = stat.getUid();

        int directUserThreshold = ObjectUtil.defaultIfNull(level.getDirectUserCount(), 0);
        int teamUserThreshold = ObjectUtil.defaultIfNull(level.getTeamUserCount(), 0);
        Integer directLevelId = ObjectUtil.defaultIfNull(level.getDirectLevelId(), 0);
        int directLevelThreshold = ObjectUtil.defaultIfNull(level.getDirectLevelCount(), 0);
        BigDecimal totalConsumeThreshold = ObjectUtil.defaultIfNull(level.getTotalConsumeAmount(), BigDecimal.ZERO);
        BigDecimal totalRechargeThreshold = ObjectUtil.defaultIfNull(level.getTotalRechargeAmount(), BigDecimal.ZERO);
        BigDecimal teamProductThreshold = ObjectUtil.defaultIfNull(level.getTeamProductAmount(), BigDecimal.ZERO);
        BigDecimal directConsumeThreshold = ObjectUtil.defaultIfNull(level.getDirectConsumeAmount(), BigDecimal.ZERO);
        // 下单指定商品：解析逗号分隔的商品ID
        List<Integer> orderProductIds = parseOrderProductIds(level.getOrderProductIds());

        // 全部条件门槛为空：视为未配置，不参与自动升级
        boolean noCondition = directUserThreshold <= 0 && teamUserThreshold <= 0
                && (directLevelId <= 0 || directLevelThreshold <= 0)
                && totalConsumeThreshold.compareTo(BigDecimal.ZERO) <= 0
                && totalRechargeThreshold.compareTo(BigDecimal.ZERO) <= 0
                && teamProductThreshold.compareTo(BigDecimal.ZERO) <= 0
                && directConsumeThreshold.compareTo(BigDecimal.ZERO) <= 0
                && orderProductIds.isEmpty();
        if (noCondition) {
            return false;
        }

        boolean c1 = directUserThreshold <= 0 || statDao.countDirectUsers(uid) >= directUserThreshold;
        boolean c2 = teamUserThreshold <= 0 || statDao.countTeamUsers(uid) >= teamUserThreshold;
        boolean c3 = directLevelId <= 0 || directLevelThreshold <= 0
                || statDao.countDirectLevelUsers(uid, directLevelId) >= directLevelThreshold;
        boolean c4 = totalConsumeThreshold.compareTo(BigDecimal.ZERO) <= 0
                || ObjectUtil.defaultIfNull(stat.getTotalConsumeAmount(), BigDecimal.ZERO).compareTo(totalConsumeThreshold) >= 0;
        boolean c5 = totalRechargeThreshold.compareTo(BigDecimal.ZERO) <= 0
                || ObjectUtil.defaultIfNull(statDao.sumRechargeAmount(uid), BigDecimal.ZERO).compareTo(totalRechargeThreshold) >= 0;
        boolean c6 = teamProductThreshold.compareTo(BigDecimal.ZERO) <= 0
                || ObjectUtil.defaultIfNull(stat.getTeamProductAmount(), BigDecimal.ZERO).compareTo(teamProductThreshold) >= 0;
        boolean c7 = directConsumeThreshold.compareTo(BigDecimal.ZERO) <= 0
                || ObjectUtil.defaultIfNull(stat.getDirectConsumeAmount(), BigDecimal.ZERO).compareTo(directConsumeThreshold) >= 0;
        // 下单指定商品：本人已支付订单中包含任一选中商品即满足
        boolean c8;
        if (orderProductIds.isEmpty()) {
            // 未选择商品，该条件视为自动满足
            c8 = true;
        } else if (ObjectUtil.defaultIfNull(level.getOrderProductMode(), 1) == 2) {
            // 需全部购买：命中的指定商品数需覆盖全部
            c8 = statDao.countSelfPaidProductOrders(uid, orderProductIds) >= orderProductIds.size();
        } else {
            // 任买一件即可
            c8 = statDao.countSelfPaidProductOrders(uid, orderProductIds) > 0;
        }

        boolean result = c1;
        result = combine(level.getDirectUserRelation(), result, c2);
        result = combine(level.getTeamUserRelation(), result, c3);
        result = combine(level.getDirectLevelRelation(), result, c4);
        result = combine(level.getTotalConsumeRelation(), result, c5);
        result = combine(level.getTotalRechargeRelation(), result, c6);
        result = combine(level.getTeamProductRelation(), result, c7);
        return combine(level.getOrderProductRelation(), result, c8);
    }

    /**
     * 解析「下单指定商品」逗号分隔的商品ID，空/非法返回空列表
     */
    private List<Integer> parseOrderProductIds(String orderProductIds) {
        if (StrUtil.isBlank(orderProductIds)) {
            return new ArrayList<>();
        }
        return Arrays.stream(orderProductIds.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(s -> {
                    try {
                        return Integer.valueOf(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 1=与，2=或，空或其他按「与」
     */
    private boolean combine(Integer relation, boolean left, boolean right) {
        if (ObjectUtil.defaultIfNull(relation, 1) == 2) {
            return left || right;
        }
        return left && right;
    }

    private UserDistributorLevelStat getOrInitStat(Integer uid) {
        LambdaQueryWrapper<UserDistributorLevelStat> lqw = Wrappers.lambdaQuery();
        lqw.eq(UserDistributorLevelStat::getUid, uid);
        lqw.last(" limit 1");
        UserDistributorLevelStat stat = statDao.selectOne(lqw);
        if (ObjectUtil.isNotNull(stat)) {
            stat.setTotalConsumeAmount(ObjectUtil.defaultIfNull(stat.getTotalConsumeAmount(), BigDecimal.ZERO));
            stat.setDirectConsumeAmount(ObjectUtil.defaultIfNull(stat.getDirectConsumeAmount(), BigDecimal.ZERO));
            stat.setDirectUserConsumeAmount(ObjectUtil.defaultIfNull(stat.getDirectUserConsumeAmount(), BigDecimal.ZERO));
            stat.setTeamProductAmount(ObjectUtil.defaultIfNull(stat.getTeamProductAmount(), BigDecimal.ZERO));
            return stat;
        }
        UserDistributorLevelStat init = new UserDistributorLevelStat();
        init.setUid(uid);
        init.setTotalConsumeAmount(BigDecimal.ZERO);
        init.setDirectConsumeAmount(BigDecimal.ZERO);
        init.setDirectUserConsumeAmount(BigDecimal.ZERO);
        init.setTeamProductAmount(BigDecimal.ZERO);
        init.setCreateTime(DateUtil.date());
        init.setUpdateTime(DateUtil.date());
        statDao.insert(init);
        return init;
    }

    private int getMaxDepth() {
        String str = systemConfigService.getValueByKey("distributor_level_max_depth");
        try {
            int v = Integer.parseInt(ObjectUtil.defaultIfNull(str, "0"));
            return v <= 0 ? 0 : Math.min(v, 200);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean isEnabled() {
        return !"0".equals(systemConfigService.getValueByKey("distributor_level_enabled"));
    }
}
