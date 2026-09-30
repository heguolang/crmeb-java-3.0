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

// 首页数据概览
export function viewModelApi() {
  return request({
    url: '/admin/statistics/home/index',
    method: 'GET',
  });
}

// 用户曲线图
export function chartUserApi() {
  return request({
    url: '/admin/statistics/home/chart/user',
    method: 'get',
  });
}

// 用户购买统计
export function chartBuyApi() {
  return request({
    url: '/admin/statistics/home/chart/user/buy',
    method: 'get',
  });
}

// 订单量趋势 30天
export function chartOrder30Api() {
  return request({
    url: '/admin/statistics/home/chart/order',
    method: 'get',
  });
}

// 订单量趋势 月
export function chartOrderMonthApi() {
  return request({
    url: '/admin/statistics/home/chart/order/month',
    method: 'get',
  });
}

// 订单量趋势 周
export function chartOrderWeekApi() {
  return request({
    url: '/admin/statistics/home/chart/order/week',
    method: 'get',
  });
}

// 订单量趋势 年
export function chartOrderYearApi() {
  return request({
    url: '/admin/statistics/home/chart/order/year',
    method: 'get',
  });
}

// 首页经营数据
export function businessData() {
  return request({
    url: '/admin/statistics/home/operating/data',
    method: 'get',
  });
}

// ==================== 数据中心大屏展示名称设置（2026-09-30） ====================

// 获取大屏大标题设置（运营大数据/市场大数据），返回 JSON 字符串，未配置返回空串
export function getBigdataScreenSettingApi() {
  return request({
    url: '/admin/system/config/get/bigdata/screen/setting',
    method: 'get',
  });
}

// 保存大屏大标题设置，value 为 JSON 字符串：{ operation: '...', market: '...' }
export function saveBigdataScreenSettingApi(value) {
  return request({
    url: '/admin/system/config/save/bigdata/screen/setting',
    method: 'post',
    data: { value },
  });
}
