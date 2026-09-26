package com.qxkj.front.service;


import com.qxkj.common.request.IosBindingPhoneRequest;
import com.qxkj.common.request.IosLoginRequest;
import com.qxkj.common.request.WxBindingPhoneRequest;
import com.qxkj.common.response.LoginResponse;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
public interface IosService {

    /**
     * ios登录
     * @param loginRequest 登录请求对象
     */
    LoginResponse login(IosLoginRequest loginRequest);

    /**
     * IOS绑定手机号
     * @param request 绑定请求对象
     * @return 登录信息
     */
    LoginResponse registerBindingPhone(WxBindingPhoneRequest request);

    /**
     * ios绑定手机号（登录后）
     * @param request 请求对象
     * @return 是否绑定
     */
    Boolean bindingPhone(IosBindingPhoneRequest request);
}
