// +----------------------------------------------------------------------
// | 订货系统路由
// +----------------------------------------------------------------------
import Layout from '@/layout';

export default {
  path: '/stock',
  component: Layout,
  redirect: '/stock/agent',
  name: 'Stock',
  meta: {
    title: '订货',
    icon: 'clipboard'
  },
  children: [
    {
      path: 'level',
      component: () => import('@/views/stock/level/index'),
      name: 'StockLevel',
      meta: { title: '订货商级别设置', icon: '' }
    },
    {
      path: 'agent',
      component: () => import('@/views/stock/agent/index'),
      name: 'StockAgent',
      meta: { title: '订货商管理', icon: '' }
    },
    {
      path: 'changelog',
      component: () => import('@/views/stock/changelog/index'),
      name: 'StockChangeLog',
      meta: { title: '订货商变更记录', icon: '' }
    },
    {
      path: 'product',
      component: () => import('@/views/stock/product/index'),
      name: 'StockProduct',
      meta: { title: '商品与库存', icon: '' }
    },
    {
      path: 'order',
      component: () => import('@/views/stock/order/index'),
      name: 'StockOrder',
      meta: { title: '订货订单', icon: '' }
    },
    {
      path: 'exchange',
      component: () => import('@/views/stock/exchange/index'),
      name: 'StockExchange',
      meta: { title: '换货管理', icon: '' }
    },
    {
      path: 'reward',
      component: () => import('@/views/stock/reward/index'),
      name: 'StockReward',
      meta: { title: '奖金明细', icon: '' }
    },
    {
      path: 'setting',
      component: () => import('@/views/stock/setting/index'),
      name: 'StockSetting',
      meta: { title: '订货商设置', icon: '' }
    },
    {
      path: 'report',
      component: () => import('@/views/stock/report/index'),
      name: 'StockReport',
      meta: { title: '数据报表', icon: '' }
    }
  ]
};
