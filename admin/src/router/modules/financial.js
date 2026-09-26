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

const financialRouter = {
  path: '/financial',
  component: Layout,
  redirect: '/financial/commission/template',
  name: 'Financial',
  meta: {
    title: '财务',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'commission',
      component: () => import('@/views/financial/index'),
      name: 'Commission',
      meta: { title: '财务操作', icon: '' },
      alwaysShow: true,
      children: [
        {
          path: 'template',
          component: () => import('@/views/financial/commission/withdrawal/index'),
          name: 'commissionTemplate',
          meta: { title: '申请提现', icon: '' },
        },
        {
          path: 'setting',
          component: () => import('@/views/financial/commission/setting/index'),
          name: 'extractSetting',
          meta: { title: '提现设置', icon: '' },
        },
        {
          path: 'recharge',
          component: () => import('@/views/financial/commission/recharge/index'),
          name: 'rechargeSetting',
          meta: { title: '充值设置', icon: '' },
        },
      ],
    },
    {
      path: 'record',
      component: () => import('@/views/financial/record/index'),
      name: 'financialRecord',
      meta: { title: '财务记录', icon: '' },
      alwaysShow: true,
      children: [
        {
          path: 'charge',
          component: () => import('@/views/financial/record/charge/index'),
          name: 'Charge',
          meta: { title: '充值记录', icon: '' },
        },
        {
          path: 'monitor',
          component: () => import('@/views/financial/record/monitor/index'),
          name: 'Monitor',
          meta: { title: '资金监控', icon: '' },
        },
      ],
    },
    {
      path: 'brokerage',
      component: () => import('@/views/financial/brokerage/index'),
      name: 'Brokerage',
      meta: { title: '佣金记录', icon: '' },
    },
  ],
};

export default financialRouter;
