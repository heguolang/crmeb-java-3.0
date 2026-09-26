// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------
export const advertisementDefault = () => {
  return {
    isShowAddBtn: true, //添加按钮
    isShowEdit: true, //删除按钮
    isShowStatus: false, //开启状态
    isShowLinkUrl: true, //链接地址
    isShowLinkUrlChose: true, //选择地址选项
    isShowImageUrl: true, //图片地址
    isShowMoreLinkUrl: false, //多条链接
    maxList: 5,
    title: '标题',
    defaultList: {
      name: '',
      imageUrl: '',
      linkUrl: '',
      id: 0,
      sort: 0,
    },
    modelMaxLength: 5,
    list: [
      {
        name: '',
        imageUrl: '',
        linkUrl: '',
        id: 0,
        sort: 0,
      },
    ],
  };
};
