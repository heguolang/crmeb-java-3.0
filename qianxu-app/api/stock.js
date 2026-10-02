// +----------------------------------------------------------------------
// | 订货系统 API（会员端）
// +----------------------------------------------------------------------

import request from "@/utils/request.js";

/** 我的代理身份 */
export function getStockAgentInfo() {
  return request.get('stock/agent/info');
}

/** 可选层级 */
export function getStockLevels() {
  return request.get('stock/agent/levels');
}

/** 新增下级订货商（对方需在订货中心同意后生效） */
export function createSubAgent(data) {
  return request.post('stock/agent/createSub', data);
}

/** 同意成为上级邀请的订货商 */
export function agreeStockAgent() {
  return request.post('stock/agent/agree');
}

/** 拒绝订货商邀请 */
export function rejectStockAgent() {
  return request.post('stock/agent/reject');
}

/** 我的下级列表 */
export function getSubAgentList() {
  return request.get('stock/agent/subList');
}

/** 订货商品中心 */
export function getStockProducts(params) {
  return request.get('stock/product/list', params);
}

/** 提交订货单 */
export function createStockOrder(data) {
  return request.post('stock/order/create', data);
}

/** 订货单支付（yue=余额 weixin=微信） */
export function payStockOrder(data) {
  return request.post('stock/order/pay', data);
}

/** 取消待付款订货单 */
export function cancelStockOrder(id) {
  return request.post('stock/order/cancel?id=' + id);
}

/** 我的虚拟库存列表 */
export function getMyVirtualStock() {
  return request.get('stock/virtual/list');
}

/** 虚拟库存提货 */
export function pickupVirtual(data) {
  return request.post('stock/virtual/pickup', data);
}

/** 我的实体库存（云仓可供应量） */
export function getMyPhysicalStock() {
  return request.get('stock/physical/list');
}

/** 线下销售出库（扣减库存） */
export function sellOffline(data) {
  return request.post('stock/physical/sell', data);
}

/** 我的库存变动记录（含后台手动调整；stockType 1=实体 2=虚拟，不传=全部） */
export function getMyStockLogs(params) {
  return request.get('stock/stockLog/list', params);
}

/** 我的订货单列表 */
export function getMyStockOrders(params) {
  return request.get('stock/order/list', params);
}

/** 下级成员的订货订单列表（团队成员页查看下级订单） */
export function getSubAgentOrders(params) {
  return request.get('stock/order/subList', params);
}

/** 订单详情 */
export function getStockOrderDetail(id) {
  return request.get('stock/order/detail', { id });
}

/** 待我审核的订单 */
export function getAuditOrders(params) {
  return request.get('stock/order/auditList', params);
}

/** 上级审核订单 */
export function auditStockOrder(id, data) {
  return request.post('stock/order/audit?id=' + id, data);
}

/** 上级代理发货（stock_parent_deliver=1 时启用；仅实体库存订货单） */
export function parentSendStockOrder(id, data) {
  return request.post('stock/order/parentSend?id=' + id, data);
}

/** 上级修改已发货订单的物流信息（仅本上级发出的实体订货单，待收货状态） */
export function parentUpdateStockExpress(id, data) {
  return request.post('stock/order/parentUpdateExpress?id=' + id, data);
}

/** 确认收货 */
export function receiveStockOrder(id) {
  return request.post('stock/order/receive?id=' + id);
}

/** 提交换货申请 */
export function applyStockExchange(data) {
  return request.post('stock/exchange/apply', data);
}

/** 我的换货单列表 */
export function getMyExchanges(params) {
  return request.get('stock/exchange/list', params);
}

/** 待我审核的换货单（下级提交的实体换货；status 不传=待我处理，-2=全部） */
export function getExchangeAuditList(params) {
  return request.get('stock/exchange/auditList', params);
}

/** 可换入商品清单（含目标拿货价与差价） */
export function getExchangeOptions(productId, skuKey) {
  return request.get('stock/exchange/options', { productId, skuKey: skuKey || '' });
}

/** 换货可申请余量（原单购买数/已换数/还可申请数；一单一换时 blocked=true） */
export function getExchangeQuota(params) {
  return request.get('stock/exchange/quota', params || {});
}

/** 支付换货差价 */
export function payExchangeDiff(data) {
  return request.post('stock/exchange/payDiff', data);
}

/** 填写旧品退回快递 */
export function fillExchangeBackExpress(id, data) {
  return request.post('stock/exchange/backExpress?id=' + id, data);
}

/** 上级审核换货单 */
export function auditStockExchange(id, data) {
  return request.post('stock/exchange/audit?id=' + id, data);
}

/** 上级确认换货旧品入库（查收下级寄回的旧品，状态 2 -> 3） */
export function confirmExchangeBack(id) {
  return request.post('stock/exchange/confirmBack?id=' + id);
}

/** 上级发出换货新品（填新快递单号，状态 3 -> 5 待收货） */
export function sendExchangeNew(id, data) {
  return request.post('stock/exchange/sendNew?id=' + id, data);
}

/** 换货人确认收货（状态 5 -> 4 已完成，差价奖励此时结算） */
export function confirmExchangeReceive(id) {
  return request.post('stock/exchange/confirmReceive?id=' + id);
}

/** 我的业绩 */
export function getMyStockPerformance(params) {
  return request.get('stock/performance', params);
}

/** 我的业绩订单明细（source：1=个人业绩 2=团队业绩，不传=全部） */
export function getMyStockPerformanceOrders(params) {
  return request.get('stock/performance/orders', params);
}

/** 我的奖金中心 */
export function getMyStockBonus() {
  return request.get('stock/bonus');
}

/** 我的奖金明细 */
export function getMyStockRewards(params) {
  return request.get('stock/reward/list', params);
}

/** 我的消息 */
export function getStockNotices(params) {
  return request.get('stock/notice/list', params);
}

/** 未读消息数 */
export function getStockNoticeUnread() {
  return request.get('stock/notice/unreadCount');
}

/** 标记消息已读 */
export function readStockNotice(id) {
  return request.post('stock/notice/read?id=' + id);
}

/** 一键全部已读 */
export function readAllStockNotices() {
  return request.post('stock/notice/readAll');
}
