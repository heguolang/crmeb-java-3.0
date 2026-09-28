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
    // 旧地址兜底：/market/screen 现在是"数据中心大屏"目录，进来自动到运营大数据
    {
      path: 'screen',
      redirect: 'screen/operation'
    },
    {
      path: 'screen/operation',
      component: () => import('@/views/market/screen/index'),
      name: 'MarketDataScreen',
      // 页面自身 position:fixed 全视口铺满（不动后台框架），新标签页打开即全屏投屏
      meta: { title: '运营大数据', icon: '' }
    },
    {
      path: 'screen/market',
      component: () => import('@/views/market/bigdata/index'),
      name: 'MarketBigData',
      meta: { title: '市场大数据', icon: '' }
    },
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
      path: 'dividend/setting',
      component: () => import('@/views/market/dividend/setting'),
      name: 'DividendSetting',
      meta: { title: '分红设置', icon: '' }
    },
    {
      path: 'dividend/level',
      component: () => import('@/views/market/dividend/level'),
      name: 'DividendLevel',
      meta: { title: '分红等级', icon: '' }
    },
    {
      path: 'dividend/record',
      component: () => import('@/views/market/dividend/record'),
      name: 'DividendRecord',
      meta: { title: '分红记录', icon: '' }
    }
  ]
};
