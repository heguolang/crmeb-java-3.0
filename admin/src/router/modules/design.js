// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import Layout from '@/layout';

const designRouter = {
  path: '/design',
  component: Layout,
  redirect: '/design/mall_theme',
  name: 'design',
  meta: {
    title: '装修',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'theme',
      name: 'theme',
      component: () => import('@/views/design/theme/index'),
      meta: {
        title: '一键换色',
      },
    },
    {
      path: 'mall_theme',
      name: 'mallTheme',
      component: () => import('@/views/design/mall_theme/index'),
      meta: {
        title: '商城主题',
      },
    },
    {
      path: 'my_theme',
      name: 'myTheme',
      component: () => import('@/views/design/my_theme/index'),
      meta: {
        title: '我的主题',
      },
    },
    {
      path: 'micro_theme',
      name: 'microTheme',
      component: () => import('@/views/design/micro_theme/index'),
      meta: {
        title: '专题页面',
      },
    },
    {
      path: 'edit_theme',
      name: 'editTheme',
      hidden: true,
      component: () => import('@/views/design/edit_theme/index'),
      meta: {
        title: '主题风格',
        fullScreen: true,
        activeMenu: '/design/mall_theme',
      },
    },
    {
      path: 'viewDesign',
      name: 'viewDesign',
      component: () => import('@/views/design/viewDesign/index'),
      meta: {
        title: '页面设计',
      },
    },
    {
      path: 'advertisement',
      name: 'advertisement',
      component: () => import('@/views/design/advertisement/index'),
      meta: {
        title: '开屏广告',
      },
    },
    {
      path: 'spread_poster',
      name: 'spreadPoster',
      component: () => import('@/views/design/spread_poster/index'),
      meta: {
        title: '推广海报',
      },
    },
  ],
};

export default designRouter;
