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

const storeRouter = {
  path: '/store',
  component: Layout,
  redirect: '/store/index',
  name: 'Store',
  meta: {
    title: '商品',
    icon: 'clipboard',
  },
  children: [
    {
      path: 'index',
      component: () => import('@/views/store/index'),
      name: 'StoreIndex',
      meta: { title: '商品管理', icon: '' },
    },
    {
      path: 'sort',
      component: () => import('@/views/store/sort/index'),
      name: 'Sort',
      meta: { title: '商品分类', icon: '' },
    },
    {
      path: 'attr',
      component: () => import('@/views/store/storeAttr/index'),
      name: 'SortAttr',
      meta: { title: '商品规格', icon: '' },
    },
    {
      path: 'comment',
      component: () => import('@/views/store/storeComment/index'),
      name: 'StoreComment',
      meta: { title: '商品评论', icon: '' },
    },
    {
      path: 'list/creatProduct/:id?/:isDisabled?',
      component: () => import('@/views/store/creatStore/index'),
      name: 'SortCreat',
      meta: { title: '商品添加', noCache: true, activeMenu: `/store/index` },
      hidden: true,
    },
    {
      path: 'guarantee',
      component: () => import('@/views/store/guarantee/index'),
      name: 'StoreGuarantee',
      meta: { title: '保障服务', icon: '' },
    },
    {
      path: 'productGroup',
      component: () => import('@/views/store/productGroup/index'),
      name: 'StoreProductGroup',
      meta: { title: '商品分组', icon: '' },
    },
    {
      path: 'productGroup/edit/:id?',
      component: () => import('@/views/store/productGroup/edit'),
      name: 'StoreProductGroupEdit',
      meta: { title: '编辑商品分组', noCache: true, activeMenu: '/store/productGroup' },
      hidden: true,
    },
    {
      path: 'commentSetting',
      component: () => import('@/views/store/commentSetting/index'),
      name: 'commentSetting',
      meta: { title: '评论设置', icon: '' },
    },
  ],
};

export default storeRouter;
