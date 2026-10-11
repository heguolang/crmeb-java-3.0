import request from '@/utils/request';

export function chainConfigGetApi() {
  return request({ url: '/admin/chain/config', method: 'get' });
}

export function chainConfigSaveApi(data) {
  return request({ url: '/admin/chain/config', method: 'put', data });
}

export function chainRulesGetApi() {
  return request({ url: '/admin/chain/rules', method: 'get' });
}

export function chainRulesSaveApi(data) {
  return request({ url: '/admin/chain/rules', method: 'put', data });
}

export function chainUsersApi(params) {
  return request({ url: '/admin/chain/users', method: 'get', params });
}

export function chainRelationUpdateApi(data) {
  return request({ url: '/admin/chain/relation-update', method: 'post', data });
}

export function chainSlideLogsApi(params) {
  return request({ url: '/admin/chain/slide-logs', method: 'get', params });
}

export function chainCommissionApi(params) {
  return request({ url: '/admin/chain/commission', method: 'get', params });
}

export function chainDashboardApi() {
  return request({ url: '/admin/chain/dashboard', method: 'get' });
}

export function chainCommissionExportApi(params) {
  return request({ url: '/admin/chain/commission/export', method: 'get', params });
}

export function chainAuditLogsApi(params) {
  return request({ url: '/admin/chain/audit-logs', method: 'get', params });
}

export function chainUserDetailApi(params) {
  return request({ url: '/admin/chain/user-detail', method: 'get', params });
}

export function chainRiskLogsApi(params) {
  return request({ url: '/admin/chain/risk-logs', method: 'get', params });
}

export function chainDividendApi(params) {
  return request({ url: '/admin/chain/dividend', method: 'get', params });
}

export function chainDividendRunApi(params) {
  return request({ url: '/admin/chain/dividend/run', method: 'post', params });
}
