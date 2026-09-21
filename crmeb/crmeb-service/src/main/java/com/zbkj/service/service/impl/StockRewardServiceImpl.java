package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.constants.BrokerageRecordConstants;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockExchange;
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
    private com.zbkj.service.dao.StockExchangeDao stockExchangeDao;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // ==================== 奖励核算 ====================

    @Override
    public void settleOrderReward(StockOrder order) {
        // 团队奖开关：关闭时不结算任何订单奖励（差价/平级）
        if (!"1".equals(systemConfigService.getValueByKey("sys_switch_team_reward"))) {
            return;
        }
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

    /**
     * 货款结算（2026-09-18 汪总口径）：下级订单完成后，货款给到其【供货上级】（结算时点的挂靠上级），
     * 分两笔入账：
     *   1) 进货成本回款：供货上级的层级拿货价 × 数量（type=4，始终结算，不受开关控制）
     *   2) 差价佣金：下级拿货价 − 供货上级拿货价（正数部分）× 数量（type=1，受 stock_diff_reward_status 开关）
     * 例：区级 80 拿货、供货上级市级拿价 70 → 市级账户入 70 成本 + 10 差价；
     *     若链路一路上浮到分公司（拿价 40）→ 分公司入 40 成本 + 40 差价，合计仍为买家实付 80。
     * 供货价按【结算时点】实时重算：订单可能被向上匹配改挂上级，下单时的 parentPrice 快照不可用。
     */
    private void calcDiffReward(StockOrder order) {
        if (order.getParentAgentId() == null || order.getParentAgentId() == 0) {
            return;
        }
        StockAgent supplier = stockService.getAgentById(order.getParentAgentId());
        if (supplier == null || supplier.getStatus() == 0) {
            return;
        }
        boolean diffEnabled = "1".equals(systemConfigService.getValueByKey("stock_diff_reward_status"));
        List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId()));
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal diff = BigDecimal.ZERO;
        for (StockOrderProduct item : items) {
            BigDecimal buyerPrice = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            BigDecimal supplyPrice;
            try {
                supplyPrice = stockService.getProductPrice(supplier, item.getProductId(), item.getSkuKey());
            } catch (Exception e) {
                supplyPrice = item.getParentPrice();
            }
            if (supplyPrice == null || supplyPrice.signum() < 0) {
                supplyPrice = BigDecimal.ZERO;
            }
            // 兜底：上级成本不超过买家实付（价格配置异常时确保上级合计不超订单金额）
            if (supplyPrice.compareTo(buyerPrice) > 0) {
                supplyPrice = buyerPrice;
            }
            BigDecimal num = new BigDecimal(item.getNum() == null ? 0 : item.getNum());
            cost = cost.add(supplyPrice.multiply(num));
            BigDecimal d = buyerPrice.subtract(supplyPrice);
            if (d.signum() > 0) {
                diff = diff.add(d.multiply(num));
            }
        }
        if (cost.signum() > 0) {
            createRewardIfAbsent(supplier.getUid(), StockReward.TYPE_COST, order.getOrderNo(), order.getUid(),
                    cost, BigDecimal.ZERO, cost,
                    "货款成本回款：" + order.getOrderNo() + "（下级订单货款按您的拿货价结算成本）");
        }
        if (diffEnabled && diff.signum() > 0) {
            createRewardIfAbsent(supplier.getUid(), StockReward.TYPE_DIFF, order.getOrderNo(), order.getUid(),
                    diff, BigDecimal.ZERO, diff, "差价佣金：" + order.getOrderNo()
                            + "（下级拿价 − 您的拿价）×" + order.getTotalNum());
        }
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
        // 团队奖开关：关闭时阶梯奖同样停发
        if (!"1".equals(systemConfigService.getValueByKey("sys_switch_team_reward"))) {
            throw new CrmebException("团队奖未开启");
        }
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
            agents = stockService.getAdminAgentList(null, null, null, null, bigPage).getList();
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
        // 会员端奖励明细同样需要展示「下单人 / 订单号 / 商品」，与后台口径一致
        fillRewards(list);
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
    public CommonPage<StockOrder> getMyPerformanceOrderList(Integer uid, String dateLimit, Integer source, PageParamRequest page) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        String[] range = parseDateLimit(dateLimit);
        // 团队业绩范围 = 名下全部下级 uid（含间接）
        List<Integer> subAgentIds = stockService.collectSubAgentIds(agent.getId());
        Map<Integer, Integer> uidMap = stockService.getAgentUidMap(subAgentIds);
        List<Integer> subUids = new ArrayList<>(uidMap.values());

        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getStatus, StockOrder.STATUS_COMPLETE).eq(StockOrder::getIsDel, 0);
        if (Integer.valueOf(1).equals(source)) {
            // 只看个人业绩：自己下的完成单
            lqw.eq(StockOrder::getUid, uid);
        } else if (Integer.valueOf(2).equals(source)) {
            // 只看团队业绩：下级下的完成单
            if (subUids.isEmpty()) {
                return CommonPage.restPage(new PageInfo<>(new ArrayList<>()));
            }
            lqw.in(StockOrder::getUid, subUids);
        } else {
            // 不区分来源：自己 + 名下全部下级
            List<Integer> allUids = new ArrayList<>();
            allUids.add(uid);
            allUids.addAll(subUids);
            lqw.in(StockOrder::getUid, allUids);
        }
        applyRange(lqw, range);
        lqw.orderByDesc(StockOrder::getId);
        PageHelper.startPage(page.getPage(), page.getLimit());
        List<StockOrder> list = stockOrderDao.selectList(lqw);
        fillPerfOrders(list, uid);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    /** 业绩订单补充展示字段：业绩来源、下单人昵称、商品明细 */
    private void fillPerfOrders(List<StockOrder> orders, Integer selfUid) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Integer> orderIds = new ArrayList<>();
        List<Integer> uids = new ArrayList<>();
        for (StockOrder o : orders) {
            orderIds.add(o.getId());
            uids.add(o.getUid());
            boolean self = o.getUid() != null && o.getUid().equals(selfUid);
            o.setPerfSource(self ? 1 : 2);
            o.setPerfSourceText(self ? "个人业绩" : "团队业绩");
        }
        HashMap<Integer, User> userMap = new HashMap<>();
        for (User u : userService.lambdaQuery().in(User::getUid, uids).list()) {
            userMap.put(u.getUid(), u);
        }
        HashMap<Integer, List<StockOrderProduct>> itemMap = new HashMap<>();
        for (StockOrderProduct op : stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .in(StockOrderProduct::getOrderId, orderIds))) {
            itemMap.computeIfAbsent(op.getOrderId(), k -> new ArrayList<>()).add(op);
        }
        for (StockOrder o : orders) {
            User u = userMap.get(o.getUid());
            o.setNickname(u == null ? ("用户" + o.getUid()) : u.getNickname());
            o.setProductList(itemMap.get(o.getId()));
        }
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

    @Override
    public Integer readAllNotices(Integer uid) {
        // 一条 UPDATE 全部置已读（Mapper 没有 updateBatchById，那是 IService 的方法）
        int before = stockNoticeDao.selectCount(new LambdaQueryWrapper<StockNotice>()
                .eq(StockNotice::getUid, uid).eq(StockNotice::getIsRead, 0));
        if (before <= 0) {
            return 0;
        }
        stockNoticeDao.update(null, new LambdaUpdateWrapper<StockNotice>()
                .eq(StockNotice::getUid, uid)
                .eq(StockNotice::getIsRead, 0)
                .set(StockNotice::getIsRead, 1));
        return before;
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
    public HashMap<String, Object> getReport(Integer uid, String dateLimit, PageParamRequest page) {
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
                stockService.getAdminAgentList(null, uid, null, null, page);
        for (StockAgent agent : agentPage.getList()) {
            HashMap<String, Object> row = new HashMap<>();
            row.put("agentId", agent.getId());
            row.put("uid", agent.getUid());
            row.put("nickname", agent.getNickname());
            row.put("avatar", agent.getAvatar());
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

    /**
     * 奖金明细补充展示字段：得奖用户昵称、下单人（业绩产生用户）昵称/手机号、
     * 以及关联订货单的商品明细摘要。会员端「奖金中心」与后台明细共用，保证口径一致。
     */
    private void fillRewards(List<StockReward> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Integer> all = new HashSet<>();
        List<String> orderNos = new ArrayList<>();
        for (StockReward r : list) {
            if (r.getUid() != null && r.getUid() > 0) {
                all.add(r.getUid());
            }
            if (r.getLinkUid() != null && r.getLinkUid() > 0) {
                all.add(r.getLinkUid());
            }
            // 阶梯奖励的 orderNo 形如 period:M2026-09（非真实订货单），不参与商品关联
            if (r.getOrderNo() != null && !r.getOrderNo().isEmpty() && !r.getOrderNo().startsWith("period:")) {
                orderNos.add(r.getOrderNo());
            }
        }
        Map<Integer, User> userMap = new HashMap<>();
        if (!all.isEmpty()) {
            for (User u : userService.lambdaQuery().in(User::getUid, all).list()) {
                userMap.put(u.getUid(), u);
            }
        }
        // 下单信息：按订单号回查订货单商品明细，拼成「商品名×数量，商品名×数量」
        Map<String, String> productNameMap = new HashMap<>();
        if (!orderNos.isEmpty()) {
            List<StockOrder> orders = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                    .in(StockOrder::getOrderNo, orderNos).eq(StockOrder::getIsDel, 0));
            if (!orders.isEmpty()) {
                List<Integer> orderIds = new ArrayList<>();
                for (StockOrder o : orders) {
                    orderIds.add(o.getId());
                }
                Map<Integer, List<String>> itemMap = new HashMap<>();
                for (StockOrderProduct op : stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                        .in(StockOrderProduct::getOrderId, orderIds))) {
                    String name = (op.getProductName() == null || op.getProductName().isEmpty())
                            ? ("商品" + op.getProductId()) : op.getProductName();
                    itemMap.computeIfAbsent(op.getOrderId(), k -> new ArrayList<>())
                            .add(name + "×" + (op.getNum() == null ? 0 : op.getNum()));
                }
                for (StockOrder o : orders) {
                    List<String> names = itemMap.get(o.getId());
                    if (names != null && !names.isEmpty()) {
                        productNameMap.put(o.getOrderNo(), String.join("，", names));
                    }
                }
            }
        }
        // 换货差价奖励：orderNo 是换货单号（HE...），不在订货单表，需回查换货单展示商品
        List<String> missExchangeNos = new ArrayList<>();
        for (String no : orderNos) {
            if (no.startsWith("HE") && !productNameMap.containsKey(no)) {
                missExchangeNos.add(no);
            }
        }
        if (!missExchangeNos.isEmpty()) {
            for (StockExchange ex : stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                    .in(StockExchange::getExchangeNo, missExchangeNos).eq(StockExchange::getIsDel, 0))) {
                String name = ex.getTargetProductName();
                if (name == null || name.isEmpty()) {
                    name = ex.getProductName();
                }
                if (name == null || name.isEmpty()) {
                    name = "商品" + ex.getTargetProductId();
                }
                productNameMap.put(ex.getExchangeNo(), name + "×" + (ex.getNum() == null ? 0 : ex.getNum()));
            }
        }
        for (StockReward r : list) {
            User u = r.getUid() == null ? null : userMap.get(r.getUid());
            r.setNickname(u == null ? "" : u.getNickname());
            User lu = r.getLinkUid() == null ? null : userMap.get(r.getLinkUid());
            r.setLinkNickname(lu == null ? "" : lu.getNickname());
            r.setLinkPhone(lu == null ? "" : lu.getPhone());
            r.setProductNames(productNameMap.getOrDefault(r.getOrderNo(), ""));
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
