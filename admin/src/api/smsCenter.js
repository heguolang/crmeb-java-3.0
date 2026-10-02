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
 * @description 读取短信通道配置（账号/密码设置状态/mock/通道）
 */
export function smsChannelConfigApi() {
  return request({
    url: '/admin/sms/channel/config',
    method: 'get',
  });
}

/**
 * @description 保存短信通道配置（pwd 留空表示不修改）
 */
export function smsChannelSaveApi(data) {
  return request({
    url: '/admin/sms/channel/save',
    method: 'post',
    data,
  });
}

/**
 * @description 滨礼账户余额
 */
export function smsBalanceApi() {
  return request({
    url: '/admin/sms/balance',
    method: 'get',
  });
}

/**
 * @description 短信测试发送
 */
export function smsTestSendApi(data) {
  return request({
    url: '/admin/sms/send/test',
    method: 'post',
    data,
  });
}

/**
 * @description 发送记录分页列表
 */
export function smsRecordListApi(params) {
  return request({
    url: '/admin/sms/record/list',
    method: 'get',
    params,
  });
}

/**
 * @description 从滨礼拉取状态回执并更新记录
 */
export function smsRecordSyncApi() {
  return request({
    url: '/admin/sms/record/sync',
    method: 'post',
  });
}
