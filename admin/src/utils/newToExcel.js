// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------
import { export_json_to_excel } from '../vendor/Export1Excel';

/**
 * @method exportExcel
 * @param {Array} header   表头
 * @param {Array} filterVal 表头属性字段
 * @param {String} filename 文件名称
 * @param {Array} tableData 列表数据
 **/
export default function exportExcel(header, filterVal, filename, tableData) {
  var data = formatJson(filterVal, tableData);
  export_json_to_excel(header, data, filename);
}

function formatJson(filterVal, tableData) {
  return tableData.map((v) => {
    return filterVal.map((j) => {
      return v[j];
    });
  });
}
