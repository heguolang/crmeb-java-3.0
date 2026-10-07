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
    // 旧地址兜底：/market/screen 现在是"数据中心大屏"目录，进来自动到大数据中心
    {
      path: 'screen',
      redirect: 'screen/hub'
    },
    // 大数据中心：运营/市场两个大屏的聚合入口（缩略图 + 打开按钮 + 名称设置）
    {
      path: 'screen/hub',
      component: () => import('@/views/market/screen/hub'),
      name: 'MarketScreenHub',
      meta: { title: '大数据中心', icon: '' }
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
    },
    // ==================== 企业管理（软件开发服务） ====================
    {
      path: 'enterprise/demand',
      component: () => import('@/views/market/enterprise/demand/index'),
      name: 'SvcDemand',
      meta: { title: '服务需求', icon: '' }
    },
    {
      path: 'enterprise/contract',
      component: () => import('@/views/market/enterprise/contract/index'),
      name: 'SvcContract',
      meta: { title: '合同管理', icon: '' }
    },
    {
      path: 'enterprise/platform',
      component: () => import('@/views/market/enterprise/platform/index'),
      name: 'SvcPlatform',
      meta: { title: '签约渠道', icon: '' }
    },
    {
      path: 'enterprise/invoice',
      component: () => import('@/views/market/enterprise/invoice/index'),
      name: 'SvcInvoice',
      meta: { title: '发票管理', icon: '' }
    },
    {
      path: 'enterprise/project',
      component: () => import('@/views/market/enterprise/project/index'),
      name: 'SvcProject',
      meta: { title: '项目进度', icon: '' }
    },
    {
      path: 'enterprise/case',
      component: () => import('@/views/market/enterprise/case/index'),
      name: 'SvcCase',
      meta: { title: '客户案例', icon: '' }
    },
    {
      path: 'enterprise/setting',
      component: () => import('@/views/market/enterprise/setting/index'),
      name: 'SvcSetting',
      meta: { title: '服务设置', icon: '' }
    },
    // ==================== 招聘系统 ====================
    {
      path: 'recruit/job',
      component: () => import('@/views/market/recruit/job/index'),
      name: 'RecruitJob',
      meta: { title: '职位管理', icon: '' }
    },
    {
      path: 'recruit/apply',
      component: () => import('@/views/market/recruit/apply/index'),
      name: 'RecruitApply',
      meta: { title: '应聘管理', icon: '' }
    },
    {
      path: 'recruit/setting',
      component: () => import('@/views/market/recruit/setting/index'),
      name: 'RecruitSetting',
      meta: { title: '招聘设置', icon: '' }
    }
  ]
};
