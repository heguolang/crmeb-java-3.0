// +----------------------------------------------------------------------
// | 区域代理 API
// +----------------------------------------------------------------------

import request from '@/utils/request';

/**
 * 代理列表
 */
export function agentListApi(params) {
  return request({
    url: '/admin/agent/list',
    method: 'get',
    params,
  });
}

/**
 * 添加代理
 */
export function agentSaveApi(data) {
  return request({
    url: '/admin/agent/save',
    method: 'post',
    data,
  });
}

/**
 * 修改代理
 */
export function agentUpdateApi(data) {
  return request({
    url: '/admin/agent/update',
    method: 'post',
    data,
  });
}

/**
 * 审核代理
 */
export function agentAuditApi(id, status) {
  return request({
    url: '/admin/agent/audit',
    method: 'post',
    params: { id, status },
  });
}

/**
 * 删除代理
 */
export function agentDeleteApi(id) {
  return request({
    url: '/admin/agent/delete',
    method: 'post',
    params: { id },
  });
}

/**
 * 代理奖励明细
 */
export function agentRewardListApi(params) {
  return request({
    url: '/admin/agent/reward/list',
    method: 'get',
    params,
  });
}

/**
 * 获取代理设置
 */
export function agentSettingApi() {
  return request({
    url: '/admin/agent/setting',
    method: 'get',
  });
}

/**
 * 保存代理设置
 */
export function agentSettingSaveApi(data) {
  return request({
    url: '/admin/agent/setting/save',
    method: 'post',
    data,
  });
}

/**
 * 省市区树
 */
export function cityTreeApi() {
  return request({
    url: '/admin/system/city/list/tree',
    method: 'get',
  });
}
