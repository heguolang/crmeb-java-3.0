// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

//小程序 微信过滤器
import Cookies from 'js-cookie';
/**
 * @description 小程序所属类目
 */
export function wxCategoryFilter(status) {
  if (!status) {
    return '';
  }
  if (!Cookies.get('WxCategory')) {
    return;
  }
  let arrayList = JSON.parse(Cookies.get('WxCategory'));
  if (arrayList.filter((item) => Number(status) === Number(item.id)).length < 1) {
    return '';
  }
  return arrayList.filter((item) => Number(status) === Number(item.id))[0].name;
}

/**
 * @description 小程序模板类型
 */
export function wxTypeFilter(status) {
  const statusMap = {
    2: '一次性订阅',
    3: '长期订阅',
  };
  return statusMap[status];
}
