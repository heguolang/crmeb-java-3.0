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
 * 提现申请 列表
 * @param pram
 */
export function applyListApi(params) {
  return request({
    url: '/admin/finance/apply/list',
    method: 'get',
    params,
  });
}

/**
 * 提现申请 金额
 * @param pram
 */
export function applyBalanceApi(params) {
  return request({
    url: '/admin/finance/apply/balance',
    method: 'post',
    params,
  });
}

/**
 * 提现申请 修改
 * @param pram
 */
export function applyUpdateApi(params) {
  return request({
    url: '/admin/finance/apply/update',
    method: 'post',
    params,
  });
}

/**
 * 提现申请 审核
 * @param pram
 */
export function applyStatusApi(params, data) {
  return request({
    url: '/admin/finance/apply/apply',
    method: 'post',
    params,
    data,
  });
}

/**
 * 充值 列表
 * @param pram
 */
export function topUpLogListApi(params) {
  return request({
    url: '/admin/user/topUpLog/list',
    method: 'get',
    params,
  });
}

/**
 * 充值 金额
 * @param pram
 */
export function balanceApi() {
  return request({
    url: '/admin/user/topUpLog/balance',
    method: 'post',
  });
}

/**
 * 充值 删除
 * @param pram
 */
export function topUpLogDeleteApi(params) {
  return request({
    url: '/admin/user/topUpLog/delete',
    method: 'get',
    params,
  });
}

/**
 * 充值 退款
 * @param pram
 */
export function refundApi(data) {
  return request({
    url: '/admin/user/topUpLog/refund',
    method: 'post',
    data,
  });
}

/**
 * 资金监控 列表
 * @param pram
 */
export function monitorListApi(params) {
  return request({
    url: '/admin/finance/founds/monitor/list',
    method: 'get',
    params,
  });
}

/**
 * 资金监控 明细类型
 * @param pram
 */
export function monitorListOptionApi() {
  return request({
    url: `/admin/finance/founds/monitor/list/option`,
    method: 'get',
  });
}

/**
 * 佣金记录 列表
 * @param pram
 */
export function brokerageListApi(params) {
  return request({
    url: '/admin/finance/founds/monitor/brokerage/record',
    method: 'get',
    params,
  });
}

/**
 * 提现设置获取
 */
export function extractSettingGetApi() {
  return request({
    url: '/admin/finance/apply/setting/get',
    method: 'get',
  });
}

/**
 * 提现设置保存
 */
export function extractSettingSaveApi(data) {
  return request({
    url: '/admin/finance/apply/setting/save',
    method: 'post',
    data,
  });
}
