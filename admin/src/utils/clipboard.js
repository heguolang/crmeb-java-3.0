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
import Clipboard from 'clipboard';

function clipboardSuccess() {
  Vue.prototype.$message({
    message: 'Copy successfully',
    type: 'success',
    duration: 1500,
  });
}

function clipboardError() {
  Vue.prototype.$message({
    message: 'Copy failed',
    type: 'error',
  });
}

export default function handleClipboard(text, event) {
  const clipboard = new Clipboard(event.target, {
    text: () => text,
  });
  clipboard.on('success', () => {
    clipboardSuccess();
    clipboard.destroy();
  });
  clipboard.on('error', () => {
    clipboardError();
    clipboard.destroy();
  });
  clipboard.onClick(event);
}
