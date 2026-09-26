// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import {
  Confirm as confirm,
  Alert as alert,
  Toast as toast,
  Notify as notify,
  Loading as loading,
} from 'vue-ydui/dist/lib.rem/dialog';

const dialog = {
  confirm,
  alert,
  toast,
  notify,
  loading,
};

const icons = { error: '操作失败', success: '操作成功' };
Object.keys(icons).reduce((dialog, key) => {
  dialog[key] = (mes, obj = {}) => {
    return new Promise(function (resolve) {
      toast({
        mes: mes || icons[key],
        timeout: 1000,
        icon: key,
        callback: () => {
          resolve();
        },
        ...obj,
      });
    });
  };
  return dialog;
}, dialog);

dialog.message = (mes = '操作失败', obj = {}) => {
  return new Promise(function (resolve) {
    toast({
      mes,
      timeout: 1000,
      callback: () => {
        resolve();
      },
      ...obj,
    });
  });
};

dialog.validateError = (...args) => {
  validatorDefaultCatch(...args);
};

export function validatorDefaultCatch(err, type = 'message') {
  console.log(err);
  return dialog[type](err.errors[0].message);
}

export default dialog;
