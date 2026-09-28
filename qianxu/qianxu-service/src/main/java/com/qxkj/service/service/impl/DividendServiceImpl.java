package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.BrokerageRecordConstants;
import com.qxkj.common.model.dividend.DividendDetail;
import com.qxkj.common.model.dividend.DividendLevel;
import com.qxkj.common.model.dividend.DividendRecord;
import com.qxkj.common.model.system.DistributorLevel;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBill;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.dao.DividendDetailDao;
import com.qxkj.service.dao.DividendLevelDao;
import com.qxkj.service.dao.DividendRecordDao;
import com.qxkj.service.service.DistributorLevelService;
import com.qxkj.service.service.UserBillService;
import com.qxkj.service.service.UserBrokerageRecordService;
import com.qxkj.service.service.DividendService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全球分红服务实现
 */
@Service
public class DividendServiceImpl extends ServiceImpl<DividendRecordDao, DividendRecord> implements DividendService {

    private static final Logger logger = LoggerFactory.getLogger(DividendServiceImpl.class);

    public static final String CONFIG_STATUS = "sys_dividend_status";
    public static final String CONFIG_CYCLE = "sys_dividend_cycle";
    public static final String CONFIG_BASE = "sys_dividend_base";

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter SQL_DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DividendRecordDao recordDao;

    @Autowired
    private DividendDetailDao detailDao;

    @Autowired
    private DividendLevelDao dividendLevelDao;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private DistributorLevelService distributorLevelService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;

