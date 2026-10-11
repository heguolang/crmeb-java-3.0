// +----------------------------------------------------------------------
// | 2+1 链动（挂在大市场下，独立于分销商）
// +----------------------------------------------------------------------

import Layout from '@/layout';

const chainRouter = {
  path: '/chain',
  component: Layout,
  redirect: '/chain/dashboard',
  name: 'Chain',
  meta: {
    title: '2+1链动',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'dashboard',
      component: () => import('@/views/distribution/chainDashboard/index'),
      name: 'ChainDashboard',
      meta: { title: '链动看板', icon: '' },
    },
    {
      path: 'config',
      component: () => import('@/views/distribution/chainConfig/index'),
      name: 'ChainConfig',
      meta: { title: '链动配置', icon: '' },
    },
    {
      path: 'users',
      component: () => import('@/views/distribution/chainUsers/index'),
      name: 'ChainUsers',
      meta: { title: '链动用户', icon: '' },
    },
    {
      path: 'commission',
      component: () => import('@/views/distribution/chainCommission/index'),
      name: 'ChainCommission',
      meta: { title: '链动佣金', icon: '' },
    },
    {
      path: 'slide',
      component: () => import('@/views/distribution/chainSlide/index'),
      name: 'ChainSlide',
      meta: { title: '滑落日志', icon: '' },
    },
    {
      path: 'audit',
      component: () => import('@/views/distribution/chainAudit/index'),
      name: 'ChainAudit',
      meta: { title: '关系审计', icon: '' },
    },
    {
      path: 'risk',
      component: () => import('@/views/distribution/chainRisk/index'),
      name: 'ChainRisk',
      meta: { title: '风控日志', icon: '' },
    },
    {
      path: 'dividend',
      component: () => import('@/views/distribution/chainDividend/index'),
      name: 'ChainDividend',
      meta: { title: '团队分红', icon: '' },
    },
  ],
};

export default chainRouter;
