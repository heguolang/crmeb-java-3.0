// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

//订单过滤器

/**
 * @description 支付状态
 */
export function paidFilter(status) {
  const statusMap = {
    true: '已支付',
    false: '未支付',
  };
  return statusMap[status];
}

/**
 * @description 订单状态
 * 2,已收货，待评价
 */
export function orderStatusFilter(status) {
  const statusMap = {
    0: '待发货',
    1: '待收货',
    2: '已收货',
    3: '待评价',
    '-2': '已退款',
    '-1': '退款中',
  };
  return statusMap[status];
}

/**
 * @description 退款状态
 * 2,已收货，待评价
 */
export function refundStatusFilter(status) {
  const statusMap = {
    0: '未退款',
    1: '申请中',
    2: '已退款',
    3: '退款中',
  };
  return statusMap[status];
}

/**
 * @description 支付方式
 */
export function payTypeFilter(status) {
  const statusMap = {
    weixin: '微信',
    alipay: '支付宝',
    yue: '余额',
  };
  return statusMap[status] || '-';
}

/**
 * @description 订单类型
 */
export function orderTypeFilter(status) {
  const statusMap = {
    1: '普通订单',
    2: '核销订单',
  };
  return statusMap[status];
}
