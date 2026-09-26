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

const distributionRouter = {
  path: '/distribution',
  component: Layout,
  redirect: '/distribution/distributionconfig',
  name: 'Distribution',
  meta: {
    title: '分销',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'index',
      component: () => import('@/views/distribution/index'),
      name: 'distributionIndex',
      meta: { title: '分销员管理', icon: '' },
    },
    {
      path: 'distributionconfig',
      component: () => import('@/views/distribution/config/index'),
      name: 'distributionConfig',
      meta: { title: '分销配置', icon: '' },
    },
    {
      path: 'distributorLevel',
      component: () => import('@/views/distribution/distributorLevel/index'),
      name: 'DistributorLevel',
      meta: { title: '分销商等级', icon: '' },
    },
    {
      path: 'teamGrade',
      component: () => import('@/views/distribution/teamGrade/index'),
      name: 'TeamGrade',
      meta: { title: '团队等级', icon: '' },
    },
    {
      path: 'teamLevelConfig',
      component: () => import('@/views/distribution/teamLevelConfig/index'),
      name: 'TeamLevelConfig',
      meta: { title: '团队等级配置', icon: '' },
    },
    {
      path: 'teamUser',
      component: () => import('@/views/distribution/teamUser/index'),
      name: 'TeamLevelUser',
      meta: { title: '团队关联用户', icon: '' },
    },
    {
      path: 'teamRecord',
      component: () => import('@/views/distribution/teamRecord/index'),
      name: 'TeamLevelRecord',
      meta: { title: '团队变更记录', icon: '' },
    },
    {
      path: 'teamBrokerageRecord',
      component: () => import('@/views/distribution/teamBrokerageRecord/index'),
      name: 'TeamBrokerageRecord',
      meta: { title: '团队奖资金记录', icon: '' },
    },
  ],
};

export default distributionRouter;
