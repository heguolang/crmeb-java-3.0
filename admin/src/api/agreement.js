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
 * @description 协议管理-- 详情
 */
export function agreementInfoApi(data) {
  return request.get(`admin/agreement/${data}`);
}

/**
 * @description 协议管理-- 保存
 */
export function agreementSaveApi(save, data) {
  // return request.post(`admin/platform/agreement/${save}`, data);
  return request({
    url: `admin/agreement/${save}`,
    method: 'POST',
    data,
  });
}
