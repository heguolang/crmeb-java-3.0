package com.zbkj.service.service;

import com.zbkj.common.model.stock.StockExchange;
import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.StockOrderAddRequest;
import com.zbkj.common.request.StockRequests;

import java.util.HashMap;
import java.util.List;

/**
 * 订货系统-订单/换货服务
 */
public interface StockOrderService {

    // ==================== 会员端 ====================

    /** 代理下单 */
    HashMap<String, Object> createOrder(Integer uid, StockOrderAddRequest request);

    /** 我的订单列表（status: null=全部） */
    CommonPage<StockOrder> getMyOrderList(Integer uid, Integer status, com.zbkj.common.request.PageParamRequest page);

    /** 订单详情（校验归属或上级可见） */
    StockOrder getOrderDetail(Integer uid, Integer orderId);

    /** 上级审核订单：1=通过 -1=驳回（驳回需原因） */
    Boolean auditOrder(Integer uid, Integer orderId, StockRequests.StockAuditRequest request);

    /** 代理确认收货（触发奖励核算） */
    Boolean receiveOrder(Integer uid, Integer orderId);

    /** 上级代理发货（stock_parent_deliver=1 时启用） */
    Boolean parentSendOrder(Integer uid, Integer orderId, StockRequests.StockSendRequest request);

    /** 我需要审核的订单（直接下级提交） */
    CommonPage<StockOrder> getAuditOrderList(Integer uid, Integer status, com.zbkj.common.request.PageParamRequest page);

    /** 提交换货申请 */
    Boolean applyExchange(Integer uid, StockRequests.StockExchangeApplyRequest request);

    /** 我的换货单列表 */
    CommonPage<StockExchange> getMyExchangeList(Integer uid, com.zbkj.common.request.PageParamRequest page);

    /** 代理填写旧品退回快递 */
    Boolean fillBackExpress(Integer uid, Integer exchangeId, StockRequests.StockExchangeBackRequest request);

    // ==================== 后台 ====================

    /** 全部订货单列表 */
    CommonPage<StockOrder> getAdminOrderList(Integer uid, String orderNo, Integer status, Integer payStatus, com.zbkj.common.request.PageParamRequest page);

    /** 确认收款（记账欠款 -> 已付款，状态流转到待发货） */
    Boolean confirmPay(Integer orderId);

    /** 总部发货（填快递单号） */
    Boolean sendOrder(Integer orderId, StockRequests.StockSendRequest request);

    /** 总部标记订单完成（兼容处理） */
    Boolean finishOrder(Integer orderId);

    /** 总部审核换货单 */
    Boolean auditExchangeByHq(Integer exchangeId, StockRequests.StockAuditRequest request);

    /** 总部确认旧品入库（回补库存） */
    Boolean confirmExchangeBack(Integer exchangeId);

    /** 总部发新品（扣库存，填快递单号） */
    Boolean sendExchangeNew(Integer exchangeId, StockRequests.StockSendRequest request);

    /** 换货单列表 */
    CommonPage<StockExchange> getAdminExchangeList(Integer status, com.zbkj.common.request.PageParamRequest page);

    // ==================== 公共 ====================

    /** 上级审核换货单（状态 0 -> 1 / 驳回） */
    Boolean auditExchangeByParent(Integer uid, Integer exchangeId, StockRequests.StockAuditRequest request);

    /** 查询换货单 */
    StockExchange getExchange(Integer id);

    /** 订单完成后奖励核算入口（事务内由 receive/finish 调用） */
    void settleOrderReward(StockOrder order);
}
