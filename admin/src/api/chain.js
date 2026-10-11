import request from '@/utils/request';

// ---------------- 配置 ----------------
export function chainConfigGetApi() {
  return request({ url: '/admin/chain/config', method: 'get' });
}

export function chainConfigSaveApi(data) {
  return request({ url: '/admin/chain/config', method: 'put', data });
}

// ---------------- 奖励规则 ----------------
export function chainRulesGetApi() {
  return request({ url: '/admin/chain/rules', method: 'get' });
}

export function chainRulesSaveApi(data) {
  return request({ url: '/admin/chain/rules', method: 'put', data });
}

// ---------------- 链动用户 ----------------
export function chainUsersApi(params) {
  return request({ url: '/admin/chain/users', method: 'get', params });
}

export function chainRelationUpdateApi(data) {
  return request({ url: '/admin/chain/relation-update', method: 'post', data });
}

export function chainUserDetailApi(params) {
  return request({ url: '/admin/chain/user-detail', method: 'get', params });
}

// ---------------- 关系变更（走人留人轨迹 + 人工调整审计）----------------
export function chainSlideLogsApi(params) {
  return request({ url: '/admin/chain/slide-logs', method: 'get', params });
}

export function chainAuditLogsApi(params) {
  return request({ url: '/admin/chain/audit-logs', method: 'get', params });
}

// ---------------- 佣金 ----------------
export function chainCommissionApi(params) {
  return request({ url: '/admin/chain/commission', method: 'get', params });
}

export function chainCommissionExportApi(params) {
  return request({ url: '/admin/chain/commission/export', method: 'get', params });
}

// ---------------- 看板 ----------------
export function chainDashboardApi() {
  return request({ url: '/admin/chain/dashboard', method: 'get' });
}

// ---------------- 团队分红 ----------------
export function chainDividendApi(params) {
  return request({ url: '/admin/chain/dividend', method: 'get', params });
}

export function chainDividendRunApi(params) {
  return request({ url: '/admin/chain/dividend/run', method: 'post', params });
}

// ---------------- 链动产品（含按商品配置奖励）----------------
export function chainProductListApi(params) {
  return request({ url: '/admin/chain/product/list', method: 'get', params });
}

export function chainProductSaveApi(data) {
  return request({ url: '/admin/chain/product/save', method: 'post', data });
}

export function chainProductDeleteApi(params) {
  return request({ url: '/admin/chain/product/delete', method: 'post', params });
}
