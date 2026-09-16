// +----------------------------------------------------------------------
// | 订货系统 API
// +----------------------------------------------------------------------
import request from '@/utils/request';

/** 层级列表 */
export function stockLevelListApi() {
  return request({ url: '/admin/stock/level/list', method: 'get' });
}

/** 保存层级 */
export function stockLevelSaveApi(data) {
  return request({ url: '/admin/stock/level/save', method: 'post', data });
}

/** 删除层级 */
export function stockLevelDeleteApi(id) {
  return request({ url: '/admin/stock/level/delete', method: 'post', params: { id } });
}

/** 订货代理列表 */
export function stockAgentListApi(params) {
  return request({ url: '/admin/stock/agent/list', method: 'get', params });
}

/** 新增代理 */
export function stockAgentSaveApi(data) {
  return request({ url: '/admin/stock/agent/save', method: 'post', data });
}

/** 修改代理 */
export function stockAgentUpdateApi(data) {
  return request({ url: '/admin/stock/agent/update', method: 'post', data });
}

/** 启用/禁用代理 */
export function stockAgentStatusApi(id, status) {
  return request({ url: '/admin/stock/agent/status', method: 'post', params: { id, status } });
}

/** 删除代理 */
export function stockAgentDeleteApi(id) {
  return request({ url: '/admin/stock/agent/delete', method: 'post', params: { id } });
}

/** 商品列表（含库存与拿货价） */
export function stockProductListApi(params) {
  return request({ url: '/admin/stock/product/list', method: 'get', params });
}

/** 保存拿货价 */
export function stockPriceSaveApi(data) {
  return request({ url: '/admin/stock/price/save', method: 'post', data });
}

/** 调整库存 */
export function stockAdjustApi(data) {
  return request({ url: '/admin/stock/stock/adjust', method: 'post', data });
}

/** 库存日志 */
export function stockLogListApi(params) {
  return request({ url: '/admin/stock/log/list', method: 'get', params });
}

/** 订货订单列表 */
export function stockOrderListApi(params) {
  return request({ url: '/admin/stock/order/list', method: 'get', params });
}

/** 确认收款 */
export function stockOrderPayApi(id) {
  return request({ url: '/admin/stock/order/pay', method: 'post', params: { id } });
}

/** 订单发货 */
export function stockOrderSendApi(id, data) {
  return request({ url: '/admin/stock/order/send', method: 'post', params: { id }, data });
}

/** 标记完成 */
export function stockOrderFinishApi(id) {
  return request({ url: '/admin/stock/order/finish', method: 'post', params: { id } });
}

/** 换货单列表 */
export function stockExchangeListApi(params) {
  return request({ url: '/admin/stock/exchange/list', method: 'get', params });
}

/** 总部审核换货单 */
export function stockExchangeAuditApi(id, data) {
  return request({ url: '/admin/stock/exchange/audit', method: 'post', params: { id }, data });
}

/** 确认旧品入库 */
export function stockExchangeBackApi(id) {
  return request({ url: '/admin/stock/exchange/back', method: 'post', params: { id } });
}

/** 发新品 */
export function stockExchangeSendApi(id, data) {
  return request({ url: '/admin/stock/exchange/send', method: 'post', params: { id }, data });
}

/** 奖金明细 */
export function stockRewardListApi(params) {
  return request({ url: '/admin/stock/reward/list', method: 'get', params });
}

/** 提现列表 */
export function stockWithdrawListApi(params) {
  return request({ url: '/admin/stock/withdraw/list', method: 'get', params });
}

/** 提现审核 */
export function stockWithdrawAuditApi(id, data) {
  return request({ url: '/admin/stock/withdraw/audit', method: 'post', params: { id }, data });
}

/** 奖励规则读取 */
export function stockSettingApi() {
  return request({ url: '/admin/stock/setting/get', method: 'get' });
}

/** 奖励规则保存 */
export function stockSettingSaveApi(data) {
  return request({ url: '/admin/stock/setting/save', method: 'post', data });
}

/** 数据报表 */
export function stockReportApi(params) {
  return request({ url: '/admin/stock/report', method: 'get', params });
}

/** 级差月结 */
export function stockMonthlySettleApi(data) {
  return request({ url: '/admin/stock/reward/monthlySettle', method: 'post', data });
}
