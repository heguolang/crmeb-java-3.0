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

const mobileRouter = {
  path: '/javaMobile',
  component: Layout,
  redirect: '/javaMobile/index',
  name: 'Mobile',
  alwaysShow: true,
  meta: {
    title: '移动端',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'orderCancellation',
      component: () => import('@/views/mobile/orderCancellation/index.vue'),
      name: 'OrderCancellation',
      meta: { title: '订单核销', icon: '' },
    },
    {
      path: 'orderStatistics',
      component: () => import('@/views/mobile/orderStatistics/index.vue'),
      name: 'OrderStatistics',
      meta: { title: '订单统计' },
    },
    {
      path: 'orderList/:types?',
      component: () => import('@/views/mobile/orderStatistics/orderList.vue'),
      name: 'OrderList',
      meta: { title: '订单列表' },
    },
    {
      path: 'orderDelivery/:oid/:id?',
      component: () => import('@/views/mobile/orderStatistics/orderDelivery.vue'),
      name: 'OrderDelivery',
      meta: { title: '订单发货' },
    },
    {
      path: 'orderDetail/:id?/:goname?',
      component: () => import('@/views/mobile/orderStatistics/orderDetail.vue'),
      name: 'OrderDetail',
      meta: { title: '订单详情' },
    },
    {
      path: 'orderStatisticsDetail/:type/:time?',
      component: () => import('@/views/mobile/orderStatistics/Statistics.vue'),
      name: 'OrderStatisticsDetail',
      meta: { title: '订单数据统计' },
    },
  ],
};

export default mobileRouter;
