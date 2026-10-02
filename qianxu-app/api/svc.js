import request from '@/utils/request.js';

// ==================== 企业服务（软件开发服务） ====================

/** 服务配置（免登录）：模式开关 + 公司名 + 联系方式 */
export function getSvcConfig() {
  return request.get('svc/config', {}, { noAuth: true });
}

/** 客户案例列表（免登录） */
export function getSvcCases(limit) {
  return request.get('svc/case/list', limit ? { limit } : {}, { noAuth: true });
}

/** 我的合同分页列表 */
export function getSvcContracts(data) {
  return request.get('svc/contract/list', data);
}

/** 合同详情 */
export function getSvcContractInfo(id) {
  return request.get('svc/contract/info', { id });
}

/** 签约合同（负责人手写签名 + 短信验证） */
export function signSvcContract(data) {
  return request.post('svc/contract/sign', data);
}

/** 发送签约短信验证码 */
export function sendSvcSignCode(phone) {
  return request.post('svc/contract/sign/code', { phone });
}

/** 我的项目分页列表 */
export function getSvcProjects(data) {
  return request.get('svc/project/list', data);
}

/** 项目详情（含进度记录） */
export function getSvcProjectInfo(id) {
  return request.get('svc/project/info', { id });
}

/** 会员中心统计 */
export function getSvcStats() {
  return request.get('svc/stats');
}

/** 最新动态 */
export function getSvcDynamic(limit) {
  return request.get('svc/dynamic', { limit: limit || 5 });
}

// ==================== 服务需求 ====================
// 客户只提交与查看，不需要任何确认动作

/** 提交服务需求 */
export function submitSvcDemand(data) {
  return request.post('svc/demand/submit', data);
}

/** 我的需求分页列表 */
export function getSvcDemands(data) {
  return request.get('svc/demand/list', data);
}

/** 需求详情（含报价与跟进进度） */
export function getSvcDemandInfo(id) {
  return request.get('svc/demand/info', { id });
}

/** 撤销需求（仅「待受理」可撤销）—— id 走 query，后端 @RequestParam 取 */
export function cancelSvcDemand(id) {
  return request.post('svc/demand/cancel?id=' + id);
}

/** 同意报价 */
export function acceptSvcDemandQuote(id) {
  return request.post('svc/demand/accept?id=' + id);
}

// ==================== 发票管理 ====================

/** 提交开票申请 */
export function submitSvcInvoice(data) {
  return request.post('svc/invoice/submit', data);
}

/** 我的开票申请分页列表 */
export function getSvcInvoices(data) {
  return request.get('svc/invoice/list', data);
}

/** 开票申请详情（含状态进度与成品发票） */
export function getSvcInvoiceInfo(id) {
  return request.get('svc/invoice/info', { id });
}

/** 撤销开票申请（仅「待开票」可撤销） */
export function cancelSvcInvoice(id) {
  return request.post('svc/invoice/cancel?id=' + id);
}
