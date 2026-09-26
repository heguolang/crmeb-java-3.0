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
 * @description 分销设置 -- 详情
 */
export function configApi() {
  return request({
    url: '/admin/store/retail/spread/manage/get',
    method: 'get',
  });
}

/**
 * @description 分销设置 -- 表单提交
 */
export function configUpdateApi(data) {
  return request({
    url: '/admin/store/retail/spread/manage/set',
    method: 'post',
    data,
  });
}

/**
 * @description 分销员 -- 列表
 */
export function promoterListApi(params) {
  return request({
    url: '/admin/store/retail/list',
    method: 'get',
    params,
  });
}

/**
 * @description 推广人 -- 列表
 */
export function spreadListApi(params, data) {
  return request({
    url: '/admin/store/retail/spread/userlist',
    method: 'post',
    params,
    data,
  });
}

/**
 * @description 推广人订单 -- 列表
 */
export function spreadOrderListApi(params, data) {
  return request({
    url: '/admin/store/retail/spread/orderlist',
    method: 'post',
    params,
    data,
  });
}

/**
 * @description 推广人 -- 清除上级推广人
 */
export function spreadClearApi(id) {
  return request({
    url: `/admin/store/retail/spread/clean/${id}`,
    method: 'get',
  });
}

/**
 * @description 分销统计
 */
export function spreadStatisticsApi(params) {
  return request({
    url: `/admin/store/retail/statistics`,
    method: 'get',
    params,
  });
}

