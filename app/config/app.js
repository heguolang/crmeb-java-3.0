// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------
let domain = 'http://127.0.0.1:8081'
// 生产（线上）
// let domain = 'https://api.qianxutec.com'

module.exports = {
	// 请求域名 格式： https://您的域名
	// #ifdef MP || APP-PLUS
		// HTTP_REQUEST_URL:'',
		HTTP_REQUEST_URL: domain,
		// H5商城地址
		// 本地调试用：H5 dev (8090)
		HTTP_H5_URL: 'http://127.0.0.1:8090',
		// 生产（线上）
		// HTTP_H5_URL: 'https://app.qianxutec.com',
	// #endif
	// #ifdef H5
		HTTP_REQUEST_URL:domain,
	// #endif
	HEADER:{
		'content-type': 'application/json'
	},
	HEADERPARAMS:{
		'content-type': 'application/x-www-form-urlencoded'
	},
	// 回话密钥名称 请勿修改此配置
	TOKENNAME: 'Authori-zation',
	// 缓存时间 0 永久
	EXPIRE:0,
	//分页最多显示条数
	LIMIT: 10
};
