import request from '@/utils/request';

// ==================== 招聘系统（大市场 → 招聘系统） ====================

// ---------- 职位管理 ----------

/** 职位分页列表 */
export function recruitJobListApi(params) {
  return request({ url: '/admin/recruit/job/list', method: 'get', params });
}
/** 职位详情 */
export function recruitJobInfoApi(id) {
  return request({ url: '/admin/recruit/job/info', method: 'get', params: { id } });
}
/** 保存职位（新增/修改） */
export function recruitJobSaveApi(data) {
  return request({ url: '/admin/recruit/job/save', method: 'post', data });
}
/** 职位上下架 */
export function recruitJobStatusApi(id, status) {
  return request({ url: '/admin/recruit/job/status', method: 'post', params: { id, status } });
}
/** 删除职位 */
export function recruitJobDeleteApi(id) {
  return request({ url: '/admin/recruit/job/delete', method: 'post', params: { id } });
}
/** 职位统计 */
export function recruitJobStatsApi() {
  return request({ url: '/admin/recruit/job/stats', method: 'get' });
}
/** 在招职位下拉 */
export function recruitJobEnabledApi() {
  return request({ url: '/admin/recruit/job/enabled', method: 'get' });
}

// ---------- 应聘管理 ----------

/** 应聘登记分页列表 */
export function recruitApplyListApi(params) {
  return request({ url: '/admin/recruit/apply/list', method: 'get', params });
}
/** 应聘详情 */
export function recruitApplyInfoApi(id) {
  return request({ url: '/admin/recruit/apply/info', method: 'get', params: { id } });
}
/** 处理应聘（改状态 + 面试时间 + 备注） */
export function recruitApplyHandleApi(data) {
  return request({ url: '/admin/recruit/apply/handle', method: 'post', data });
}
/** 删除应聘记录 */
export function recruitApplyDeleteApi(id) {
  return request({ url: '/admin/recruit/apply/delete', method: 'post', params: { id } });
}
/** 应聘统计（各状态数量） */
export function recruitApplyStatsApi() {
  return request({ url: '/admin/recruit/apply/stats', method: 'get' });
}

// ---------- 招聘设置 ----------

/** 招聘设置读取 */
export function recruitSettingGetApi() {
  return request({ url: '/admin/recruit/setting/get', method: 'get' });
}
/** 招聘设置保存 */
export function recruitSettingSaveApi(data) {
  return request({ url: '/admin/recruit/setting/save', method: 'post', data });
}
