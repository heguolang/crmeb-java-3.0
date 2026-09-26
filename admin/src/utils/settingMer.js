// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

// 请求接口地址 如果没有配置自动获取当前网址路径
const VUE_APP_API_URL = process.env.VUE_APP_BASE_API || `${location.origin}`;
const VUE_APP_WS_URL =
  process.env.VUE_APP_WS_URL || (location.protocol === 'https' ? 'wss' : 'ws') + ':' + location.hostname;
const SettingMer = {
  // 服务器地址
  httpUrl: VUE_APP_API_URL,
  // 接口请求地址
  apiBaseURL: VUE_APP_API_URL + '/api/',
  // socket连接
  wsSocketUrl: VUE_APP_WS_URL,
};

export default SettingMer;
