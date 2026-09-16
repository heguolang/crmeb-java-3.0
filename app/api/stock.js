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

/** 新增下级代理 */
export function createSubAgent(data) {
  return request.post('stock/agent/createSub', data);
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

/** 我的订货单列表 */
export function getMyStockOrders(params) {
  return request.get('stock/order/list', params);
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

/** 填写旧品退回快递 */
export function fillExchangeBackExpress(id, data) {
  return request.post('stock/exchange/backExpress?id=' + id, data);
}

/** 上级审核换货单 */
export function auditStockExchange(id, data) {
  return request.post('stock/exchange/audit?id=' + id, data);
}

/** 我的业绩 */
export function getMyStockPerformance(params) {
  return request.get('stock/performance', params);
}

/** 我的奖金中心 */
export function getMyStockBonus() {
  return request.get('stock/bonus');
}

/** 我的奖金明细 */
export function getMyStockRewards(params) {
  return request.get('stock/reward/list', params);
}

/** 申请提现 */
export function applyStockWithdraw(data) {
  return request.post('stock/withdraw/apply', data);
}

/** 我的提现记录 */
export function getMyStockWithdraws(params) {
  return request.get('stock/withdraw/list', params);
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
