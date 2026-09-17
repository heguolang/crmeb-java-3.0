package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockLadder;
import com.zbkj.common.model.stock.StockNotice;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockOrderProduct;
import com.zbkj.common.model.stock.StockReward;
import com.zbkj.common.model.stock.StockWithdraw;
import com.zbkj.common.model.user.User;
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
    private SystemConfigService systemConfigService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // ==================== 奖励核算 ====================

    @Override
    public void settleOrderReward(StockOrder order) {
        transactionTemplate.executeWithoutResult(status -> {
            // 1) 差价奖励
            calcDiffReward(order);
            // 2) 级差奖励（按单结算模式下立即结算）
            calcLadderReward(order);
            // 3) 平级奖励
            calcPeerReward(order);
        });
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

    /** 级差：沿上级链，团队业绩阶梯差额比例 × 本单金额（按单周期）；按月周期在此跳过，由月结处理 */
    private void calcLadderReward(StockOrder order) {
        if (!"1".equals(systemConfigService.getValueByKey("stock_ladder_status"))) {
            return;
        }
        if ("2".equals(systemConfigService.getValueByKey("stock_ladder_cycle"))) {
            // 按月结算：完成单只累计业绩，月结时统一发放
            return;
        }
        StockAgent start = stockService.getAgentById(order.getAgentId());
        if (start == null) {
            return;
        }
        List<StockLadder> ladders = getLadders();
        if (ladders.isEmpty()) {
            return;
        }
        Set<Integer> visited = new HashSet<>();
        StockAgent current = start;
        int depth = 0;
        while (current.getParentId() != null && current.getParentId() > 0 && depth < 50) {
            StockAgent ancestor = stockService.getAgentById(current.getParentId());
            if (ancestor == null || ancestor.getStatus() == 0 || !visited.add(ancestor.getId())) {
                break;
            }
            BigDecimal myTeamPerf = teamPerformance(ancestor, null);
            BigDecimal myRate = ladderRate(ladders, myTeamPerf);
            // 直接下级中团队业绩最大者的比例
            BigDecimal maxChildRate = BigDecimal.ZERO;
            List<StockAgent> children = directChildren(ancestor.getId());
            for (StockAgent child : children) {
                BigDecimal childPerf = teamPerformance(child, null);
                BigDecimal childRate = ladderRate(ladders, childPerf);
                if (childRate.compareTo(maxChildRate) > 0) {
                    maxChildRate = childRate;
                }
            }
            BigDecimal diffRate = myRate.subtract(maxChildRate);
            if (diffRate.signum() > 0) {
                BigDecimal reward = order.getTotalPrice().multiply(diffRate)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                if (reward.signum() > 0) {
                    createRewardIfAbsent(ancestor.getUid(), StockReward.TYPE_LADDER, order.getOrderNo(), order.getUid(),
                            order.getTotalPrice(), diffRate, reward,
                            "级差奖励：单 " + order.getOrderNo() + " 团队业绩 " + myTeamPerf + " 比例 " + diffRate + "%");
                }
            }
            current = ancestor;
            depth++;
        }
    }

    /**
     * 平级奖励：
     * 需求口径 —— A 推荐 B 成为【同级】代理，B 产生订货业绩时 A 拿 B 业绩的一定比例。
     * 实现：以下单代理 B 自身的层级为基准，沿其上级链向上找与 B 同层级的代理，
     *      最多往上拿 generations 代（stock_peer_generations，默认 1 = 只拿直接平推的同级）。
     */
    private void calcPeerReward(StockOrder order) {
        if (!"1".equals(systemConfigService.getValueByKey("stock_peer_status"))) {
            return;
        }
        String rateStr = systemConfigService.getValueByKey("stock_peer_rate");
        String genStr = systemConfigService.getValueByKey("stock_peer_generations");
        BigDecimal rate = parseDecimal(rateStr, BigDecimal.ZERO);
        int generations = parseInt(genStr, 1);
        if (rate.signum() <= 0 || generations <= 0) {
            return;
        }
        StockAgent start = stockService.getAgentById(order.getAgentId());
        if (start == null) {
            return;
        }
        // 沿上级链向上找与下单者同层级的代理
        int found = 0;
        StockAgent cursor = start;
        int depth = 0;
        while (found < generations && depth < 50) {
            if (cursor.getParentId() == null || cursor.getParentId() == 0) {
                break;
            }
            StockAgent up = stockService.getAgentById(cursor.getParentId());
            if (up == null) {
                break;
            }
            if (up.getLevelId().equals(start.getLevelId())) {
                if (up.getStatus() != null && up.getStatus() == 1) {
                    BigDecimal reward = order.getTotalPrice().multiply(rate)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    if (reward.signum() > 0) {
                        createRewardIfAbsent(up.getUid(), StockReward.TYPE_PEER, order.getOrderNo(), order.getUid(),
                                order.getTotalPrice(), rate, reward,
                                "平级奖励：同级下级 " + order.getUid() + " 单 " + order.getOrderNo()
                                        + " 比例 " + rate + "%（第" + (found + 1) + "代）");
                    }
                }
                found++;
            }
            cursor = up;
            depth++;
        }
    }

    /** 级差月结：重算指定月份（幂等：先失效该月已发级差再统一重发） */
    @Override
    public Boolean monthlySettle(String month) {
        if (!"1".equals(systemConfigService.getValueByKey("stock_ladder_status"))) {
            throw new CrmebException("级差奖励未开启");
        }
        String[] range = monthRange(month);
        return transactionTemplate.execute(status -> {
            // 失效该月已发级差
            List<StockReward> olds = stockRewardDao.selectList(new LambdaQueryWrapper<StockReward>()
                    .eq(StockReward::getType, StockReward.TYPE_LADDER)
                    .likeRight(StockReward::getMark, "级差月结|" + month));
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
                BigDecimal myTeamPerf = teamPerformance(agent, range);
                if (myTeamPerf.signum() <= 0) {
                    continue;
                }
                BigDecimal myRate = ladderRate(ladders, myTeamPerf);
                BigDecimal maxChildRate = BigDecimal.ZERO;
                for (StockAgent child : directChildren(agent.getId())) {
                    BigDecimal childPerf = teamPerformance(child, range);
                    BigDecimal childRate = ladderRate(ladders, childPerf);
                    if (childRate.compareTo(maxChildRate) > 0) {
                        maxChildRate = childRate;
                    }
                }
                BigDecimal diffRate = myRate.subtract(maxChildRate);
                if (diffRate.signum() <= 0) {
                    continue;
                }
                BigDecimal reward = myTeamPerf.multiply(diffRate)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                if (reward.signum() <= 0) {
                    continue;
                }
                StockReward r = new StockReward();
                r.setUid(agent.getUid());
                r.setType(StockReward.TYPE_LADDER);
                r.setSource(StockReward.SOURCE_ORDER);
                r.setOrderNo("month:" + month);
                r.setLinkUid(0);
                r.setBasePrice(myTeamPerf);
                r.setRate(diffRate);
                r.setRewardPrice(reward);
                r.setMark("级差月结|" + month + "|团队业绩 " + myTeamPerf + " 比例 " + diffRate + "%");
                r.setStatus(StockReward.STATUS_CREDITED);
                stockRewardDao.insert(r);
            }
            return true;
        }) != null;
    }

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> getMyBonus(Integer uid) {
        HashMap<String, Object> map = new HashMap<>();
        // 已入账奖励总额
        BigDecimal totalReward = sumReward(uid, null);
        // 可提现余额 = 已入账 - (待审核 + 已打款提现)
        BigDecimal lockedWithdraw = stockWithdrawDao.selectList(new LambdaQueryWrapper<StockWithdraw>()
                        .eq(StockWithdraw::getUid, uid).in(StockWithdraw::getStatus, 0, 1))
                .stream().map(StockWithdraw::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        map.put("totalReward", totalReward);
        map.put("balance", totalReward.subtract(lockedWithdraw).max(BigDecimal.ZERO));
        map.put("lockedWithdraw", lockedWithdraw);
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
        sendNotice(uid, StockNotice.TYPE_REWARD, "奖金到账",
                "订单 " + orderNo + " 产生奖金 ¥" + rewardPrice + "，已计入您的奖金账户");
    }

    private List<StockLadder> getLadders() {
        return stockLadderDao.selectList(new LambdaQueryWrapper<StockLadder>()
                .orderByAsc(StockLadder::getSort));
    }

    private BigDecimal ladderRate(List<StockLadder> ladders, BigDecimal performance) {
        for (StockLadder ladder : ladders) {
            boolean geMin = performance.compareTo(ladder.getMinAmount()) >= 0;
            boolean leMax = ladder.getMaxAmount() == null || ladder.getMaxAmount().signum() == 0
                    || performance.compareTo(ladder.getMaxAmount()) <= 0;
            if (geMin && leMax) {
                return ladder.getRate();
            }
        }
        return BigDecimal.ZERO;
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

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
