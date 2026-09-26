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
 * 商品统计数据
 * @param pram
 */
export function productDataApi(params) {
  return request({
    url: `/admin/statistics/product/data`,
    method: 'GET',
    params,
  });
}

/**
 * 商品排行数据
 * @param pram
 */
export function productRankApi(params) {
  return request({
    url: `/admin/statistics/product/ranking`,
    method: 'GET',
    params,
  });
}

/**
 * 商品趋势数据
 * @param pram
 */
export function productTrendApi(params) {
  return request({
    url: `/admin/statistics/product/trend`,
    method: 'GET',
    params,
  });
}

/**
 * 交易统计数据
 * @param pram
 */
export function tradeDataApi() {
  return request({
    url: `/admin/statistics/trade/data`,
    method: 'GET',
  });
}

/**
 * 交易概览
 * @param pram
 */
export function tradeOverviewApi(params) {
  return request({
    url: `/admin/statistics/trade/overview`,
    method: 'GET',
    params,
  });
}

/**
 * 交易趋势
 * @param pram
 */
export function tradeTrendApi(params) {
  return request({
    url: `/admin/statistics/trade/trend`,
    method: 'GET',
    params,
  });
}

/**
 * 用户总数据
 * @param pram
 */
export function userTotalData() {
  return request({
    url: `/admin/statistics/user/total/data`,
    method: 'GET',
  });
}

/**
 * 用户区域数据
 * @param pram
 */
export function userAreaData() {
  return request({
    url: `/admin/statistics/user/area`,
    method: 'GET',
  });
}

/**
 * 用户渠道数据
 * @param pram
 */
export function userChannelData() {
  return request({
    url: `/admin/statistics/user/channel`,
    method: 'GET',
  });
}

/**
 * 用户概览
 * @param pram
 */
export function userOverviewData(params) {
  return request({
    url: `/admin/statistics/user/overview`,
    method: 'GET',
    params,
  });
}

/**
 * 用户性别数据
 * @param pram
 */
export function userSexData() {
  return request({
    url: `/admin/statistics/user/sex`,
    method: 'GET',
  });
}

/**
 * 用户概览列表
 * @param pram
 */
export function userOverviewListApi(params) {
  return request({
    url: `/admin/statistics/user/overview/list`,
    method: 'GET',
    params,
  });
}
