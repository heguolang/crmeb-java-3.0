// +----------------------------------------------------------------------
// | 区域代理路由
// +----------------------------------------------------------------------

import Layout from '@/layout';

const dailiRouter = {
  path: '/daili',
  component: Layout,
  redirect: '/daili/agentList',
  name: 'Daili',
  meta: {
    title: '代理',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'agentList',
      component: () => import('@/views/daili/agentList/index'),
      name: 'AgentList',
      meta: { title: '代理管理', icon: '' },
    },
    {
      path: 'agentReward',
      component: () => import('@/views/daili/agentReward/index'),
      name: 'AgentReward',
      meta: { title: '代理奖励明细', icon: '' },
    },
    {
      path: 'agentSetting',
      component: () => import('@/views/daili/agentSetting/index'),
      name: 'AgentSetting',
      meta: { title: '代理设置', icon: '' },
    },
  ],
};

export default dailiRouter;
