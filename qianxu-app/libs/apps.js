// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import { appAuth } from '../api/public';
import { tokenIsExistApi } from '@/api/api.js';
class Apps{
	/**
	 * 校验token是否有效,true为有效，false为无效
	 */
	getTokenIsExist(){
		return new Promise( (resolve,reject) => {
			tokenIsExistApi().then(res => {
				resolve(res.data);
			}).catch(reject);
		})
	}
	
	/**
	 * 授权登录获取token
	 * @param {Object} code
	 */
	authApp(code) {
		return new Promise((resolve, reject) => {
			appAuth(code,{'spread_spid': 0})
				.then(({
					data
				}) => {
					resolve(data);
					Cache.set(WX_AUTH, code);
					Cache.clear(STATE_KEY);
					loginType && Cache.clear(LOGINTYPE);
					
				})
				.catch(reject);
		});
	}
}
export default new Apps();