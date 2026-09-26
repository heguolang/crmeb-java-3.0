// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import router from './router';
import store from './store';
import { Message } from 'element-ui';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css'; // progress bar style
import { getToken } from '@/utils/auth'; // get token from cookie
import getPageTitle from '@/utils/get-page-title';

NProgress.configure({ showSpinner: false }); // NProgress Configuration

const whiteList = ['/login', '/auth-redirect']; // no redirect whitelist

// 系统运维专属模块：仅 qxtec 账号可见 / 可访问
//   后端 /admin/getMenus 已按账号过滤这些顶级菜单（不下发即左侧菜单不显示）
//   这里只做「直接输 URL 深链访问」的兜底，按维护实际拥有的页面路由列举
//   注意：素材管理 /maintain/picture 虽在同一路由树下，但菜单已归属「设置」，不在此列
const OPS_ONLY_ACCOUNT = 'qxtec';
const OPS_ONLY_PATHS = [
  '/hidden', // 系统 → 运维面板
  '/maintain/devconfiguration', // 维护 → 开发配置（配置分类 / 组合数据 / 表单配置）
  '/maintain/logistics', // 维护 → 物流设置（城市数据 / 物流公司）
  '/maintain/schedule', // 维护 → 定时任务管理
  '/operation/roleManager/promiseRules', // 维护 → 权限规则
];
const isOpsOnlyPath = (path) => OPS_ONLY_PATHS.some((root) => path === root || path.startsWith(`${root}/`));

router.beforeEach(async (to, from, next) => {
  // start progress bar
  NProgress.start();

  // set page title
  document.title = getPageTitle(to.meta.title);

  // determine whether the user has logged in
  const hasToken = getToken();

  if (hasToken) {
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' });
      NProgress.done();
    } else {
      const hasRoles = store.getters.roles && store.getters.roles.length > 0;
      if (hasRoles) {
        // 运维专属模块（系统 / 维护）非 qxtec 一律按不存在处理
        if (isOpsOnlyPath(to.path) && store.getters.name !== OPS_ONLY_ACCOUNT) {
          next({ path: '/404' });
          NProgress.done();
        } else {
          next();
        }
      } else {
        try {
          const roles = await store.dispatch('user/getInfo');
          const accessRoutes = await store.dispatch('permission/generateRoutes', roles);
          router.addRoutes(accessRoutes);
          next({ ...to, replace: true });
        } catch (error) {
          // remove token and go to login page to re-login
          await store.dispatch('user/resetToken');
          Message.error(error || 'Has Error');
          next(`/login?redirect=${to.path}`);
          NProgress.done();
        }
      }
    }
  } else {
    /* has no token*/
    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next();
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.path}`);
      NProgress.done();
    }
  }
});

router.afterEach(() => {
  // finish progress bar
  NProgress.done();
});
