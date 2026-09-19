// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

import axios from 'axios';
import { MessageBox, Message } from 'element-ui';
import store from '@/store';
import { getToken } from '@/utils/auth';
import SettingMer from '@/utils/settingMer';
import { isPhone } from '@/libs/wechat';
const service = axios.create({
  baseURL: SettingMer.apiBaseURL,
  timeout: 60000, // 过期时间
});

/**
 * 判断是否为「一号通未配置」类错误，这类错误无需打扰用户，仅控制台记录。
 * 场景：本地未登录/未配置一号通（accessKey、secretKey），商家寄件、电子面单等
 * 依赖云平台的功能会返回此类错误。属于可选功能未开通，不影响系统正常使用。
 */
function isOnePassDisabledMessage(msg) {
  if (!msg) return false;
  const text = String(msg);
  return (
    text.indexOf('accessKey') > -1 ||
    text.indexOf('secretKey') > -1 ||
    text.indexOf('一号通') > -1 ||
    text.indexOf('平台接口') > -1
  );
}

// request interceptor
service.interceptors.request.use(
  (config) => {
    // 发送请求之前做的
    const token = !store.getters.token ? sessionStorage.getItem('token') : store.getters.token;
    config.headers['X-Source'] = 'df07addc462f7f8f';
    if (token) {
      config.headers['Authori-zation'] = token;
    }
    if (/get/i.test(config.method)) {
      config.params = config.params || {};
      config.params.temp = Date.parse(new Date()) / 1000;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// response interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    // if the custom code is not 20000, it is judged as an error.
    if (res.code === 401) {
      // to re-login
      Message.error('无效的会话，或者登录已过期，请重新登录。');
      if (window.location.pathname !== '/login') location.href = '/login';
    } else if (res.code === 403) {
      Message.error('没有权限访问。');
    }
    if (![0, 200].includes(res.code) && res.code !== 401) {
      if (isPhone()) {
        //移动端
        return Promise.reject(res || 'Error');
      }
      // 一号通未配置类错误静默处理：不打搅用户，仅在控制台留痕
      if (isOnePassDisabledMessage(res.message)) {
        console.warn('[一号通未配置] ' + (res.message || '') + '（该功能需登录一号通后使用，忽略即可）');
        return Promise.reject(res || 'Error');
      }
      Message({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000,
      });
      return Promise.reject();
    } else {
      return res.data;
    }
  },
  (error) => {
    // 模块开关关闭时接口返回 404（接口已隐藏），静默处理避免已打开页签反复弹错
    if (error.response && error.response.status === 404) {
      return Promise.reject(error);
    }
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000,
    });
    return Promise.reject(error);
  },
);

export default service;
