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

const contentRouter = {
  path: '/content',
  component: Layout,
  redirect: '/content/classifManager',
  name: 'content',
  meta: {
    title: '内容',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'articleManager',
      name: 'articleManager',
      component: () => import('@/views/content/article/list'),
      meta: {
        title: '文章管理',
        icon: 'clipboard',
      },
    },
    {
      path: 'articleCreat/:id?',
      name: 'articleCreat',
      component: () => import('@/views/content/article/edit'),
      meta: {
        title: '添加文章',
        noCache: true,
        activeMenu: `/content/articleManager`,
      },
    },
    {
      path: 'classifManager',
      name: 'classifManager',
      component: () => import('@/views/content/articleclass/list'),
      meta: {
        title: '文章分类',
        icon: 'clipboard',
      },
    },
  ],
};

export default contentRouter;
