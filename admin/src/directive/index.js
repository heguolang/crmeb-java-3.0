// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import hasRole from './permission/hasRole';
import hasPermi from './permission/hasPermi';
import dialogDrag from './dialog/drag';
import dialogDragWidth from './dialog/dragWidth';
import dialogDragHeight from './dialog/dragHeight';
import copy from './copy/copy';
import dbClick from './module/dbClick';

const install = function (Vue) {
  Vue.directive('hasRole', hasRole);
  Vue.directive('hasPermi', hasPermi);
  Vue.directive('dialogDrag', dialogDrag);
  Vue.directive('dialogDragWidth', dialogDragWidth);
  Vue.directive('dialogDragHeight', dialogDragHeight);
  Vue.directive('copy', copy);
  Vue.directive('dbClick', dbClick);
};

if (window.Vue) {
  window['hasRole'] = hasRole;
  window['hasPermi'] = hasPermi;
  Vue.use(install); // eslint-disable-line
}

export default install;
