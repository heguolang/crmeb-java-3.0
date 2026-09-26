// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import Vue from 'vue';
import store from '@/store';
import { isString, isArray } from '@/utils/validate';
import settings from '@/settings';

// you can set in settings.js
// errorLog:'production' | ['production', 'development']
const { errorLog: needErrorLog } = settings;

function checkNeed() {
  const env = process.env.NODE_ENV;
  if (isString(needErrorLog)) {
    return env === needErrorLog;
  }
  if (isArray(needErrorLog)) {
    return needErrorLog.includes(env);
  }
  return false;
}

if (checkNeed()) {
  Vue.config.errorHandler = function (err, vm, info) {
    // Don't ask me why I use Vue.nextTick, it just a hack.
    // detail see https://forum.vuejs.org/t/dispatch-in-vue-config-errorhandler-has-some-problem/23500
    Vue.nextTick(() => {
      store.dispatch('errorLog/addErrorLog', {
        err,
        vm,
        info,
        url: window.location.href,
      });
    });
  };
}
