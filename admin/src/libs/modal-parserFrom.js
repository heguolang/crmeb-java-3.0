// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

/**
 * 弹窗样式的表单配置的提交
 * @param title 标题
 * @param formName 表单name
 * @param isCreate 是否是编辑
 * @param editData 详情数据
 * @param callback 回调函数
 * @param keyNum 重置表单key值
 * @returns {Promise<any>}
 */
export default function modalParserFrom(title, formName, isCreate, editData, callback, keyNum) {
  const h = this.$createElement;
  return new Promise((resolve, reject) => {
    this.$msgbox({
      title,
      customClass: 'upload-form',
      closeOnClickModal: false,
      showClose: true,
      message: h('div', { class: 'parserFrom_modal'}, [
        h('ZBParser', {
          props: {
            formName,
            isCreate,
            editData,
            keyNum,
          },
          on: {
            submit(formValue) {
              callback(formValue);
            }
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
        // this.$message({
        //   type: 'info',
        //   message: '已取消',
        // });
      });
  });
}
