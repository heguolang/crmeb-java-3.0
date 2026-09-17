// +----------------------------------------------------------------------
// | 门店系统 API
// +----------------------------------------------------------------------
import request from '@/utils/request';

/** 门店分页列表 */
export function merchantStoreListApi(params) {
  return request({ url: '/admin/merchantStore/list', method: 'get', params });
}

/** 门店详情 */
export function merchantStoreInfoApi(id) {
  return request({ url: '/admin/merchantStore/info', method: 'get', params: { id } });
}

/** 新增门店 */
export function merchantStoreSaveApi(data) {
  return request({ url: '/admin/merchantStore/save', method: 'post', data });
}

/** 编辑门店 */
export function merchantStoreUpdateApi(data) {
  return request({ url: '/admin/merchantStore/update', method: 'post', data });
}

/** 启用/禁用门店 */
export function merchantStoreStatusApi(id, isShow) {
  return request({ url: '/admin/merchantStore/status', method: 'post', params: { id, isShow } });
}

/** 删除门店 */
export function merchantStoreDeleteApi(id) {
  return request({ url: '/admin/merchantStore/delete', method: 'post', params: { id } });
}

/** 门店核销记录分页 */
export function merchantStoreVerifyListApi(params) {
  return request({ url: '/admin/merchantStore/verify/list', method: 'get', params });
}
