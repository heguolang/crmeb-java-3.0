package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockExchange;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockNotice;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockOrderProduct;
import com.zbkj.common.model.user.User;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockOrderAddRequest;
import com.zbkj.common.request.StockRequests;
import com.zbkj.service.dao.StockExchangeDao;
import com.zbkj.service.dao.StockLevelDao;
import com.zbkj.service.dao.StockOrderDao;
import com.zbkj.service.dao.StockOrderProductDao;
import com.zbkj.service.service.StoreProductService;
import com.zbkj.service.service.StockOrderService;
import com.zbkj.service.service.StockRewardService;
import com.zbkj.service.service.StockService;
import com.zbkj.service.service.SystemConfigService;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 订货系统-订单/换货服务实现
 */
@Service
public class StockOrderServiceImpl implements StockOrderService {

    @Autowired
    private StockOrderDao stockOrderDao;

    @Autowired
    private StockOrderProductDao stockOrderProductDao;

    @Autowired
    private StockExchangeDao stockExchangeDao;

    @Autowired
    private StockLevelDao stockLevelDao;

    @Autowired
    private StockService stockService;

    @Resource
    private UserService userService;

    @Resource
    private StoreProductService storeProductService;

    @Resource
    private SystemConfigService systemConfigService;

    @Autowired
    @Lazy
    private StockRewardService stockRewardService;

    @Resource
    private TransactionTemplate transactionTemplate;

