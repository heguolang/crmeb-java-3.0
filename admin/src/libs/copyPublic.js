// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------
import { copyrightInfoApi } from '@/api/authInformation';

/**
 * @description 短信平台登录态已废弃（兼容旧调用）
 */
export function isLogin() {
  return Promise.resolve({ status: false, isLogin: false });
}

/**
 * @description 获取公司版权信息
 */
export function getCopyrightInfo() {
  return new Promise((resolve, reject) => {
    copyrightInfoApi()
      .then(async (res) => {
        resolve(res);
      })
      .catch((res) => {
        reject(res);
      });
  });
}

/**
 * @description 表格列表中删除最后一页中的唯一一个数据的操作
 */
export function handleDeleteTable(length, tableFrom) {
  if (length === 1 && tableFrom.page > 1) return (tableFrom.page = tableFrom.page - 1);
}
