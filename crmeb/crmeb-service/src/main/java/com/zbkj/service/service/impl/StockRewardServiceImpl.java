package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.constants.BrokerageRecordConstants;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockLadder;
import com.zbkj.common.model.stock.StockNotice;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockOrderProduct;
import com.zbkj.common.model.stock.StockReward;
import com.zbkj.common.model.stock.StockWithdraw;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserBrokerageRecord;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockRequests;
import com.zbkj.service.dao.StockLadderDao;
import com.zbkj.service.dao.StockNoticeDao;
import com.zbkj.service.dao.StockOrderDao;
import com.zbkj.service.dao.StockOrderProductDao;
import com.zbkj.service.dao.StockRewardDao;
import com.zbkj.service.dao.StockWithdrawDao;
import com.zbkj.service.service.StockRewardService;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订货系统-奖励/奖金/提现/业绩/消息服务实现
 */
@Service
public class StockRewardServiceImpl implements StockRewardService {

    @Autowired
    private StockRewardDao stockRewardDao;

    @Autowired
    private StockWithdrawDao stockWithdrawDao;

    @Autowired
    private StockNoticeDao stockNoticeDao;

    @Autowired
    private StockLadderDao stockLadderDao;

    @Autowired
    private StockOrderDao stockOrderDao;

    @Autowired
    private StockOrderProductDao stockOrderProductDao;

    @Autowired
    private StockService stockService;

    @Resource
    private UserService userService;

    @Resource
    private com.zbkj.service.service.UserBrokerageRecordService userBrokerageRecordService;

    @Resource
    private com.zbkj.service.dao.StockLevelDao stockLevelDao;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // ==================== 奖励核算 ====================

    @Override
    public void settleOrderReward(StockOrder order) {
        transactionTemplate.executeWithoutResult(status -> {
            // 1) 差价奖励
            calcDiffReward(order);
            // 2) 阶梯业绩奖励：不再按单结算，由周期性任务/手动结算统一发放（settleLadderReward）
            // 3) 平级奖励（按层级设置中的平级奖比例）
            calcPeerReward(order);
        });
    }

    /**
     * 换货差价奖励：换货人补付的差价 × 配置比例，奖励给其直接上级（幂等，按换货单号去重）
     */
    @Override
    public void settleExchangeDiffReward(com.zbkj.common.model.stock.StockExchange exchange) {
        if (exchange == null || exchange.getDiffPrice() == null || exchange.getDiffPrice().signum() <= 0) {
            return;
        }
        if (!"1".equals(systemConfigService.getValueByKey("stock_exchange_diff"))) {
            return;
        }
        if (exchange.getParentAgentId() == null || exchange.getParentAgentId() <= 0) {
            return;
        }
        BigDecimal rate;
        try {
            String r = systemConfigService.getValueByKey("stock_exchange_diff_parent_rate");
            rate = (r == null || r.trim().isEmpty()) ? BigDecimal.ZERO : new BigDecimal(r.trim());
        } catch (Exception e) {
            rate = BigDecimal.ZERO;
        }
        if (rate.signum() <= 0) {
            return;
        }
        BigDecimal reward = exchange.getDiffPrice().multiply(rate)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        if (reward.signum() <= 0) {
            return;
        }
        StockAgent parent = stockService.getAgentById(exchange.getParentAgentId());
        if (parent == null || parent.getStatus() == 0) {
            return;
        }
        createRewardIfAbsent(parent.getUid(), StockReward.TYPE_DIFF, exchange.getExchangeNo(), exchange.getUid(),
                exchange.getDiffPrice(), rate, reward,
                "换货差价奖励：" + exchange.getExchangeNo() + "（差价 " + exchange.getDiffPrice()
                        + " × " + rate.stripTrailingZeros().toPlainString() + "%）");
    }

