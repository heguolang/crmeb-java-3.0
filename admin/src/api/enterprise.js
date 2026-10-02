import axios from 'axios';
import request from '@/utils/request';
import store from '@/store';
import SettingMer from '@/utils/settingMer';

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
/** 准备合同正文：doc/docx/xls/图片 自动转 PDF，返回页数与用印元信息 */
export function svcContractPrepareApi(id) {
  return request({ url: '/admin/svc/contract/prepare', method: 'post', params: { id } });
}
/** 保存用印位置（百分比坐标布局 JSON） */
export function svcContractLayoutSaveApi(data) {
  return request({ url: '/admin/svc/contract/layout/save', method: 'post', data });
}
/** 乙方（黔序）一键用印：按设定位置把乙方公章/法人签字/日期合成进正文 PDF */
export function svcContractOurStampApi(id) {
  return request({ url: '/admin/svc/contract/our-stamp', method: 'post', params: { id } });
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