    @Autowired
    private UserBillService userBillService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "1".equals(StrUtil.trimToEmpty(systemConfigService.getValueByKey(CONFIG_STATUS))));
        map.put("cycle", parseConfigInt(CONFIG_CYCLE, 1));
        map.put("base", parseConfigInt(CONFIG_BASE, 1));
        // 分红等级（比例与升级条件独立维护），附完整条件字段供编辑回显
        map.put("levels", levelList());
        // 可参考的分销商等级（前端「引用分销商等级条件」用）
        map.put("distributorLevels", distributorLevelService.getList());
        return map;
    }

    @Override
    public Boolean saveConfig(Boolean status, Integer cycle, Integer base) {
        if (cycle == null || cycle < 1 || cycle > 3) {
            throw new QianxuException("请选择正确的分红周期");
        }
        if (base == null || base < 1 || base > 2) {
            throw new QianxuException("请选择正确的分红基数");
        }
        systemConfigService.updateOrSaveValueByName(CONFIG_STATUS, Boolean.TRUE.equals(status) ? "1" : "0");
        systemConfigService.updateOrSaveValueByName(CONFIG_CYCLE, String.valueOf(cycle));
        systemConfigService.updateOrSaveValueByName(CONFIG_BASE, String.valueOf(base));
        return Boolean.TRUE;
    }

    @Override
    public List<DividendLevel> levelList() {
        return dividendLevelDao.selectList(Wrappers.<DividendLevel>lambdaQuery()
                .eq(DividendLevel::getIsDel, false)
                .orderByAsc(DividendLevel::getSort)
                .orderByAsc(DividendLevel::getId));
    }

    @Override
    public Boolean saveLevel(DividendLevel level) {
        validateLevel(level);
        level.setId(null);
        level.setIsDel(false);
        if (level.getSort() == null) {
            level.setSort(0);
        }
        level.setCreateTime(new Date());
        level.setUpdateTime(new Date());
        return dividendLevelDao.insert(level) > 0;
    }

    @Override
    public Boolean updateLevel(DividendLevel level) {
        if (level.getId() == null) {
            throw new QianxuException("缺少分红级别ID");
        }
        DividendLevel exists = dividendLevelDao.selectById(level.getId());
        if (ObjectUtil.isNull(exists) || Boolean.TRUE.equals(exists.getIsDel())) {
            throw new QianxuException("分红级别不存在");
        }
        validateLevel(level);
        level.setUpdateTime(new Date());
        return dividendLevelDao.updateById(level) > 0;
    }

    @Override
    public Boolean deleteLevel(Integer id) {
        if (id == null) {
            throw new QianxuException("缺少分红级别ID");
        }
        DividendLevel exists = dividendLevelDao.selectById(id);
        if (ObjectUtil.isNull(exists) || Boolean.TRUE.equals(exists.getIsDel())) {
            throw new QianxuException("分红级别不存在");
        }
        exists.setIsDel(true);
        exists.setUpdateTime(new Date());
        return dividendLevelDao.updateById(exists) > 0;
    }

    /**
     * 分红等级校验：名称、比例范围（升级条件可全部为空=条件未配置，不参与自动达标）
     */
    private void validateLevel(DividendLevel level) {
        if (level == null || StrUtil.isBlank(level.getName())) {
            throw new QianxuException("请填写分红等级名称");
        }
        if (level.getDividendRate() == null || level.getDividendRate().compareTo(BigDecimal.ZERO) < 0
                || level.getDividendRate().compareTo(new BigDecimal("100")) > 0) {
            throw new QianxuException("分红比例须在 0~100 之间");
        }
    }

    /**
     * 把分红等级的升级条件字段复制为条件判定载体（复用分销商等级那套判定逻辑）
     */
    private DistributorLevel toConditionHolder(DividendLevel level) {
        return new DistributorLevel()
                .setDirectUserCount(level.getDirectUserCount())
                .setDirectUserRelation(level.getDirectUserRelation())
                .setTeamUserCount(level.getTeamUserCount())
                .setTeamUserRelation(level.getTeamUserRelation())
                .setDirectLevelId(level.getDirectLevelId())
                .setDirectLevelCount(level.getDirectLevelCount())
                .setDirectLevelRelation(level.getDirectLevelRelation())
                .setTotalConsumeAmount(level.getTotalConsumeAmount())
                .setTotalConsumeRelation(level.getTotalConsumeRelation())
                .setTotalRechargeAmount(level.getTotalRechargeAmount())
                .setTotalRechargeRelation(level.getTotalRechargeRelation())
                .setTeamProductAmount(level.getTeamProductAmount())
                .setTeamProductRelation(level.getTeamProductRelation())
                .setDirectConsumeAmount(level.getDirectConsumeAmount())
                .setDirectConsumeRelation(level.getDirectConsumeRelation())
                .setOrderProductIds(level.getOrderProductIds())
                .setOrderProductRelation(level.getOrderProductRelation())
                .setOrderProductMode(level.getOrderProductMode());
    }

    @Override
    public com.github.pagehelper.PageInfo<DividendRecord> recordList(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<DividendRecord> list = recordDao.selectList(Wrappers.<DividendRecord>lambdaQuery()
                .orderByDesc(DividendRecord::getId));
        return new com.github.pagehelper.PageInfo<>(list);
    }

    @Override
    public com.github.pagehelper.PageInfo<DividendDetail> detailList(Integer recordId, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<DividendDetail> lqw = Wrappers.lambdaQuery();
        if (recordId != null) {
            lqw.eq(DividendDetail::getRecordId, recordId);
        }
        lqw.orderByDesc(DividendDetail::getAmount);
        List<DividendDetail> list = detailDao.selectList(lqw);
        return new com.github.pagehelper.PageInfo<>(list);
    }

    @Override
    public void dividendCheck() {
        String statusStr = StrUtil.trimToEmpty(systemConfigService.getValueByKey(CONFIG_STATUS));
        if (!"1".equals(statusStr)) {
            return;
        }
        int cycle = parseConfigInt(CONFIG_CYCLE, 1);
        LocalDateTime now = LocalDateTime.now();
        boolean hit;
        if (cycle == 2) {
            // 周分红：每周周一凌晨 00:01
            hit = now.getDayOfWeek() == DayOfWeek.MONDAY && now.getHour() == 0 && now.getMinute() == 1;
        } else if (cycle == 3) {
            // 月分红：每月 1 号凌晨 01:00
            hit = now.getDayOfMonth() == 1 && now.getHour() == 1 && now.getMinute() == 0;
        } else {
            // 日分红：每天 00:01
            hit = now.getHour() == 0 && now.getMinute() == 1;
        }
        if (hit) {
            try {
                settleLastPeriod(cycle, null);
            } catch (Exception e) {
                logger.error("DividendCheckTask | 分红结算失败 cycle={} | {}", cycle, e.getMessage(), e);
            }
        }
    }

    @Override
    public Integer settleLastPeriod(Integer cycleType, String mark) {
        int cycle = cycleType == null ? parseConfigInt(CONFIG_CYCLE, 1) : cycleType;
        int base = parseConfigInt(CONFIG_BASE, 1);
        LocalDateTime now = LocalDateTime.now();

        // 结算对象 = 刚结束的上一周期
        LocalDate today = now.toLocalDate();
        LocalDate start;
        LocalDate end;
        String periodNo;
        switch (cycle) {
            case 2:
                LocalDate lastMonday = today.with(DayOfWeek.MONDAY).minusWeeks(1);
                start = lastMonday;
                end = today.with(DayOfWeek.MONDAY);
                int week = lastMonday.get(WeekFields.ISO.weekOfWeekBasedYear());
                int weekYear = lastMonday.get(WeekFields.ISO.weekBasedYear());
                periodNo = "W" + weekYear + "-W" + week;
                break;
            case 3:
                LocalDate firstDayOfThisMonth = today.withDayOfMonth(1);
                start = firstDayOfThisMonth.minusMonths(1);
                end = firstDayOfThisMonth;
                periodNo = "M" + start.format(DateTimeFormatter.ofPattern("yyyyMM"));
                break;
            case 1:
            default:
                start = today.minusDays(1);
                end = today;
                periodNo = "D" + start.format(DAY_FMT);
                break;
        }

        // 幂等：该期已结算直接返回
        Integer existed = recordDao.selectCount(new LambdaQueryWrapper<DividendRecord>()
                .eq(DividendRecord::getPeriodNo, periodNo));
        if (existed != null && existed > 0) {
            return 0;
        }

        return transactionTemplate.execute(e ->
                settlePeriod(periodNo, cycle, base, start.atStartOfDay(), end.atStartOfDay(), mark));
    }

    /**
     * 结算指定区间（事务内）
     */
    private Integer settlePeriod(String periodNo, int cycle, int base, LocalDateTime start, LocalDateTime end, String mark) {
        // 二次幂等校验（事务内防并发）
        Integer existed = recordDao.selectCount(new LambdaQueryWrapper<DividendRecord>()
                .eq(DividendRecord::getPeriodNo, periodNo));
        if (existed != null && existed > 0) {
            return 0;
        }

        // 1. 统计分红基数（订单口径：已支付、未删除、支付时间在期内）
        Map<String, Object> stat = base == 2
                ? detailDao.statProfit(start.format(SQL_DATETIME_FMT), end.format(SQL_DATETIME_FMT))
                : detailDao.statPerformance(start.format(SQL_DATETIME_FMT), end.format(SQL_DATETIME_FMT));
        BigDecimal baseAmount = new BigDecimal(String.valueOf(stat.getOrDefault("baseAmount", "0")));
        int orderCount = Integer.parseInt(String.valueOf(stat.getOrDefault("orderCount", "0")));

        DividendRecord record = new DividendRecord();
        record.setPeriodNo(periodNo);
        record.setCycleType(cycle);
        record.setBaseType(base);
        record.setBaseAmount(baseAmount);
        record.setOrderCount(orderCount);
        record.setTotalPaid(BigDecimal.ZERO);
        record.setMemberCount(0);
        record.setCreateTime(new Date());

        BigDecimal totalPaid = BigDecimal.ZERO;
        int memberCount = 0;
        List<DividendDetail> details = new ArrayList<>();
        StringBuilder markBuilder = new StringBuilder(StrUtil.nullToEmpty(mark));

        if (baseAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 2. 分红等级：比例与升级条件独立维护，达标判定复用分销商等级那套条件逻辑
            //    参与者 = 满足该等级自身升级条件的会员（与分销商等级本身无关）
            List<DividendLevel> levels = levelList();
            List<User> candidates = userService.list(Wrappers.<User>lambdaQuery()
                    .select(User::getUid, User::getDividendLevelId)
                    .eq(User::getStatus, true)
                    .eq(User::getIsLogoff, false));
            List<Integer> candidateUids = new ArrayList<>();
            // 人工指定的分红等级：uid -> levelId（只收 >0 的），指定后不再参与其它等级
            Map<Integer, Integer> assignedMap = new HashMap<>();
            for (User u : candidates) {
                candidateUids.add(u.getUid());
                if (u.getDividendLevelId() != null && u.getDividendLevelId() > 0) {
                    assignedMap.put(u.getUid(), u.getDividendLevelId());
                }
            }

            for (DividendLevel level : levels) {
                BigDecimal rate = level.getDividendRate();
                if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                DistributorLevel conditionHolder = toConditionHolder(level);
                List<Integer> uids = new ArrayList<>();
                for (Integer uid : candidateUids) {
                    Integer assigned = assignedMap.get(uid);
                    if (assigned != null) {
                        // 人工指定优先：只在该等级参与分红，不再按条件判定进其它等级
                        if (assigned.equals(level.getId())) {
                            uids.add(uid);
                        }
                        continue;
                    }
                    if (distributorLevelService.meetsCondition(conditionHolder, uid)) {
                        uids.add(uid);
                    }
                }
                if (uids.isEmpty()) {
                    continue;
                }
                // 本级分红总额 = 基数 × 比例%，均分到人（金额向下保留 2 位）
                BigDecimal pool = baseAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
                BigDecimal per = pool.divide(new BigDecimal(uids.size()), 2, RoundingMode.DOWN);
                if (per.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal paidThisLevel = per.multiply(new BigDecimal(uids.size()));
                markBuilder.append(String.format("[%s] %d人×%.2f元(基数%.2f×%.3f%%); ",
                        level.getName(), uids.size(), per, baseAmount, rate));
                for (Integer uid : uids) {
                    grantDividend(uid, per, level, periodNo);
                    details.add(new DividendDetail()
                            .setPeriodNo(periodNo)
                            .setUid(uid)
                            .setLevelId(level.getId())
                            .setLevelName(level.getName())
                            .setRate(rate)
                            .setAmount(per)
                            .setCreateTime(new Date()));
                }
                totalPaid = totalPaid.add(paidThisLevel);
                memberCount += uids.size();
            }
        }

        record.setTotalPaid(totalPaid);
        record.setMemberCount(memberCount);
        record.setMark(markBuilder.toString());
        recordDao.insert(record);
        record = recordDao.selectOne(new LambdaQueryWrapper<DividendRecord>()
                .eq(DividendRecord::getPeriodNo, periodNo));
        if (CollUtil.isNotEmpty(details)) {
            for (DividendDetail detail : details) {
                detail.setRecordId(record.getId());
            }
            // 逐条插入（唯一键 uk_period_uid 兜底防重）
            for (DividendDetail detail : details) {
                detailDao.insert(detail);
            }
        }
        return record.getId();
    }

    /**
     * 发放个人分红：入佣金账户 + 佣金流水 + 资金明细
     */
    private void grantDividend(Integer uid, BigDecimal price, DividendLevel level, String periodNo) {
        User user = userService.getById(uid);
        if (ObjectUtil.isNull(user)) {
            throw new QianxuException("分红发放失败，用户不存在，uid=" + uid);
        }
        Boolean addOk = userService.operationBrokerage(uid, price,
                ObjectUtil.defaultIfNull(user.getBrokeragePrice(), BigDecimal.ZERO), "add");
        if (!Boolean.TRUE.equals(addOk)) {
            throw new QianxuException("分红发放失败，uid=" + uid);
        }
        UserBrokerageRecord record = new UserBrokerageRecord();
        record.setUid(uid);
        record.setLinkId(periodNo);
        record.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        record.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        record.setTitle("全球分红");
        record.setPrice(price);
        record.setBalance(ObjectUtil.defaultIfNull(user.getBrokeragePrice(), BigDecimal.ZERO).add(price));
        record.setMark("[" + level.getName() + "] " + periodNo + " 分红");
        record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
        record.setFrozenTime(0);
        record.setCreateTime(QianxuDateUtil.nowDateTime());
        userBrokerageRecordService.save(record);

        UserBill bill = new UserBill();
        bill.setUid(uid);
        bill.setLinkId(periodNo);
        bill.setTitle("全球分红");
        bill.setCategory(Constants.USER_BILL_CATEGORY_BROKERAGE_PRICE);
        bill.setType(Constants.USER_BILL_TYPE_BROKERAGE);
        bill.setNumber(price);
        bill.setBalance(ObjectUtil.defaultIfNull(user.getBrokeragePrice(), BigDecimal.ZERO).add(price));
        bill.setMark("[" + level.getName() + "] " + periodNo + " 分红");
        bill.setPm(1);
        userBillService.save(bill);
    }

    private int parseConfigInt(String key, int defVal) {
        String val = StrUtil.trimToEmpty(systemConfigService.getValueByKey(key));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defVal;
        }
    }
}
