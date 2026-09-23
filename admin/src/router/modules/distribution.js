// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
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
