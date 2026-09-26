// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

export default {
  // 设置选中的方法
  // 记忆选择核心方法
  changePageCoreRecordData: function (multipleSelectionAll, multipleSelection, tableData, successFn) {
    // 标识当前行的唯一键的名称
    const idKey = 'uid';
    const that = this;
    // 如果总记忆中还没有选择的数据，那么就直接取当前页选中的数据，不需要后面一系列计算
    if (multipleSelectionAll.length <= 0) {
      multipleSelectionAll = multipleSelection;
      successFn(multipleSelectionAll);
      return;
    }
    // 总选择里面的key集合
    const selectAllIds = [];
    multipleSelectionAll.forEach((row) => {
      selectAllIds.push(row[idKey]);
    });
    const selectIds = [];
    // 获取当前页选中的id
    multipleSelection.forEach((row) => {
      selectIds.push(row[idKey]);
      // 如果总选择里面不包含当前页选中的数据，那么就加入到总选择集合里
      if (selectAllIds.indexOf(row[idKey]) < 0) {
        multipleSelectionAll.push(row);
      }
    });
    const noSelectIds = [];
    // 得到当前页没有选中的id
    tableData.forEach((row) => {
      if (selectIds.indexOf(row[idKey]) < 0) {
        noSelectIds.push(row[idKey]);
      }
    });
    noSelectIds.forEach((uid) => {
      if (selectAllIds.indexOf(uid) >= 0) {
        for (let i = 0; i < multipleSelectionAll.length; i++) {
          if (multipleSelectionAll[i][idKey] == uid) {
            // 如果总选择中有未被选中的，那么就删除这条
            multipleSelectionAll.splice(i, 1);
            break;
          }
        }
      }
    });
    successFn(multipleSelectionAll);
  },
};
