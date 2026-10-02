import axios from 'axios';
import request from '@/utils/request';
import store from '@/store';
import SettingMer from '@/utils/settingMer';

// ==================== 企业服务（大市场 → 企业管理） ====================

// ---------- 服务需求 ----------

/** 服务需求分页列表 */
export function svcDemandListApi(params) {
  return request({ url: '/admin/svc/demand/list', method: 'get', params });
}
/** 服务需求详情（含跟进记录与附件） */
export function svcDemandInfoApi(id) {
  return request({ url: '/admin/svc/demand/info', method: 'get', params: { id } });
}
/** 保存服务需求（新增/修改） */
export function svcDemandSaveApi(data) {
  return request({ url: '/admin/svc/demand/save', method: 'post', data });
}
/** 删除服务需求 */
export function svcDemandDeleteApi(id) {
  return request({ url: '/admin/svc/demand/delete', method: 'post', params: { id } });
}
/** 需求报价 / 指派跟进人 */
export function svcDemandQuoteApi(data) {
  return request({ url: '/admin/svc/demand/quote', method: 'post', data });
}
/** 追加一条跟进记录 */
export function svcDemandFollowApi(data) {
  return request({ url: '/admin/svc/demand/follow', method: 'post', data });
}
/** 关闭需求 */
export function svcDemandCloseApi(data) {
  return request({ url: '/admin/svc/demand/close', method: 'post', data });
}
/** 需求转合同（body 为合同登记信息） */
export function svcDemandToContractApi(demandId, data) {
  return request({ url: '/admin/svc/demand/to-contract', method: 'post', params: { demandId }, data });
}
/** 服务需求统计 */
export function svcDemandStatsApi() {
  return request({ url: '/admin/svc/demand/stats', method: 'get' });
}

// ---------- 签约渠道平台 ----------

/** 签约渠道平台分页列表 */
export function svcPlatformListApi(params) {
  return request({ url: '/admin/svc/platform/list', method: 'get', params });
}
/** 启用的签约渠道平台（登记合同下拉用） */
export function svcPlatformEnabledApi() {
  return request({ url: '/admin/svc/platform/enabled', method: 'get' });
}
/** 保存签约渠道平台 */
export function svcPlatformSaveApi(data) {
  return request({ url: '/admin/svc/platform/save', method: 'post', data });
}
/** 删除签约渠道平台 */
export function svcPlatformDeleteApi(id) {
  return request({ url: '/admin/svc/platform/delete', method: 'post', params: { id } });
}

// ---------- 发票管理 ----------

/** 发票申请分页列表 */
export function svcInvoiceListApi(params) {
  return request({ url: '/admin/svc/invoice/list', method: 'get', params });
}
/** 发票申请详情 */
export function svcInvoiceInfoApi(id) {
  return request({ url: '/admin/svc/invoice/info', method: 'get', params: { id } });
}
/** 保存 / 开票 / 变更发票状态 */
export function svcInvoiceSaveApi(data) {
  return request({ url: '/admin/svc/invoice/save', method: 'post', data });
}
/** 删除发票申请 */
export function svcInvoiceDeleteApi(id) {
  return request({ url: '/admin/svc/invoice/delete', method: 'post', params: { id } });
}
/** 发票统计 */
export function svcInvoiceStatsApi() {
  return request({ url: '/admin/svc/invoice/stats', method: 'get' });
}

// ---------- 合同 ----------

/** 变更签约状态 */
export function svcContractStatusSaveApi(data) {
  return request({ url: '/admin/svc/contract/status/save', method: 'post', data });
}
/** 合同统计 */
export function svcContractStatsApi() {
  return request({ url: '/admin/svc/contract/stats', method: 'get' });
}

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
/** 准备合同正文：doc/docx/xls/图片 自动转 PDF，返回页数与用印元信息 */
export function svcContractPrepareApi(id) {
  return request({ url: '/admin/svc/contract/prepare', method: 'post', params: { id } });
}
/** 保存用印位置（百分比坐标布局 JSON） */
export function svcContractLayoutSaveApi(data) {
  return request({ url: '/admin/svc/contract/layout/save', method: 'post', data });
}
/** 读取乙方（黔序）签约方默认值（新建合同时自动带出） */
export function svcContractOurDefaultApi() {
  return request({ url: '/admin/svc/contract/our-default/get', method: 'get' });
}
/** 保存乙方（黔序）签约方默认值：会员账号 + 名称/信用代码/法人/电话/地址 */
export function svcContractOurDefaultSaveApi(data) {
  return request({ url: '/admin/svc/contract/our-default/save', method: 'post', data });
}

/**
 * 取合同正文某页的 PNG（用印位置设置底图），返回可直接给 <img> 用的 ObjectURL。
 * 走原生 axios：公共 request 的响应拦截器会把二进制当 JSON 解析。
 * 需要在不再使用时调用 releaseObjectUrl 释放。
 */
export function svcContractPageObjectUrl(id, page, maxWidth) {
  const token = !store.getters.token ? sessionStorage.getItem('token') : store.getters.token;
  const headers = { 'X-Source': 'df07addc462f7f8f' };
  if (token) headers['Authori-zation'] = token;
  return axios({
    url: SettingMer.apiBaseURL + 'admin/svc/contract/page',
    method: 'get',
    params: { id, page, maxWidth: maxWidth || 1240, temp: Date.parse(new Date()) / 1000 },
    headers,
    responseType: 'blob',
    timeout: 90000,
  }).then((res) => {
    const type = (res.data && res.data.type) || '';
    if (type.indexOf('json') >= 0) {
      // 出错时后端返回的是 JSON 错误体，读出提示
      return new Promise((resolve, reject) => {
        const fr = new FileReader();
        fr.onload = () => {
          let msg = '合同页面加载失败';
          try {
            msg = JSON.parse(fr.result).message || msg;
          } catch (e) {}
          reject(new Error(msg));
        };
        fr.readAsText(res.data);
      });
    }
    return window.URL.createObjectURL(res.data);
  });
}
/** 释放 svcContractPageObjectUrl 生成的 ObjectURL */
export function releaseObjectUrl(url) {
  if (url && String(url).indexOf('blob:') === 0) {
    try {
      window.URL.revokeObjectURL(url);
    } catch (e) {}
  }
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
