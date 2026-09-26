// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

const getters = {
  sidebar: (state) => state.app.sidebar,
  size: (state) => state.app.size,
  device: (state) => state.app.device,
  visitedViews: (state) => state.tagsView.visitedViews,
  cachedViews: (state) => state.tagsView.cachedViews,
  token: (state) => state.user.token,
  avatar: (state) => state.user.avatar,
  name: (state) => state.user.name,
  introduction: (state) => state.user.introduction,
  roles: (state) => state.user.roles,
  permission_routes: (state) => state.permission.routes,
  permissions: (state) => state.user.permissions,
  sidebarRouters: (state) => state.permission.sidebarRouters,
  errorLogs: (state) => state.errorLog.logs,
  isLogin: (state) => state.user.isLogin,
  adminProductClassify: (state) => state.product.adminProductClassify,
  frontDomain: (state) => state.settings.frontDomain,
  mediaDomain: (state) => state.settings.mediaDomain,
  mobileTheme: (state) => state.settings.mobileTheme,
};
export default getters;
