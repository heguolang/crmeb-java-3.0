// +----------------------------------------------------------------------
// | 拼购系统 API
// +----------------------------------------------------------------------
import request from '@/utils/request';

/** 拼购活动分页列表 */
export function pingouActivityListApi(params) {
  return request({ url: '/admin/pingou/activity/list', method: 'get', params });
}

/** 新增拼购活动 */
export function pingouActivitySaveApi(data) {
  return request({ url: '/admin/pingou/activity/save', method: 'post', data });
}

/** 修改拼购活动 */
export function pingouActivityUpdateApi(data) {
  return request({ url: '/admin/pingou/activity/update', method: 'post', data });
}

/** 启用/禁用拼购活动 */
export function pingouActivityStatusApi(id, status) {
  return request({ url: '/admin/pingou/activity/update/status', method: 'post', params: { id, status } });
}

/** 删除拼购活动 */
export function pingouActivityDeleteApi(id) {
  return request({ url: '/admin/pingou/activity/delete', method: 'get', params: { id } });
}

/** 拼购团记录分页列表 */
export function pingouTeamListApi(params) {
  return request({ url: '/admin/pingou/team/list', method: 'get', params });
}

/** 拼购团详情（成员与中奖情况） */
export function pingouTeamDetailApi(id) {
  return request({ url: '/admin/pingou/team/detail/' + id, method: 'get' });
}

/** 商品分页列表（活动选择拼购商品用） */
export function pingouProductListApi(params) {
  return request({ url: '/admin/store/product/list', method: 'get', params });
}
