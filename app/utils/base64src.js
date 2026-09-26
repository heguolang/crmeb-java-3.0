// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

const fsm = wx.getFileSystemManager ? wx.getFileSystemManager() : null;
const FILE_BASE_NAME = 'tmp_base64src'; //自定义文件名

export function base64src(base64data, dateminutes, cb) {
	const [, format, bodyData] = /data:image\/(\w+);base64,(.*)/.exec(base64data) || [];
	if (!format) {
		return (new Error('ERROR_BASE64SRC_PARSE'));
	}
	const filePath = `${wx.env.USER_DATA_PATH}/${dateminutes+FILE_BASE_NAME}.${format}`;
	const buffer = wx.base64ToArrayBuffer(bodyData);
	fsm.writeFile({
		filePath,
		data: buffer,
		encoding: 'binary',
		success() {
			cb(filePath);
		},
		fail() {
			return (new Error('ERROR_BASE64SRC_WRITE'));
		},
	});
};
//module.exports = base64src;
