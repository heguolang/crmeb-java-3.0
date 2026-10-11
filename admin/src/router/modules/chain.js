// +----------------------------------------------------------------------
// | 2+1 链动（挂在大市场下，独立于分销商）
// | 侧栏只保留：看板 / 产品 / 配置 / 用户；其余入口 hidden，功能并入 Tab
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
      path: 'product',
      component: () => import('@/views/distribution/chainProduct/index'),
      name: 'ChainProduct',
      meta: { title: '链动产品', icon: '' },
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
    // ---- 以下已并入看板/用户 Tab，侧栏隐藏；直连 URL 仍可用 ----
    {
      path: 'commission',
      component: () => import('@/views/distribution/chainCommission/index'),
      name: 'ChainCommission',
      hidden: true,
      meta: { title: '链动佣金', icon: '' },
    },
    {
      path: 'slide',
      component: () => import('@/views/distribution/chainSlide/index'),
      name: 'ChainSlide',
      hidden: true,
      meta: { title: '滑落日志', icon: '' },
    },
    {
      path: 'audit',
      component: () => import('@/views/distribution/chainAudit/index'),
      name: 'ChainAudit',
      hidden: true,
      meta: { title: '关系审计', icon: '' },
    },
    {
      path: 'risk',
      component: () => import('@/views/distribution/chainRisk/index'),
      name: 'ChainRisk',
      hidden: true,
      meta: { title: '风控日志', icon: '' },
    },
    {
      path: 'dividend',
      component: () => import('@/views/distribution/chainDividend/index'),
      name: 'ChainDividend',
      hidden: true,
      meta: { title: '团队分红', icon: '' },
    },
  ],
};

export default chainRouter;
