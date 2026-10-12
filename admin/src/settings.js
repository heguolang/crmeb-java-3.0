// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

// 请求接口地址：有环境变量用环境变量；浏览器里用当前域名；Node 构建（vue.config 引用）回退 /api/
function resolveApiBaseURL() {
  if (process.env.VUE_APP_BASE_API) {
    return process.env.VUE_APP_BASE_API;
  }
  if (typeof location !== 'undefined' && location.origin) {
    return `${location.origin}/api/`;
  }
  return '/api/';
}
const VUE_APP_API_URL = resolveApiBaseURL();
module.exports = {
  // 接口请求地址
  apiBaseURL: VUE_APP_API_URL,

  title: '加载中...',

  /**
   * @type {boolean} true | false
   * @description Whether show the settings right-panel
   */
  showSettings: true,

  /**
   * @type {boolean} true | false
   * @description Whether need tagsView
   */
  tagsView: true,

  /**
   * @type {boolean} true | false
   * @description Whether fix the header
   */
  fixedHeader: true,

  /**
   * @type {boolean} true | false
   * @description Whether show the logo in sidebar
   */
  sidebarLogo: true,

  /**
   * @type {string | array} 'production' | ['production', 'development']
   * @description Need show err logs component.
   * The default is only used in the production env
   * If you want to also use it in dev, you can pass ['production', 'development']
   */
  errorLog: 'production',
};
