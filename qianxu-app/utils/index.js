// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import { spread } from "@/api/user";
import Cache from "@/utils/cache";
import { getCity } from '@/api/api.js';

/**
 * 静默授权绑定上下级，使用在已经登录后扫描了别人的推广二维码
 * @param {Object} puid
 */
export function silenceBindingSpread() {
	//#ifdef H5
	let puid = Cache.get('spread');
	//#endif 
	//#ifdef MP || APP-PLUS
	let puid = getApp().globalData.spread;
	//#endif

	puid = parseInt(puid);
	if (Number.isNaN(puid)) {
		puid = 0;
	}
	if (puid) {
		spread(puid).then(res => {}).catch(res => {
			//#ifdef H5
			Cache.clear("spread");
			//#endif
			
			//#ifdef MP || APP-PLUS
			getApp().globalData.spread = 0;
			//#endif
		});
	} else {
		Cache.set('spread', 0);
	}
}

export function isWeixin() {
	return navigator.userAgent.toLowerCase().indexOf("micromessenger") !== -1;
}

export function parseQuery() {
	const res = {};

	const query = (location.href.split("?")[1] || "")
		.trim()
		.replace(/^(\?|#|&)/, "");

	if (!query) {
		return res;
	}

	query.split("&").forEach(param => {
		const parts = param.replace(/\+/g, " ").split("=");
		const key = decodeURIComponent(parts.shift());
		const val = parts.length > 0 ? decodeURIComponent(parts.join("=")) : null;

		if (res[key] === undefined) {
			res[key] = val;
		} else if (Array.isArray(res[key])) {
			res[key].push(val);
		} else {
			res[key] = [res[key], val];
		}
	});

	return res;
}

export function getCustomer(url) {
	uni.navigateTo({
		url: url || '/pages/users/web_page/index'
	});
}

// #ifdef H5
const VUE_APP_WS_URL = process.env.VUE_APP_WS_URL || `ws://${location.hostname}:20001`;
export {
	VUE_APP_WS_URL
}
// #endif

// 获取地址数据
export function getCityList() {
	return new Promise((resolve, reject) => {
		getCity().then(res => {
			resolve(res.data);
			let oneDay = 24 * 3600 * 1000;
			Cache.setItem({
				name: 'cityList',
				value: res.data,
				expires: oneDay * 7
			}); //设置七天过期时间
		})
	});
}

export default parseQuery;
