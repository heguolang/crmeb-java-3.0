import request from '@/utils/request';

// ==================== 企业服务（大市场 → 企业管理） ====================

/** 合同分页列表 */
export function svcContractListApi(params) {
  return request({ url: '/admin/svc/contract/list', method: 'get', params });
}
/** 合同详情 */
export function svcContractInfoApi(id) {
  return request({ url: '/admin/svc/contract/info', method: 'get', params: { id } });
}
/** 保存合同（新增/修改） */
export function svcContractSaveApi(data) {
  return request({ url: '/admin/svc/contract/save', method: 'post', data });
}
/** 删除合同 */
export function svcContractDeleteApi(id) {
  return request({ url: '/admin/svc/contract/delete', method: 'post', params: { id } });
}

/** 项目分页列表 */
export function svcProjectListApi(params) {
  return request({ url: '/admin/svc/project/list', method: 'get', params });
}
/** 项目详情（含进度记录） */
export function svcProjectInfoApi(id) {
  return request({ url: '/admin/svc/project/info', method: 'get', params: { id } });
}
/** 保存项目（新增/修改） */
export function svcProjectSaveApi(data) {
  return request({ url: '/admin/svc/project/save', method: 'post', data });
}
/** 设置项目进度 */
export function svcProgressSaveApi(data) {
  return request({ url: '/admin/svc/project/progress/save', method: 'post', data });
}
/** 项目进度记录列表 */
export function svcProgressListApi(projectId) {
  return request({ url: '/admin/svc/project/progress/list', method: 'get', params: { projectId } });
}
/** 删除项目 */
export function svcProjectDeleteApi(id) {
  return request({ url: '/admin/svc/project/delete', method: 'post', params: { id } });
}

/** 客户案例分页列表 */
export function svcCaseListApi(params) {
  return request({ url: '/admin/svc/case/list', method: 'get', params });
}
/** 保存客户案例 */
export function svcCaseSaveApi(data) {
  return request({ url: '/admin/svc/case/save', method: 'post', data });
}
/** 删除客户案例 */
export function svcCaseDeleteApi(id) {
  return request({ url: '/admin/svc/case/delete', method: 'post', params: { id } });
}

/** 服务设置读取 */
export function svcSettingGetApi() {
  return request({ url: '/admin/svc/setting/get', method: 'get' });
}
/** 服务设置保存 */
export function svcSettingSaveApi(data) {
  return request({ url: '/admin/svc/setting/save', method: 'post', data });
}
