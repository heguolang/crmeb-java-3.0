// +----------------------------------------------------------------------
// | 门店系统 API（会员端）
// +----------------------------------------------------------------------

import request from "@/utils/request.js";

/** 我的门店中心（负责人身份 + 数据概览） */
export function getMyStoreInfo() {
  return request.get('merchantStore/my');
}

/** 附近/可服务门店列表 */
export function getNearbyStores(params) {
  return request.get('merchantStore/nearby', params);
}

/** 核销码预览待核销订单 */
export function previewVerifyOrder(vCode) {
  return request.get('merchantStore/verify/preview', { vCode });
}

/** 核销订单 */
export function confirmVerifyOrder(vCode) {
  return request.post('merchantStore/verify/confirm', { vCode });
}

/** 我的门店核销记录 */
export function getMyVerifyRecords(params) {
  return request.get('merchantStore/verify/records', params);
}
