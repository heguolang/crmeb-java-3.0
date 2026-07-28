// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2025 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
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

/**
 * @description 会员返佣配置 -- 列表
 */
export function brokerageListApi() {
  return request({
    url: '/admin/system/user/level/brokerage/list',
    method: 'get',
  });
}

/**
 * @description 会员返佣配置 -- 详情
 */
export function brokerageInfoApi(levelId) {
  return request({
    url: '/admin/system/user/level/brokerage/info',
    method: 'get',
    params: { levelId },
  });
}

/**
 * @description 会员返佣配置 -- 保存
 */
export function brokerageSaveApi(data) {
  return request({
    url: '/admin/system/user/level/brokerage/save',
    method: 'post',
    data,
  });
}

/**
 * @description 会员返佣配置 -- 删除
 */
export function brokerageDeleteApi(levelId) {
  return request({
    url: `/admin/system/user/level/brokerage/delete/${levelId}`,
    method: 'post',
  });
}