    /** 差价：直接上级赚取（上级拿价 - 下级拿价）×数量 */
    private void calcDiffReward(StockOrder order) {
        if (!"1".equals(systemConfigService.getValueByKey("stock_diff_reward_status"))) {
            return;
        }
        if (order.getParentAgentId() == null || order.getParentAgentId() == 0) {
            return;
        }
        StockAgent parent = stockService.getAgentById(order.getParentAgentId());
        if (parent == null || parent.getStatus() == 0) {
            return;
        }
        List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId()));
        BigDecimal diff = BigDecimal.ZERO;
        for (StockOrderProduct item : items) {
            if (item.getParentPrice() == null) {
                continue;
            }
            // 差价 = 下级拿货价 - 上级拿货价（例：总代拿货 80、下级拿货 100，则总代每单赚 20）
            BigDecimal d = item.getPrice().subtract(item.getParentPrice());
            if (d.signum() > 0) {
                diff = diff.add(d.multiply(new BigDecimal(item.getNum())));
            }
        }
        if (diff.signum() <= 0) {
            return;
        }
        createRewardIfAbsent(parent.getUid(), StockReward.TYPE_DIFF, order.getOrderNo(), order.getUid(),
                diff, BigDecimal.ZERO, diff, "差价奖励：" + order.getOrderNo()
                        + "（下级拿价 - 上级拿价）×" + order.getTotalNum());
    }

    /**
     * 平级奖励（2026-09-18 调整：全局配置已废弃，改为按层级设置中的平级奖比例）：
     * 需求口径 —— 下级代理 B 产生订货业绩时，其【直接上级】A 若与 B 同层级（同级平推），
     * A 按其层级在层级设置中配置的平级奖比例（eb_stock_level.peer_rate）拿 B 本单金额的奖励，只拿直接一代。
     */
    private void calcPeerReward(StockOrder order) {
        StockAgent start = stockService.getAgentById(order.getAgentId());
        if (start == null) {
            return;
        }
        if (start.getParentId() == null || start.getParentId() <= 0) {
            return;
        }
        StockAgent up = stockService.getAgentById(start.getParentId());
        if (up == null || up.getStatus() == null || up.getStatus() != 1) {
            return;
        }
        // 仅同级平推产生平级奖
        if (!up.getLevelId().equals(start.getLevelId())) {
            return;
        }
        com.zbkj.common.model.stock.StockLevel level = stockLevelDao.selectById(up.getLevelId());
        if (level == null || level.getPeerRate() == null || level.getPeerRate().signum() <= 0) {
            return;
        }
        BigDecimal reward = order.getTotalPrice().multiply(level.getPeerRate())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (reward.signum() > 0) {
            createRewardIfAbsent(up.getUid(), StockReward.TYPE_PEER, order.getOrderNo(), order.getUid(),
                    order.getTotalPrice(), level.getPeerRate(), reward,
                    "平级奖励：同级下级单 " + order.getOrderNo() + " 按层级【" + level.getName()
                            + "】比例 " + level.getPeerRate() + "%");
        }
    }

    /**
     * 阶梯业绩奖励结算（2026-09-18 改造：取消级差差额模式）：
     * 每个订货商规则相同 —— 团队业绩落入阶梯区间即得一次性奖励。
     * 每档奖励【固定金额 / 业绩比例】二选一：固定金额 > 0 时直接发固定金额，否则按 团队业绩 × 比例% 发放。
     * type: 1=月度 2=季度 3=年度；month 为该周期内任一月份（yyyy-MM），自动归集周期起止。
     * 幂等：先失效同周期已发记录再统一重发，可重复执行。
     */
    @Override
    public Boolean settleLadderReward(Integer type, String month) {
        if (!"1".equals(systemConfigService.getValueByKey("stock_ladder_status"))) {
            throw new CrmebException("阶梯业绩奖励未开启");
        }
        int t = type == null || type <= 0 ? 1 : type;
        String[] range = periodRange(t, month);
        String periodKey = periodKey(t, month);
        String periodName = t == 1 ? "月度" : (t == 2 ? "季度" : "年度");
        return transactionTemplate.execute(status -> {
            // 失效同周期已发记录（幂等）
            List<StockReward> olds = stockRewardDao.selectList(new LambdaQueryWrapper<StockReward>()
                    .eq(StockReward::getType, StockReward.TYPE_LADDER)
                    .likeRight(StockReward::getMark, "阶梯业绩结算|" + periodKey));
            for (StockReward old : olds) {
                old.setStatus(StockReward.STATUS_INVALID);
                stockRewardDao.updateById(old);
            }
            List<StockLadder> ladders = getLadders();
            if (ladders.isEmpty()) {
                return true;
            }
            List<StockAgent> agents;
            PageParamRequest bigPage = new PageParamRequest();
            bigPage.setPage(1);
            bigPage.setLimit(10000);
            agents = stockService.getAdminAgentList(null, null, null, bigPage).getList();
            for (StockAgent agent : agents) {
                BigDecimal teamPerf = teamPerformance(agent, range);
                if (teamPerf.signum() <= 0) {
                    continue;
                }
                StockLadder hit = matchLadder(ladders, teamPerf);
                if (hit == null) {
                    continue;
                }
                // 固定金额 / 业绩比例 二选一：固定金额 > 0 直接发固定，否则按 团队业绩 × 比例%
                BigDecimal fixed = hit.getReward() == null ? BigDecimal.ZERO : hit.getReward();
                BigDecimal reward;
                String ruleDesc;
                if (fixed.signum() > 0) {
                    reward = fixed;
                    ruleDesc = "固定 " + fixed + " 元";
                } else {
                    reward = teamPerf.multiply(nz(hit.getRate()))
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    ruleDesc = "业绩 " + teamPerf + " × " + nz(hit.getRate()) + "%";
                }
                if (reward.signum() <= 0) {
                    continue;
                }
                String mark = "阶梯业绩结算|" + periodKey + "|" + periodName + "|团队业绩 " + teamPerf
                        + " " + ruleDesc;
                StockReward r = new StockReward();
                r.setUid(agent.getUid());
                r.setType(StockReward.TYPE_LADDER);
                r.setSource(StockReward.SOURCE_ORDER);
                r.setOrderNo("period:" + periodKey);
                r.setLinkUid(0);
                r.setBasePrice(teamPerf);
                r.setRate(fixed.signum() > 0 ? BigDecimal.ZERO : nz(hit.getRate()));
                r.setRewardPrice(reward);
                r.setMark(mark);
                r.setStatus(StockReward.STATUS_CREDITED);
                stockRewardDao.insert(r);
                // 阶梯奖励同步计入佣金余额
                creditBrokerage(agent.getUid(), reward, "period:" + periodKey, "订货奖金", mark);
            }
            return true;
        }) != null;
    }

    /** 团队业绩落入的阶梯（min <= 业绩 < max，max=0 不限） */
    private StockLadder matchLadder(List<StockLadder> ladders, BigDecimal perf) {
        for (StockLadder l : ladders) {
            BigDecimal min = nz(l.getMinAmount());
            BigDecimal max = nz(l.getMaxAmount());
            boolean geMin = perf.compareTo(min) >= 0;
            boolean ltMax = max.signum() == 0 || perf.compareTo(max) < 0;
            if (geMin && ltMax) {
                return l;
            }
        }
        return null;
    }

    /**
     * 周期起止：type=1 该自然月；type=2 该月所在季度；type=3 该月所在年度。
     * 返回 [startyyyy-MM-dd, endyyyy-MM-dd]（含头含尾的日期字符串，具体格式同 monthRange）
     */
    private String[] periodRange(int type, String month) {
        if (type == 2) {
            String qStart = quarterStartMonth(month);
            String[] start = monthRange(qStart);
            java.time.LocalDate first = java.time.LocalDate.parse(qStart + "-01");
            java.time.LocalDate end = first.plusMonths(3).minusDays(1);
            return new String[]{start[0], end.toString()};
        }
        if (type == 3) {
            String year = month.trim().substring(0, 4);
            String[] start = monthRange(year + "-01");
            java.time.LocalDate first = java.time.LocalDate.parse(year + "-12-01");
            java.time.LocalDate end = first.plusMonths(1).minusDays(1);
            return new String[]{start[0], end.toString()};
        }
        return monthRange(month);
    }

    /** 周期标识：M2026-09 / Q2026-3(该季度首月所在年) / Y2026 */
    private String periodKey(int type, String month) {
        if (type == 2) {
            String qStart = quarterStartMonth(month);
            int q = (Integer.parseInt(qStart.substring(5, 7)) - 1) / 3 + 1;
            return "Q" + qStart.substring(0, 4) + "-" + q;
        }
        if (type == 3) {
            return "Y" + month.trim().substring(0, 4);
        }
        return "M" + month.trim();
    }

    /** 该月所在季度的首月（yyyy-MM） */
    private String quarterStartMonth(String month) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(parseMonth(month));
        int m = c.get(java.util.Calendar.MONTH) + 1; // 1-12
        int qStart = (m - 1) / 3 * 3 + 1;
        return month.trim().substring(0, 4) + "-" + String.format("%02d", qStart);
    }

    private java.util.Date parseMonth(String month) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM").parse(month.trim());
        } catch (java.text.ParseException e) {
            throw new CrmebException("月份格式错误：" + month);
        }
    }

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> getMyBonus(Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        // 已入账奖励总额
        BigDecimal totalReward = sumReward(uid, null);
        // 奖金已并入佣金余额，可提现金额直接取用户佣金余额（走系统统一佣金提现）
        User user = userService.getById(uid);
        BigDecimal commission = user == null || user.getBrokeragePrice() == null ? BigDecimal.ZERO : user.getBrokeragePrice();
        map.put("totalReward", totalReward);
        map.put("balance", commission);
        map.put("commission", commission);
        // 分类小计
        map.put("diffReward", sumReward(uid, StockReward.TYPE_DIFF));
        map.put("ladderReward", sumReward(uid, StockReward.TYPE_LADDER));
        map.put("peerReward", sumReward(uid, StockReward.TYPE_PEER));
        return map;
    }

    @Override
    public CommonPage<StockReward> getMyRewardList(Integer uid, Integer type, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockReward> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockReward::getUid, uid).eq(StockReward::getStatus, StockReward.STATUS_CREDITED);
        if (type != null && type > 0) {
            lqw.eq(StockReward::getType, type);
        }
        lqw.orderByDesc(StockReward::getId);
        List<StockReward> list = stockRewardDao.selectList(lqw);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public HashMap<String, Object> getMyPerformance(Integer uid, String dateLimit) {
        String[] range = parseDateLimit(dateLimit);
        HashMap<String, Object> map = new HashMap<>();
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        // 个人业绩：自己下的完成单
        LambdaQueryWrapper<StockOrder> selfLqw = new LambdaQueryWrapper<>();
        selfLqw.eq(StockOrder::getUid, uid).eq(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                .eq(StockOrder::getIsDel, 0);
        applyRange(selfLqw, range);
        List<StockOrder> selfOrders = stockOrderDao.selectList(selfLqw);
        BigDecimal selfPerf = BigDecimal.ZERO;
        int selfCount = selfOrders.size();
        for (StockOrder o : selfOrders) {
            selfPerf = selfPerf.add(o.getTotalPrice());
        }
        map.put("selfPerformance", selfPerf);
        map.put("selfOrderCount", selfCount);
        // 团队业绩：名下全部下级完成单
        List<Integer> subAgentIds = stockService.collectSubAgentIds(agent.getId());
        Map<Integer, Integer> uidMap = stockService.getAgentUidMap(subAgentIds);
        BigDecimal teamPerf = BigDecimal.ZERO;
        int teamCount = 0;
        if (!uidMap.isEmpty()) {
            LambdaQueryWrapper<StockOrder> teamLqw = new LambdaQueryWrapper<>();
            teamLqw.in(StockOrder::getUid, uidMap.values())
                    .eq(StockOrder::getStatus, StockOrder.STATUS_COMPLETE).eq(StockOrder::getIsDel, 0);
            applyRange(teamLqw, range);
            List<StockOrder> teamOrders = stockOrderDao.selectList(teamLqw);
            teamCount = teamOrders.size();
            for (StockOrder o : teamOrders) {
                teamPerf = teamPerf.add(o.getTotalPrice());
            }
        }
        map.put("teamPerformance", teamPerf);
        map.put("teamOrderCount", teamCount);
        map.put("subAgentCount", subAgentIds.size());
        return map;
    }

    @Override
    public Boolean applyWithdraw(Integer uid, BigDecimal price, String mark) {
        if (price == null || price.signum() <= 0) {
            throw new CrmebException("提现金额必须大于0");
        }
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() == 0) {
            throw new CrmebException("您还不是订货代理或已被禁用");
        }
        HashMap<String, Object> bonus = getMyBonus(uid);
        BigDecimal balance = (BigDecimal) bonus.get("balance");
        if (price.compareTo(balance) > 0) {
            throw new CrmebException("超出可提现余额，当前可提现 ¥" + balance);
        }
        StockWithdraw withdraw = new StockWithdraw();
        withdraw.setUid(uid);
        withdraw.setWithdrawNo("WD" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000)));
        withdraw.setPrice(price);
        withdraw.setStatus(StockWithdraw.STATUS_WAIT_AUDIT);
        withdraw.setMark(mark == null ? "" : mark);
        return stockWithdrawDao.insert(withdraw) > 0;
    }

    @Override
    public CommonPage<StockWithdraw> getMyWithdrawList(Integer uid, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        List<StockWithdraw> list = stockWithdrawDao.selectList(new LambdaQueryWrapper<StockWithdraw>()
                .eq(StockWithdraw::getUid, uid).orderByDesc(StockWithdraw::getId));
        fillWithdraws(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public CommonPage<StockNotice> getMyNoticeList(Integer uid, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        List<StockNotice> list = stockNoticeDao.selectList(new LambdaQueryWrapper<StockNotice>()
                .eq(StockNotice::getUid, uid).orderByDesc(StockNotice::getId));
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean readNotice(Integer uid, Integer noticeId) {
        StockNotice notice = stockNoticeDao.selectById(noticeId);
        if (notice == null || !notice.getUid().equals(uid)) {
            throw new CrmebException("消息不存在");
        }
        notice.setIsRead(1);
        return stockNoticeDao.updateById(notice) > 0;
    }

    @Override
    public Long unreadNoticeCount(Integer uid) {
        Integer count = stockNoticeDao.selectCount(new LambdaQueryWrapper<StockNotice>()
                .eq(StockNotice::getUid, uid).eq(StockNotice::getIsRead, 0));
        return count == null ? 0L : count.longValue();
    }

    // ==================== 后台 ====================

    @Override
    public CommonPage<StockReward> getAdminRewardList(Integer uid, Integer type, String orderNo, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockReward> lqw = new LambdaQueryWrapper<>();
        if (uid != null && uid > 0) {
            lqw.eq(StockReward::getUid, uid);
        }
        if (type != null && type > 0) {
            lqw.eq(StockReward::getType, type);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            lqw.like(StockReward::getOrderNo, orderNo.trim());
        }
        lqw.orderByDesc(StockReward::getId);
        List<StockReward> list = stockRewardDao.selectList(lqw);
        fillRewards(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public CommonPage<StockWithdraw> getAdminWithdrawList(Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockWithdraw> lqw = new LambdaQueryWrapper<>();
        if (status != null) {
            lqw.eq(StockWithdraw::getStatus, status);
        }
        lqw.orderByDesc(StockWithdraw::getId);
        List<StockWithdraw> list = stockWithdrawDao.selectList(lqw);
        fillWithdraws(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean auditWithdraw(Integer withdrawId, StockRequests.StockWithdrawAuditRequest request) {
        StockWithdraw withdraw = stockWithdrawDao.selectById(withdrawId);
        if (withdraw == null) {
            throw new CrmebException("提现单不存在");
        }
        if (!withdraw.getStatus().equals(StockWithdraw.STATUS_WAIT_AUDIT)) {
            throw new CrmebException("该提现单已审核");
        }
        withdraw.setStatus(request.getStatus());
        withdraw.setAuditMark(request.getAuditMark() == null ? "" : request.getAuditMark());
        withdraw.setAuditTime(new Date());
        boolean ok = stockWithdrawDao.updateById(withdraw) > 0;
        if (ok) {
            String msg = request.getStatus() == 1 ? "已打款" : "被驳回：" + withdraw.getAuditMark();
            sendNotice(withdraw.getUid(), StockNotice.TYPE_WITHDRAW, "提现审核结果",
                    "您的提现申请 " + withdraw.getWithdrawNo() + "（¥" + withdraw.getPrice() + "）" + msg);
        }
        return ok;
    }

    @Override
    public HashMap<String, Object> getReport(String dateLimit, PageParamRequest page) {
        String[] range = parseDateLimit(dateLimit);
        HashMap<String, Object> map = new HashMap<>();
        // 订货总览
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getIsDel, 0);
        applyRange(lqw, range);
        List<StockOrder> orders = stockOrderDao.selectList(lqw);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = orders.size();
        BigDecimal doneAmount = BigDecimal.ZERO;
        int doneCount = 0;
        for (StockOrder o : orders) {
            totalAmount = totalAmount.add(o.getTotalPrice());
            if (o.getStatus().equals(StockOrder.STATUS_COMPLETE)) {
                doneAmount = doneAmount.add(o.getTotalPrice());
                doneCount++;
            }
        }
        map.put("totalAmount", totalAmount);
        map.put("totalCount", totalCount);
        map.put("doneAmount", doneAmount);
        map.put("doneCount", doneCount);
        // 奖励总览
        LambdaQueryWrapper<StockReward> rlqw = new LambdaQueryWrapper<>();
        rlqw.eq(StockReward::getStatus, StockReward.STATUS_CREDITED);
        if (range != null) {
            rlqw.ge(StockReward::getCreateTime, range[0]).le(StockReward::getCreateTime, range[1] + " 23:59:59");
        }
        List<StockReward> rewards = stockRewardDao.selectList(rlqw);
        BigDecimal diffSum = BigDecimal.ZERO;
        BigDecimal ladderSum = BigDecimal.ZERO;
        BigDecimal peerSum = BigDecimal.ZERO;
        for (StockReward r : rewards) {
            if (r.getType().equals(StockReward.TYPE_DIFF)) {
                diffSum = diffSum.add(r.getRewardPrice());
            } else if (r.getType().equals(StockReward.TYPE_LADDER)) {
                ladderSum = ladderSum.add(r.getRewardPrice());
            } else if (r.getType().equals(StockReward.TYPE_PEER)) {
                peerSum = peerSum.add(r.getRewardPrice());
            }
        }
        map.put("diffSum", diffSum);
        map.put("ladderSum", ladderSum);
        map.put("peerSum", peerSum);
        map.put("rewardSum", diffSum.add(ladderSum).add(peerSum));
        // 按代理汇总（分页）
        List<HashMap<String, Object>> rows = new ArrayList<>();
        com.zbkj.common.page.CommonPage<StockAgent> agentPage =
                stockService.getAdminAgentList(null, null, null, page);
        for (StockAgent agent : agentPage.getList()) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("uid", agent.getUid());
            row.put("nickname", agent.getNickname());
            row.put("phone", agent.getPhone());
            row.put("levelName", agent.getLevelName());
            // 订货金额
            LambdaQueryWrapper<StockOrder> olqw = new LambdaQueryWrapper<>();
            olqw.eq(StockOrder::getUid, agent.getUid()).eq(StockOrder::getIsDel, 0);
            applyRange(olqw, range);
            List<StockOrder> os = stockOrderDao.selectList(olqw);
            BigDecimal orderSum = BigDecimal.ZERO;
            for (StockOrder o : os) {
                orderSum = orderSum.add(o.getTotalPrice());
            }
            row.put("orderAmount", orderSum);
            row.put("orderCount", os.size());
            row.put("selfPerformance", orderSum);
            row.put("teamPerformance", teamPerformance(agent, range));
            // 奖励
            LambdaQueryWrapper<StockReward> rw = new LambdaQueryWrapper<>();
            rw.eq(StockReward::getUid, agent.getUid()).eq(StockReward::getStatus, StockReward.STATUS_CREDITED);
            if (range != null) {
                rw.ge(StockReward::getCreateTime, range[0]).le(StockReward::getCreateTime, range[1] + " 23:59:59");
            }
            BigDecimal rewardSum = BigDecimal.ZERO;
            for (StockReward r : stockRewardDao.selectList(rw)) {
                rewardSum = rewardSum.add(r.getRewardPrice());
            }
            row.put("rewardSum", rewardSum);
            rows.add(row);
        }
        map.put("agentRows", rows);
        map.put("total", agentPage.getTotal());
        return map;
    }

    @Override
    public void sendNotice(Integer uid, Integer type, String title, String content) {
        StockNotice notice = new StockNotice();
        notice.setUid(uid);
        notice.setType(type);
        notice.setTitle(title == null ? "" : title);
        notice.setContent(content == null ? "" : content);
        notice.setIsRead(0);
        stockNoticeDao.insert(notice);
    }

    // ==================== 内部 ====================

    private void createRewardIfAbsent(Integer uid, Integer type, String orderNo, Integer linkUid,
                                      BigDecimal basePrice, BigDecimal rate, BigDecimal rewardPrice, String mark) {
        Integer count = stockRewardDao.selectCount(new LambdaQueryWrapper<StockReward>()
                .eq(StockReward::getUid, uid).eq(StockReward::getType, type)
                .eq(StockReward::getOrderNo, orderNo));
        if (count != null && count > 0) {
            return;
        }
        StockReward r = new StockReward();
        r.setUid(uid);
        r.setType(type);
        r.setSource(StockReward.SOURCE_ORDER);
        r.setOrderNo(orderNo);
        r.setLinkUid(linkUid == null ? 0 : linkUid);
        r.setBasePrice(basePrice);
        r.setRate(rate);
        r.setRewardPrice(rewardPrice);
        r.setMark(mark == null ? "" : mark);
        r.setStatus(StockReward.STATUS_CREDITED);
        stockRewardDao.insert(r);
        // 奖金同步计入佣金余额（status=3 直接到账不冻结，走系统统一佣金提现）
        creditBrokerage(uid, rewardPrice, orderNo, "订货奖金", mark);
        sendNotice(uid, StockNotice.TYPE_REWARD, "奖金到账",
                "订单 " + orderNo + " 产生奖金 ¥" + rewardPrice + "，已计入您的佣金余额");
    }

    /** 奖金入账到佣金余额：写佣金记录（已完成状态）+ 增加用户 brokerage_price */
    private void creditBrokerage(Integer uid, BigDecimal price, String linkId, String title, String mark) {
        User user = userService.getById(uid);
        if (user == null || price == null || price.signum() <= 0) {
            return;
        }
        UserBrokerageRecord record = new UserBrokerageRecord();
        record.setUid(uid);
        record.setLinkId(linkId == null ? "" : linkId);
        record.setLinkType(BrokerageRecordConstants.BROKERAGE_RECORD_LINK_TYPE_ORDER);
        record.setType(BrokerageRecordConstants.BROKERAGE_RECORD_TYPE_ADD);
        record.setTitle(title);
        record.setPrice(price);
        record.setBalance(user.getBrokeragePrice() == null ? price : user.getBrokeragePrice().add(price));
        record.setMark(mark == null ? title : mark);
        record.setStatus(BrokerageRecordConstants.BROKERAGE_RECORD_STATUS_COMPLETE);
        record.setFrozenTime(0);
        record.setCreateTime(com.zbkj.common.utils.CrmebDateUtil.nowDateTime());
        userBrokerageRecordService.save(record);
        userService.operationBrokerage(uid, price, user.getBrokeragePrice(), "add");
    }

    private List<StockLadder> getLadders() {
        return stockLadderDao.selectList(new LambdaQueryWrapper<StockLadder>()
                .orderByAsc(StockLadder::getSort));
    }

    /** 团队业绩：名下全部下级（子树）完成单金额。range=null 表示不限时间 */
    private BigDecimal teamPerformance(StockAgent agent, String[] range) {
        List<Integer> subAgentIds = stockService.collectSubAgentIds(agent.getId());
        if (subAgentIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        Map<Integer, Integer> uidMap = stockService.getAgentUidMap(subAgentIds);
        if (uidMap.isEmpty()) {
            return BigDecimal.ZERO;
        }
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.in(StockOrder::getUid, uidMap.values())
                .eq(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                .eq(StockOrder::getIsDel, 0);
        applyRange(lqw, range);
        List<StockOrder> orders = stockOrderDao.selectList(lqw);
        BigDecimal sum = BigDecimal.ZERO;
        for (StockOrder o : orders) {
            sum = sum.add(o.getTotalPrice());
        }
        return sum;
    }

    private List<StockAgent> directChildren(Integer agentId) {
        return stockService.getDirectChildren(agentId);
    }

    private BigDecimal sumReward(Integer uid, Integer type) {
        LambdaQueryWrapper<StockReward> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockReward::getUid, uid).eq(StockReward::getStatus, StockReward.STATUS_CREDITED);
        if (type != null) {
            lqw.eq(StockReward::getType, type);
        }
        List<StockReward> list = stockRewardDao.selectList(lqw);
        BigDecimal sum = BigDecimal.ZERO;
        for (StockReward r : list) {
            sum = sum.add(r.getRewardPrice());
        }
        return sum;
    }

    /** dateLimit: "2026-09" -> 本月区间；"2026-09-01,2026-09-30" -> 指定区间；null -> 全部 */
    private String[] parseDateLimit(String dateLimit) {
        if (dateLimit == null || dateLimit.trim().isEmpty()) {
            return null;
        }
        String s = dateLimit.trim();
        if (s.contains(",")) {
            String[] parts = s.split(",");
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return monthRange(s);
    }

    private String[] monthRange(String month) {
        // month: yyyy-MM
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        String start = String.format("%04d-%02d-01", year, m);
        int nextY = m == 12 ? year + 1 : year;
        int nextM = m == 12 ? 1 : m + 1;
        java.time.LocalDate first = java.time.LocalDate.of(nextY, nextM, 1);
        String end = first.minusDays(1).toString();
        return new String[]{start, end};
    }

    private void applyRange(LambdaQueryWrapper<StockOrder> lqw, String[] range) {
        if (range != null) {
            lqw.ge(StockOrder::getCreateTime, range[0]).le(StockOrder::getCreateTime, range[1] + " 23:59:59");
        }
    }

    private void fillWithdraws(List<StockWithdraw> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Integer> uids = new ArrayList<>();
        for (StockWithdraw w : list) {
            uids.add(w.getUid());
        }
        Map<Integer, User> userMap = new HashMap<>();
        for (User u : userService.lambdaQuery().in(User::getUid, uids).list()) {
            userMap.put(u.getUid(), u);
        }
        for (StockWithdraw w : list) {
            User u = userMap.get(w.getUid());
            w.setNickname(u == null ? "" : u.getNickname());
            w.setPhone(u == null ? "" : u.getPhone());
        }
    }

    private void fillRewards(List<StockReward> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Integer> uids = new ArrayList<>();
        List<Integer> linkUids = new ArrayList<>();
        for (StockReward r : list) {
            uids.add(r.getUid());
            if (r.getLinkUid() != null && r.getLinkUid() > 0) {
                linkUids.add(r.getLinkUid());
            }
        }
        Set<Integer> all = new HashSet<>(uids);
        all.addAll(linkUids);
        Map<Integer, User> userMap = new HashMap<>();
        for (User u : userService.lambdaQuery().in(User::getUid, all).list()) {
            userMap.put(u.getUid(), u);
        }
        for (StockReward r : list) {
            User u = userMap.get(r.getUid());
            r.setNickname(u == null ? "" : u.getNickname());
            User lu = userMap.get(r.getLinkUid());
            r.setLinkNickname(lu == null ? "" : lu.getNickname());
        }
    }

    private BigDecimal parseDecimal(String s, BigDecimal def) {
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
