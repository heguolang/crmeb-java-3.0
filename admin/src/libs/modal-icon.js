// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

export default function modalIcon(callback) {
  const h = this.$createElement;
  return new Promise((resolve, reject) => {
    this.$msgbox({
      title: '菜单图标',
      customClass: 'upload-form',
      closeOnClickModal: false,
      showClose: true,
      message: h('div', { class: 'common-form-upload' }, [
        h('iconFrom', {
          on: {
            getIcon(n) {
              callback(n);
            },
          },
        }),
      ]),
      showCancelButton: false,
      showConfirmButton: false,
    })
      .then(() => {
        resolve();
      })
      .catch(() => {
        reject();
        this.$message({
          type: 'info',
          message: '已取消',
        });
      });
  });
}
