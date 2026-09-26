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

const userRouter = {
  path: '/user',
  component: Layout,
  redirect: '/user/index',
  name: 'User',
  meta: {
    title: '用户',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'index',
      component: () => import('@/views/user/list/index'),
      name: 'UserIndex',
      meta: { title: '用户管理', icon: '' },
    },
    {
      path: 'grade',
      component: () => import('@/views/user/grade/index'),
      name: 'Grade',
      meta: { title: '用户等级', icon: '' },
    },
    {
      path: 'label',
      component: () => import('@/views/user/group/index'),
      name: 'Label',
      meta: { title: '用户标签', icon: '' },
    },
    {
      path: 'group',
      component: () => import('@/views/user/group/index'),
      name: 'Group',
      meta: { title: '用户分组', icon: '' },
    },
  ],
};

export default userRouter;
