package com.qxkj.front.controller;


import com.qxkj.common.request.LoginMobileRequest;
import com.qxkj.common.request.LoginRequest;
import com.qxkj.common.response.LoginConfigResponse;
import com.qxkj.common.response.LoginResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.front.service.LoginService;
import com.qxkj.service.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
@RestController("FrontLoginController")
@RequestMapping("api/front")
@Api(tags = "用户 -- 登录注册")
public class LoginController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private LoginService loginService;

    /**
     * 手机号登录接口
     */
    @ApiOperation(value = "手机号登录接口")
    @RequestMapping(value = "/login/mobile", method = RequestMethod.POST)
    public CommonResult<LoginResponse> phoneLogin(@RequestBody @Validated LoginMobileRequest loginRequest) {
        return CommonResult.success(loginService.phoneLogin(loginRequest));
    }

    /**
     * 账号密码登录
     */
    @ApiOperation(value = "账号密码登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResult<LoginResponse> login(@RequestBody @Validated LoginRequest loginRequest) {
        return CommonResult.success(loginService.login(loginRequest));
    }

    /**
     * 手机号密码注册（不依赖短信验证码）
     * 说明：H5 会员端注册入口，注册成功直接返回登录态
     */
    @ApiOperation(value = "手机号密码注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public CommonResult<LoginResponse> register(@RequestBody @Validated LoginRequest loginRequest) {
        return CommonResult.success(loginService.register(loginRequest));
    }


    /**
     * 退出登录
     */
    @ApiOperation(value = "退出")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public CommonResult<String> loginOut(HttpServletRequest request) {
        loginService.loginOut(request);
        return CommonResult.success();
    }

    /**
     * 发送短信登录验证码
     * 说明：H5 会员端已改为仅手机号+密码登录，短信验证码登录已下线，
     * 此处直接拦截，防止绕过前端直接调用接口发送短信。
     *
     * @param phone 手机号码
     * @return 发送是否成功
     */
    @ApiOperation(value = "发送短信登录验证码")
    @RequestMapping(value = "/sendCode", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号码", required = true)
    })
    public CommonResult<Object> sendCode(@RequestParam String phone) {
        return CommonResult.failed("短信验证码登录已关闭，请使用手机号密码登录");
    }

    /**
     * 发送短信登录验证码（原有逻辑，如需恢复验证码登录请启用）
     */
    public CommonResult<Object> sendCodeOriginal(String phone) {
        if (smsService.sendCommonCode(phone)) {
            return CommonResult.success("发送成功");
        } else {
            return CommonResult.failed("发送失败");
        }
    }

    @ApiOperation(value = "校验token是否有效")
    @RequestMapping(value = "/token/is/exist", method = RequestMethod.POST)
    public CommonResult<Boolean> tokenIsExist() {
        return CommonResult.success(loginService.tokenIsExist());
    }

    @ApiOperation(value = "获取登录配置")
    @RequestMapping(value = "/login/config", method = RequestMethod.GET)
    public CommonResult<LoginConfigResponse> getLoginConfig() {
        return CommonResult.success(loginService.getLoginConfig());
    }
}



