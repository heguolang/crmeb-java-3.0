// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

//会员过滤器

/**
 * 等级
 */
export function levelFilter(status) {
  if (!status) {
    return '';
  }
  let arrayList = JSON.parse(localStorage.getItem('single-admin-levelKey'));
  let array = arrayList.filter((item) => status === item.id);
  if (array.length) {
    return array[0].name;
  } else {
    return '';
  }
}

/**
 * 用户类型
 */
export function typeFilter(status) {
  const statusMap = {
    wechat: '微信用户',
    routine: '小程序用户',
    h5: 'H5用户',
  };
  return statusMap[status];
}

/**
 * 用户类型
 */
export function filterIsPromoter(status) {
  const statusMap = {
    true: '推广员',
    false: '普通用户',
  };
  return statusMap[status];
}
