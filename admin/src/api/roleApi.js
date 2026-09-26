// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import request from '@/utils/request';

/**
 * 角色详情
 */
export function getRoleById(pram) {
  return request({
    url: `/admin/system/role/info/${pram.roles}`,
    method: 'GET',
  });
}

/**
 * 菜单
 * @param pram
 */
export function menuListApi() {
  return request({
    url: '/admin/getMenus',
    method: 'GET',
  });
}
