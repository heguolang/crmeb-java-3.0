// +----------------------------------------------------------------------
// | 大市场路由（业务大改版一级菜单）
// +----------------------------------------------------------------------
import Layout from '@/layout';

export default {
  path: '/market',
  component: Layout,
  redirect: '/market/pingou/activity',
  name: 'Market',
  meta: {
    title: '大市场',
    icon: 'goods'
  },
  children: [
    {
      path: 'pingou/activity',
      component: () => import('@/views/market/pingou/activity/index'),
      name: 'PingouActivity',
      meta: { title: '拼购活动', icon: '' }
    },
    {
      path: 'pingou/team',
      component: () => import('@/views/market/pingou/team/index'),
      name: 'PingouTeam',
      meta: { title: '拼购团记录', icon: '' }
    },
    {
      path: 'pingou/level',
      component: () => import('@/views/market/pingou/level/index'),
      name: 'PingouLevel',
      meta: { title: '拼购等级', icon: '' }
    },
    {
      path: 'dividend',
      component: () => import('@/views/market/dividend/index'),
      name: 'Dividend',
      meta: { title: '全球分红', icon: '' }
    }
  ]
};