    private static final String CFG_ORDER_AUDIT = "stock_order_audit";

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> createOrder(Integer uid, StockOrderAddRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() == 0) {
            throw new CrmebException("您还不是订货代理或已被禁用，无法下单");
        }
        StockAgent parent = agent.getParentId() != null && agent.getParentId() > 0
                ? stockService.getAgentById(agent.getParentId()) : null;
        if (agent.getParentId() != null && agent.getParentId() > 0 && (parent == null || parent.getStatus() == 0)) {
            throw new CrmebException("上级代理账号异常，请联系上级处理");
        }
        StockLevel level = stockLevelDao.selectById(agent.getLevelId());
        // 组装明细并算价
        List<StockOrderProduct> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalNum = 0;
        for (StockOrderAddRequest.StockOrderItem item : request.getItems()) {
            if (item.getNum() == null || item.getNum() <= 0) {
                throw new CrmebException("商品数量必须大于0");
            }
            StoreProduct product = storeProductService.getById(item.getProductId());
            if (product == null || product.getIsDel() || !product.getIsShow()) {
                throw new CrmebException("商品不存在或已下架");
            }
            if (product.getStock() < item.getNum()) {
                throw new CrmebException("商品【" + product.getStoreName() + "】云仓库存不足，当前库存：" + product.getStock());
            }
            BigDecimal price = stockService.getProductPrice(agent, product.getId());
            StockOrderProduct op = new StockOrderProduct();
            op.setProductId(product.getId());
            op.setProductName(product.getStoreName());
            op.setImage(product.getImage());
            op.setNum(item.getNum());
            op.setPrice(price);
            op.setParentPrice(parent == null ? BigDecimal.ZERO : stockService.getProductPrice(parent, product.getId()));
            op.setTotalPrice(price.multiply(new BigDecimal(item.getNum())));
            items.add(op);
            totalPrice = totalPrice.add(op.getTotalPrice());
            totalNum += item.getNum();
        }
        String orderNo = "SK" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        StockOrder order = new StockOrder();
        order.setOrderNo(orderNo);
        order.setUid(uid);
        order.setAgentId(agent.getId());
        order.setParentAgentId(parent == null ? 0 : parent.getId());
        order.setLevelName(level == null ? "" : level.getName());
        order.setTotalNum(totalNum);
        order.setTotalPrice(totalPrice);
        order.setPayType(request.getPayType() == null ? StockOrder.PAY_TYPE_RECORD : request.getPayType());
        order.setPayStatus(0);
        order.setMark(request.getMark() == null ? "" : request.getMark());
        order.setIsDel(0);
        // 审核开关：开=待上级审核；关=直接待付款（且直接视为已审核，扣库存在审核环节，此分支下单即扣）
        boolean needAudit = !"0".equals(systemConfigService.getValueByKey(CFG_ORDER_AUDIT));
        order.setStatus(needAudit ? StockOrder.STATUS_WAIT_PARENT_AUDIT : StockOrder.STATUS_WAIT_PAY);
        transactionTemplate.executeWithoutResult(status -> {
            stockOrderDao.insert(order);
            for (StockOrderProduct op : items) {
                op.setOrderId(order.getId());
                stockOrderProductDao.insert(op);
            }
            if (!needAudit) {
                // 无上级审核模式：下单直接扣云仓库存
                for (StockOrderProduct op : items) {
                    stockService.deductStock(op.getProductId(), op.getNum(), 1, orderNo, "下单直接扣库存（审核开关关闭）");
                }
            }
        });
        // 通知上级
        if (needAudit && parent != null) {
            User parentUser = userService.getById(parent.getUid());
            if (parentUser != null) {
                stockRewardService.sendNotice(parentUser.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单待审核",
                        "您的下级【" + nickOf(uid) + "】提交了订货单 " + orderNo + "，金额 ¥" + totalPrice + "，请及时审核");
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("orderNo", orderNo);
        map.put("totalPrice", totalPrice);
        map.put("payType", order.getPayType());
        return map;
    }

    @Override
    public CommonPage<StockOrder> getMyOrderList(Integer uid, Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getUid, uid).eq(StockOrder::getIsDel, 0);
        if (status != null) {
            lqw.eq(StockOrder::getStatus, status);
        }
        lqw.orderByDesc(StockOrder::getId);
        List<StockOrder> list = stockOrderDao.selectList(lqw);
        fillOrders(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public StockOrder getOrderDetail(Integer uid, Integer orderId) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        // 本人或上级链可见
        boolean visible = order.getUid().equals(uid);
        if (!visible) {
            visible = isAncestorAgent(uid, order.getAgentId());
        }
        if (!visible) {
            throw new CrmebException("无权查看该订单");
        }
        fillOrder(order);
        return order;
    }

    @Override
    public Boolean auditOrder(Integer uid, Integer orderId, StockRequests.StockAuditRequest request) {
        StockAgent parentAgent = stockService.getAgentByUid(uid);
        if (parentAgent == null) {
            throw new CrmebException("您不是订货代理，无权审核");
        }
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PARENT_AUDIT)) {
            throw new CrmebException("订单当前状态不可审核");
        }
        // 校验审核人是订单的直接上级
        if (order.getParentAgentId() == null || order.getParentAgentId() == 0
                || !parentAgent.getId().equals(order.getParentAgentId())) {
            throw new CrmebException("只有该订单的直接上级才能审核");
        }
        if (request.getStatus() == -1) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new CrmebException("驳回必须填写原因");
            }
            order.setStatus(StockOrder.STATUS_REJECT);
            order.setRejectReason(request.getReason().trim());
            stockOrderDao.updateById(order);
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单被驳回",
                    "您的订货单 " + order.getOrderNo() + " 被上级驳回，原因：" + request.getReason().trim());
            return true;
        }
        // 审核通过：扣云仓库存 -> 待付款
        return transactionTemplate.execute(status -> {
            List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                    .eq(StockOrderProduct::getOrderId, order.getId()));
            for (StockOrderProduct op : items) {
                stockService.deductStock(op.getProductId(), op.getNum(), 1, order.getOrderNo(), "上级审核通过扣库存");
            }
            order.setStatus(StockOrder.STATUS_WAIT_PAY);
            order.setAuditTime(new Date());
            stockOrderDao.updateById(order);
            return true;
        }) != null;
    }

    @Override
    public Boolean receiveOrder(Integer uid, Integer orderId) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getUid().equals(uid)) {
            throw new CrmebException("只有下单代理可确认收货");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_RECEIVE)) {
            throw new CrmebException("订单当前状态不可确认收货");
        }
        order.setStatus(StockOrder.STATUS_COMPLETE);
        order.setFinishTime(new Date());
        stockOrderDao.updateById(order);
        // 完成后核算奖励
        settleOrderReward(order);
        return true;
    }

    @Override
    public CommonPage<StockOrder> getAuditOrderList(Integer uid, Integer status, PageParamRequest page) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getParentAgentId, agent.getId()).eq(StockOrder::getIsDel, 0);
        if (status != null) {
            lqw.eq(StockOrder::getStatus, status);
        }
        lqw.orderByDesc(StockOrder::getId);
        List<StockOrder> list = stockOrderDao.selectList(lqw);
        fillOrders(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean applyExchange(Integer uid, StockRequests.StockExchangeApplyRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        StockOrder order = stockOrderDao.selectById(request.getOrderId());
        if (order == null || order.getIsDel() == 1 || !order.getUid().equals(uid)) {
            throw new CrmebException("原订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_COMPLETE)) {
            throw new CrmebException("只有已完成的订单才能申请换货");
        }
        StockOrderProduct item = stockOrderProductDao.selectOne(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId())
                .eq(StockOrderProduct::getProductId, request.getProductId()));
        if (item == null) {
            throw new CrmebException("该订单中没有此商品");
        }
        if (request.getNum() == null || request.getNum() <= 0 || request.getNum() > item.getNum()) {
            throw new CrmebException("换货数量不合法");
        }
        StockExchange exchange = new StockExchange();
        exchange.setExchangeNo("HE" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000)));
        exchange.setUid(uid);
        exchange.setAgentId(agent.getId());
        exchange.setParentAgentId(order.getParentAgentId());
        exchange.setOrderId(order.getId());
        exchange.setProductId(request.getProductId());
        exchange.setProductName(item.getProductName());
        exchange.setNum(request.getNum());
        exchange.setReason(request.getReason().trim());
        exchange.setStatus(StockExchange.STATUS_WAIT_PARENT_AUDIT);
        exchange.setIsDel(0);
        boolean ok = stockExchangeDao.insert(exchange) > 0;
        if (ok && exchange.getParentAgentId() != null && exchange.getParentAgentId() > 0) {
            StockAgent parent = stockService.getAgentById(exchange.getParentAgentId());
            if (parent != null) {
                stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单待审核",
                        "您的下级【" + nickOf(uid) + "】提交了换货申请 " + exchange.getExchangeNo() + "，请及时审核");
            }
        }
        return ok;
    }

    @Override
    public CommonPage<StockExchange> getMyExchangeList(Integer uid, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        List<StockExchange> list = stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getUid, uid).eq(StockExchange::getIsDel, 0)
                .orderByDesc(StockExchange::getId));
        fillExchanges(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean fillBackExpress(Integer uid, Integer exchangeId, StockRequests.StockExchangeBackRequest request) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1 || !exchange.getUid().equals(uid)) {
            throw new CrmebException("换货单不存在");
        }
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_BACK)) {
            throw new CrmebException("当前状态无需填写退回快递");
        }
        exchange.setBackExpressName(request.getBackExpressName());
        exchange.setBackExpressNum(request.getBackExpressNum());
        return stockExchangeDao.updateById(exchange) > 0;
    }

    // ==================== 后台 ====================

    @Override
    public CommonPage<StockOrder> getAdminOrderList(Integer uid, String orderNo, Integer status, Integer payStatus, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getIsDel, 0);
        if (uid != null && uid > 0) {
            lqw.eq(StockOrder::getUid, uid);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            lqw.like(StockOrder::getOrderNo, orderNo.trim());
        }
        if (status != null) {
            lqw.eq(StockOrder::getStatus, status);
        }
        if (payStatus != null) {
            lqw.eq(StockOrder::getPayStatus, payStatus);
        }
        lqw.orderByDesc(StockOrder::getId);
        List<StockOrder> list = stockOrderDao.selectList(lqw);
        fillOrders(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public Boolean confirmPay(Integer orderId) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (order.getPayStatus() == 1) {
            throw new CrmebException("该订单已付款");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PAY)) {
            throw new CrmebException("订单当前状态不可确认收款");
        }
        order.setPayStatus(1);
        order.setPayTime(new Date());
        order.setStatus(StockOrder.STATUS_WAIT_SEND);
        return stockOrderDao.updateById(order) > 0;
    }

    @Override
    public Boolean sendOrder(Integer orderId, StockRequests.StockSendRequest request) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_SEND)) {
            throw new CrmebException("订单当前状态不可发货");
        }
        order.setExpressName(request.getExpressName());
        order.setExpressNum(request.getExpressNum());
        order.setStatus(StockOrder.STATUS_WAIT_RECEIVE);
        order.setSendTime(new Date());
        boolean ok = stockOrderDao.updateById(order) > 0;
        if (ok) {
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_SEND, "订货单已发货",
                    "您的订货单 " + order.getOrderNo() + " 已由云仓发出，快递：" + request.getExpressName()
                            + " " + request.getExpressNum() + "，请留意查收");
        }
        return ok;
    }

    @Override
    public Boolean finishOrder(Integer orderId) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_RECEIVE)) {
            throw new CrmebException("订单当前状态不可标记完成");
        }
        order.setStatus(StockOrder.STATUS_COMPLETE);
        order.setFinishTime(new Date());
        stockOrderDao.updateById(order);
        settleOrderReward(order);
        return true;
    }

    @Override
    public Boolean auditExchangeByParent(Integer uid, Integer exchangeId, StockRequests.StockAuditRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您不是订货代理，无权审核");
        }
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_PARENT_AUDIT)) {
            throw new CrmebException("换货单当前状态不可审核");
        }
        if (!agent.getId().equals(exchange.getParentAgentId())) {
            throw new CrmebException("只有该换货单的直接上级才能审核");
        }
        if (request.getStatus() == -1) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new CrmebException("驳回必须填写原因");
            }
            exchange.setStatus(StockExchange.STATUS_REJECT);
            exchange.setRejectReason(request.getReason().trim());
            stockExchangeDao.updateById(exchange);
            return true;
        }
        exchange.setStatus(StockExchange.STATUS_WAIT_HQ_AUDIT);
        exchange.setAuditTime(new Date());
        return stockExchangeDao.updateById(exchange) > 0;
    }

    @Override
    public Boolean auditExchangeByHq(Integer exchangeId, StockRequests.StockAuditRequest request) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_HQ_AUDIT)) {
            throw new CrmebException("换货单当前状态不可总部审核");
        }
        if (request.getStatus() == -1) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new CrmebException("驳回必须填写原因");
            }
            exchange.setStatus(StockExchange.STATUS_REJECT);
            exchange.setRejectReason(request.getReason().trim());
            stockExchangeDao.updateById(exchange);
            return true;
        }
        exchange.setStatus(StockExchange.STATUS_WAIT_BACK);
        exchange.setHqAuditTime(new Date());
        return stockExchangeDao.updateById(exchange) > 0;
    }

    @Override
    public Boolean confirmExchangeBack(Integer exchangeId) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_BACK)) {
            throw new CrmebException("当前状态不可确认旧品入库");
        }
        return transactionTemplate.execute(status -> {
            // 旧品核验入库：回补库存
            stockService.addStock(exchange.getProductId(), exchange.getNum(), 4,
                    exchange.getExchangeNo(), "换货旧品退回核验入库");
            exchange.setStatus(StockExchange.STATUS_WAIT_SEND);
            exchange.setBackTime(new Date());
            return stockExchangeDao.updateById(exchange) > 0;
        }) != null;
    }

    @Override
    public Boolean sendExchangeNew(Integer exchangeId, StockRequests.StockSendRequest request) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_SEND)) {
            throw new CrmebException("当前状态不可发新品");
        }
        return transactionTemplate.execute(status -> {
            stockService.deductStock(exchange.getProductId(), exchange.getNum(), 3,
                    exchange.getExchangeNo(), "换货发出新品");
            exchange.setNewExpressName(request.getExpressName());
            exchange.setNewExpressNum(request.getExpressNum());
            exchange.setStatus(StockExchange.STATUS_COMPLETE);
            exchange.setSendTime(new Date());
            exchange.setFinishTime(new Date());
            boolean ok = stockExchangeDao.updateById(exchange) > 0;
            if (ok) {
                stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_SEND, "换货新品已发出",
                        "您的换货单 " + exchange.getExchangeNo() + " 新品已发出，快递：" + request.getExpressName()
                                + " " + request.getExpressNum());
            }
            return ok;
        }) != null;
    }

    @Override
    public CommonPage<StockExchange> getAdminExchangeList(Integer status, PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockExchange> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockExchange::getIsDel, 0);
        if (status != null) {
            lqw.eq(StockExchange::getStatus, status);
        }
        lqw.orderByDesc(StockExchange::getId);
        List<StockExchange> list = stockExchangeDao.selectList(lqw);
        fillExchanges(list);
        return CommonPage.restPage(new PageInfo<>(list));
    }

    @Override
    public StockExchange getExchange(Integer id) {
        StockExchange exchange = stockExchangeDao.selectById(id);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        List<StockExchange> one = new ArrayList<>();
        one.add(exchange);
        fillExchanges(one);
        return exchange;
    }

    // ==================== 奖励核算入口 ====================

    @Override
    public void settleOrderReward(StockOrder order) {
        stockRewardService.settleOrderReward(order);
    }

    // ==================== 内部 ====================

    /** uid 是否为 agentId 的祖先（上级链） */
    private boolean isAncestorAgent(Integer uid, Integer agentId) {
        StockAgent viewer = stockService.getAgentByUid(uid);
        if (viewer == null) {
            return false;
        }
        StockAgent current = stockService.getAgentById(agentId);
        int depth = 0;
        while (current != null && current.getParentId() != null && current.getParentId() > 0 && depth < 50) {
            if (current.getParentId().equals(viewer.getId())) {
                return true;
            }
            current = stockService.getAgentById(current.getParentId());
            depth++;
        }
        return false;
    }

    private String nickOf(Integer uid) {
        User user = userService.getById(uid);
        return user == null ? "用户" + uid : user.getNickname();
    }

    private void fillOrders(List<StockOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Integer> orderIds = new ArrayList<>();
        List<Integer> uids = new ArrayList<>();
        for (StockOrder o : orders) {
            orderIds.add(o.getId());
            uids.add(o.getUid());
        }
        HashMap<Integer, List<StockOrderProduct>> itemMap = new HashMap<>();
        for (StockOrderProduct op : stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .in(StockOrderProduct::getOrderId, orderIds))) {
            itemMap.computeIfAbsent(op.getOrderId(), k -> new ArrayList<>()).add(op);
        }
        HashMap<Integer, User> userMap = new HashMap<>();
        for (User u : userService.lambdaQuery().in(User::getUid, uids).list()) {
            userMap.put(u.getUid(), u);
        }
        for (StockOrder o : orders) {
            o.setProductList(itemMap.get(o.getId()));
            User u = userMap.get(o.getUid());
            o.setNickname(u == null ? "" : u.getNickname());
            o.setPhone(u == null ? "" : u.getPhone());
        }
    }

    private void fillOrder(StockOrder order) {
        List<StockOrder> one = new ArrayList<>();
        one.add(order);
        fillOrders(one);
    }

    private void fillExchanges(List<StockExchange> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Integer> uids = new ArrayList<>();
        List<Integer> orderIds = new ArrayList<>();
        for (StockExchange e : list) {
            uids.add(e.getUid());
            orderIds.add(e.getOrderId());
        }
        HashMap<Integer, User> userMap = new HashMap<>();
        for (User u : userService.lambdaQuery().in(User::getUid, uids).list()) {
            userMap.put(u.getUid(), u);
        }
        HashMap<Integer, String> orderNoMap = new HashMap<>();
        for (StockOrder o : stockOrderDao.selectBatchIds(orderIds)) {
            orderNoMap.put(o.getId(), o.getOrderNo());
        }
        for (StockExchange e : list) {
            User u = userMap.get(e.getUid());
            e.setNickname(u == null ? "" : u.getNickname());
            e.setOrderNo(orderNoMap.get(e.getOrderId()) == null ? "" : orderNoMap.get(e.getOrderId()));
        }
    }
}
