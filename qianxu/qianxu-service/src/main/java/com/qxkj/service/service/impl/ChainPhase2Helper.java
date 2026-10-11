package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.constants.BrokerageRecordConstants;
import com.qxkj.common.constants.ChainConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.chain.*;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.service.dao.ChainCommissionDao;
import com.qxkj.service.dao.ChainDividendRecordDao;
import com.qxkj.service.dao.ChainNotifyLogDao;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserBrokerageRecordService;
import com.qxkj.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 链动辅助：站内通知 / 团队分红 / 看板增强。
 * <p>
 * 说明：原「风控日志 + 6 项风控阈值」已按需求文档移除（文档将风控定义为写死的硬约束，不做可配项）。
 */
@Slf4j
@Component
public class ChainPhase2Helper {

    @Autowired
    private SystemConfigService systemConfigService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserBrokerageRecordService userBrokerageRecordService;
    @Autowired
    private ChainNotifyLogDao chainNotifyLogDao;
    @Autowired
    private ChainDividendRecordDao chainDividendRecordDao;
    @Autowired
    private ChainCommissionDao chainCommissionDao;
    @Autowired
    private com.qxkj.service.service.StoreOrderService storeOrderService;

    public void putPhase2Config(Map<String, Object> map) {
        map.put(ChainConstants.CONFIG_NOTIFY_STATUS, cfg(ChainConstants.CONFIG_NOTIFY_STATUS, "1"));
        map.put(ChainConstants.CONFIG_DIVIDEND_STATUS, cfg(ChainConstants.CONFIG_DIVIDEND_STATUS, "0"));
        map.put(ChainConstants.CONFIG_DIVIDEND_RATIO, cfg(ChainConstants.CONFIG_DIVIDEND_RATIO, "5"));
        map.put(ChainConstants.CONFIG_DIVIDEND_MIN_TEAM, cfg(ChainConstants.CONFIG_DIVIDEND_MIN_TEAM, "2"));
    }

