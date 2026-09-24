package com.zbkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.common.constants.Constants;
import com.zbkj.common.exception.CrmebException;
import com.zbkj.common.model.product.StoreProduct;
import com.zbkj.common.model.stock.StockAdjustLog;
import com.zbkj.common.model.stock.StockAgent;
import com.zbkj.common.model.stock.StockExchange;
import com.zbkj.common.model.stock.StockExchangeConfig;
import com.zbkj.common.model.stock.StockExchangeTarget;
import com.zbkj.common.model.stock.StockLevel;
import com.zbkj.common.model.stock.StockOfflineSale;
import com.zbkj.common.model.stock.StockNotice;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockOrderProduct;
import com.zbkj.common.model.stock.StockVirtualStock;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserAddress;
import com.zbkj.common.model.user.UserBill;
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
import com.zbkj.service.service.UserAddressService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

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
    private com.zbkj.service.dao.StockAgentDao stockAgentDao;

    @Resource
    private com.zbkj.service.dao.StockVirtualStockDao stockVirtualStockDao;

    @Resource
    private com.zbkj.service.dao.StockOfflineSaleDao stockOfflineSaleDao;

    @Resource
    private com.zbkj.service.dao.StockExchangeConfigDao stockExchangeConfigDao;

    @Resource
    private com.zbkj.service.dao.StockExchangeTargetDao stockExchangeTargetDao;

    @Resource
    private com.zbkj.service.dao.StockAdjustLogDao stockAdjustLogDao;

    @Resource
    private com.zbkj.service.service.UserBillService userBillService;

    @Autowired
    private StockService stockService;

    @Resource
    private UserService userService;

    @Resource
    private UserAddressService userAddressService;

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
    private static final String CFG_PARENT_DELIVER = "stock_parent_deliver";
    private static final String CFG_UP_SEARCH_HOURS = "stock_up_search_hours";
    private static final String CFG_WAIT_PAY_HOURS = "stock_wait_pay_hours";
    private static final String CFG_VIRTUAL_AUDIT = "stock_virtual_audit";
    private static final String CFG_EXCHANGE_ONCE = "stock_exchange_once";

    /** 实体换货是否需总部复核（1=需要，默认；0=上级审核通过直接进入旧品退回，总部仍可在后台介入） */
    private static final String CFG_EXCHANGE_HQ_AUDIT = "stock_exchange_hq_audit";

    /** 总部换货退回收件地址（上级为总部时展示给申请人寄回旧品用） */
    private static final String CFG_EXCHANGE_RETURN_ADDRESS = "stock_exchange_return_address";

    // ==================== 会员端 ====================

    @Override
    public HashMap<String, Object> createOrder(Integer uid, StockOrderAddRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            throw new CrmebException("您还不是订货代理或已被禁用，无法下单");
        }
        StockAgent parent = agent.getParentId() != null && agent.getParentId() > 0
                ? stockService.getAgentById(agent.getParentId()) : null;
        if (agent.getParentId() != null && agent.getParentId() > 0 && (parent == null || parent.getStatus() != 1)) {
            throw new CrmebException("上级代理账号异常，请联系上级处理");
        }
        // 懒处理：释放该代理已到期的"等待匹配"订单（付款单挂在状态10）
        processUpSearchOrders(agent);
        StockLevel level = stockLevelDao.selectById(agent.getLevelId());
        // 库存类型：1=实体 2=虚拟（虚拟单不发货，付款后入账虚拟库存）
        boolean isVirtual = StockOrder.STOCK_TYPE_VIRTUAL.equals(request.getStockType());
        // 组装明细并算价（取价商品级，skuKey 先落库为规格地基）
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
            // 商品是否支持所选库存类型（后台「商品与库存 → 设置拿货价」里配置）
            com.zbkj.common.model.stock.StockProductRel rel = stockService.getProductRel(product.getId());
            if (rel != null) {
                boolean supported = isVirtual
                        ? (rel.getSupportVirtual() == null || rel.getSupportVirtual())
                        : (rel.getSupportPhysical() == null || rel.getSupportPhysical());
                if (!supported) {
                    throw new CrmebException("商品【" + product.getStoreName() + "】不支持"
                            + (isVirtual ? "虚拟" : "实体") + "库存下单");
                }
            }
            String skuKey = item.getSkuKey() == null ? "" : item.getSkuKey().trim();
            // 云仓库存只约束实体单（虚拟单不涉及总部实物发货）；有规格时按规格库存校验
            if (!isVirtual) {
                if (!skuKey.isEmpty()) {
                    int skuStock = stockService.getSkuStock(product.getId(), skuKey);
                    if (skuStock < item.getNum()) {
                        throw new CrmebException("商品【" + product.getStoreName() + "】该规格云仓库存不足，当前库存：" + skuStock);
                    }
                } else if (product.getStock() < item.getNum()) {
                    throw new CrmebException("商品【" + product.getStoreName() + "】云仓库存不足，当前库存：" + product.getStock());
                }
            }
            BigDecimal price = stockService.getProductPrice(agent, product.getId(), skuKey);
            StockOrderProduct op = new StockOrderProduct();
            op.setProductId(product.getId());
            op.setSkuKey(skuKey);
            op.setProductName(product.getStoreName());
            op.setImage(product.getImage());
            op.setNum(item.getNum());
            op.setPrice(price);
            op.setParentPrice(parent == null ? BigDecimal.ZERO
                    : stockService.getProductPrice(parent, product.getId(), skuKey));
            op.setTotalPrice(price.multiply(new BigDecimal(item.getNum())));
            items.add(op);
            totalPrice = totalPrice.add(op.getTotalPrice());
            totalNum += item.getNum();
        }
        // 收货地址：实体单必填（复用系统收货地址簿，校验归属并快照）；虚拟单无需地址
        UserAddress address = null;
        if (!isVirtual) {
            address = userAddressService.getById(request.getAddressId());
            if (address == null || Boolean.TRUE.equals(address.getIsDel()) || !uid.equals(address.getUid())) {
                throw new CrmebException("请选择正确的收货地址");
            }
        }
        // 上级库存预判：仅用于下单提示，最终以付款成功时点的库存为准
        // 虚拟单看上级虚拟库存（虚拟卖空不得用实体兜底），实体单看实体可供应量
        boolean parentStockOk = parentCanSupply(parent, items, null, isVirtual);
        String orderNo = "SK" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        StockOrder order = new StockOrder();
        order.setOrderNo(orderNo);
        order.setUid(uid);
        order.setAgentId(agent.getId());
        order.setParentAgentId(parent == null ? 0 : parent.getId());
        order.setLevelName(level == null ? "" : level.getName());
        order.setTotalNum(totalNum);
        order.setTotalPrice(totalPrice);
        order.setPayStatus(0);
        // 下单默认进入待付款
        order.setStatus(StockOrder.STATUS_WAIT_PAY);
        order.setStockType(isVirtual ? StockOrder.STOCK_TYPE_VIRTUAL : StockOrder.STOCK_TYPE_PHYSICAL);
        order.setOrderType(StockOrder.ORDER_TYPE_PURCHASE);
        if (address != null) {
            order.setAddressId(address.getId());
            order.setRealName(address.getRealName());
            order.setPhone(address.getPhone());
            order.setUserAddress(buildAddressStr(address));
        }
        order.setMark(request.getMark() == null ? "" : request.getMark());
        order.setIsDel(0);
        transactionTemplate.executeWithoutResult(status -> {
            stockOrderDao.insert(order);
            for (StockOrderProduct op : items) {
                op.setOrderId(order.getId());
                stockOrderProductDao.insert(op);
            }
        });
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("orderNo", orderNo);
        map.put("totalPrice", totalPrice);
        map.put("payStatus", 0);
        map.put("upSearchWaiting", !parentStockOk);
        if (!parentStockOk) {
            map.put("upSearchMessage", "最近上级【" + nickOf(parent.getUid()) + "】"
                    + (isVirtual ? "虚拟库存不足" : "暂无库存") + "，仍可下单付款；"
                    + (isVirtual ? "付款时能供上的部分正常入账，不足部分自动拆为等待单，" : "付款后订单将进入等待队列，")
                    + getUpSearchHours()
                    + "小时内上级补货可直接进入审核发货，超时系统将自动向上匹配有货的上级，差价在最终审核发货的上级处结算");
        }
        return map;
    }

    @Override
    public StockOrder getByOrderNo(String orderNo) {
        return stockOrderDao.selectOne(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getOrderNo, orderNo).eq(StockOrder::getIsDel, 0).last(" limit 1"));
    }

    @Override
    public Boolean payStockOrder(String orderNo, Integer payType) {
        StockOrder order = getByOrderNo(orderNo);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        // 幂等：已付款直接返回成功
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return true;
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PAY)) {
            throw new CrmebException("订单当前状态不可支付");
        }
        // 付款时点重算上级库存（等待期间上级可能补货/库存被其他订单占用）
        StockAgent parent = order.getParentAgentId() != null && order.getParentAgentId() > 0
                ? stockService.getAgentById(order.getParentAgentId()) : null;
        List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId()));
        boolean needAudit = !"0".equals(systemConfigService.getValueByKey(CFG_ORDER_AUDIT));
        boolean isVirtual = isVirtualOrder(order);
        // 虚拟单审核开关：1=付款后仍需上级审核，0=付款即完成入账
        boolean virtualNeedAudit = isVirtual && "1".equals(systemConfigService.getValueByKey(CFG_VIRTUAL_AUDIT));
        // 上级库存判定：虚拟单只看上级虚拟库存（虚拟卖空不能用实体兜底），实体单沿用实体可供应量
        boolean parentStockOk;
        StockOrder splitWaitOrder = null;
        // 事务 lambda 内只能引用有效 final 变量，先落一份原始明细引用（后面 items 会被重读覆盖）
        final List<StockOrderProduct> rawItems = items;
        if (isVirtual && parent != null && !parentHasVirtualStock(parent, items)) {
            // 虚拟库存不足：先尝试部分供货 —— 能供的留在原单正常完成，缺口拆成一张「等待匹配」子单
            splitWaitOrder = transactionTemplate.execute(status ->
                    splitVirtualOrderIfShort(order, rawItems, parent, payType));
            parentStockOk = splitWaitOrder != null;
            if (parentStockOk) {
                // 原单数量/金额已被收敛，重读明细供后续入账使用
                items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                        .eq(StockOrderProduct::getOrderId, order.getId()));
            }
        } else {
            // 虚拟单上级虚拟库存充足时按虚拟口径判定（不受实体库存影响）；实体单沿用实体可供应量
            parentStockOk = parentCanSupply(parent, items, null, isVirtual);
        }
        final List<StockOrderProduct> payItems = items;
        transactionTemplate.executeWithoutResult(status ->
                applyPaidTransition(order, payItems, payType, parentStockOk, needAudit, isVirtual, virtualNeedAudit));
        // 部分分货：告知会员缺口部分已进等待队列
        if (splitWaitOrder != null) {
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "部分商品进入等待队列",
                    "您的订单 " + orderNo + " 中 " + splitWaitOrder.getTotalNum() + " 件因上级虚拟库存不足"
                            + "已拆分为等待单 " + splitWaitOrder.getOrderNo() + "，" + getUpSearchHours()
                            + "小时内上级补货可直接完成，超时系统将自动向上匹配有货的上级");
        }
        // 事务外通知与升级
        if (parentStockOk) {
            if (isVirtual && virtualNeedAudit && parent != null) {
                stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_AUDIT, "虚拟库存单待审核",
                        "您的下级【" + nickOf(order.getUid()) + "】的虚拟库存单 " + orderNo
                                + " 已完成付款，金额 ¥" + order.getTotalPrice() + "，请及时审核");
            }
            if (!isVirtual && needAudit && parent != null) {
                stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单待审核",
                        "您的下级【" + nickOf(order.getUid()) + "】的订货单 " + orderNo
                                + " 已完成付款，金额 ¥" + order.getTotalPrice() + "，请及时审核发货");
            }
            if (!isVirtual && !needAudit) {
                stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单支付成功",
                        "您的订货单 " + orderNo + " 支付成功，等待发货");
                // 上级发货模式：提醒上级及时发货
                notifyParentDeliver(getByOrderNo(orderNo));
            }
            if (isVirtual && !virtualNeedAudit) {
                stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "虚拟库存入账成功",
                        "您的订货单 " + orderNo + " 支付成功，虚拟库存已入账，可在会员中心【虚拟库存】中提货");
                // 虚拟单付款即完成：差价结算给上级
                settleOrderReward(getByOrderNo(orderNo));
            }
            try {
                stockService.checkAndUpgrade(order.getUid());
            } catch (Exception ignored) {
            }
        } else {
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订单等待匹配上级",
                    "您的订货单 " + orderNo + " 已付款，因上级库存不足进入等待队列，"
                            + getUpSearchHours() + "小时内上级补货可直接审核发货，超时系统将自动向上匹配有货的上级");
        }
        return true;
    }

    /**
     * 付款成功后的统一状态流转（须在事务内调用）
     * 实体单：有货 needAudit -> 0 待上级审核；!needAudit -> 2 待发货并扣云仓库存
     * 虚拟单：有货 virtualNeedAudit -> 0 待上级审核；否则 -> 4 完成并入账虚拟库存（不扣云仓）
     * 无货：-> 10 等待匹配（匹配释放时再流转）
     */
    private void applyPaidTransition(StockOrder order, List<StockOrderProduct> items, Integer payType,
                                     boolean parentStockOk, boolean needAudit, boolean isVirtual, boolean virtualNeedAudit) {
        LambdaUpdateWrapper<StockOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StockOrder::getId, order.getId())
                .eq(StockOrder::getPayStatus, 0)
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_PAY)
                .set(StockOrder::getPayStatus, 1)
                .set(StockOrder::getPayTime, new Date());
        if (payType != null) {
            wrapper.set(StockOrder::getPayType, payType);
        }
        if (parentStockOk) {
            if (isVirtual) {
                if (virtualNeedAudit) {
                    wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT);
                } else {
                    wrapper.set(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                            .set(StockOrder::getFinishTime, new Date());
                }
            } else if (needAudit) {
                wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT);
            } else {
                wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_SEND);
            }
        } else {
            wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                    .set(StockOrder::getUpSearchTime, new Date())
                    .set(StockOrder::getUpSearchNum, 0);
        }
        boolean updated = stockOrderDao.update(null, wrapper) > 0;
        if (!updated) {
            throw new CrmebException("订单状态已变更，请刷新后重试");
        }
        if (parentStockOk && isVirtual && !virtualNeedAudit) {
            // 虚拟单付款即完成：虚拟库存入账（不扣云仓）
            for (StockOrderProduct op : items) {
                creditVirtualStock(order, op);
            }
        }
        if (parentStockOk && !isVirtual && !needAudit) {
            for (StockOrderProduct op : items) {
                stockService.deductStockBySku(op.getProductId(), op.getSkuKey(), op.getNum(), 1,
                        order.getOrderNo(), "订货单付款成功扣库存（审核开关关闭）");
            }
        }
    }

    @Override
    public Boolean cancelStockOrder(Integer uid, Integer orderId) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getUid().equals(uid)) {
            throw new CrmebException("只有下单代理可取消订单");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PAY)) {
            throw new CrmebException("仅待付款订单可取消");
        }
        return stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                .eq(StockOrder::getId, order.getId())
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_PAY)
                .set(StockOrder::getStatus, StockOrder.STATUS_CANCEL)
                .set(StockOrder::getCancelTime, new Date())) > 0;
    }

    @Override
    public void processExpiredUpSearchOrders() {
        Date expire = new Date(System.currentTimeMillis() - getUpSearchHours() * 3600_000L);
        List<StockOrder> expired = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                .eq(StockOrder::getIsDel, 0)
                .isNotNull(StockOrder::getUpSearchTime)
                .le(StockOrder::getUpSearchTime, expire));
        if (expired.isEmpty()) {
            return;
        }
        for (StockOrder order : expired) {
            try {
                StockAgent agent = stockService.getAgentById(order.getAgentId());
                if (agent != null) {
                    matchUpOrder(order, agent);
                }
            } catch (Exception e) {
                // 单笔失败不影响其他订单
            }
        }
    }

    @Override
    public void cancelExpiredUnpaidOrders() {
        int hours = getWaitPayHours();
        Date expire = new Date(System.currentTimeMillis() - hours * 3600_000L);
        List<StockOrder> unpaid = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_PAY)
                .eq(StockOrder::getPayStatus, 0)
                .eq(StockOrder::getIsDel, 0)
                .le(StockOrder::getCreateTime, expire));
        if (unpaid.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (StockOrder order : unpaid) {
            stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                    .eq(StockOrder::getId, order.getId())
                    .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_PAY)
                    .set(StockOrder::getStatus, StockOrder.STATUS_CANCEL)
                    .set(StockOrder::getCancelTime, now));
        }
    }

    // ==================== 虚拟库存（二期） ====================

    @Override
    public List<StockVirtualStock> getMyVirtualStock(Integer uid) {
        List<StockVirtualStock> list = stockVirtualStockDao.selectList(new LambdaQueryWrapper<StockVirtualStock>()
                .eq(StockVirtualStock::getUid, uid)
                .eq(StockVirtualStock::getIsDel, 0)
                .gt(StockVirtualStock::getRemainNum, 0)
                .orderByDesc(StockVirtualStock::getId));
        // 附加「换货入库」累计：虚拟库存入账会并入已有行，无法从本表区分来源，改为从已完成换货单推导
        if (!list.isEmpty()) {
            Map<String, Integer> exIn = new HashMap<>();
            for (StockExchange e : stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                    .eq(StockExchange::getUid, uid)
                    .eq(StockExchange::getIsDel, 0)
                    .eq(StockExchange::getStatus, StockExchange.STATUS_COMPLETE)
                    .eq(StockExchange::getTargetStockType, 2)
                    .gt(StockExchange::getNum, 0))) {
                String key = e.getTargetProductId() + "_" + (e.getTargetSkuKey() == null ? "" : e.getTargetSkuKey());
                exIn.merge(key, e.getNum(), Integer::sum);
            }
            for (StockVirtualStock v : list) {
                String key = v.getProductId() + "_" + (v.getSkuKey() == null ? "" : v.getSkuKey());
                v.setExchangeInNum(exIn.getOrDefault(key, 0));
            }
            // 兼容历史脏行：换货入库新建行时未写商品名/图片（2026-09-21 前），读取时按商品回填，
            // 否则会员端该卡片没有图、没有名称；只补内存不回写，避免读接口产生写操作。
            List<Integer> missingIds = new ArrayList<>();
            for (StockVirtualStock v : list) {
                boolean noName = v.getProductName() == null || v.getProductName().trim().isEmpty();
                boolean noImage = v.getImage() == null || v.getImage().trim().isEmpty();
                if ((noName || noImage) && v.getProductId() != null
                        && !missingIds.contains(v.getProductId())) {
                    missingIds.add(v.getProductId());
                }
            }
            if (!missingIds.isEmpty()) {
                Map<Integer, StoreProduct> productMap = new HashMap<>();
                for (StoreProduct p : storeProductService.listByIds(missingIds)) {
                    productMap.put(p.getId(), p);
                }
                for (StockVirtualStock v : list) {
                    StoreProduct p = productMap.get(v.getProductId());
                    if (p == null) {
                        continue;
                    }
                    if (v.getProductName() == null || v.getProductName().trim().isEmpty()) {
                        v.setProductName(p.getStoreName() == null ? "" : p.getStoreName());
                    }
                    if (v.getImage() == null || v.getImage().trim().isEmpty()) {
                        v.setImage(p.getImage() == null ? "" : p.getImage());
                    }
                }
            }
        }
        return list;
    }

    @Override
    public List<HashMap<String, Object>> getMyPhysicalStock(Integer uid) {
        List<HashMap<String, Object>> result = new ArrayList<>();
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            return result;
        }
        // 按商品聚合：【已完成】的实体采购单数量（虚拟单只入虚拟库存，不占云仓，故排除）
        // 口径：订单走到「已完成」（确认收货）才算货到手，才计入可支配实体库存；
        // 已付款但在途的单不计入，改为下方的「在途」单独展示，避免付款即虚增库存。
        List<StockOrder> myOrders = stockOrderDao.selectList(physicalStockInWrapper()
                .eq(StockOrder::getAgentId, agent.getId()));
        Map<Integer, Integer> purchased = new HashMap<>();
        Map<Integer, StockOrderProduct> productInfo = new HashMap<>();
        accumulateOrderProducts(myOrders, purchased, productInfo);
        // 在途数量：已付款但尚未完成的实体采购单（等待匹配/待上级审核/待发货/待收货），只展示不参与计算
        List<StockOrder> inTransitOrders = stockOrderDao.selectList(physicalPurchaseWrapper()
                .eq(StockOrder::getAgentId, agent.getId())
                .in(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH,
                        StockOrder.STATUS_WAIT_PARENT_AUDIT,
                        StockOrder.STATUS_WAIT_SEND,
                        StockOrder.STATUS_WAIT_RECEIVE));
        Map<Integer, Integer> inTransit = new HashMap<>();
        accumulateOrderProducts(inTransitOrders, inTransit, null);
        // 已供应给直接下级的数量（下级已付款实体采购单 —— 此处仍是「付款即占用」口径，防超卖）
        List<StockOrder> childOrders = stockOrderDao.selectList(physicalPurchaseWrapper()
                .eq(StockOrder::getParentAgentId, agent.getId()));
        Map<Integer, Integer> supplied = new HashMap<>();
        accumulateOrderProducts(childOrders, supplied, null);
        // 线下销售出库数量（从可供应量中扣减）
        Map<Integer, Integer> sold = new HashMap<>();
        for (StockOfflineSale s : stockOfflineSaleDao.selectList(new LambdaQueryWrapper<StockOfflineSale>()
                .eq(StockOfflineSale::getAgentId, agent.getId())
                .eq(StockOfflineSale::getIsDel, 0))) {
            sold.merge(s.getProductId(), s.getNum() == null ? 0 : s.getNum(), Integer::sum);
        }
        // 后台实体库存调整（正=增加，负=扣减）
        Map<Integer, Integer> adjustMap = new HashMap<>();
        for (StockAdjustLog a : stockAdjustLogDao.selectList(new LambdaQueryWrapper<StockAdjustLog>()
                .eq(StockAdjustLog::getAgentId, agent.getId())
                .eq(StockAdjustLog::getStockType, StockAdjustLog.STOCK_TYPE_PHYSICAL)
                .eq(StockAdjustLog::getIsDel, 0))) {
            adjustMap.merge(a.getProductId(), a.getNum() == null ? 0 : a.getNum(), Integer::sum);
        }
        // 净持有量 > 0 的商品（含已完成换货的换入/+、换出/-，以及下级换货发出新品对上级的扣减）
        // 换货中（未完成未驳回）的换出数量单独锁定：可供应量 = 净持有 - 换货占用，换货中数量单独返回供前端展示
        Map<Integer, Integer> exDelta = exchangeStockDeltaMap(agent.getId());
        Map<Integer, Integer> exOutDelta = exchangeOutDeltaMap(agent.getId());
        Map<Integer, Integer> exPending = exchangePendingDeltaMap(agent.getId());
        java.util.Set<Integer> pids = new java.util.HashSet<>(purchased.keySet());
        pids.addAll(exDelta.keySet());
        pids.addAll(exOutDelta.keySet());
        pids.addAll(exPending.keySet());
        pids.addAll(adjustMap.keySet());
        pids.addAll(inTransit.keySet());
        for (Integer pid : pids) {
            int net = purchased.getOrDefault(pid, 0)
                    - supplied.getOrDefault(pid, 0)
                    - sold.getOrDefault(pid, 0)
                    + adjustMap.getOrDefault(pid, 0)
                    + exDelta.getOrDefault(pid, 0)
                    + exOutDelta.getOrDefault(pid, 0);
            // pendingRaw 为负数（占用记 -num），取其绝对值作为「换货中锁定量」，且不超过净持有量
            int pendingRaw = exPending.getOrDefault(pid, 0);
            int pending = pendingRaw < 0 ? Math.min(-pendingRaw, Math.max(net, 0)) : 0;
            // 在途（已付款未收货）：只展示，不参与可供应量
            int onWay = inTransit.getOrDefault(pid, 0);
            // 净库存、换货锁定、在途全为 0 才不展示 —— 只有在途的商品也要出一行，
            // 否则用户会以为"我付了钱的货不见了"
            if (net <= 0 && pending <= 0 && onWay <= 0) {
                continue;
            }
            HashMap<String, Object> row = new HashMap<>();
            row.put("productId", pid);
            StockOrderProduct op = productInfo.get(pid);
            if (op != null) {
                row.put("productName", op.getProductName());
                row.put("image", op.getImage());
            } else {
                StoreProduct sp = storeProductService.getById(pid);
                row.put("productName", sp == null ? ("商品" + pid) : sp.getStoreName());
                row.put("image", sp == null ? "" : sp.getImage());
            }
            // 可供应量兜底不为负：入库口径改严后，净额可能为负（货还没到、下级已下单占用）
            row.put("num", Math.max(net - pending, 0));
            row.put("exchangeNum", pending);
            row.put("inTransitNum", onWay);
            result.add(row);
        }
        result.sort((a, b) -> Integer.compare(
                (Integer) b.getOrDefault("num", 0), (Integer) a.getOrDefault("num", 0)));
        return result;
    }

    /**
     * 实体采购单【占用侧】查询条件：已付款 + 采购单(order_type=1) + 实体库存单(stock_type=1 或历史空值) + 未删除
     *
     * 用途：推导「上级已被占用掉多少可供应量」。下级一付款即占用上级库存，防止上级把同一批货重复卖给多个下级。
     * 虚拟采购单只入虚拟库存、不占云仓；提货单(order_type=2)不参与实体库存推导。
     * 注意：必须排除已驳回(-1)与已取消(-2)的订单，否则上级驳回后仍占用上级可供应量，导致库存无法释放。
     */
    private LambdaQueryWrapper<StockOrder> physicalPurchaseWrapper() {
        return new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getPayStatus, 1)
                .eq(StockOrder::getOrderType, StockOrder.ORDER_TYPE_PURCHASE)
                .and(w -> w.eq(StockOrder::getStockType, StockOrder.STOCK_TYPE_PHYSICAL)
                        .or().isNull(StockOrder::getStockType))
                .notIn(StockOrder::getStatus, StockOrder.STATUS_REJECT, StockOrder.STATUS_CANCEL)
                .eq(StockOrder::getIsDel, 0);
    }

    /**
     * 实体采购单【入库侧】查询条件：已完成 + 采购单(order_type=1) + 实体库存单(stock_type=1 或历史空值) + 未删除
     *
     * 用途：推导「我自己有多少可支配实体库存」。**订单走到「已完成」（确认收货）才算货到手**，
     * 钱付了但货还在途（待审核/待发货/待收货/等待匹配）一律不上账，避免"付款即虚增库存"。
     * 与占用侧 physicalPurchaseWrapper 刻意分开，两者口径不对称是业务要求：
     *   入库＝完成才 +（货真到手）；占用＝下级付款即 −（防超卖）。
     * 已完成必然已付款，故无需再判 payStatus；-1/-2 已被 status=4 天然排除。
     */
    private LambdaQueryWrapper<StockOrder> physicalStockInWrapper() {
        return new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                .eq(StockOrder::getOrderType, StockOrder.ORDER_TYPE_PURCHASE)
                .and(w -> w.eq(StockOrder::getStockType, StockOrder.STOCK_TYPE_PHYSICAL)
                        .or().isNull(StockOrder::getStockType))
                .eq(StockOrder::getIsDel, 0);
    }

    /** 累计订单明细数量到聚合表（infoMap 可为 null 则不记录商品快照） */
    private void accumulateOrderProducts(List<StockOrder> orders,
                                         Map<Integer, Integer> numMap,
                                         Map<Integer, StockOrderProduct> infoMap) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<Integer> orderIds = new ArrayList<>();
        for (StockOrder o : orders) {
            orderIds.add(o.getId());
        }
        for (StockOrderProduct op : stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .in(StockOrderProduct::getOrderId, orderIds))) {
            numMap.merge(op.getProductId(), op.getNum() == null ? 0 : op.getNum(), Integer::sum);
            if (infoMap != null && !infoMap.containsKey(op.getProductId())) {
                infoMap.put(op.getProductId(), op);
            }
        }
    }

    @Override
    public HashMap<String, Object> pickupVirtual(Integer uid, StockRequests.StockVirtualPickupRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            throw new CrmebException("您还不是订货代理或已被禁用");
        }
        if (request.getNum() == null || request.getNum() <= 0) {
            throw new CrmebException("提货数量必须大于0");
        }
        StockVirtualStock vs = stockVirtualStockDao.selectById(request.getVirtualId());
        if (vs == null || vs.getIsDel() == 1 || !vs.getUid().equals(uid)) {
            throw new CrmebException("虚拟库存记录不存在");
        }
        if (vs.getRemainNum() == null || vs.getRemainNum() < request.getNum()) {
            throw new CrmebException("可提货数量不足，当前剩余：" + vs.getRemainNum());
        }
        UserAddress address = userAddressService.getById(request.getAddressId());
        if (address == null || Boolean.TRUE.equals(address.getIsDel()) || !uid.equals(address.getUid())) {
            throw new CrmebException("请选择正确的收货地址");
        }
        StoreProduct product = storeProductService.getById(vs.getProductId());
        if (product == null || product.getIsDel() || !product.getIsShow()) {
            throw new CrmebException("商品不存在或已下架，无法提货");
        }
        if (product.getStock() < request.getNum()) {
            throw new CrmebException("总部云仓库存不足，当前库存：" + product.getStock() + "，请稍后再试");
        }
        String orderNo = "SK" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
        StockOrder order = new StockOrder();
        order.setOrderNo(orderNo);
        order.setUid(uid);
        order.setAgentId(agent.getId());
        order.setParentAgentId(vs.getParentAgentId() == null ? 0 : vs.getParentAgentId());
        order.setLevelName("");
        order.setTotalNum(request.getNum());
        order.setTotalPrice(BigDecimal.ZERO);
        // 虚拟库存抵扣，无资金流：标记已付，金额 0
        order.setPayType(StockOrder.PAY_TYPE_RECORD);
        order.setPayStatus(1);
        // 提货单直接进入待发货，由总部发货扣云仓
        order.setStatus(StockOrder.STATUS_WAIT_SEND);
        order.setStockType(StockOrder.STOCK_TYPE_VIRTUAL);
        order.setOrderType(StockOrder.ORDER_TYPE_PICKUP);
        order.setAddressId(address.getId());
        order.setRealName(address.getRealName());
        order.setPhone(address.getPhone());
        order.setUserAddress(buildAddressStr(address));
        order.setMark(request.getMark() == null ? "虚拟库存提货" : request.getMark());
        order.setIsDel(0);
        StockOrderProduct op = new StockOrderProduct();
        op.setProductId(vs.getProductId());
        op.setSkuKey(vs.getSkuKey() == null ? "" : vs.getSkuKey());
        op.setProductName(vs.getProductName());
        op.setImage(vs.getImage());
        op.setNum(request.getNum());
        op.setPrice(BigDecimal.ZERO);
        op.setParentPrice(BigDecimal.ZERO);
        op.setTotalPrice(BigDecimal.ZERO);
        transactionTemplate.executeWithoutResult(status -> {
            // 乐观扣减虚拟库存，防超提
            int rows = stockVirtualStockDao.update(null, new LambdaUpdateWrapper<StockVirtualStock>()
                    .eq(StockVirtualStock::getId, vs.getId())
                    .ge(StockVirtualStock::getRemainNum, request.getNum())
                    .setSql("remain_num = remain_num - " + request.getNum()));
            if (rows == 0) {
                throw new CrmebException("可提货数量不足，请刷新后重试");
            }
            stockOrderDao.insert(order);
            op.setOrderId(order.getId());
            stockOrderProductDao.insert(op);
            // 虚拟库存出库留痕：不写台账的话，会员端「库存记录-虚拟库存」看不到这次提货扣减
            StockAdjustLog log = new StockAdjustLog();
            log.setAgentId(agent.getId());
            log.setUid(uid);
            log.setLinkUid(0);
            log.setProductId(vs.getProductId());
            log.setSkuKey(vs.getSkuKey() == null ? "" : vs.getSkuKey());
            log.setNum(-request.getNum());
            log.setStockType(StockAdjustLog.STOCK_TYPE_VIRTUAL);
            log.setMark("虚拟库存提货（订单 " + orderNo + "），总部直接发货，本次扣减 " + request.getNum());
            log.setIsDel(0);
            stockAdjustLogDao.insert(log);
        });
        // 通知原上级（有单据记录语义）
        if (order.getParentAgentId() != null && order.getParentAgentId() > 0) {
            StockAgent parent = stockService.getAgentById(order.getParentAgentId());
            if (parent != null) {
                stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_SEND, "下级虚拟提货",
                        "您的下级【" + nickOf(uid) + "】使用虚拟库存提货 " + orderNo
                                + "（" + vs.getProductName() + " ×" + request.getNum() + "），由总部直接发货");
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", order.getId());
        map.put("orderNo", orderNo);
        map.put("num", request.getNum());
        map.put("remainNum", vs.getRemainNum() - request.getNum());
        return map;
    }

    @Override
    public CommonPage<StockOrder> getMyOrderList(Integer uid, Integer status, PageParamRequest page) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent != null) {
            // 懒处理：查询时把到期的“向上找上级”等待订单释放掉
            try {
                processUpSearchOrders(agent);
            } catch (Exception ignored) {
            }
            // 懒处理：直接上级已补货的等待订单立即释放回正常订货流程（不必等满等待时长）
            try {
                if (agent.getParentId() != null && agent.getParentId() > 0) {
                    releaseRestockedOrdersForParent(stockService.getAgentById(agent.getParentId()));
                }
            } catch (Exception ignored) {
            }
        }
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
    public CommonPage<StockOrder> getSubAgentOrderList(Integer uid, Integer subUid, Integer status, PageParamRequest page) {
        if (subUid == null) {
            throw new CrmebException("请选择要查看的成员");
        }
        if (uid.equals(subUid)) {
            throw new CrmebException("请在本页查看自己的订单");
        }
        StockAgent sub = stockService.getAgentByUid(subUid);
        if (sub == null) {
            throw new CrmebException("该成员还不是订货代理");
        }
        // 权限校验：只能查看自己下级链路上的成员订单
        if (!isAncestorAgent(uid, sub.getId())) {
            throw new CrmebException("只能查看自己下级成员的订单");
        }
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getUid, subUid).eq(StockOrder::getIsDel, 0);
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
        // 校验审核人是订单的直接上级
        if (order.getParentAgentId() == null || order.getParentAgentId() == 0
                || !parentAgent.getId().equals(order.getParentAgentId())) {
            throw new CrmebException("只有该订单的直接上级才能审核");
        }
        if (StockOrder.STATUS_WAIT_MATCH.equals(order.getStatus())) {
            // 下单时上级无库存而挂起的订单（10）：上级仍应看到并处理。
            // 通过审核前必须先补足库存；已补货则即时归位为待上级审核，继续走正常审核流程。
            if (request.getStatus() != -1) {
                List<StockOrderProduct> waitItems = stockOrderProductDao.selectList(
                        new LambdaQueryWrapper<StockOrderProduct>().eq(StockOrderProduct::getOrderId, order.getId()));
                if (!parentCanSupply(parentAgent, waitItems, order.getId(), isVirtualOrder(order))) {
                    throw new CrmebException("您的" + (isVirtualOrder(order) ? "虚拟库存" : "库存")
                            + "不足，暂不可审核通过该订单；请尽快补货后再审核，"
                            + getUpSearchHours() + "小时内未补货，系统将自动把该订单向上匹配给有货的上级");
                }
                if (!restoreWaitMatchToAudit(order)) {
                    throw new CrmebException("订单状态已变更，请刷新后重试");
                }
            }
        } else if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PARENT_AUDIT)) {
            throw new CrmebException("订单当前状态不可审核");
        }
        if (request.getStatus() == -1) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new CrmebException("驳回必须填写原因");
            }
            order.setStatus(StockOrder.STATUS_REJECT);
            order.setRejectReason(request.getReason().trim());
            stockOrderDao.updateById(order);
            transactionTemplate.executeWithoutResult(status ->
                    refundOrderToBalance(order, "上级驳回"));
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单被驳回",
                    "您的订货单 " + order.getOrderNo() + " 被上级驳回，原因：" + request.getReason().trim()
                            + "。已支付货款 ¥" + order.getTotalPrice() + " 已退回您的余额");
            return true;
        }
        // 审核通过前校验上级库存（排除本单自身占用量，避免"补足货仍判库存不足"）
        List<StockOrderProduct> auditItems = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId()));
        if (!parentCanSupply(parentAgent, auditItems, order.getId(), isVirtualOrder(order))) {
            throw new CrmebException("您的" + (isVirtualOrder(order) ? "虚拟库存" : "库存")
                    + "不足，无法审核通过该订单；请尽快补货后再审核，"
                    + "或等待系统将该订单自动匹配至更高级上级");
        }
        // 审核通过：实体单扣云仓库存 -> 待发货；虚拟单 -> 完成并入账虚拟库存
        return transactionTemplate.execute(status -> {
            List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                    .eq(StockOrderProduct::getOrderId, order.getId()));
            order.setAuditTime(new Date());
            if (isVirtualOrder(order)) {
                order.setStatus(StockOrder.STATUS_COMPLETE);
                order.setFinishTime(new Date());
                stockOrderDao.updateById(order);
                for (StockOrderProduct op : items) {
                    creditVirtualStock(order, op);
                }
                // 虚拟单完成：差价结算给上级（幂等，与付款即完成路径一致）
                settleOrderReward(order);
                stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "虚拟库存入账成功",
                        "您的订货单 " + order.getOrderNo() + " 已由上级审核通过，虚拟库存已入账，可在会员中心【虚拟库存】中提货");
            } else {
                for (StockOrderProduct op : items) {
                    stockService.deductStockBySku(op.getProductId(), op.getSkuKey(), op.getNum(), 1, order.getOrderNo(), "上级审核通过扣库存");
                }
                order.setStatus(StockOrder.STATUS_WAIT_SEND);
                stockOrderDao.updateById(order);
                stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单审核通过",
                        "您的订货单 " + order.getOrderNo() + " 已由上级审核通过，等待发货");
                // 上级发货模式：提醒上级及时发货
                notifyParentDeliver(order);
            }
            // 审核通过后按升级规则自动升级
            try {
                stockService.checkAndUpgrade(order.getUid());
            } catch (Exception ignored) {
            }
            return true;
        }) != null;
    }

    @Override
    public Boolean auditOrderByAdmin(Integer orderId, StockRequests.StockAuditRequest request) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (StockOrder.STATUS_WAIT_MATCH.equals(order.getStatus())) {
            // 总部介入：先把「等待匹配」订单归位为待上级审核（清空匹配标记），再走总部审核流程
            stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                    .eq(StockOrder::getId, order.getId())
                    .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                    .set(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT)
                    .set(StockOrder::getUpSearchTime, null));
            order.setStatus(StockOrder.STATUS_WAIT_PARENT_AUDIT);
            order.setUpSearchTime(null);
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_PARENT_AUDIT)) {
            throw new CrmebException("订单当前状态不可介入审核（仅待上级审核状态可介入）");
        }
        if (request.getStatus() == -1) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new CrmebException("驳回必须填写原因");
            }
            order.setStatus(StockOrder.STATUS_REJECT);
            order.setRejectReason("[总部介入]" + request.getReason().trim());
            stockOrderDao.updateById(order);
            transactionTemplate.executeWithoutResult(status ->
                    refundOrderToBalance(order, "总部驳回"));
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单被驳回",
                    "您的订货单 " + order.getOrderNo() + " 被总部驳回，原因：" + request.getReason().trim()
                            + "。已支付货款 ¥" + order.getTotalPrice() + " 已退回您的余额");
            return true;
        }
        // 总部介入通过：实体单直接扣云仓库存 -> 待发货；虚拟单 -> 完成并入账虚拟库存
        return transactionTemplate.execute(status -> {
            List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                    .eq(StockOrderProduct::getOrderId, order.getId()));
            order.setAuditTime(new Date());
            if (isVirtualOrder(order)) {
                order.setStatus(StockOrder.STATUS_COMPLETE);
                order.setFinishTime(new Date());
                stockOrderDao.updateById(order);
                for (StockOrderProduct op : items) {
                    creditVirtualStock(order, op);
                }
                // 虚拟单完成：差价结算给上级（幂等，与付款即完成路径一致）
                settleOrderReward(order);
                stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "虚拟库存入账成功",
                        "您的订货单 " + order.getOrderNo() + " 已由总部审核通过，虚拟库存已入账，可在会员中心【虚拟库存】中提货");
            } else {
                for (StockOrderProduct op : items) {
                    stockService.deductStockBySku(op.getProductId(), op.getSkuKey(), op.getNum(), 1, order.getOrderNo(), "总部介入审核扣库存");
                }
                order.setStatus(StockOrder.STATUS_WAIT_SEND);
                stockOrderDao.updateById(order);
                stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单审核通过",
                        "您的订货单 " + order.getOrderNo() + " 已由总部审核通过，等待发货");
            }
            // 审核通过后按升级规则自动升级
            try {
                stockService.checkAndUpgrade(order.getUid());
            } catch (Exception ignored) {
            }
            return true;
        }) != null;
    }

    @Override
    public Boolean skipMatchUpOrder(Integer orderId) {
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!StockOrder.STATUS_WAIT_MATCH.equals(order.getStatus())) {
            throw new CrmebException("只有「等待匹配上级」状态的订单才能跳过匹配");
        }
        StockAgent agent = stockService.getAgentById(order.getAgentId());
        if (agent == null || agent.getStatus() != 1) {
            throw new CrmebException("下单代理不存在或已被禁用");
        }
        if (order.getUpSearchTime() == null) {
            order.setUpSearchTime(new Date());
        }
        matchUpOrder(order, agent);
        return true;
    }

    @Override
    public CommonPage<StockOrder> getWaitMatchOrderList(PageParamRequest page) {
        PageHelper.startPage(page.getPage(), page.getLimit());
        List<StockOrder> list = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                .eq(StockOrder::getIsDel, 0)
                .orderByAsc(StockOrder::getUpSearchTime)
                .orderByDesc(StockOrder::getId));
        fillOrders(list);
        return CommonPage.restPage(new PageInfo<>(list));
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
        // 完成后核算奖励（虚拟提货单金额为0，不参与奖励结算）
        if (!isPickupOrder(order)) {
            settleOrderReward(order);
        }
        return true;
    }

    @Override
    public CommonPage<StockOrder> getAuditOrderList(Integer uid, Integer status, PageParamRequest page) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        // 懒处理：自己名下「等待匹配」的订单，若已补足库存立即释放回正常订货流程（进入待我审核）
        try {
            releaseRestockedOrdersForParent(agent);
        } catch (Exception ignored) {
        }
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockOrder::getParentAgentId, agent.getId()).eq(StockOrder::getIsDel, 0);
        // 虚拟提货单由总部审核发货，上级既不能审也不能发，一律不出现在该列表
        lqw.ne(StockOrder::getOrderType, StockOrder.ORDER_TYPE_PICKUP);
        if (status != null) {
            if (StockOrder.STATUS_WAIT_PARENT_AUDIT.equals(status)) {
                // 「待我处理」同时包含：待上级审核(0) 与 等待匹配上级(10，下单时上级无库存待补货)
                lqw.in(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT, StockOrder.STATUS_WAIT_MATCH);
            } else {
                lqw.eq(StockOrder::getStatus, status);
            }
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
        StockOrder order;
        if (request.getOrderId() != null) {
            order = stockOrderDao.selectById(request.getOrderId());
        } else {
            // 库存页发起的换货：自动匹配该会员最近一笔含此商品且库存类型一致的已完成订单
            order = findLatestCompletedOrder(uid, request.getProductId(), request.getSourceStockType());
        }
        if (order == null || order.getIsDel() == 1 || !order.getUid().equals(uid)) {
            throw new CrmebException("原订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_COMPLETE)) {
            throw new CrmebException("只有已完成的订单才能申请换货");
        }
        // 虚拟提货单（order_type=2）不支持换货：货已用虚拟余额抵扣提走，由总部直接发货，无换货语义
        if (StockOrder.ORDER_TYPE_PICKUP.equals(order.getOrderType())) {
            throw new CrmebException("虚拟库存提货单不支持换货");
        }
        // 带规格key时精确匹配明细（防止同订单同商品多规格取错/多行抛错）
        // 注意：取值表达式必须用局部变量，直接写 request.getSkuKey().trim() 会在 skuKey 为 null 时先求值而 NPE
        String reqSkuKey = request.getSkuKey() == null ? "" : request.getSkuKey().trim();
        StockOrderProduct item = stockOrderProductDao.selectOne(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId())
                .eq(StockOrderProduct::getProductId, request.getProductId())
                .eq(!reqSkuKey.isEmpty(), StockOrderProduct::getSkuKey, reqSkuKey)
                .last(" limit 1"));
        if (item == null) {
            throw new CrmebException("该订单中没有此商品");
        }
        // 一单一换：该订单已有有效换货单（非驳回）则不允许再次申请（开关 stock_exchange_once=0 时关闭）
        if (!"0".equals(systemConfigService.getValueByKey(CFG_EXCHANGE_ONCE))) {
            int exist = stockExchangeDao.selectCount(new LambdaQueryWrapper<StockExchange>()
                    .eq(StockExchange::getOrderId, order.getId())
                    .ne(StockExchange::getStatus, StockExchange.STATUS_REJECT)
                    .eq(StockExchange::getIsDel, 0));
            if (exist > 0) {
                throw new CrmebException("该订单已申请过换货，不能重复申请换货");
            }
        }
        // 累计换货数量校验：同订单同商品的有效换货单（未驳回）已换数量 + 本次不得超过购买数量
        int used = 0;
        for (StockExchange e : stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getOrderId, order.getId())
                .eq(StockExchange::getProductId, request.getProductId())
                .ne(StockExchange::getStatus, StockExchange.STATUS_REJECT)
                .eq(StockExchange::getIsDel, 0))) {
            used += e.getNum() == null ? 0 : e.getNum();
        }
        if (request.getNum() + used > item.getNum()) {
            throw new CrmebException("换货数量超出可换范围：该订单此商品共购 " + item.getNum()
                    + " 件，已换 " + used + " 件，本次最多可换 " + (item.getNum() - used) + " 件");
        }
        // 换货开关：商品/规格级配置优先，未配置则沿用原行为（允许）
        if (!isExchangeAllowed(request.getProductId(), item.getSkuKey())) {
            throw new CrmebException("该商品未开放换货，请联系您的上级");
        }
        // 一次一件（配置 stock_exchange_single=1 时生效）
        String singleCfg = systemConfigService.getValueByKey("stock_exchange_single");
        if ("1".equals(singleCfg) && !Integer.valueOf(1).equals(request.getNum())) {
            throw new CrmebException("换货一次只能申请一件");
        }
        if (request.getNum() == null || request.getNum() <= 0 || request.getNum() > item.getNum()) {
            throw new CrmebException("换货数量不合法");
        }
        // ---- 换入目标校验 + 差价计算 ----
        Integer targetProductId = request.getTargetProductId();
        if (targetProductId == null || targetProductId.equals(request.getProductId())) {
            throw new CrmebException("请选择要换入的商品（不能与原商品相同）");
        }
        BigDecimal originPrice = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
        BigDecimal targetPrice = stockService.getProductPrice(agent, targetProductId,
                request.getTargetSkuKey() == null ? "" : request.getTargetSkuKey());
        if (targetPrice.compareTo(originPrice) < 0) {
            throw new CrmebException("换入商品拿货价不能低于原商品（原 ¥" + originPrice + "，目标 ¥" + targetPrice + "）");
        }
        // 最低换入价校验（后台「换货设置」配置，规格级优先，未配置回退整品级）
        BigDecimal minTargetPrice = getMinTargetPrice(request.getProductId(), item.getSkuKey());
        if (minTargetPrice != null && minTargetPrice.signum() > 0 && targetPrice.compareTo(minTargetPrice) < 0) {
            throw new CrmebException("换入商品拿货价低于设置的最低换入价 ¥" + minTargetPrice);
        }
        boolean allowedTarget = false;
        for (StockExchangeTarget t : getExchangeTargetList(request.getProductId(), item.getSkuKey())) {
            if (targetProductId.equals(t.getTargetProductId())) {
                allowedTarget = true;
                break;
            }
        }
        if (!allowedTarget) {
            throw new CrmebException("该商品不在可换清单内，请联系您的上级");
        }
        StoreProduct targetProduct = storeProductService.getById(targetProductId);
        if (targetProduct == null || targetProduct.getIsDel() || !targetProduct.getIsShow()) {
            throw new CrmebException("换入商品不存在或已下架");
        }
        BigDecimal diffPrice = targetPrice.subtract(originPrice)
                .multiply(new BigDecimal(request.getNum()));
        boolean virtualExchange = isVirtualOrder(order);
        // 换货中锁定校验：换出数量不得超过当前可供应量（换货中占用的量已被锁定，不能再重复换出/卖掉）
        if (!virtualExchange) {
            int available = getAgentStockNum(agent, request.getProductId());
            if (available < request.getNum()) {
                throw new CrmebException("可换实体库存不足，当前可供应 " + available + " 件"
                        + "（换货中的库存已锁定，不可再次换出或销售）");
            }
        }
        // 换入库存类型：虚拟库存换货由会员选择（换实体/换虚拟）；实体库存换货只能换实体
        int targetStockType;
        if (virtualExchange) {
            Integer ts = request.getTargetStockType();
            if (ts == null || (ts != 1 && ts != 2)) {
                throw new CrmebException("请选择换入实体库存还是虚拟库存");
            }
            targetStockType = ts;
        } else {
            if (request.getTargetStockType() != null && request.getTargetStockType() != 1) {
                throw new CrmebException("实体库存换货只能换实体商品");
            }
            targetStockType = 1;
        }
        // 换入实体商品必须有收货地址（默认带原订单地址，可在申请时改选）
        Integer addressId = order.getAddressId();
        if (targetStockType == 1 && request.getAddressId() != null && request.getAddressId() > 0) {
            UserAddress addr = userAddressService.getById(request.getAddressId());
            if (addr == null || addr.getIsDel() || !addr.getUid().equals(uid)) {
                throw new CrmebException("收货地址不存在");
            }
            addressId = addr.getId();
        }
        if (targetStockType == 1 && (addressId == null || addressId <= 0)) {
            throw new CrmebException("换入实体商品需要填写收货地址");
        }
        // 换入虚拟商品：上级（或总部）需有对应虚拟库存可换出，提前校验给出明确提示
        if (targetStockType == 2 && hasParentStockCheckNeeded(order)) {
            StockAgent parent = stockService.getAgentById(order.getParentAgentId());
            if (parent == null) {
                throw new CrmebException("上级订货商不存在，无法换入虚拟库存");
            }
            int parentVirtual = getParentVirtualStockNum(parent.getUid(), targetProductId,
                    request.getTargetSkuKey() == null ? "" : request.getTargetSkuKey());
            if (parentVirtual < request.getNum()) {
                throw new CrmebException("上级虚拟库存不足，当前可换：" + parentVirtual);
            }
        }

        StockExchange exchange = new StockExchange();
        exchange.setExchangeNo("HE" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000)));
        exchange.setUid(uid);
        exchange.setAgentId(agent.getId());
        exchange.setParentAgentId(order.getParentAgentId());
        exchange.setOrderId(order.getId());
        exchange.setProductId(request.getProductId());
        exchange.setProductName(item.getProductName());
        exchange.setSkuKey(item.getSkuKey() == null ? "" : item.getSkuKey());
        exchange.setNum(request.getNum());
        exchange.setReason(request.getReason().trim());
        exchange.setExchangeType(virtualExchange ? 1 : 2);
        exchange.setTargetProductId(targetProductId);
        exchange.setTargetSkuKey(request.getTargetSkuKey() == null ? "" : request.getTargetSkuKey());
        exchange.setTargetProductName(targetProduct.getStoreName());
        exchange.setTargetStockType(targetStockType);
        exchange.setOriginPrice(originPrice);
        exchange.setTargetPrice(targetPrice);
        exchange.setDiffPrice(diffPrice);
        exchange.setDiffPayStatus(0);
        exchange.setRealName(order.getRealName());
        exchange.setPhone(order.getPhone());
        exchange.setUserAddress(order.getUserAddress());
        exchange.setAddressId(addressId);
        // 原订单的直接上级：为 0/null 表示下单时的上级就是总部（自有订货商）
        boolean hasParentAgent = order.getParentAgentId() != null && order.getParentAgentId() > 0;
        // 统一走审核流：实体换货审核后走旧品退回/发新品；
        // 虚拟换虚拟审核通过后直接扣上级虚拟库存并入账给申请人（无需地址/发货）；
        // 虚拟换实体审核后进入待发新品，由总部/上级发货。
        exchange.setStatus(hasParentAgent ? StockExchange.STATUS_WAIT_PARENT_AUDIT : StockExchange.STATUS_WAIT_HQ_AUDIT);
        exchange.setIsDel(0);

        boolean ok;
        if (virtualExchange) {
            ok = transactionTemplate.execute(status -> {
                deductVirtualStock(uid, request.getProductId(), item.getSkuKey(), request.getNum());
                return stockExchangeDao.insert(exchange) > 0;
            }) != null;
        } else {
            ok = stockExchangeDao.insert(exchange) > 0;
        }
        if (ok) {
            String targetDesc = targetStockType == 2 ? "换入虚拟库存" : "换入实体商品";
            if (hasParentAgent) {
                StockAgent parent = stockService.getAgentById(exchange.getParentAgentId());
                if (parent != null) {
                    stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单待审核",
                            "您的下级【" + nickOf(uid) + "】提交了换货申请 " + exchange.getExchangeNo()
                                    + "（" + targetDesc + "），请及时审核");
                }
                stockRewardService.sendNotice(uid, StockNotice.TYPE_ORDER_AUDIT, "换货申请已提交",
                        "换货单 " + exchange.getExchangeNo() + " 已提交，等待您的上级【" + nickOf(parent == null ? 0 : parent.getUid())
                                + "】审核，可在「订单审核」中查看进度");
            } else {
                stockRewardService.sendNotice(uid, StockNotice.TYPE_ORDER_AUDIT, "换货申请已提交",
                        "换货单 " + exchange.getExchangeNo() + " 已提交，您的上级是总部，将由总部直接审核");
            }
        }
        return ok;
    }

    /** 虚拟换货换入虚拟库存时，是否需要校验上级虚拟库存（上级为总部时无需校验） */
    private boolean hasParentStockCheckNeeded(StockOrder order) {
        return order.getParentAgentId() != null && order.getParentAgentId() > 0;
    }

    /** 查询某用户指定商品的虚拟库存可提总量 */
    private int getParentVirtualStockNum(Integer uid, Integer productId, String skuKey) {
        int total = 0;
        for (StockVirtualStock v : stockVirtualStockDao.selectList(new LambdaQueryWrapper<StockVirtualStock>()
                .eq(StockVirtualStock::getUid, uid)
                .eq(StockVirtualStock::getProductId, productId)
                .eq(StockVirtualStock::getSkuKey, skuKey == null ? "" : skuKey)
                .eq(StockVirtualStock::getIsDel, 0))) {
            total += v.getRemainNum() == null ? 0 : v.getRemainNum();
        }
        return total;
    }

    /**
     * 按规格回补虚拟库存（合并到已有行；无则新增）。
     *
     * @param asIncome true=真实入库（换货入库），累计入账 num 与剩余可提 remain_num 都加；
     *                 false=回补占用（驳回换货/取消恢复，对应 {@link #deductVirtualStock} 只减过 remain_num），
     *                 只加 remain_num，不能加 num，否则「累计入账」会被驳回操作虚增。
     */
    private void creditVirtualStockNum(Integer uid, Integer productId, String skuKey, Integer num, boolean asIncome) {
        if (num == null || num <= 0) {
            return;
        }
        String sku = skuKey == null ? "" : skuKey;
        StockVirtualStock exist = stockVirtualStockDao.selectOne(new LambdaQueryWrapper<StockVirtualStock>()
                .eq(StockVirtualStock::getUid, uid)
                .eq(StockVirtualStock::getProductId, productId)
                .eq(StockVirtualStock::getSkuKey, sku)
                .eq(StockVirtualStock::getIsDel, 0)
                .last(" limit 1"));
        if (exist != null) {
            // num=累计入账、remain_num=剩余可提（提货/转卖只减 remain_num）。
            // 真实入库两条都加，否则合并后会出现「累计入账 1 · 剩余可提 2」（2026-09-21 修）；
            // 驳回恢复只加 remain_num（申请时也只减过 remain_num）。
            LambdaUpdateWrapper<StockVirtualStock> uw = new LambdaUpdateWrapper<StockVirtualStock>()
                    .eq(StockVirtualStock::getId, exist.getId())
                    .setSql("remain_num = remain_num + " + num);
            if (asIncome) {
                uw.setSql("num = num + " + num);
            }
            // 顺手补历史脏行（换货入库早期未写商品名/图片，合并时不会自愈）
            boolean noName = exist.getProductName() == null || exist.getProductName().trim().isEmpty();
            boolean noImage = exist.getImage() == null || exist.getImage().trim().isEmpty();
            if (noName || noImage) {
                StoreProduct product = storeProductService.getById(productId);
                if (product != null) {
                    if (noName && product.getStoreName() != null) {
                        uw.set(StockVirtualStock::getProductName, product.getStoreName());
                    }
                    if (noImage && product.getImage() != null) {
                        uw.set(StockVirtualStock::getImage, product.getImage());
                    }
                }
            }
            stockVirtualStockDao.update(null, uw);
        } else {
            // 新建行必须补商品名与图片：否则会员端「虚拟库存」换货入库那张卡会没有图、没有名称（2026-09-21 修）
            StoreProduct product = storeProductService.getById(productId);
            StockVirtualStock v = new StockVirtualStock();
            v.setUid(uid);
            v.setProductId(productId);
            v.setProductName(product == null || product.getStoreName() == null ? "" : product.getStoreName());
            v.setImage(product == null || product.getImage() == null ? "" : product.getImage());
            v.setSkuKey(sku);
            v.setNum(asIncome ? num : 0);
            v.setRemainNum(num);
            v.setIsDel(0);
            stockVirtualStockDao.insert(v);
        }
    }

    /** 扣减上级的虚拟库存（换入虚拟商品时，新品从上级虚拟库存中出） */
    private void deductParentVirtualStock(Integer parentAgentId, Integer productId, String skuKey, Integer num) {
        StockAgent parent = stockService.getAgentById(parentAgentId);
        if (parent == null) {
            throw new CrmebException("上级订货商不存在");
        }
        deductVirtualStock(parent.getUid(), productId, skuKey, num);
    }

    /** 按 (uid, 商品, 规格) 顺序扣减虚拟库存可提数量，不足则抛异常（调用方需在事务内） */
    private void deductVirtualStock(Integer uid, Integer productId, String skuKey, Integer num) {
        if (num == null || num <= 0) {
            throw new CrmebException("扣减数量不合法");
        }
        String sku = skuKey == null ? "" : skuKey;
        List<StockVirtualStock> list = stockVirtualStockDao.selectList(new LambdaQueryWrapper<StockVirtualStock>()
                .eq(StockVirtualStock::getUid, uid)
                .eq(StockVirtualStock::getProductId, productId)
                .eq(StockVirtualStock::getSkuKey, sku)
                .eq(StockVirtualStock::getIsDel, 0)
                .gt(StockVirtualStock::getRemainNum, 0)
                .orderByAsc(StockVirtualStock::getId));
        int total = 0;
        for (StockVirtualStock v : list) {
            total += v.getRemainNum() == null ? 0 : v.getRemainNum();
        }
        if (total < num) {
            throw new CrmebException("虚拟库存不足，当前可换：" + total);
        }
        int need = num;
        for (StockVirtualStock v : list) {
            if (need <= 0) {
                break;
            }
            int cut = Math.min(need, v.getRemainNum());
            stockVirtualStockDao.update(null, new LambdaUpdateWrapper<StockVirtualStock>()
                    .eq(StockVirtualStock::getId, v.getId())
                    .ge(StockVirtualStock::getRemainNum, cut)
                    .setSql("remain_num = remain_num - " + cut));
            need -= cut;
        }
        if (need > 0) {
            throw new CrmebException("虚拟库存不足，请刷新后重试");
        }
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

    /**
     * 待我审核的换货单（我作为直接上级）。
     * 换货单存在 eb_stock_exchange 独立表里，和订货单不是一张表，
     * 所以「订单审核」列表必须单独带出这批单子，否则上级永远看不到、下级提交后就一直卡在待审核。
     */
    @Override
    public CommonPage<StockExchange> getExchangeAuditList(Integer uid, Integer status, PageParamRequest page) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您还不是订货代理");
        }
        PageHelper.startPage(page.getPage(), page.getLimit());
        LambdaQueryWrapper<StockExchange> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StockExchange::getParentAgentId, agent.getId()).eq(StockExchange::getIsDel, 0);
        // 未支付差价的单不推给上级：支付后才进入上级的换货列表（申请人在「我的换货」里支付）
        lqw.and(w -> w.ne(StockExchange::getStatus, StockExchange.STATUS_WAIT_PARENT_AUDIT)
                .or().le(StockExchange::getDiffPrice, BigDecimal.ZERO)
                .or().eq(StockExchange::getDiffPayStatus, 1));
        if (status == null) {
            // 默认：待我处理的（待我审核 0 + 旧品待入库 2 + 待发新品 3），
            // 上级在会员端完成「审核 → 收旧品 → 发新品」整条链，不再只能去后台操作
            lqw.in(StockExchange::getStatus, StockExchange.STATUS_WAIT_PARENT_AUDIT,
                    StockExchange.STATUS_WAIT_BACK, StockExchange.STATUS_WAIT_SEND);
        } else if (status != -2) {
            // -2 = 我经手过的全部（含已通过/已驳回）
            lqw.eq(StockExchange::getStatus, status);
        }
        lqw.orderByDesc(StockExchange::getId);
        List<StockExchange> list = stockExchangeDao.selectList(lqw);
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

    @Override
    public HashMap<String, Integer> myPendingCounts(Integer uid) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("audit", 0);
        map.put("send", 0);
        map.put("exchangeAudit", 0);
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            return map;
        }
        // 待我审核的订货单（0 待上级审核 + 10 等待匹配上级，与「订单审核」列表口径一致）
        // 提货单（order_type=2）由总部发货，与上级无关，不计入
        Integer audit = stockOrderDao.selectCount(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getParentAgentId, agent.getId())
                .eq(StockOrder::getIsDel, 0)
                .ne(StockOrder::getOrderType, StockOrder.ORDER_TYPE_PICKUP)
                .in(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT, StockOrder.STATUS_WAIT_MATCH));
        // 待我发货的订货单（2 待发货，上级发货模式下需要我发出；提货单由总部发货不计入）
        Integer send = stockOrderDao.selectCount(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getParentAgentId, agent.getId())
                .eq(StockOrder::getIsDel, 0)
                .ne(StockOrder::getOrderType, StockOrder.ORDER_TYPE_PICKUP)
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_SEND));
        // 待我处理的换货单（待我审核 0 + 旧品待入库 2 + 待发新品 3，与换货审核列表口径一致）
        // 未支付差价的单不推给上级，不计入角标
        Integer exchangeAudit = stockExchangeDao.selectCount(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getParentAgentId, agent.getId())
                .eq(StockExchange::getIsDel, 0)
                .and(w -> w.ne(StockExchange::getStatus, StockExchange.STATUS_WAIT_PARENT_AUDIT)
                        .or().le(StockExchange::getDiffPrice, BigDecimal.ZERO)
                        .or().eq(StockExchange::getDiffPayStatus, 1))
                .in(StockExchange::getStatus, StockExchange.STATUS_WAIT_PARENT_AUDIT,
                        StockExchange.STATUS_WAIT_BACK, StockExchange.STATUS_WAIT_SEND));
        map.put("audit", audit == null ? 0 : audit);
        map.put("send", send == null ? 0 : send);
        map.put("exchangeAudit", exchangeAudit == null ? 0 : exchangeAudit);
        return map;
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
        boolean isVirtual = isVirtualOrder(order);
        List<StockOrderProduct> items = stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId()));
        transactionTemplate.executeWithoutResult(status -> {
            // 乐观锁防并发：仅未支付+待付款状态可确认收款
            LambdaUpdateWrapper<StockOrder> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(StockOrder::getId, order.getId())
                    .eq(StockOrder::getPayStatus, 0)
                    .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_PAY)
                    .set(StockOrder::getPayStatus, 1)
                    .set(StockOrder::getPayTime, new Date())
                    .set(StockOrder::getPayType, StockOrder.PAY_TYPE_RECORD);
            if (isVirtual) {
                // 虚拟采购单：总部确认收款 = 完成并入账虚拟库存（不扣云仓、不发货）
                wrapper.set(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                        .set(StockOrder::getFinishTime, new Date());
            } else {
                // 实体采购单：总部确认收款 = 跳过审核直接待发货，需扣云仓库存（与付款/审核通过路径一致）
                wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_SEND);
            }
            boolean updated = stockOrderDao.update(null, wrapper) > 0;
            if (!updated) {
                throw new CrmebException("订单状态已变更，请刷新后重试");
            }
            if (isVirtual) {
                for (StockOrderProduct op : items) {
                    creditVirtualStock(order, op);
                }
            } else {
                for (StockOrderProduct op : items) {
                    stockService.deductStockBySku(op.getProductId(), op.getSkuKey(), op.getNum(), 1,
                            order.getOrderNo(), "总部确认收款扣库存");
                }
            }
        });
        StockOrder latest = getByOrderNo(order.getOrderNo());
        if (isVirtualOrder(latest)) {
            // 虚拟单完成：差价结算给上级（与付款即完成路径一致，幂等）
            settleOrderReward(latest);
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "虚拟库存入账成功",
                    "您的订货单 " + order.getOrderNo() + " 已由总部确认收款，虚拟库存已入账，可在会员中心【虚拟库存】中提货");
        } else {
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订货单支付成功",
                    "您的订货单 " + order.getOrderNo() + " 已由总部确认收款，等待发货");
        }
        // 付款后按升级规则自动升级（自购 / 直推 / 团队业绩）
        try {
            stockService.checkAndUpgrade(order.getUid());
        } catch (Exception ignored) {
        }
        return true;
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
        // 上级发货模式：仅「实体库存采购单」由上级代理在会员端发货
        // （虚拟采购单付款/审核即完成入账不会到待发货；虚拟提货单由总部发货，均不在此列）
        if (isParentDeliverOrder(order)) {
            throw new CrmebException("已开启上级发货模式，该订单需由上级代理在会员端发货");
        }
        boolean pickup = isPickupOrder(order);
        transactionTemplate.executeWithoutResult(status -> {
            if (pickup) {
                // 虚拟提货单发货：总部扣云仓实物库存
                List<StockOrderProduct> items = stockOrderProductDao.selectList(
                        new LambdaQueryWrapper<StockOrderProduct>().eq(StockOrderProduct::getOrderId, order.getId()));
                for (StockOrderProduct op : items) {
                    stockService.deductStockBySku(op.getProductId(), op.getSkuKey(), op.getNum(), 1,
                            order.getOrderNo(), "虚拟库存提货发货扣云仓库存");
                }
            }
            LambdaUpdateWrapper<StockOrder> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(StockOrder::getId, order.getId())
                    .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_SEND)
                    .set(StockOrder::getExpressName, request.getExpressName())
                    .set(StockOrder::getExpressNum, request.getExpressNum())
                    .set(StockOrder::getStatus, StockOrder.STATUS_WAIT_RECEIVE)
                    .set(StockOrder::getSendTime, new Date());
            stockOrderDao.update(null, wrapper);
        });
        stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_SEND, "订货单已发货",
                "您的" + (pickup ? "提货单" : "订货单") + " " + order.getOrderNo() + " 已由云仓发出，快递："
                        + request.getExpressName() + " " + request.getExpressNum() + "，请留意查收");
        return true;
    }

    /**
     * 该订单是否走「上级代理发货」：需同时满足
     * 1. 后台开关 stock_parent_deliver=1；
     * 2. 实体库存采购单（stock_type=1 且非提货单）——虚拟单付款/审核即完成入账、提货单由总部发货，均不在此列；
     * 3. 有直接上级代理（parent_agent_id>0）。
     */
    private boolean isParentDeliverOrder(StockOrder order) {
        if (!"1".equals(systemConfigService.getValueByKey(CFG_PARENT_DELIVER))) {
            return false;
        }
        if (order.getParentAgentId() == null || order.getParentAgentId() <= 0) {
            return false;
        }
        return StockOrder.STOCK_TYPE_PHYSICAL.equals(order.getStockType()) && !isPickupOrder(order);
    }

    /** 上级发货模式下，实体订单进入待发货时提醒上级及时发货 */
    private void notifyParentDeliver(StockOrder order) {
        try {
            if (!isParentDeliverOrder(order)) {
                return;
            }
            StockAgent parent = stockService.getAgentById(order.getParentAgentId());
            if (parent == null || parent.getUid() == null) {
                return;
            }
            stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_SEND, "下级订单待发货",
                    "下级【" + nickOf(order.getUid()) + "】的订货单 " + order.getOrderNo()
                            + " 已进入待发货，请在会员端【订货中心-订单发货】中安排发货");
        } catch (Exception ignored) {
        }
    }

    @Override
    public Boolean parentSendOrder(Integer uid, Integer orderId, StockRequests.StockSendRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            throw new CrmebException("您不是订货代理或已被禁用，无法发货");
        }
        if (!"1".equals(systemConfigService.getValueByKey(CFG_PARENT_DELIVER))) {
            throw new CrmebException("当前未开启上级发货模式，订单由总部发货");
        }
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_SEND)) {
            throw new CrmebException("订单当前状态不可发货");
        }
        // 仅实体库存采购单由上级发货；虚拟采购单付款/审核即完成入账，虚拟提货单由总部直接发货
        if (!StockOrder.STOCK_TYPE_PHYSICAL.equals(order.getStockType()) || isPickupOrder(order)) {
            throw new CrmebException("仅实体库存订货单由上级发货，虚拟库存相关订单由总部直接发货");
        }
        if (order.getParentAgentId() == null || !agent.getId().equals(order.getParentAgentId())) {
            throw new CrmebException("只有该订单的直接上级才能发货");
        }
        order.setExpressName(request.getExpressName());
        order.setExpressNum(request.getExpressNum());
        order.setStatus(StockOrder.STATUS_WAIT_RECEIVE);
        order.setSendTime(new Date());
        boolean ok = stockOrderDao.updateById(order) > 0;
        if (ok) {
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_SEND, "订货单已发货",
                    "您的订货单 " + order.getOrderNo() + " 已由上级【" + nickOf(uid) + "】发出，快递："
                            + request.getExpressName() + " " + request.getExpressNum() + "，请留意查收");
        }
        return ok;
    }

    /**
     * 上级修改自己发出的实体订货单物流信息（待收货状态）。
     * 解决发货后快递单号填错无处修改、且发货记录在上级端看不到的问题。
     */
    @Override
    public Boolean parentUpdateExpress(Integer uid, Integer orderId, StockRequests.StockSendRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            throw new CrmebException("您不是订货代理或已被禁用");
        }
        if (!"1".equals(systemConfigService.getValueByKey(CFG_PARENT_DELIVER))) {
            throw new CrmebException("当前未开启上级发货模式");
        }
        StockOrder order = stockOrderDao.selectById(orderId);
        if (order == null || order.getIsDel() == 1) {
            throw new CrmebException("订单不存在");
        }
        if (!order.getStatus().equals(StockOrder.STATUS_WAIT_RECEIVE)) {
            throw new CrmebException("仅待收货状态的订单可修改物流信息");
        }
        if (!StockOrder.STOCK_TYPE_PHYSICAL.equals(order.getStockType()) || isPickupOrder(order)) {
            throw new CrmebException("仅实体库存订货单支持修改上级发货物流");
        }
        if (order.getParentAgentId() == null || !agent.getId().equals(order.getParentAgentId())) {
            throw new CrmebException("只有该订单的直接上级才能修改物流信息");
        }
        if (request.getExpressNum() == null || request.getExpressNum().trim().isEmpty()) {
            throw new CrmebException("快递单号不能为空");
        }
        boolean ok = stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                .eq(StockOrder::getId, order.getId())
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_RECEIVE)
                .set(StockOrder::getExpressName, request.getExpressName() == null ? "" : request.getExpressName().trim())
                .set(StockOrder::getExpressNum, request.getExpressNum().trim())) > 0;
        if (ok) {
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_SEND, "订货单物流已更新",
                    "您的订货单 " + order.getOrderNo() + " 物流信息已由上级更新，快递："
                            + (request.getExpressName() == null ? "" : request.getExpressName().trim())
                            + " " + request.getExpressNum().trim() + "，请留意查收");
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
        if (!isPickupOrder(order)) {
            settleOrderReward(order);
        }
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
            transactionTemplate.executeWithoutResult(status -> {
                refundExchangeDiffToBalance(exchange);
                // 虚拟库存换货申请时已扣旧品虚拟库存，驳回后原样恢复
                restoreVirtualOnReject(exchange);
            });
            return true;
        }
        // 通过：差价未支付不允许审核（支付后才推给上级），再确认上级真的有货可发
        requireDiffPaid(exchange);
        requireParentStockEnough(exchange, "通过换货申请");
        if (isVirtualExchange(exchange)) {
            if (isVirtualTarget(exchange)) {
                // 虚拟换虚拟：上级审核通过即完成 —— 扣上级虚拟库存、入账给申请人，无需地址与发货
                requireDiffPaid(exchange);
                transactionTemplate.executeWithoutResult(status -> completeVirtualToVirtual(exchange, "您的上级"));
            } else {
                // 虚拟换实体：无需旧品退回，直接进入待发新品
                exchange.setStatus(StockExchange.STATUS_WAIT_SEND);
                stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单审核通过",
                        "您的换货单 " + exchange.getExchangeNo() + " 已审核通过，新品将尽快发出");
            }
        } else {
            // 实体换货：开关 stock_exchange_hq_audit=1（默认）时流转总部复核；
            // =0 时上级审核通过直接进入待旧品退回，由上级在会员端完成收旧品/发新品（总部后台仍可介入）
            if (!"0".equals(systemConfigService.getValueByKey(CFG_EXCHANGE_HQ_AUDIT))) {
                exchange.setStatus(StockExchange.STATUS_WAIT_HQ_AUDIT);
            } else {
                exchange.setStatus(StockExchange.STATUS_WAIT_BACK);
                stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单审核通过",
                        "您的换货单 " + exchange.getExchangeNo() + " 已由上级审核通过，请寄回旧品并填写退回快递");
            }
        }
        exchange.setAuditTime(new Date());
        return stockExchangeDao.updateById(exchange) > 0;
    }

    /** 换货来源是否虚拟库存（exchangeType=1） */
    private boolean isVirtualExchange(StockExchange exchange) {
        return exchange.getExchangeType() != null && exchange.getExchangeType() == 1;
    }

    /** 换入是否虚拟库存（NULL/1=实体） */
    private boolean isVirtualTarget(StockExchange exchange) {
        return exchange.getTargetStockType() != null && exchange.getTargetStockType() == 2;
    }

    /** 有差价未支付时禁止推进（发货/完成） */
    private void requireDiffPaid(StockExchange exchange) {
        if (exchange.getDiffPrice() != null && exchange.getDiffPrice().signum() > 0
                && (exchange.getDiffPayStatus() == null || exchange.getDiffPayStatus() != 1)) {
            throw new CrmebException("会员尚未支付换货差价 ¥" + exchange.getDiffPrice());
        }
    }

    /**
     * 换货要由上级发出新品时，先校验上级是否真的有可发的库存。
     * 换入实体 → 看上级该商品的实体可供应量（与「库存记录」台账同口径，
     *            含已完成换货净额，见 getAgentStockNum）；换入虚拟 → 看上级虚拟剩余量。
     * 不足即拒绝：否则会出现「上级根本没有该商品库存，换货照样完成」，
     * 台账里凭空多出一条上级的扣减行、可供应量被扣成负数。
     * 上级是总部(parentAgentId<=0)时由总部云仓直发，无需校验。
     */
    private void requireParentStockEnough(StockExchange exchange, String action) {
        Integer parentAgentId = exchange.getParentAgentId();
        if (parentAgentId == null || parentAgentId <= 0) {
            return;
        }
        int num = exchange.getNum() == null ? 0 : exchange.getNum();
        if (num <= 0) {
            return;
        }
        StockAgent parent = stockService.getAgentById(parentAgentId);
        if (parent == null) {
            throw new CrmebException("该换货单的上级不存在，无法" + action);
        }
        Integer targetProductId = (exchange.getTargetProductId() != null && exchange.getTargetProductId() > 0)
                ? exchange.getTargetProductId() : exchange.getProductId();
        String productName = (exchange.getTargetProductName() == null || exchange.getTargetProductName().isEmpty())
                ? ("商品ID " + targetProductId) : exchange.getTargetProductName();
        if (isVirtualTarget(exchange)) {
            int remain = getParentVirtualStockNum(parent.getUid(), targetProductId, exchange.getTargetSkuKey());
            if (remain < num) {
                throw new CrmebException("上级虚拟库存不足，无法" + action + "：换入【" + productName
                        + "】上级仅剩 " + remain + " 件，需要 " + num + " 件");
            }
        } else {
            int usable = getAgentStockNum(parent, targetProductId);
            if (usable < num) {
                throw new CrmebException("上级没有【" + productName + "】的库存，无法" + action
                        + "：上级可供应 " + usable + " 件，需要 " + num + " 件，请先补货后再处理");
            }
        }
    }

    /** 虚拟换虚拟审核通过：扣上级虚拟库存 → 入账申请人 → 直接完成（须在事务内调用） */
    private void completeVirtualToVirtual(StockExchange exchange, String auditorDesc) {
        if (exchange.getParentAgentId() != null && exchange.getParentAgentId() > 0) {
            deductParentVirtualStock(exchange.getParentAgentId(), exchange.getTargetProductId(),
                    exchange.getTargetSkuKey(), exchange.getNum());
        }
        creditVirtualStockNum(exchange.getUid(), exchange.getTargetProductId(),
                exchange.getTargetSkuKey(), exchange.getNum(), true);
        exchange.setStatus(StockExchange.STATUS_COMPLETE);
        exchange.setFinishTime(new Date());
        stockExchangeDao.updateById(exchange);
        // 换货完成才结算差价奖励（支付时只收钱不发奖）
        stockRewardService.settleExchangeDiffReward(exchange);
        stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_SEND, "换货完成",
                "您的换货单 " + exchange.getExchangeNo() + " 已由" + auditorDesc + "审核通过，"
                        + "新品虚拟库存已入账，可在【虚拟库存】中查看");
    }

    /** 驳回时恢复申请时扣减的虚拟库存（仅虚拟来源换货） */
    private void restoreVirtualOnReject(StockExchange exchange) {
        if (!isVirtualExchange(exchange)) {
            return;
        }
        creditVirtualStockNum(exchange.getUid(), exchange.getProductId(), exchange.getSkuKey(), exchange.getNum(), false);
    }

    @Override
    public Boolean auditExchangeByHq(Integer exchangeId, StockRequests.StockAuditRequest request) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        // 后台可介入审核：待上级审核(0)阶段可直接介入，待总部审核(1)为正常流程
        boolean intervene = exchange.getStatus().equals(StockExchange.STATUS_WAIT_PARENT_AUDIT);
        if (!intervene && !exchange.getStatus().equals(StockExchange.STATUS_WAIT_HQ_AUDIT)) {
            throw new CrmebException("换货单当前状态不可总部审核");
        }
        if (request.getStatus() == -1) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new CrmebException("驳回必须填写原因");
            }
            exchange.setStatus(StockExchange.STATUS_REJECT);
            exchange.setRejectReason("[总部介入]" + request.getReason().trim());
            stockExchangeDao.updateById(exchange);
            transactionTemplate.executeWithoutResult(status -> {
                refundExchangeDiffToBalance(exchange);
                // 虚拟库存换货申请时已扣旧品虚拟库存，驳回后原样恢复
                restoreVirtualOnReject(exchange);
            });
            stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单已驳回",
                    "您的换货单 " + exchange.getExchangeNo() + " 被总部驳回：" + request.getReason().trim());
            return true;
        }
        // 通过：差价未支付不允许审核；再确认上级有货可发（总部发货的单子除外）
        requireDiffPaid(exchange);
        requireParentStockEnough(exchange, "通过换货申请");
        if (isVirtualExchange(exchange) && isVirtualTarget(exchange)) {
            requireDiffPaid(exchange);
            transactionTemplate.executeWithoutResult(status -> completeVirtualToVirtual(exchange, "总部"));
            return true;
        }
        exchange.setStatus(StockExchange.STATUS_WAIT_BACK);
        if (isVirtualExchange(exchange)) {
            // 虚拟换实体：没有旧品可退，跳过旧品退回直接待发新品
            exchange.setStatus(StockExchange.STATUS_WAIT_SEND);
            stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单审核通过",
                    "您的换货单 " + exchange.getExchangeNo() + " 已由总部审核通过，新品将尽快发出");
        } else if (intervene) {
            stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_AUDIT, "换货单审核通过",
                    "您的换货单 " + exchange.getExchangeNo() + " 已由总部审核通过，请寄回旧品并填写退回快递");
        }
        exchange.setHqAuditTime(new Date());
        return stockExchangeDao.updateById(exchange) > 0;
    }

    @Override
    public Boolean confirmExchangeBack(Integer exchangeId) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        return doConfirmExchangeBack(exchange);
    }

    /** 上级在会员端确认旧品入库（直接上级 + 待旧品退回状态） */
    @Override
    public Boolean confirmExchangeBackByParent(Integer uid, Integer exchangeId) {
        StockExchange exchange = requireParentExchange(uid, exchangeId, "确认旧品入库");
        return doConfirmExchangeBack(exchange);
    }

    /** 上级侧操作（收旧品/发新品）的公共权限校验：必须是该换货单的直接上级 */
    private StockExchange requireParentExchange(Integer uid, Integer exchangeId, String action) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null) {
            throw new CrmebException("您不是订货代理，无权" + action);
        }
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        if (exchange.getParentAgentId() == null || exchange.getParentAgentId() <= 0
                || !agent.getId().equals(exchange.getParentAgentId())) {
            throw new CrmebException("只有该换货单的直接上级才能" + action);
        }
        return exchange;
    }

    /** 确认旧品核验入库：回补库存并推进到待发新品（状态 2 → 3） */
    private boolean doConfirmExchangeBack(StockExchange exchange) {
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_BACK)) {
            throw new CrmebException("当前状态不可确认旧品入库");
        }
        return transactionTemplate.execute(status -> {
            // 旧品核验入库：回补库存
            stockService.addStockBySku(exchange.getProductId(), exchange.getSkuKey(), exchange.getNum(), 4,
                    exchange.getExchangeNo(), "换货旧品退回核验入库");
            exchange.setStatus(StockExchange.STATUS_WAIT_SEND);
            exchange.setBackTime(new Date());
            boolean ok = stockExchangeDao.updateById(exchange) > 0;
            if (ok) {
                stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_AUDIT, "旧品已入库",
                        "您的换货单 " + exchange.getExchangeNo() + " 旧品已核验入库，新品将尽快发出");
            }
            return ok;
        }) != null;
    }

    @Override
    public Boolean sendExchangeNew(Integer exchangeId, StockRequests.StockSendRequest request) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        return doSendExchangeNew(exchange, request);
    }

    /** 上级在会员端发出新品（直接上级 + 待发新品状态） */
    @Override
    public Boolean sendExchangeNewByParent(Integer uid, Integer exchangeId, StockRequests.StockSendRequest request) {
        StockExchange exchange = requireParentExchange(uid, exchangeId, "发出新品");
        return doSendExchangeNew(exchange, request);
    }

    /** 发出新品：扣减库存、记录新快递单号（状态 3 → 5 待下级收货，换货人确认收货后才完成） */
    private boolean doSendExchangeNew(StockExchange exchange, StockRequests.StockSendRequest request) {
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_SEND)) {
            throw new CrmebException("当前状态不可发新品");
        }
        if (exchange.getDiffPrice() != null && exchange.getDiffPrice().signum() > 0
                && (exchange.getDiffPayStatus() == null || exchange.getDiffPayStatus() != 1)) {
            throw new CrmebException("会员尚未支付换货差价 ¥" + exchange.getDiffPrice() + "，不可发货");
        }
        // 发货前再校验一次：审核到发货之间上级库存可能已被别处消耗
        requireParentStockEnough(exchange, "发出新品");
        Integer shipProductId = (exchange.getTargetProductId() != null && exchange.getTargetProductId() > 0)
                ? exchange.getTargetProductId() : exchange.getProductId();
        return transactionTemplate.execute(status -> {
            stockService.deductStockBySku(shipProductId, exchange.getTargetSkuKey(), exchange.getNum(), 3,
                    exchange.getExchangeNo(), "换货发出新品");
            exchange.setNewExpressName(request.getExpressName());
            exchange.setNewExpressNum(request.getExpressNum());
            exchange.setStatus(StockExchange.STATUS_WAIT_RECEIVE);
            exchange.setSendTime(new Date());
            boolean ok = stockExchangeDao.updateById(exchange) > 0;
            if (ok) {
                stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_SEND, "换货新品已发出",
                        "您的换货单 " + exchange.getExchangeNo() + " 新品已发出，快递：" + request.getExpressName()
                                + " " + request.getExpressNum() + "，请在收到货后确认收货");
            }
            return ok;
        }) != null;
    }

    /** 换货人确认收货（状态 5 → 4 已完成，差价奖励在此时结算） */
    private boolean doConfirmReceiveExchange(StockExchange exchange) {
        if (!exchange.getStatus().equals(StockExchange.STATUS_WAIT_RECEIVE)) {
            throw new CrmebException("当前状态无需确认收货");
        }
        exchange.setStatus(StockExchange.STATUS_COMPLETE);
        exchange.setFinishTime(new Date());
        boolean ok = stockExchangeDao.updateById(exchange) > 0;
        if (ok) {
            // 换货完成才结算差价奖励（支付时只收钱不发奖）
            stockRewardService.settleExchangeDiffReward(exchange);
            stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_ORDER_SEND, "换货完成",
                    "您的换货单 " + exchange.getExchangeNo() + " 已确认收货，换货完成");
        }
        return ok;
    }

    @Override
    public Boolean confirmReceiveExchange(Integer uid, Integer exchangeId) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1 || !exchange.getUid().equals(uid)) {
            throw new CrmebException("换货单不存在");
        }
        return doConfirmReceiveExchange(exchange);
    }

    @Override
    public Boolean confirmReceiveExchangeByAdmin(Integer exchangeId) {
        StockExchange exchange = stockExchangeDao.selectById(exchangeId);
        if (exchange == null || exchange.getIsDel() == 1) {
            throw new CrmebException("换货单不存在");
        }
        return doConfirmReceiveExchange(exchange);
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

    /** 上级等待时长(小时)，默认 12 */
    private int getUpSearchHours() {
        try {
            String v = systemConfigService.getValueByKey(CFG_UP_SEARCH_HOURS);
            if (v != null && !v.trim().isEmpty()) {
                return Integer.parseInt(v.trim());
            }
        } catch (Exception ignored) {
        }
        return 12;
    }

    /** 待付款超时时长(小时)，默认 24 */
    private int getWaitPayHours() {
        try {
            String v = systemConfigService.getValueByKey(CFG_WAIT_PAY_HOURS);
            if (v != null && !v.trim().isEmpty()) {
                return Integer.parseInt(v.trim());
            }
        } catch (Exception ignored) {
        }
        return 24;
    }

    /** 用户地址转地址字符串快照 */
    private String buildAddressStr(UserAddress address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null) sb.append(address.getProvince());
        if (address.getCity() != null) sb.append(address.getCity());
        if (address.getDistrict() != null) sb.append(address.getDistrict());
        if (address.getDetail() != null) sb.append(address.getDetail());
        return sb.toString();
    }

    /** 是否虚拟库存单 */
    private boolean isVirtualOrder(StockOrder order) {
        return order.getStockType() != null && StockOrder.STOCK_TYPE_VIRTUAL.equals(order.getStockType());
    }

    /** 是否虚拟提货单 */
    private boolean isPickupOrder(StockOrder order) {
        return order.getOrderType() != null && StockOrder.ORDER_TYPE_PICKUP.equals(order.getOrderType());
    }

    /**
     * 虚拟库存入账（须在事务内调用）：按 (uid, productId, skuKey) 合并累加
     */
    private void creditVirtualStock(StockOrder order, StockOrderProduct op) {
        String skuKey = op.getSkuKey() == null ? "" : op.getSkuKey();
        StockVirtualStock exist = stockVirtualStockDao.selectOne(new LambdaQueryWrapper<StockVirtualStock>()
                .eq(StockVirtualStock::getUid, order.getUid())
                .eq(StockVirtualStock::getProductId, op.getProductId())
                .eq(StockVirtualStock::getSkuKey, skuKey)
                .eq(StockVirtualStock::getIsDel, 0)
                .last(" limit 1"));
        if (exist != null) {
            stockVirtualStockDao.update(null, new LambdaUpdateWrapper<StockVirtualStock>()
                    .eq(StockVirtualStock::getId, exist.getId())
                    .setSql("num = num + " + op.getNum())
                    .setSql("remain_num = remain_num + " + op.getNum())
                    .set(StockVirtualStock::getSourceOrderNo, order.getOrderNo())
                    .set(StockVirtualStock::getParentAgentId, order.getParentAgentId() == null ? 0 : order.getParentAgentId()));
        } else {
            StockVirtualStock vs = new StockVirtualStock();
            vs.setUid(order.getUid());
            vs.setProductId(op.getProductId());
            vs.setProductName(op.getProductName());
            vs.setImage(op.getImage());
            vs.setSkuKey(skuKey);
            vs.setNum(op.getNum());
            vs.setRemainNum(op.getNum());
            vs.setSourceOrderNo(order.getOrderNo());
            vs.setParentAgentId(order.getParentAgentId() == null ? 0 : order.getParentAgentId());
            vs.setIsDel(0);
            stockVirtualStockDao.insert(vs);
        }
        // 虚拟库存入账留痕：会员端「库存记录-虚拟库存」必须能看到采购入账，
        // 否则只有出库（提货/转卖）没有入库，台账看起来像凭空扣减。
        // 幂等：同一订单只记一条（本方法在付款/上级审核/总部审核/确认收款多处被调，防止重复审核重复记账）
        int credited = stockAdjustLogDao.selectCount(new LambdaQueryWrapper<StockAdjustLog>()
                .eq(StockAdjustLog::getAgentId, order.getAgentId())
                .eq(StockAdjustLog::getStockType, StockAdjustLog.STOCK_TYPE_VIRTUAL)
                .gt(StockAdjustLog::getNum, 0)
                .like(StockAdjustLog::getMark, order.getOrderNo()));
        if (credited == 0) {
            StockAdjustLog inLog = new StockAdjustLog();
            inLog.setAgentId(order.getAgentId());
            inLog.setUid(order.getUid());
            inLog.setLinkUid(0);
            inLog.setProductId(op.getProductId());
            inLog.setSkuKey(skuKey);
            inLog.setNum(op.getNum());
            inLog.setStockType(StockAdjustLog.STOCK_TYPE_VIRTUAL);
            inLog.setMark("虚拟库存入账（订单 " + order.getOrderNo() + "），本次增加 " + op.getNum());
            inLog.setIsDel(0);
            stockAdjustLogDao.insert(inLog);
        }
        // 虚拟库存转卖：下级入账的同时，从直接上级虚拟库存中等量扣减
        transferVirtualStockFromParent(order, op);
    }

    /**
     * 虚拟库存转卖（须在事务内调用）：下级虚拟单入账时，从直接上级的虚拟库存中等量扣减，保证总量守恒。
     * 入账前各入口（付款分货、上级审核、等待单释放、向上匹配）均已校验上级虚拟库存充足，
     * 正常流程不会短缺；这里**不再做"虚拟不足就以实体库存供货"的兜底**，
     * 仅在异常短缺时把差额写进流水提示便于核查（2026-09-21 按需求取消实体兜底）。
     */
    private void transferVirtualStockFromParent(StockOrder order, StockOrderProduct op) {
        Integer parentAgentId = order.getParentAgentId();
        if (parentAgentId == null || parentAgentId <= 0) {
            return; // 总部直供，无上级虚拟库存可扣
        }
        StockAgent parent = stockService.getAgentById(parentAgentId);
        if (parent == null) {
            return;
        }
        int num = op.getNum() == null ? 0 : op.getNum();
        if (num <= 0) {
            return;
        }
        // 幂等：该订单已做过转卖留痕则跳过
        int existed = stockAdjustLogDao.selectCount(new LambdaQueryWrapper<StockAdjustLog>()
                .eq(StockAdjustLog::getAgentId, parent.getId())
                .eq(StockAdjustLog::getStockType, StockAdjustLog.STOCK_TYPE_VIRTUAL)
                .like(StockAdjustLog::getMark, order.getOrderNo()));
        if (existed > 0) {
            return;
        }
        String sku = op.getSkuKey() == null ? "" : op.getSkuKey();
        List<StockVirtualStock> list = stockVirtualStockDao.selectList(new LambdaQueryWrapper<StockVirtualStock>()
                .eq(StockVirtualStock::getUid, parent.getUid())
                .eq(StockVirtualStock::getProductId, op.getProductId())
                .eq(StockVirtualStock::getSkuKey, sku)
                .eq(StockVirtualStock::getIsDel, 0)
                .gt(StockVirtualStock::getRemainNum, 0)
                .orderByAsc(StockVirtualStock::getId));
        int need = num;
        for (StockVirtualStock v : list) {
            if (need <= 0) {
                break;
            }
            int cut = Math.min(need, v.getRemainNum());
            stockVirtualStockDao.update(null, new LambdaUpdateWrapper<StockVirtualStock>()
                    .eq(StockVirtualStock::getId, v.getId())
                    .ge(StockVirtualStock::getRemainNum, cut)
                    .setSql("remain_num = remain_num - " + cut));
            need -= cut;
        }
        int transferred = num - need;
        StockAdjustLog log = new StockAdjustLog();
        log.setAgentId(parent.getId());
        log.setUid(parent.getUid());
        // 溯源：记录采购人（下单的下级会员UID），库存记录据此显示"谁采购的"
        log.setLinkUid(order.getUid() == null ? 0 : order.getUid());
        log.setProductId(op.getProductId());
        log.setSkuKey(sku);
        log.setNum(-transferred);
        log.setStockType(StockAdjustLog.STOCK_TYPE_VIRTUAL);
        log.setMark("虚拟库存转卖给下级（订单 " + order.getOrderNo() + "），本次扣减 " + transferred
                + (need > 0 ? ("，上级虚拟库存异常短缺 " + need + " 件（入账前应已分货，出现此提示请核查数据）") : ""));
        log.setIsDel(0);
        stockAdjustLogDao.insert(log);
    }

    /** 某代理对某商品的可用库存（不排除任何订单） */
    private int getAgentStockNum(StockAgent agent, Integer productId) {
        return getAgentStockNum(agent, productId, null);
    }

    /**
     * 某代理对某商品的可用库存 = 其历史【已完成】实体采购数量 - 已供应给直接下级的实体数量
     *                              - 线下销售出库 - 换货中占用 + 后台调整 + 已完成换货净额
     * 仅统计实体采购单：虚拟采购单只入虚拟库存、提货单不参与推导
     *
     * ⚠️ 出入库口径刻意不对称（2026-09-24 业务确认）：
     *   入库 = 已完成（货真到手才算自己的货，在途不供货）；
     *   占用 = 下级已付款（一付款即扣上级可供应量，防超卖）。
     * 因此本方法既可能因"在途未收货"而暂时为 0，也可能因"下级已占用"而为负，统一兜底为 0。
     *
     * @param excludeChildOrderId 计算"已供应给下级"时排除的订单ID。用于判定"某笔订单能否被该上级满足"：
     *                            该订单本身（已付款）也计入 supplied，若不排除会重复占用自己所需的量，
     *                            导致上级明明补足了货仍被判库存不足，订单卡在"匹配上级中"。
     */
    private int getAgentStockNum(StockAgent agent, Integer productId, Integer excludeChildOrderId) {
        // 上级自己采购的数量（【已完成】的实体订单 —— 在途未收货的不算，虚拟单不计入实体可供应量）
        List<StockOrder> myOrders = stockOrderDao.selectList(physicalStockInWrapper()
                .eq(StockOrder::getAgentId, agent.getId()));
        int purchased = 0;
        List<Integer> myOrderIds = new ArrayList<>();
        for (StockOrder o : myOrders) {
            myOrderIds.add(o.getId());
        }
        if (!myOrderIds.isEmpty()) {
            for (StockOrderProduct op : stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                    .in(StockOrderProduct::getOrderId, myOrderIds).eq(StockOrderProduct::getProductId, productId))) {
                purchased += op.getNum();
            }
        }
        // 已供应给直接下级的数量（下级已付款实体订单）
        boolean exclude = excludeChildOrderId != null && excludeChildOrderId > 0;
        List<StockOrder> childOrders = stockOrderDao.selectList(physicalPurchaseWrapper()
                .eq(StockOrder::getParentAgentId, agent.getId())
                .ne(exclude, StockOrder::getId, excludeChildOrderId));
        int supplied = 0;
        List<Integer> childOrderIds = new ArrayList<>();
        for (StockOrder o : childOrders) {
            childOrderIds.add(o.getId());
        }
        if (!childOrderIds.isEmpty()) {
            for (StockOrderProduct op : stockOrderProductDao.selectList(new LambdaQueryWrapper<StockOrderProduct>()
                    .in(StockOrderProduct::getOrderId, childOrderIds).eq(StockOrderProduct::getProductId, productId))) {
                supplied += op.getNum();
            }
        }
        // 线下销售出库数量（自己卖掉的货，从可供应量中扣减）
        int sold = 0;
        for (StockOfflineSale s : stockOfflineSaleDao.selectList(new LambdaQueryWrapper<StockOfflineSale>()
                .eq(StockOfflineSale::getAgentId, agent.getId())
                .eq(StockOfflineSale::getProductId, productId)
                .eq(StockOfflineSale::getIsDel, 0))) {
            sold += s.getNum() == null ? 0 : s.getNum();
        }
        // 后台实体库存调整（正=增加，负=扣减）
        int adjusted = 0;
        for (StockAdjustLog a : stockAdjustLogDao.selectList(new LambdaQueryWrapper<StockAdjustLog>()
                .eq(StockAdjustLog::getAgentId, agent.getId())
                .eq(StockAdjustLog::getProductId, productId)
                .eq(StockAdjustLog::getStockType, StockAdjustLog.STOCK_TYPE_PHYSICAL)
                .eq(StockAdjustLog::getIsDel, 0))) {
            adjusted += a.getNum() == null ? 0 : a.getNum();
        }
        int net = purchased - supplied - sold + adjusted
                + exchangeStockDeltaMap(agent.getId()).getOrDefault(productId, 0)
                + exchangeOutDeltaMap(agent.getId()).getOrDefault(productId, 0)
                + exchangePendingDeltaMap(agent.getId()).getOrDefault(productId, 0);
        // 可供应量兜底不为负：入库口径改严后，净额可能为负（在途未收货、下级已占用），
        // 负值对外无意义，且会让"当前剩余：-3"这类提示很难看
        return Math.max(net, 0);
    }

    /**
     * 已完成换货对该代理【自身】实体库存的净影响：
     * - 换出商品 -num：仅实体来源换货（虚拟来源的旧品扣的是虚拟库存，与实体无关）
     * - 换入商品 +num：仅换入实体商品（换入虚拟的记在虚拟库存）
     */
    private Map<Integer, Integer> exchangeStockDeltaMap(Integer agentId) {
        Map<Integer, Integer> map = new HashMap<>();
        List<StockExchange> done = stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getAgentId, agentId)
                .eq(StockExchange::getStatus, StockExchange.STATUS_COMPLETE)
                .eq(StockExchange::getIsDel, 0));
        for (StockExchange e : done) {
            int num = e.getNum() == null ? 0 : e.getNum();
            if (!isVirtualExchange(e) && e.getProductId() != null) {
                map.merge(e.getProductId(), -num, Integer::sum);
            }
            if (!isVirtualTarget(e) && e.getTargetProductId() != null && e.getTargetProductId() > 0) {
                map.merge(e.getTargetProductId(), num, Integer::sum);
            }
        }
        return map;
    }

    /**
     * 已完成换货对【上级】实体库存的影响：下级换货发出新品 = 从上级实体库存中出库，
     * 上级对应商品可供应量 -num。仅统计换入实体商品的换货单（虚拟换虚拟不走发货）。
     * 待收货(5) 也计入——货已从上级发出、在途，不能再按有货算。
     */
    private Map<Integer, Integer> exchangeOutDeltaMap(Integer agentId) {
        Map<Integer, Integer> map = new HashMap<>();
        List<StockExchange> done = stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getParentAgentId, agentId)
                .in(StockExchange::getStatus, StockExchange.STATUS_COMPLETE, StockExchange.STATUS_WAIT_RECEIVE)
                .eq(StockExchange::getIsDel, 0));
        for (StockExchange e : done) {
            if (isVirtualTarget(e) || e.getTargetProductId() == null || e.getTargetProductId() <= 0) {
                continue;
            }
            int num = e.getNum() == null ? 0 : e.getNum();
            map.merge(e.getTargetProductId(), -num, Integer::sum);
        }
        return map;
    }

    /**
     * 换货中（未完成未驳回）对该代理【自身】实体库存的占用：
     * 换货单提交即锁定换出商品数量，锁定期间该数量不可线下销售、不可供货给下级；
     * 完成后由 exchangeStockDeltaMap 的「换出-换入」净额接管（两图状态集不相交，不会重复扣减），驳回即自动释放。
     * 仅统计实体来源换货（exchange_type=1 的虚拟换货在申请时已即时扣减虚拟库存，与实体无关）。
     */
    private Map<Integer, Integer> exchangePendingDeltaMap(Integer agentId) {
        Map<Integer, Integer> map = new HashMap<>();
        List<StockExchange> pending = stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getAgentId, agentId)
                .in(StockExchange::getStatus, StockExchange.STATUS_WAIT_PARENT_AUDIT,
                        StockExchange.STATUS_WAIT_HQ_AUDIT, StockExchange.STATUS_WAIT_BACK,
                        StockExchange.STATUS_WAIT_SEND, StockExchange.STATUS_WAIT_RECEIVE)
                .eq(StockExchange::getIsDel, 0));
        for (StockExchange e : pending) {
            if (isVirtualExchange(e) || e.getProductId() == null) {
                continue;
            }
            int num = e.getNum() == null ? 0 : e.getNum();
            if (num > 0) {
                map.merge(e.getProductId(), -num, Integer::sum);
            }
        }
        return map;
    }

    @Override
    public void sellOffline(Integer uid, StockRequests.StockOfflineSaleRequest request) {
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            throw new CrmebException("您还不是订货代理或已被禁用");
        }
        if (request.getNum() == null || request.getNum() <= 0) {
            throw new CrmebException("销售数量必须大于0");
        }
        int available = getAgentStockNum(agent, request.getProductId());
        if (available < request.getNum()) {
            throw new CrmebException("可销售实体库存不足，当前剩余：" + available);
        }
        StoreProduct product = storeProductService.getById(request.getProductId());
        if (product == null || product.getIsDel() || !product.getIsShow()) {
            throw new CrmebException("商品不存在或已下架");
        }
        transactionTemplate.executeWithoutResult(status -> {
            // 扣云仓库存并写库存日志（type=4 线下销售）
            stockService.deductStock(request.getProductId(), request.getNum(), 4,
                    "OFFLINE" + System.currentTimeMillis(), "线下销售出库");
            StockOfflineSale record = new StockOfflineSale();
            record.setUid(uid);
            record.setAgentId(agent.getId());
            record.setProductId(request.getProductId());
            record.setSkuKey(request.getSkuKey() == null ? "" : request.getSkuKey());
            record.setProductName(product.getStoreName());
            record.setImage(product.getImage());
            record.setNum(request.getNum());
            record.setMark(request.getMark() == null ? "" : request.getMark());
            record.setIsDel(0);
            stockOfflineSaleDao.insert(record);
        });
    }

    // ==================== 换货设置（是否允许换货） ====================

    /**
     * 订货单驳回退款：已支付货款全额退回下单人余额（幂等：按订单号查退款流水，已退过则跳过）
     */
    private void refundOrderToBalance(StockOrder order, String reason) {
        if (order.getPayStatus() == null || order.getPayStatus() != 1
                || order.getTotalPrice() == null || order.getTotalPrice().signum() <= 0) {
            return;
        }
        int refunded = userBillService.count(new LambdaQueryWrapper<UserBill>()
                .eq(UserBill::getLinkId, order.getId().toString())
                .eq(UserBill::getType, Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND)
                .eq(UserBill::getCategory, Constants.USER_BILL_CATEGORY_MONEY));
        if (refunded > 0) {
            return;
        }
        User user = userService.getById(order.getUid());
        if (user == null) {
            return;
        }
        userService.updateNowMoney(user, order.getTotalPrice(), "add");
        UserBill bill = new UserBill();
        bill.setPm(1);
        bill.setUid(order.getUid());
        bill.setLinkId(order.getId().toString());
        bill.setTitle("订货退款");
        bill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
        bill.setType(Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND);
        bill.setNumber(order.getTotalPrice());
        bill.setBalance(user.getNowMoney().add(order.getTotalPrice()));
        bill.setMark("订货单 " + order.getOrderNo() + " 被驳回，货款退回余额"
                + (reason == null || reason.isEmpty() ? "" : "（" + reason + "）"));
        userBillService.save(bill);
    }

    /**
     * 换货单驳回退款：已支付的换货差价退回申请人余额（幂等：按换货单号查退款流水）
     */
    private void refundExchangeDiffToBalance(StockExchange exchange) {
        if (exchange.getDiffPayStatus() == null || exchange.getDiffPayStatus() != 1
                || exchange.getDiffPrice() == null || exchange.getDiffPrice().signum() <= 0) {
            return;
        }
        int refunded = userBillService.count(new LambdaQueryWrapper<UserBill>()
                .eq(UserBill::getLinkId, exchange.getId().toString())
                .eq(UserBill::getType, Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND)
                .eq(UserBill::getCategory, Constants.USER_BILL_CATEGORY_MONEY)
                .like(UserBill::getMark, exchange.getExchangeNo()));
        if (refunded > 0) {
            return;
        }
        User user = userService.getById(exchange.getUid());
        if (user == null) {
            return;
        }
        userService.updateNowMoney(user, exchange.getDiffPrice(), "add");
        UserBill bill = new UserBill();
        bill.setPm(1);
        bill.setUid(exchange.getUid());
        bill.setLinkId(exchange.getId().toString());
        bill.setTitle("换货差价退款");
        bill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
        bill.setType(Constants.USER_BILL_TYPE_PAY_PRODUCT_REFUND);
        bill.setNumber(exchange.getDiffPrice());
        bill.setBalance(user.getNowMoney().add(exchange.getDiffPrice()));
        bill.setMark("换货单 " + exchange.getExchangeNo() + " 被驳回，差价退回余额");
        userBillService.save(bill);
    }


    @Override
    public List<StockExchangeConfig> getExchangeConfigList(Integer productId) {
        return stockExchangeConfigDao.selectList(new LambdaQueryWrapper<StockExchangeConfig>()
                .eq(StockExchangeConfig::getProductId, productId)
                .orderByAsc(StockExchangeConfig::getSkuKey));
    }

    @Override
    public void saveExchangeConfig(StockRequests.StockExchangeConfigRequest request) {
        String skuKey = request.getSkuKey() == null ? "" : request.getSkuKey().trim();
        BigDecimal minPrice = request.getMinTargetPrice() == null ? BigDecimal.ZERO : request.getMinTargetPrice();
        StockExchangeConfig exist = stockExchangeConfigDao.selectOne(new LambdaQueryWrapper<StockExchangeConfig>()
                .eq(StockExchangeConfig::getProductId, request.getProductId())
                .eq(StockExchangeConfig::getSkuKey, skuKey)
                .last(" limit 1"));
        if (exist != null) {
            stockExchangeConfigDao.update(null, new LambdaUpdateWrapper<StockExchangeConfig>()
                    .eq(StockExchangeConfig::getId, exist.getId())
                    .set(StockExchangeConfig::getEnable, request.getEnable())
                    .set(StockExchangeConfig::getMinTargetPrice, minPrice));
        } else {
            StockExchangeConfig cfg = new StockExchangeConfig();
            cfg.setProductId(request.getProductId());
            cfg.setSkuKey(skuKey);
            cfg.setEnable(request.getEnable());
            cfg.setMinTargetPrice(minPrice);
            stockExchangeConfigDao.insert(cfg);
        }
    }

    @Override
    public boolean isExchangeAllowed(Integer productId, String skuKey) {
        String sku = skuKey == null ? "" : skuKey;
        StockExchangeConfig cfg = stockExchangeConfigDao.selectOne(new LambdaQueryWrapper<StockExchangeConfig>()
                .eq(StockExchangeConfig::getProductId, productId)
                .eq(StockExchangeConfig::getSkuKey, sku)
                .last(" limit 1"));
        if (cfg == null && !sku.isEmpty()) {
            // 规格未单独配置时回退整品级
            cfg = stockExchangeConfigDao.selectOne(new LambdaQueryWrapper<StockExchangeConfig>()
                    .eq(StockExchangeConfig::getProductId, productId)
                    .eq(StockExchangeConfig::getSkuKey, "")
                    .last(" limit 1"));
        }
        // 未配置过 = 不限制（沿用原行为，允许换货）
        return cfg == null || Boolean.TRUE.equals(cfg.getEnable());
    }

    /** 最低换入价（规格级配置优先，未配置回退整品级；无配置返回 null） */
    private BigDecimal getMinTargetPrice(Integer productId, String skuKey) {
        String sku = skuKey == null ? "" : skuKey;
        StockExchangeConfig cfg = stockExchangeConfigDao.selectOne(new LambdaQueryWrapper<StockExchangeConfig>()
                .eq(StockExchangeConfig::getProductId, productId)
                .eq(StockExchangeConfig::getSkuKey, sku)
                .last(" limit 1"));
        if (cfg == null && !sku.isEmpty()) {
            cfg = stockExchangeConfigDao.selectOne(new LambdaQueryWrapper<StockExchangeConfig>()
                    .eq(StockExchangeConfig::getProductId, productId)
                    .eq(StockExchangeConfig::getSkuKey, "")
                    .last(" limit 1"));
        }
        return cfg == null ? null : cfg.getMinTargetPrice();
    }

    /** 自动匹配该会员最近一笔包含指定商品且已完成的订货单（会员端从库存页发起换货时用）
     *  stockType 非空时按库存类型过滤：1=实体库存（含历史空值） 2=虚拟库存
     */
    private StockOrder findLatestCompletedOrder(Integer uid, Integer productId, Integer stockType) {
        LambdaQueryWrapper<StockOrder> lqw = new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getUid, uid)
                .eq(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                .eq(StockOrder::getIsDel, 0);
        if (stockType != null && stockType == 2) {
            lqw.eq(StockOrder::getStockType, StockOrder.STOCK_TYPE_VIRTUAL);
        } else if (stockType != null) {
            lqw.and(w -> w.eq(StockOrder::getStockType, StockOrder.STOCK_TYPE_PHYSICAL)
                    .or().isNull(StockOrder::getStockType));
        }
        lqw.orderByDesc(StockOrder::getId).last(" limit 50");
        List<StockOrder> orders = stockOrderDao.selectList(lqw);
        for (StockOrder o : orders) {
            // 虚拟提货单不参与换货，自动匹配时跳过
            if (StockOrder.ORDER_TYPE_PICKUP.equals(o.getOrderType())) {
                continue;
            }
            Integer cnt = stockOrderProductDao.selectCount(new LambdaQueryWrapper<StockOrderProduct>()
                    .eq(StockOrderProduct::getOrderId, o.getId())
                    .eq(StockOrderProduct::getProductId, productId));
            if (cnt != null && cnt > 0) {
                return o;
            }
        }
        return null;
    }

    @Override
    public HashMap<String, Object> getExchangeQuota(Integer uid, Integer productId, String skuKey, Integer orderId, Integer sourceStockType) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderId == null ? 0 : orderId);
        map.put("orderNo", "");
        map.put("purchased", 0);   // 原订单该商品购买数量
        map.put("exchanged", 0);   // 该单该商品已申请换货数量（含在途，驳回不计）
        map.put("remain", 0);      // 还可申请数量
        map.put("blocked", false); // 一单一换开关下是否已有有效换货单
        map.put("blockedExchangeNo", "");
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || productId == null) {
            return map;
        }
        StockOrder order;
        if (orderId != null) {
            order = stockOrderDao.selectById(orderId);
        } else {
            order = findLatestCompletedOrder(uid, productId, sourceStockType);
        }
        if (order == null || order.getIsDel() == 1 || !order.getUid().equals(uid)
                || !order.getStatus().equals(StockOrder.STATUS_COMPLETE)) {
            map.put("blocked", true);
            map.put("blockedReason", "未找到可换货的已完成订单");
            return map;
        }
        // 虚拟提货单（order_type=2）不支持换货，前端据此隐藏入口并给出提示
        if (StockOrder.ORDER_TYPE_PICKUP.equals(order.getOrderType())) {
            map.put("blocked", true);
            map.put("blockedReason", "虚拟库存提货单不支持换货");
            return map;
        }
        String sku = skuKey == null ? "" : skuKey.trim();
        StockOrderProduct item = stockOrderProductDao.selectOne(new LambdaQueryWrapper<StockOrderProduct>()
                .eq(StockOrderProduct::getOrderId, order.getId())
                .eq(StockOrderProduct::getProductId, productId)
                .eq(!sku.isEmpty(), StockOrderProduct::getSkuKey, sku)
                .last(" limit 1"));
        if (item == null) {
            map.put("blocked", true);
            map.put("blockedReason", "该订单中没有此商品");
            return map;
        }
        int purchased = item.getNum() == null ? 0 : item.getNum();
        int used = 0;
        for (StockExchange e : stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getOrderId, order.getId())
                .eq(StockExchange::getProductId, productId)
                .ne(StockExchange::getStatus, StockExchange.STATUS_REJECT)
                .eq(StockExchange::getIsDel, 0))) {
            used += e.getNum() == null ? 0 : e.getNum();
        }
        map.put("orderId", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("purchased", purchased);
        map.put("exchanged", used);
        map.put("remain", Math.max(0, purchased - used));
        // 一单一换：已有有效换货单（非驳回）则整单不可再申请
        if (!"0".equals(systemConfigService.getValueByKey(CFG_EXCHANGE_ONCE))) {
            StockExchange active = stockExchangeDao.selectOne(new LambdaQueryWrapper<StockExchange>()
                    .eq(StockExchange::getOrderId, order.getId())
                    .ne(StockExchange::getStatus, StockExchange.STATUS_REJECT)
                    .eq(StockExchange::getIsDel, 0)
                    .orderByDesc(StockExchange::getId)
                    .last(" limit 1"));
            if (active != null) {
                map.put("blocked", true);
                map.put("blockedExchangeNo", active.getExchangeNo());
                map.put("blockedReason", "该订单已申请过换货，不能重复申请换货");
            }
        }
        return map;
    }

    @Override
    public List<StockExchangeTarget> getExchangeTargetList(Integer productId, String skuKey) {
        String sku = skuKey == null ? "" : skuKey.trim();
        List<StockExchangeTarget> list = stockExchangeTargetDao.selectList(new LambdaQueryWrapper<StockExchangeTarget>()
                .eq(StockExchangeTarget::getProductId, productId)
                .eq(StockExchangeTarget::getSkuKey, sku));
        if (list.isEmpty() && !sku.isEmpty()) {
            // 规格未单独配置时回退整品级
            list = stockExchangeTargetDao.selectList(new LambdaQueryWrapper<StockExchangeTarget>()
                    .eq(StockExchangeTarget::getProductId, productId)
                    .eq(StockExchangeTarget::getSkuKey, ""));
        }
        return list;
    }

    @Override
    public void saveExchangeTargets(StockRequests.StockExchangeTargetSaveRequest request) {
        String sku = request.getSkuKey() == null ? "" : request.getSkuKey().trim();
        transactionTemplate.executeWithoutResult(status -> {
            stockExchangeTargetDao.delete(new LambdaQueryWrapper<StockExchangeTarget>()
                    .eq(StockExchangeTarget::getProductId, request.getProductId())
                    .eq(StockExchangeTarget::getSkuKey, sku));
            if (request.getTargets() == null) {
                return;
            }
            for (StockRequests.StockExchangeTargetSaveRequest.TargetItem item : request.getTargets()) {
                if (item == null || item.getTargetProductId() == null) {
                    continue;
                }
                StoreProduct tp = storeProductService.getById(item.getTargetProductId());
                if (tp == null || tp.getIsDel()) {
                    continue;
                }
                StockExchangeTarget t = new StockExchangeTarget();
                t.setProductId(request.getProductId());
                t.setSkuKey(sku);
                t.setTargetProductId(item.getTargetProductId());
                t.setTargetSkuKey(item.getTargetSkuKey() == null ? "" : item.getTargetSkuKey().trim());
                t.setTargetProductName(tp.getStoreName());
                stockExchangeTargetDao.insert(t);
            }
        });
    }

    // ==================== 换货可选目标与差价 ====================

    @Override
    public List<HashMap<String, Object>> getExchangeOptions(Integer uid, Integer productId, String skuKey) {
        List<HashMap<String, Object>> out = new ArrayList<>();
        StockAgent agent = stockService.getAgentByUid(uid);
        if (agent == null || agent.getStatus() != 1) {
            return out;
        }
        String sku = skuKey == null ? "" : skuKey.trim();
        if (!isExchangeAllowed(productId, sku)) {
            return out;
        }
        BigDecimal originPrice = stockService.getProductPrice(agent, productId, sku);
        BigDecimal minTargetPrice = getMinTargetPrice(productId, sku);
        for (StockExchangeTarget t : getExchangeTargetList(productId, sku)) {
            BigDecimal targetPrice = stockService.getProductPrice(agent, t.getTargetProductId(), t.getTargetSkuKey());
            // 只能换同价或更高价（不能换比当前金额少的商品）
            if (targetPrice.compareTo(originPrice) < 0) {
                continue;
            }
            // 低于设置的最低换入价的商品不下发
            if (minTargetPrice != null && minTargetPrice.signum() > 0 && targetPrice.compareTo(minTargetPrice) < 0) {
                continue;
            }
            StoreProduct tp = storeProductService.getById(t.getTargetProductId());
            HashMap<String, Object> row = new HashMap<>();
            row.put("targetProductId", t.getTargetProductId());
            row.put("targetSkuKey", t.getTargetSkuKey());
            row.put("targetProductName", t.getTargetProductName());
            row.put("image", tp == null ? "" : tp.getImage());
            row.put("retailPrice", tp == null || tp.getPrice() == null ? BigDecimal.ZERO : tp.getPrice());
            // 规格名（取规格的 attrValue，如「豆沙绿 / XS」）
            String skuName = "";
            if (t.getTargetSkuKey() != null && !t.getTargetSkuKey().trim().isEmpty()) {
                for (HashMap<String, Object> skuItem : stockService.getProductSkuList(t.getTargetProductId())) {
                    if (t.getTargetSkuKey().trim().equals(String.valueOf(skuItem.get("skuKey")))) {
                        Object avText = skuItem.get("attrValue");
                        String raw = avText == null ? "" : String.valueOf(avText);
                        if (raw.startsWith("{")) {
                            raw = raw.replace("{", "").replace("}", "").replace("\"", "")
                                    .replace(":", "：").replace(",", " / ");
                        }
                        skuName = raw;
                        break;
                    }
                }
            }
            row.put("skuName", skuName);
            row.put("originPrice", originPrice);
            row.put("targetPrice", targetPrice);
            row.put("diffPrice", targetPrice.subtract(originPrice));
            out.add(row);
        }
        return out;
    }

    @Override
    public HashMap<String, Object> payExchangeDiff(Integer uid, StockRequests.StockExchangeDiffPayRequest request) {
        StockExchange exchange = stockExchangeDao.selectById(request.getExchangeId());
        if (exchange == null || exchange.getIsDel() == 1 || !uid.equals(exchange.getUid())) {
            throw new CrmebException("换货单不存在");
        }
        if (exchange.getDiffPrice() == null || exchange.getDiffPrice().signum() <= 0) {
            throw new CrmebException("该换货单无需支付差价");
        }
        if (exchange.getDiffPayStatus() != null && exchange.getDiffPayStatus() == 1) {
            throw new CrmebException("差价已支付，请勿重复支付");
        }
        if (StockExchange.STATUS_REJECT.equals(exchange.getStatus())) {
            throw new CrmebException("换货单已驳回，无法支付差价");
        }
        HashMap<String, Object> result = new HashMap<>();
        if ("yue".equalsIgnoreCase(request.getPayType())) {
            User user = userService.getById(uid);
            if (user == null || user.getNowMoney() == null
                    || user.getNowMoney().compareTo(exchange.getDiffPrice()) < 0) {
                throw new CrmebException("余额不足，请先充值");
            }
            transactionTemplate.executeWithoutResult(status -> {
                userService.updateNowMoney(user, exchange.getDiffPrice(), "sub");
                UserBill bill = new UserBill();
                bill.setPm(0);
                bill.setUid(uid);
                bill.setLinkId(exchange.getId().toString());
                bill.setTitle("换货差价");
                bill.setCategory(Constants.USER_BILL_CATEGORY_MONEY);
                bill.setType(Constants.USER_BILL_TYPE_PAY_ORDER);
                bill.setNumber(exchange.getDiffPrice());
                bill.setBalance(user.getNowMoney().subtract(exchange.getDiffPrice()));
                bill.setMark("换货单 " + exchange.getExchangeNo() + " 差价支付");
                userBillService.save(bill);
                exchange.setDiffPayStatus(1);
                exchange.setDiffPayType("yue");
                exchange.setDiffPayTime(new Date());
                stockExchangeDao.updateById(exchange);
            });
            // 差价奖励不在支付时结算：等换货单走到「已完成」再发（见 doSendExchangeNew / completeVirtualToVirtual）
            result.put("paid", true);
            result.put("payType", "yue");
        } else if ("weixin".equalsIgnoreCase(request.getPayType())) {
            throw new CrmebException("请通过微信支付接口发起差价支付");
        } else {
            throw new CrmebException("不支持的支付方式");
        }
        return result;
    }

    @Override
    public boolean confirmExchangeDiffPaid(String exchangeNo) {
        StockExchange exchange = stockExchangeDao.selectOne(new LambdaQueryWrapper<StockExchange>()
                .eq(StockExchange::getExchangeNo, exchangeNo)
                .eq(StockExchange::getIsDel, 0)
                .last(" limit 1"));
        if (exchange == null) {
            return false;
        }
        if (exchange.getDiffPayStatus() != null && exchange.getDiffPayStatus() == 1) {
            return true;
        }
        exchange.setDiffPayStatus(1);
        exchange.setDiffPayType("weixin");
        exchange.setDiffPayTime(new Date());
        stockExchangeDao.updateById(exchange);
        // 差价奖励不在支付时结算：等换货单完成再发
        stockRewardService.sendNotice(exchange.getUid(), StockNotice.TYPE_REWARD, "换货差价支付成功",
                "换货单 " + exchangeNo + " 差价 ¥" + exchange.getDiffPrice() + " 已支付成功");
        return true;
    }

    /** 校验上级对订单明细各项均有足够库存 */
    private boolean parentHasStock(StockAgent parent, List<StockOrderProduct> items) {
        return parentHasStock(parent, items, null);
    }

    /**
     * 校验上级能否满足订单明细。
     *
     * @param excludeOrderId 判定时从上级"已供应"中排除的订单ID（该订单自身，见 getAgentStockNum 说明）
     */
    private boolean parentHasStock(StockAgent parent, List<StockOrderProduct> items, Integer excludeOrderId) {
        for (StockOrderProduct op : items) {
            if (getAgentStockNum(parent, op.getProductId(), excludeOrderId) < op.getNum()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 虚拟单校验上级能否满足：**只看上级虚拟库存**（remain_num），
     * 不能用实体口径 getAgentStockNum——否则会出现「上级虚拟卖空了还在发货」，
     * 下级全额入账、上级虚拟被扣成 0，两边账目对不上（2026-09-21 修）。
     */
    private boolean parentHasVirtualStock(StockAgent parent, List<StockOrderProduct> items) {
        for (StockOrderProduct op : items) {
            if (getParentVirtualStockNum(parent.getUid(), op.getProductId(), op.getSkuKey()) < op.getNum()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 上级能否满足订单 —— 按库存类型分流：
     * 虚拟单只看上级虚拟库存（remain_num），实体单看实体可供应量。
     * 虚拟单绝不用实体口径判定，否则会出现「上级虚拟卖空仍发货、下级全额入账」的错账。
     *
     * @param excludeOrderId 实体口径下从上级"已供应"中排除的订单ID（该订单自身，见 getAgentStockNum 说明）
     */
    private boolean parentCanSupply(StockAgent parent, List<StockOrderProduct> items,
                                    Integer excludeOrderId, boolean virtual) {
        if (parent == null) {
            return true;
        }
        return virtual ? parentHasVirtualStock(parent, items) : parentHasStock(parent, items, excludeOrderId);
    }

    /**
     * 虚拟单按上级虚拟库存分货：可满足部分留在原单正常完成，缺口部分拆出一张
     * 「等待匹配上级」的子单（已付款、状态 10；等上级补货或超时自动向上匹配有虚拟库存的上级）。
     * 例：下单 2 件、上级虚拟仅 1 件 → 原单留 1 件入账完成，子单 1 件进等待队列。
     * 一件都供不了时返回 null（原单整单走等待，由付款流程的 parentStockOk=false 分支处理）。
     * 须在事务内调用。
     */
    private StockOrder splitVirtualOrderIfShort(StockOrder order, List<StockOrderProduct> items,
                                                StockAgent parent, Integer payType) {
        List<StockOrderProduct> waitItems = new ArrayList<>();
        int keepTotal = 0;
        int waitTotal = 0;
        BigDecimal keepAmount = BigDecimal.ZERO;
        BigDecimal waitAmount = BigDecimal.ZERO;
        for (StockOrderProduct op : items) {
            int need = op.getNum() == null ? 0 : op.getNum();
            int avail = getParentVirtualStockNum(parent.getUid(), op.getProductId(), op.getSkuKey());
            int keep = Math.max(0, Math.min(need, avail));
            int wait = need - keep;
            BigDecimal price = op.getPrice() == null ? BigDecimal.ZERO : op.getPrice();
            keepTotal += keep;
            keepAmount = keepAmount.add(price.multiply(new BigDecimal(keep)));
            if (wait > 0) {
                waitTotal += wait;
                waitAmount = waitAmount.add(price.multiply(new BigDecimal(wait)));
                StockOrderProduct w = new StockOrderProduct();
                w.setProductId(op.getProductId());
                w.setSkuKey(op.getSkuKey());
                w.setProductName(op.getProductName());
                w.setImage(op.getImage());
                w.setNum(wait);
                w.setPrice(price);
                w.setParentPrice(op.getParentPrice());
                w.setTotalPrice(price.multiply(new BigDecimal(wait)));
                waitItems.add(w);
            }
            if (keep != need) {
                // 原单明细收敛到可满足数量；keep=0 的明细直接删除，避免留下 0 数量的明细行
                if (keep <= 0) {
                    stockOrderProductDao.deleteById(op.getId());
                } else {
                    op.setNum(keep);
                    op.setTotalPrice(price.multiply(new BigDecimal(keep)));
                    stockOrderProductDao.updateById(op);
                }
            }
        }
        if (waitItems.isEmpty() || keepTotal <= 0) {
            return null;
        }
        stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                .eq(StockOrder::getId, order.getId())
                .set(StockOrder::getTotalNum, keepTotal)
                .set(StockOrder::getTotalPrice, keepAmount));
        order.setTotalNum(keepTotal);
        order.setTotalPrice(keepAmount);
        StockOrder waitOrder = new StockOrder();
        waitOrder.setOrderNo("SK" + System.currentTimeMillis() + String.valueOf((int) ((Math.random() * 9 + 1) * 1000)));
        waitOrder.setUid(order.getUid());
        waitOrder.setAgentId(order.getAgentId());
        waitOrder.setParentAgentId(order.getParentAgentId());
        waitOrder.setLevelName(order.getLevelName());
        waitOrder.setTotalNum(waitTotal);
        waitOrder.setTotalPrice(waitAmount);
        waitOrder.setPayStatus(1);
        waitOrder.setPayType(payType == null ? order.getPayType() : payType);
        waitOrder.setPayTime(new Date());
        waitOrder.setStatus(StockOrder.STATUS_WAIT_MATCH);
        waitOrder.setUpSearchTime(new Date());
        waitOrder.setUpSearchNum(0);
        waitOrder.setStockType(order.getStockType());
        waitOrder.setOrderType(order.getOrderType());
        waitOrder.setAddressId(order.getAddressId());
        waitOrder.setRealName(order.getRealName());
        waitOrder.setPhone(order.getPhone());
        waitOrder.setUserAddress(order.getUserAddress());
        waitOrder.setMark("虚拟库存不足拆分等待（原单 " + order.getOrderNo() + "）");
        waitOrder.setIsDel(0);
        stockOrderDao.insert(waitOrder);
        for (StockOrderProduct w : waitItems) {
            w.setOrderId(waitOrder.getId());
            stockOrderProductDao.insert(w);
        }
        return waitOrder;
    }

    /**
     * 处理等待中的订单（懒触发）：状态=10（等待匹配）且 upSearchTime 超过等待时长的订单，
     * 自动把下单代理向上改挂到第一个有货的更高级上级（都没有货则挂到总部）
     */
    private void processUpSearchOrders(StockAgent agent) {
        Date expire = new Date(System.currentTimeMillis() - getUpSearchHours() * 3600_000L);
        List<StockOrder> waiting = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getAgentId, agent.getId())
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH).eq(StockOrder::getIsDel, 0)
                .isNotNull(StockOrder::getUpSearchTime)
                .le(StockOrder::getUpSearchTime, expire));
        if (waiting.isEmpty()) {
            return;
        }
        for (StockOrder order : waiting) {
            try {
                matchUpOrder(order, agent);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 把「等待匹配(10)」订单释放回正常订货流程，并把订单改挂到 targetParentAgentId
     * （targetParentAgentId = 当前直接上级时为原地释放，不改上级）。
     * 释放结果：审核开关开启 -> 0 待上级审核；虚拟单(开关关闭) -> 4 完成并入账；实体单(开关关闭) -> 2 待发货并补扣云仓库存。
     *
     * @return 是否实际完成释放（状态非10或并发冲突时为 false）
     */
    private boolean releaseWaitMatchOrder(StockOrder order, Integer targetParentAgentId,
                                          List<StockOrderProduct> items) {
        boolean needAudit = !"0".equals(systemConfigService.getValueByKey(CFG_ORDER_AUDIT));
        boolean virtual = isVirtualOrder(order);
        int oldParent = order.getParentAgentId() == null ? 0 : order.getParentAgentId();
        boolean parentChanged = oldParent != (targetParentAgentId == null ? 0 : targetParentAgentId);
        LambdaUpdateWrapper<StockOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(StockOrder::getId, order.getId())
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                .set(StockOrder::getParentAgentId, targetParentAgentId)
                .set(StockOrder::getUpSearchTime, null);
        if (parentChanged) {
            wrapper.set(StockOrder::getUpSearchNum,
                    (order.getUpSearchNum() == null ? 0 : order.getUpSearchNum()) + 1);
        }
        if (needAudit) {
            wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT);
        } else if (virtual) {
            wrapper.set(StockOrder::getStatus, StockOrder.STATUS_COMPLETE)
                    .set(StockOrder::getFinishTime, new Date());
        } else {
            wrapper.set(StockOrder::getStatus, StockOrder.STATUS_WAIT_SEND);
        }
        if (stockOrderDao.update(null, wrapper) <= 0) {
            return false;
        }
        // 同步内存对象，后续入账/结算/扣减均以新状态与新上级为准
        order.setParentAgentId(targetParentAgentId);
        order.setUpSearchTime(null);
        if (needAudit) {
            order.setStatus(StockOrder.STATUS_WAIT_PARENT_AUDIT);
        } else if (virtual) {
            order.setStatus(StockOrder.STATUS_COMPLETE);
        } else {
            order.setStatus(StockOrder.STATUS_WAIT_SEND);
        }
        if (!needAudit && virtual) {
            // 审核开关关闭：虚拟单直接完成并入账虚拟库存
            for (StockOrderProduct op : items) {
                creditVirtualStock(order, op);
            }
            // 虚拟单完成：差价结算给上级（幂等，与付款即完成路径一致）
            settleOrderReward(getByOrderNo(order.getOrderNo()));
        }
        if (!needAudit && !virtual) {
            // 审核开关关闭：释放为待发货需补扣云仓库存（挂起时未扣）
            for (StockOrderProduct op : items) {
                try {
                    stockService.deductStockBySku(op.getProductId(), op.getSkuKey(), op.getNum(), 1,
                            order.getOrderNo(), "向上匹配后释放订单扣库存（审核开关关闭）");
                } catch (Exception ignored) {
                }
            }
            // 上级发货模式：提醒上级及时发货
            notifyParentDeliver(order);
        }
        return true;
    }

    /**
     * 等待匹配(10)订单归位为「待上级审核(0)」：清空匹配标记、保留原上级。
     * 仅做状态归位（供"直接上级已补货后直接审核"复用正常审核流程），库存扣减/入账由调用方流程负责。
     */
    private boolean restoreWaitMatchToAudit(StockOrder order) {
        boolean ok = stockOrderDao.update(null, new LambdaUpdateWrapper<StockOrder>()
                .eq(StockOrder::getId, order.getId())
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                .set(StockOrder::getStatus, StockOrder.STATUS_WAIT_PARENT_AUDIT)
                .set(StockOrder::getUpSearchTime, null)) > 0;
        if (ok) {
            order.setStatus(StockOrder.STATUS_WAIT_PARENT_AUDIT);
            order.setUpSearchTime(null);
        }
        return ok;
    }

    /**
     * 直接上级已补货 -> 立即把该上级名下「等待匹配(10)」且库存已能满足的订单释放回正常订货流程。
     * 这是"上级补货后可正常订货"的核心修复：原实现只在等待时长耗尽后才释放，导致补货后仍长期显示"匹配上级中"。
     *
     * @return 本次释放的订单数
     */
    private int releaseRestockedOrdersForParent(StockAgent parent) {
        if (parent == null) {
            return 0;
        }
        List<StockOrder> waiting = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getParentAgentId, parent.getId())
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                .eq(StockOrder::getIsDel, 0)
                .orderByAsc(StockOrder::getUpSearchTime)
                .orderByAsc(StockOrder::getId));
        int released = 0;
        for (StockOrder order : waiting) {
            try {
                List<StockOrderProduct> items = stockOrderProductDao.selectList(
                        new LambdaQueryWrapper<StockOrderProduct>().eq(StockOrderProduct::getOrderId, order.getId()));
                // 排除订单自身占用量后再判定，避免"补了货仍显示库存不足"
                if (!parentCanSupply(parent, items, order.getId(), isVirtualOrder(order))) {
                    continue;
                }
                if (releaseWaitMatchOrder(order, parent.getId(), items)) {
                    released++;
                    stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订单已进入上级审核",
                            "您的订货单 " + order.getOrderNo() + " 的上级已补货，订单已恢复正常订货流程"
                                    + (StockOrder.STATUS_WAIT_PARENT_AUDIT.equals(order.getStatus()) ? "，等待上级审核发货" : ""));
                    stockRewardService.sendNotice(parent.getUid(), StockNotice.TYPE_ORDER_AUDIT, "下级订单待审核",
                            "下级【" + nickOf(order.getUid()) + "】的订货单 " + order.getOrderNo()
                                    + " 已恢复正常订货流程，请及时审核发货");
                }
            } catch (Exception ignored) {
            }
        }
        return released;
    }

    @Override
    public void releaseRestockedWaitMatchOrders() {
        List<StockOrder> all = stockOrderDao.selectList(new LambdaQueryWrapper<StockOrder>()
                .eq(StockOrder::getStatus, StockOrder.STATUS_WAIT_MATCH)
                .eq(StockOrder::getIsDel, 0));
        Set<Integer> parentIds = new LinkedHashSet<>();
        for (StockOrder o : all) {
            if (o.getParentAgentId() != null && o.getParentAgentId() > 0) {
                parentIds.add(o.getParentAgentId());
            }
        }
        for (Integer pid : parentIds) {
            try {
                releaseRestockedOrdersForParent(stockService.getAgentById(pid));
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 单笔等待匹配订单处理：沿上级链向上找第一个有货的代理，改挂上级并释放订单。
     * 有货目标找到 或 全链无货挂总部 后：
     *   审核开关开启 -> 释放为 0 待上级审核；
     *   审核开关关闭 -> 释放为 2 待发货并补扣云仓库存（挂起期间未扣）。
     */
    private void matchUpOrder(StockOrder order, StockAgent agent) {
        List<StockOrderProduct> items = stockOrderProductDao.selectList(
                new LambdaQueryWrapper<StockOrderProduct>().eq(StockOrderProduct::getOrderId, order.getId()));
        StockAgent target = null;
        StockAgent current = agent.getParentId() != null && agent.getParentId() > 0
                ? stockService.getAgentById(agent.getParentId()) : null;
        int depth = 0;
        while (current != null && depth < 50) {
            // 排除订单自身占用量后再判定，上级补足库存时可原地释放
            if (current.getStatus() != 0 && parentCanSupply(current, items, order.getId(), isVirtualOrder(order))) {
                target = current;
                break;
            }
            current = current.getParentId() != null && current.getParentId() > 0
                    ? stockService.getAgentById(current.getParentId()) : null;
            depth++;
        }
        Integer targetParentAgentId = target == null ? 0 : target.getId();
        // 改挂上级
        StockAgent agentUpdate = new StockAgent();
        agentUpdate.setId(agent.getId());
        agentUpdate.setParentId(targetParentAgentId);
        stockAgentDao.updateById(agentUpdate);
        if (releaseWaitMatchOrder(order, targetParentAgentId, items)) {
            // 通知下单人
            stockRewardService.sendNotice(order.getUid(), StockNotice.TYPE_ORDER_AUDIT, "订单已自动匹配上级",
                    "您的订货单 " + order.getOrderNo() + " 因上级库存不足，已自动匹配至"
                            + (target == null ? "总部" : "更高级上级【" + nickOf(target.getUid()) + "】"));
        }
    }

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
        // 上级信息：后台「待上级审核」需展示直接上级的 UID/昵称/手机号；无上级代理则视为总部审核
        List<Integer> parentAgentIds = new ArrayList<>();
        for (StockOrder o : orders) {
            if (o.getParentAgentId() != null && o.getParentAgentId() > 0) {
                parentAgentIds.add(o.getParentAgentId());
            }
        }
        HashMap<Integer, StockAgent> parentAgentMap = new HashMap<>();
        if (!parentAgentIds.isEmpty()) {
            for (StockAgent a : stockAgentDao.selectList(new LambdaQueryWrapper<StockAgent>()
                    .in(StockAgent::getId, parentAgentIds))) {
                parentAgentMap.put(a.getId(), a);
            }
        }
        HashMap<Integer, User> parentUserMap = new HashMap<>();
        List<Integer> parentUids = new ArrayList<>();
        List<Integer> parentLevelIds = new ArrayList<>();
        for (StockAgent a : parentAgentMap.values()) {
            if (a.getUid() != null) {
                parentUids.add(a.getUid());
            }
            if (a.getLevelId() != null) {
                parentLevelIds.add(a.getLevelId());
            }
        }
        if (!parentUids.isEmpty()) {
            for (User u : userService.lambdaQuery().in(User::getUid, parentUids).list()) {
                parentUserMap.put(u.getUid(), u);
            }
        }
        // 层级名称不是 eb_stock_agent 的字段，需按 level_id 回查 eb_stock_level
        HashMap<Integer, String> parentLevelNameMap = new HashMap<>();
        if (!parentLevelIds.isEmpty()) {
            for (StockLevel lv : stockLevelDao.selectList(new LambdaQueryWrapper<StockLevel>()
                    .in(StockLevel::getId, parentLevelIds))) {
                parentLevelNameMap.put(lv.getId(), lv.getName());
            }
        }
        // 换货标记：存在非驳回的换货单即视为已换货（取最近一张用于展示单号与状态）
        HashMap<Integer, StockExchange> exchangeMap = new HashMap<>();
        for (StockExchange e : stockExchangeDao.selectList(new LambdaQueryWrapper<StockExchange>()
                .in(StockExchange::getOrderId, orderIds)
                .ne(StockExchange::getStatus, StockExchange.STATUS_REJECT)
                .eq(StockExchange::getIsDel, 0)
                .orderByDesc(StockExchange::getId))) {
            exchangeMap.putIfAbsent(e.getOrderId(), e);
        }
        for (StockOrder o : orders) {
            o.setProductList(itemMap.get(o.getId()));
            User u = userMap.get(o.getUid());
            o.setNickname(u == null ? "" : u.getNickname());
            o.setAvatar(u == null ? "" : u.getAvatar());
            o.setAgentPhone(u == null ? "" : u.getPhone());
            // phone 为收货电话快照（新单）；历史订单快照为空时回退显示用户手机号
            if (o.getPhone() == null || o.getPhone().isEmpty()) {
                o.setPhone(u == null ? "" : u.getPhone());
            }
            // 上级信息：有直接上级代理则展示其 UID/昵称/手机号，否则为总部审核
            StockAgent pa = o.getParentAgentId() == null ? null : parentAgentMap.get(o.getParentAgentId());
            if (pa == null) {
                o.setParentIsHeadquarters(1);
                o.setParentUid(null);
                o.setParentNickname("总部");
                o.setParentAvatar("");
                o.setParentPhone("");
                o.setParentLevelName("");
            } else {
                o.setParentIsHeadquarters(0);
                o.setParentUid(pa.getUid());
                o.setParentLevelName(parentLevelNameMap.get(pa.getLevelId()));
                User pu = parentUserMap.get(pa.getUid());
                o.setParentNickname(pu == null ? ("用户" + pa.getUid()) : pu.getNickname());
                o.setParentAvatar(pu == null ? "" : pu.getAvatar());
                o.setParentPhone(pu == null ? "" : pu.getPhone());
            }
            StockExchange e = exchangeMap.get(o.getId());
            o.setExchanged(e == null ? 0 : 1);
            o.setExchangeNo(e == null ? "" : e.getExchangeNo());
            o.setExchangeStatus(e == null ? null : e.getStatus());
            // 等待匹配上级：展示已等待时长
            if (StockOrder.STATUS_WAIT_MATCH.equals(o.getStatus()) && o.getUpSearchTime() != null) {
                long minutes = (System.currentTimeMillis() - o.getUpSearchTime().getTime()) / 60000;
                o.setWaitDurationText(minutes < 60 ? ("已等待 " + minutes + " 分钟")
                        : ("已等待 " + (minutes / 60) + " 小时" + (minutes % 60) + " 分"));
            }
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
        HashMap<Integer, Integer> stockTypeMap = new HashMap<>();
        for (StockOrder o : stockOrderDao.selectBatchIds(orderIds)) {
            orderNoMap.put(o.getId(), o.getOrderNo());
            stockTypeMap.put(o.getId(), o.getStockType());
        }
        // 商品缩略图（原商品 + 换入商品）
        List<Integer> productIds = new ArrayList<>();
        for (StockExchange e : list) {
            if (e.getProductId() != null && !productIds.contains(e.getProductId())) {
                productIds.add(e.getProductId());
            }
            if (e.getTargetProductId() != null && e.getTargetProductId() > 0 && !productIds.contains(e.getTargetProductId())) {
                productIds.add(e.getTargetProductId());
            }
        }
        HashMap<Integer, String> productImageMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            for (StoreProduct p : storeProductService.listByIds(productIds)) {
                productImageMap.put(p.getId(), p.getImage());
            }
        }
        // 换入商品规格名（按商品聚合规格后匹配 attrValue）
        HashMap<String, String> skuNameMap = new HashMap<>();        for (StockExchange e : list) {
            String sku = e.getTargetSkuKey() == null ? "" : e.getTargetSkuKey().trim();
            if (sku.isEmpty() || e.getTargetProductId() == null || skuNameMap.containsKey(e.getTargetProductId() + "::" + sku)) {
                continue;
            }
            String skuName = "";
            for (HashMap<String, Object> skuItem : stockService.getProductSkuList(e.getTargetProductId())) {
                if (sku.equals(String.valueOf(skuItem.get("skuKey")))) {
                    Object avText = skuItem.get("attrValue");
                    String raw = avText == null ? "" : String.valueOf(avText);
                    if (raw.startsWith("{")) {
                        raw = raw.replace("{", "").replace("}", "").replace("\"", "")
                                .replace(":", "：").replace(",", " / ");
                    }
                    skuName = raw;
                    break;
                }
            }
            skuNameMap.put(e.getTargetProductId() + "::" + sku, skuName);
        }
        // 旧品寄回地址：上级为总部(0/空)取后台配置地址；否则取上级会员默认收货地址
        String hqAddress = systemConfigService.getValueByKey(CFG_EXCHANGE_RETURN_ADDRESS);
        List<Integer> parentIds = new ArrayList<>();
        for (StockExchange e : list) {
            if (e.getParentAgentId() != null && e.getParentAgentId() > 0 && !parentIds.contains(e.getParentAgentId())) {
                parentIds.add(e.getParentAgentId());
            }
        }
        HashMap<Integer, StockAgent> parentAgentMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            for (StockAgent pa : stockAgentDao.selectBatchIds(parentIds)) {
                parentAgentMap.put(pa.getId(), pa);
            }
        }
        List<Integer> parentUids = new ArrayList<>();
        for (StockAgent pa : parentAgentMap.values()) {
            if (pa.getUid() != null && !parentUids.contains(pa.getUid())) {
                parentUids.add(pa.getUid());
            }
        }
        HashMap<Integer, UserAddress> parentAddrMap = new HashMap<>();
        if (!parentUids.isEmpty()) {
            // 默认地址优先；没有默认地址时回落到最近一条收货地址（很多老会员从不设置默认）
            for (UserAddress ua : userAddressService.lambdaQuery().in(UserAddress::getUid, parentUids)
                    .eq(UserAddress::getIsDel, false).orderByDesc(UserAddress::getId).list()) {
                if (Boolean.TRUE.equals(ua.getIsDefault())) {
                    parentAddrMap.put(ua.getUid(), ua);
                } else {
                    parentAddrMap.putIfAbsent(ua.getUid(), ua);
                }
            }
        }
        HashMap<Integer, User> parentUserMap = new HashMap<>();
        if (!parentUids.isEmpty()) {
            for (User pu : userService.lambdaQuery().in(User::getUid, parentUids).list()) {
                parentUserMap.put(pu.getUid(), pu);
            }
        }
        for (StockExchange e : list) {
            User u = userMap.get(e.getUid());
            e.setNickname(u == null ? "" : u.getNickname());
            e.setAvatar(u == null ? "" : u.getAvatar());
            e.setOrderNo(orderNoMap.get(e.getOrderId()) == null ? "" : orderNoMap.get(e.getOrderId()));
            e.setProductImage(productImageMap.get(e.getProductId()) == null ? "" : productImageMap.get(e.getProductId()));
            e.setStockType(stockTypeMap.get(e.getOrderId()));
            if (e.getTargetProductId() != null && e.getTargetProductId() > 0) {
                e.setTargetProductImage(productImageMap.get(e.getTargetProductId()) == null
                        ? "" : productImageMap.get(e.getTargetProductId()));
                String sku = e.getTargetSkuKey() == null ? "" : e.getTargetSkuKey().trim();
                e.setTargetSkuName(skuNameMap.getOrDefault(e.getTargetProductId() + "::" + sku, ""));
            }
            if (e.getParentAgentId() == null || e.getParentAgentId() <= 0) {
                e.setBackTarget("总部");
                e.setBackAddress(hqAddress == null || hqAddress.trim().isEmpty()
                        ? "总部未配置寄回地址，请联系客服" : hqAddress.trim());
            } else {
                StockAgent pa = parentAgentMap.get(e.getParentAgentId());
                if (pa == null) {
                    e.setBackTarget("上级");
                    e.setBackAddress("请联系上级获取寄回地址");
                } else {
                    User pu = parentUserMap.get(pa.getUid());
                    e.setBackTarget(pu == null ? "上级" : "上级 " + pu.getNickname());
                    UserAddress ua = parentAddrMap.get(pa.getUid());
                    if (ua == null) {
                        e.setBackAddress("上级未设置收货地址，请联系上级");
                    } else {
                        String contact = (ua.getRealName() == null ? "" : ua.getRealName() + " ")
                                + (ua.getPhone() == null ? "" : ua.getPhone() + "　");
                        e.setBackAddress(contact + buildAddressStr(ua));
                    }
                }
            }
        }
    }
}
