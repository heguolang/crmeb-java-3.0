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

    /** 代理下单（生成待付款订单，返回上级库存是否充足供前端提示） */
    HashMap<String, Object> createOrder(Integer uid, StockOrderAddRequest request);

    /** 根据订单号查询订单 */
    StockOrder getByOrderNo(String orderNo);

    /** 订货单支付成功处理（幂等）：有货→待审核/待发货，无货→等待匹配上级(10) */
    Boolean payStockOrder(String orderNo, Integer payType);

    /** 代理主动取消待付款订单 */
    Boolean cancelStockOrder(Integer uid, Integer orderId);

    /** 定时任务：等待匹配(10)超时订单自动向上匹配有货上级 */
    void processExpiredUpSearchOrders();

    /**
     * 定时任务：直接上级已补货的「等待匹配」订单立即释放回正常订货流程（无需等满等待时长）。
     * 解决"上级已补货但订单仍卡在匹配上级中"的问题。
     */
    void releaseRestockedWaitMatchOrders();

    /** 定时任务：待付款超时订单自动取消(-2) */
    void cancelExpiredUnpaidOrders();

    // ==================== 虚拟库存（二期） ====================

    /** 我的虚拟库存列表（剩余可提货数量>0） */
    List<com.zbkj.common.model.stock.StockVirtualStock> getMyVirtualStock(Integer uid);

    /** 虚拟库存提货：扣减 remain_num，生成 order_type=2 提货单（总部发货扣云仓） */
    HashMap<String, Object> pickupVirtual(Integer uid, StockRequests.StockVirtualPickupRequest request);

    /** 我的实体库存（云仓可供应量 = 已付款采购 - 已供应给直接下级，按商品聚合） */
    List<HashMap<String, Object>> getMyPhysicalStock(Integer uid);

    /** 线下销售出库：登记出库记录并扣减云仓库存（后续从实体可供应量中扣减） */
    void sellOffline(Integer uid, StockRequests.StockOfflineSaleRequest request);

    /** 换货设置列表（按商品，含整品与各规格） */
    List<com.zbkj.common.model.stock.StockExchangeConfig> getExchangeConfigList(Integer productId);

    /** 保存换货设置（商品级或规格级） */
    void saveExchangeConfig(StockRequests.StockExchangeConfigRequest request);

    /** 某商品（规格）是否允许换货：规格级优先，回退整品级，都未配置则不限制 */
    boolean isExchangeAllowed(Integer productId, String skuKey);

    /** 换货可选目标清单（源商品+规格 → 目标商品+规格） */
    List<com.zbkj.common.model.stock.StockExchangeTarget> getExchangeTargetList(Integer productId, String skuKey);

    /** 保存换货可选目标（覆盖式） */
    void saveExchangeTargets(StockRequests.StockExchangeTargetSaveRequest request);

    /** 会员端：该商品规格可换入的目标（含目标拿货价与差价，已过滤低于原价的目标） */
    List<HashMap<String, Object>> getExchangeOptions(Integer uid, Integer productId, String skuKey);

    /** 换货可申请余量：原单购买数 / 已换数 / 还可申请数（一单一换时 blocked=true） */
    HashMap<String, Object> getExchangeQuota(Integer uid, Integer productId, String skuKey, Integer orderId, Integer sourceStockType);

    /** 换货差价支付（yue=余额 weixin=微信），支付成功后按比例奖励直接上级 */
    HashMap<String, Object> payExchangeDiff(Integer uid, StockRequests.StockExchangeDiffPayRequest request);

    /** 微信差价支付回调：按换货单号确认差价已付并发放上级奖励（幂等） */
    boolean confirmExchangeDiffPaid(String exchangeNo);

    /** 我的订单列表（status: null=全部） */
    CommonPage<StockOrder> getMyOrderList(Integer uid, Integer status, com.zbkj.common.request.PageParamRequest page);

    /**
     * 下级成员的订货订单列表（团队成员页查看下级订单）
     *
     * @param uid    当前用户（必须是 subUid 的上级链上任意一级）
     * @param subUid 被查看的下级用户 uid
     * @param status 订单状态 null=全部
     */
    CommonPage<StockOrder> getSubAgentOrderList(Integer uid, Integer subUid, Integer status, com.zbkj.common.request.PageParamRequest page);

    /** 订单详情（校验归属或上级可见） */
    StockOrder getOrderDetail(Integer uid, Integer orderId);

    /** 上级审核订单：1=通过 -1=驳回（驳回需原因） */
    Boolean auditOrder(Integer uid, Integer orderId, StockRequests.StockAuditRequest request);

    /** 代理确认收货（触发奖励核算） */
    Boolean receiveOrder(Integer uid, Integer orderId);

    /** 上级代理发货（stock_parent_deliver=1 时启用） */
    Boolean parentSendOrder(Integer uid, Integer orderId, StockRequests.StockSendRequest request);

    /** 上级代理修改已发货订单的物流信息（仅本上级发出的实体订货单，待收货状态） */
    Boolean parentUpdateExpress(Integer uid, Integer orderId, StockRequests.StockSendRequest request);

    /** 我需要审核的订单（直接下级提交） */
    CommonPage<StockOrder> getAuditOrderList(Integer uid, Integer status, com.zbkj.common.request.PageParamRequest page);

    /** 提交换货申请 */
    Boolean applyExchange(Integer uid, StockRequests.StockExchangeApplyRequest request);

    /** 我的换货单列表（我作为申请人提交的） */
    CommonPage<StockExchange> getMyExchangeList(Integer uid, com.zbkj.common.request.PageParamRequest page);

    /**
     * 待我审核的换货单列表（我作为直接上级，下级的实体换货申请）
     * status 为 null 时只看「待上级审核」；传 -2 表示我经手过的全部换货单
     */
    CommonPage<StockExchange> getExchangeAuditList(Integer uid, Integer status, com.zbkj.common.request.PageParamRequest page);

    /** 代理填写旧品退回快递 */
    Boolean fillBackExpress(Integer uid, Integer exchangeId, StockRequests.StockExchangeBackRequest request);

    /**
     * 订货中心角标计数：audit=待我审核的订货单、send=待我发货的订货单、exchangeAudit=待我审核的换货单
     */
    HashMap<String, Integer> myPendingCounts(Integer uid);

    // ==================== 后台 ====================

    /** 全部订货单列表 */
    CommonPage<StockOrder> getAdminOrderList(Integer uid, String orderNo, Integer status, Integer payStatus, com.zbkj.common.request.PageParamRequest page);

    /** 总部介入审核订单（仅待上级审核状态）：1=通过(扣云仓库存→待付款) -1=驳回 */
    Boolean auditOrderByAdmin(Integer orderId, StockRequests.StockAuditRequest request);

    /**
     * 后台手动跳过匹配：对等待匹配上级(状态10)的订单立即沿上级链向上找有库存的上级
     */
    Boolean skipMatchUpOrder(Integer orderId);

    /**
     * 等待匹配上级的订单列表（后台）
     */
    CommonPage<StockOrder> getWaitMatchOrderList(com.zbkj.common.request.PageParamRequest page);

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

    /** 上级在会员端确认旧品入库（直接上级 + 待旧品退回状态，状态 2 -> 3） */
    Boolean confirmExchangeBackByParent(Integer uid, Integer exchangeId);

    /** 上级在会员端发出新品（直接上级 + 待发新品状态，状态 3 -> 4） */
    Boolean sendExchangeNewByParent(Integer uid, Integer exchangeId, StockRequests.StockSendRequest request);

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