    public void enrichDashboard(Map<String, Object> map) {
        Date today = DateUtil.beginOfDay(DateUtil.date());
        BigDecimal todayAmt = BigDecimal.ZERO;
        List<ChainCommission> todayList = chainCommissionDao.selectList(Wrappers.<ChainCommission>lambdaQuery()
                .ge(ChainCommission::getCreateTime, today)
                .ne(ChainCommission::getStatus, ChainConstants.COMMISSION_INVALID));
        for (ChainCommission c : todayList) {
            todayAmt = todayAmt.add(ObjectUtil.defaultIfNull(c.getAmount(), BigDecimal.ZERO));
        }
        map.put("todayCommission", todayAmt);
        map.put("clawbackCount", chainCommissionDao.selectCount(Wrappers.<ChainCommission>lambdaQuery()
                .eq(ChainCommission::getStatus, ChainConstants.COMMISSION_CLAWBACK)));

        // 近7日趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Date day = DateUtil.beginOfDay(DateUtil.offsetDay(DateUtil.date(), -i));
            Date next = DateUtil.offsetDay(day, 1);
            long commissionCount = chainCommissionDao.selectCount(Wrappers.<ChainCommission>lambdaQuery()
                    .ge(ChainCommission::getCreateTime, day).lt(ChainCommission::getCreateTime, next));
            Map<String, Object> row = new HashMap<>();
            row.put("date", DateUtil.formatDate(day));
            row.put("commissionCount", commissionCount);
            row.put("notifyCount", chainNotifyLogDao.selectCount(Wrappers.<ChainNotifyLog>lambdaQuery()
                    .eq(ChainNotifyLog::getNotifyType, ChainConstants.NOTIFY_UPGRADE)
                    .ge(ChainNotifyLog::getCreateTime, day).lt(ChainNotifyLog::getCreateTime, next)));
            trend.add(row);
        }
        map.put("last7Days", trend);
    }

    public void pushNotify(Integer uid, String type, String title, String content) {
        if (uid == null || uid <= 0) {
            return;
        }
        if (!"1".equals(cfg(ChainConstants.CONFIG_NOTIFY_STATUS, "1"))) {
            return;
        }
        ChainNotifyLog n = new ChainNotifyLog();
        n.setUserId(uid);
        n.setNotifyType(type);
        n.setTitle(StrUtil.blankToDefault(title, "链动通知"));
        n.setContent(StrUtil.blankToDefault(content, ""));
        n.setIsRead(0);
        n.setCreateTime(DateUtil.date());
        chainNotifyLogDao.insert(n);
    }

    public CommonPage<ChainNotifyLog> notifyPage(Integer uid, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        return CommonPage.restPage(new PageInfo<>(chainNotifyLogDao.selectList(
                Wrappers.<ChainNotifyLog>lambdaQuery()
                        .eq(ChainNotifyLog::getUserId, uid)
                        .orderByDesc(ChainNotifyLog::getId))));
    }

    public void markNotifyRead(Integer uid, Integer id) {
        ChainNotifyLog n = chainNotifyLogDao.selectById(id);
        if (ObjectUtil.isNull(n) || !Objects.equals(n.getUserId(), uid)) {
            return;
        }
        n.setIsRead(1);
        chainNotifyLogDao.updateById(n);
    }

    public int unreadNotifyCount(Integer uid) {
        Integer c = chainNotifyLogDao.selectCount(Wrappers.<ChainNotifyLog>lambdaQuery()
                .eq(ChainNotifyLog::getUserId, uid).eq(ChainNotifyLog::getIsRead, 0));
        return c == null ? 0 : c;
    }

    @Transactional(rollbackFor = Exception.class)
    public int runDividend(String period) {
        if (!"1".equals(cfg(ChainConstants.CONFIG_DIVIDEND_STATUS, "0"))) {
            throw new QianxuException("团队分红未开启");
        }
        if (StrUtil.isBlank(period)) {
            period = DateUtil.format(DateUtil.offsetMonth(DateUtil.date(), -1), "yyyy-MM");
        }
        BigDecimal ratio = new BigDecimal(cfg(ChainConstants.CONFIG_DIVIDEND_RATIO, "5"));
        int minTeam = intCfg(ChainConstants.CONFIG_DIVIDEND_MIN_TEAM, 2);
        Date start = DateUtil.parse(period + "-01 00:00:00");
        Date end = DateUtil.endOfMonth(start);

        List<User> bosses = userService.list(Wrappers.<User>lambdaQuery()
                .eq(User::getChainIdentity, ChainConstants.IDENTITY_BOSS));
        int issued = 0;
        int freezeDays = intCfg(ChainConstants.CONFIG_FREEZE_DAYS, 1);
        for (User boss : bosses) {
            Integer exists = chainDividendRecordDao.selectCount(Wrappers.<ChainDividendRecord>lambdaQuery()
                    .eq(ChainDividendRecord::getPeriod, period)
                    .eq(ChainDividendRecord::getUserId, boss.getUid()));
            if (exists != null && exists > 0) {
                continue;
            }
            Set<Integer> team = collectTeamUids(boss.getUid(), 20);
            if (team.size() < minTeam) {
                continue;
            }
            List<StoreOrder> orders = storeOrderService.list(Wrappers.<StoreOrder>lambdaQuery()
                    .in(StoreOrder::getUid, team)
                    .eq(StoreOrder::getPaid, true)
                    .like(StoreOrder::getRemark, "CHAIN_GIFT")
                    .ge(StoreOrder::getPayTime, start)
                    .le(StoreOrder::getPayTime, end));
            BigDecimal sales = orders.stream()
                    .map(o -> ObjectUtil.defaultIfNull(o.getPayPrice(), BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sales.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal amount = sales.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            ChainDividendRecord rec = new ChainDividendRecord();
            rec.setPeriod(period);
            rec.setUserId(boss.getUid());
            rec.setTeamSales(sales);
            rec.setRatio(ratio);
            rec.setAmount(amount);
            rec.setTeamSize(team.size());
            rec.setStatus(1);
            rec.setMark("团队礼包销售分红");
            rec.setCreateTime(DateUtil.date());
            try {
                chainDividendRecordDao.insert(rec);
            } catch (Exception e) {
                continue;
            }
            String orderKey = "dividend-" + period + "-" + boss.getUid();
            UserBrokerageRecord br = new UserBrokerageRecord();
            br.setUid(boss.getUid());
            br.setLinkId(orderKey);
            br.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
            br.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
            br.setTitle(ChainConstants.TITLE_DIVIDEND);
            br.setPrice(amount);
            br.setMark(period + " 团队分红，销售额" + sales);
            br.setBrokerageLevel(ChainConstants.BROKERAGE_LEVEL_DIVIDEND);
            br.setCreateTime(DateUtil.date());
            if (freezeDays > 0) {
                br.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_CREATE);
                br.setFrozenTime(freezeDays);
            } else {
                br.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
                br.setFrozenTime(0);
            }
            userBrokerageRecordService.save(br);
            if (freezeDays <= 0) {
                User bu = userService.getById(boss.getUid());
                if (ObjectUtil.isNotNull(bu)) {
                    userService.operationBrokerage(boss.getUid(), amount,
                            ObjectUtil.defaultIfNull(bu.getBrokeragePrice(), BigDecimal.ZERO), "add");
                }
            }
            ChainCommission cc = new ChainCommission();
            cc.setUserId(boss.getUid());
            cc.setSourceUid(0);
            cc.setOrderId(orderKey);
            cc.setRewardType(ChainConstants.REWARD_DIVIDEND);
            cc.setAmount(amount);
            cc.setStatus(freezeDays > 0 ? ChainConstants.COMMISSION_PENDING : ChainConstants.COMMISSION_SETTLED);
            cc.setMark(br.getMark());
            cc.setCreateTime(DateUtil.date());
            cc.setUpdateTime(DateUtil.date());
            chainCommissionDao.insert(cc);
            pushNotify(boss.getUid(), ChainConstants.NOTIFY_DIVIDEND, "团队分红到账",
                    period + " 分红 " + amount + " 元");
            issued++;
        }
        return issued;
    }

    public CommonPage<ChainDividendRecord> dividendPage(String period, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        return CommonPage.restPage(new PageInfo<>(chainDividendRecordDao.selectList(
                Wrappers.<ChainDividendRecord>lambdaQuery()
                        .eq(StrUtil.isNotBlank(period), ChainDividendRecord::getPeriod, period)
                        .orderByDesc(ChainDividendRecord::getId))));
    }

    private Set<Integer> collectTeamUids(Integer rootUid, int maxDepth) {
        Set<Integer> all = new HashSet<>();
        Deque<Integer> q = new ArrayDeque<>();
        q.add(rootUid);
        int depth = 0;
        while (!q.isEmpty() && depth < maxDepth) {
            int size = q.size();
            for (int i = 0; i < size; i++) {
                Integer cur = q.poll();
                List<User> children = userService.list(Wrappers.<User>lambdaQuery().eq(User::getChainParentId, cur));
                for (User c : children) {
                    if (all.add(c.getUid())) {
                        q.add(c.getUid());
                    }
                }
            }
            depth++;
        }
        return all;
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
