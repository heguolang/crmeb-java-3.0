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

const operationRouter = {
  path: '/operation',
  component: Layout,
  redirect: '/operation/setting',
  name: 'Operation',
  meta: {
    title: '设置',
    icon: 'clipboard',
    roles: ['admin'],
  },
  children: [
    {
      path: 'setting',
      name: 'setting',
      component: () => import('@/views/systemSetting/setting'),
      meta: {
        title: '系统设置',
        icon: 'clipboard',
      },
    },
    {
      path: 'guide',
      name: 'guide',
      component: () => import('@/views/systemSetting/guide'),
      meta: {
        title: '配置引导',
        icon: 'clipboard',
      },
    },
    {
      path: 'notification',
      name: 'notification',
      component: () => import('@/views/systemSetting/notification'),
      meta: {
        title: '消息通知',
        icon: 'clipboard',
      },
    },
    {
      path: 'roleManager',
      name: 'RoleManager',
      component: () => import('@/views/systemSetting/administratorAuthority'),
      meta: {
        title: '管理权限',
        icon: 'clipboard',
        roles: ['admin'],
      },
      children: [
        {
          path: 'identityManager',
          component: () => import('@/views/systemSetting/administratorAuthority/identityManager'),
          name: 'identityManager',
          meta: { title: '角色管理', icon: '' },
        },
        {
          path: 'adminList',
          component: () => import('@/views/systemSetting/administratorAuthority/adminList'),
          name: 'adminList',
          meta: { title: '管理员列表', icon: '' },
        },
        {
          path: 'promiseRules',
          component: () => import('@/views/systemSetting/administratorAuthority/permissionRules'),
          name: 'promiseRules',
          meta: { title: '权限规则', icon: '' },
        },
      ],
    },
    {
      path: 'logManager',
      name: 'LogManager',
      component: () => import('@/views/systemSetting/logManager'),
      meta: {
        title: '日志管理',
        icon: 'clipboard',
        roles: ['admin'],
      },
      children: [
        {
          path: 'adminLoginLog',
          component: () => import('@/views/systemSetting/logManager/adminLoginLog'),
          name: 'adminLoginLog',
          meta: { title: '管理员登录日志', icon: '' },
        },
        {
          path: 'adminOperateLog',
          component: () => import('@/views/systemSetting/logManager/adminOperateLog'),
          name: 'adminOperateLog',
          meta: { title: '管理员操作日志', icon: '' },
        },
      ],
    },
    {
      path: 'systemSms',
      component: () => import('@/views/sms'),
      name: 'systemSms',
      meta: {
        title: '短信设置',
        icon: 'clipboard',
        roles: ['admin'],
      },
      children: [
        {
          path: 'config',
          component: () => import('@/views/sms/smsConfig'),
          name: 'SmsConfig',
          meta: { title: '短信配置', noCache: true },
        },
        {
          path: 'template',
          component: () => import('@/views/sms/smsTemplate'),
          name: 'SmsTemplate',
          meta: { title: '短信模板', noCache: true },
        },
        {
          path: 'message',
          component: () => import('@/views/sms/smsMessage'),
          name: 'SmsMessage',
          meta: { title: '短信开关', noCache: true },
        },
      ],
    },
    {
      path: 'deliverGoods',
      name: 'deliverGoods',
      alwaysShow: true,
      component: () => import('@/views/systemSetting/deliverGoods'),
      meta: {
        title: '发货设置',
        roles: ['admin'],
      },
      children: [
        {
          path: 'freightSet',
          component: () => import('@/views/systemSetting/deliverGoods/freightSet'),
          name: 'freightSet',
          meta: { title: '运费模板', noCache: true },
        },
      ],
    },
    {
      path: 'agreement',
      name: 'agreement',
      component: () => import('@/views/systemSetting/agreement'),
      meta: {
        title: '协议管理',
      },
    },
  ],
};

export default operationRouter; //collate
