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
 * 获取版权信息
 */
export function copyrightInfoApi() {
  return request({
    url: '/admin/copyright/get/info',
    method: 'get',
  });
}

/**
 * 保存版权信息
 */
export function saveQianxuCopyRight(data) {
  return request({
    url: '/admin/copyright/update/company/info',
    method: 'post',
    data,
  });
}

/**
 * @description 账号登录检测
 */
export function accountDetectionApi(data) {
  return request({
    url: '/admin/login/account/detection',
    method: 'post',
    data,
  });
}
