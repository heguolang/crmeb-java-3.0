/** 全球分红设置 + 各等级分红比例 */
export function dividendInfoApi() {
  return request({ url: '/admin/dividend/info', method: 'get' });
}

/** 保存分红设置（status/cycle/base） */
export function dividendConfigApi(data) {
  return request({ url: '/admin/dividend/config', method: 'post', data });
}

/** 保存等级分红比例 {id, dividendRate} */
export function dividendLevelApi(data) {
  return request({ url: '/admin/dividend/level', method: 'post', data });
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
