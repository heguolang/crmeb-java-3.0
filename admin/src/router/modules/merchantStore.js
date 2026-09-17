// +----------------------------------------------------------------------
// | 门店系统路由
// +----------------------------------------------------------------------
import Layout from '@/layout';

export default {
  path: '/merchantStore',
  component: Layout,
  redirect: '/merchantStore/list',
  name: 'MerchantStore',
  meta: {
    title: '门店',
    icon: 'store'
  },
  children: [
    {
      path: 'list',
      component: () => import('@/views/merchantStore/list/index'),
      name: 'MerchantStoreList',
      meta: { title: '门店管理', icon: '' }
    },
    {
      path: 'verify',
      component: () => import('@/views/merchantStore/verify/index'),
      name: 'MerchantStoreVerify',
      meta: { title: '核销记录', icon: '' }
    }
  ]
};
