package com.qxkj.front.service;

import com.qxkj.common.model.user.User;
import com.qxkj.common.request.LoginMobileRequest;
import com.qxkj.common.request.LoginRequest;
import com.qxkj.common.response.LoginConfigResponse;
import com.qxkj.common.response.LoginResponse;

import javax.servlet.http.HttpServletRequest;

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
public interface LoginService {

    /**
     * 账号密码登录
     *
     * @return LoginResponse
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 手机号密码注册（不依赖短信验证码）
     *
     * @param loginRequest 手机号、密码、推广人
     * @return LoginResponse 注册成功直接返回登录态
     */
    LoginResponse register(LoginRequest loginRequest);

    /**
     * 手机号验证码登录
     */
    LoginResponse phoneLogin(LoginMobileRequest loginRequest);

    /**
     * 老绑定分销关系
     *
     * @param user      User 用户user类
     * @param spreadUid Integer 推广人id
     * @return Boolean
     */
    Boolean bindSpread(User user, Integer spreadUid);

    /**
     * 推出登录
     *
     * @param request HttpServletRequest
     */
    void loginOut(HttpServletRequest request);

    /**
     * 校验token是否有效
     *
     * @return true 有效， false 无效
     */
    Boolean tokenIsExist();

    /**
     * 获取登录配置
     */
    LoginConfigResponse getLoginConfig();
}
