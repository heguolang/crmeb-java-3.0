import request from '@/utils/request';

/** 全球分红设置 + 各等级分红比例 */
export function dividendInfoApi() {
  return request({ url: '/admin/dividend/info', method: 'get' });
}

/** 保存分红设置（status/cycle/base） */
export function dividendConfigApi(data) {
  return request({ url: '/admin/dividend/config', method: 'post', data });
}

/** 保存分红级别 {name, distributorLevelId, dividendRate, sort} */
export function dividendLevelSaveApi(data) {
  return request({ url: '/admin/dividend/level/save', method: 'post', data });
}

/** 修改分红级别 {id, ...} */
export function dividendLevelUpdateApi(data) {
  return request({ url: '/admin/dividend/level/update', method: 'post', data });
}

/** 删除分红级别（软删） */
export function dividendLevelDeleteApi(id) {
  return request({ url: '/admin/dividend/level/delete', method: 'get', params: { id } });
}

/** 结算记录分页 */
export function dividendRecordListApi(params) {
  return request({ url: '/admin/dividend/record/list', method: 'get', params });
}

/** 发放明细分页 */
export function dividendDetailListApi(params) {
  return request({ url: '/admin/dividend/detail/list', method: 'get', params });
}

/** 手动结算上一周期 */
export function dividendRunApi(cycleType) {
  return request({ url: '/admin/dividend/run', method: 'post', params: { cycleType } });
}
