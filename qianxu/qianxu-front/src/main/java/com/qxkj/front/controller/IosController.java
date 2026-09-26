package com.qxkj.front.controller;

import com.qxkj.common.request.IosBindingPhoneRequest;
import com.qxkj.common.request.IosLoginRequest;
import com.qxkj.common.request.WxBindingPhoneRequest;
import com.qxkj.common.response.LoginResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.front.service.IosService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@RestController("IosController")
@RequestMapping("api/front/ios")
@Api(tags = "IOS控制器")
public class IosController {

    @Autowired
    private IosService iosService;

    /**
     * ios登录
     */
    @ApiOperation(value = "ios登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResult<LoginResponse> login(@RequestBody @Validated IosLoginRequest loginRequest) {
        return CommonResult.success(iosService.login(loginRequest));
    }

    /**
     * IOS绑定手机号
     */
    @ApiOperation(value = "IOS绑定手机号-废弃")
    @RequestMapping(value = "/register/binding/phone", method = RequestMethod.POST)
    public CommonResult<LoginResponse> registerBindingPhone(@RequestBody @Validated WxBindingPhoneRequest request) {
        return CommonResult.success(iosService.registerBindingPhone(request));
    }

    /**
     * 绑定手机号
     */
    @ApiOperation(value = "IOS绑定手机号（登录后绑定）")
    @RequestMapping(value = "/binding/phone", method = RequestMethod.POST)
    public CommonResult<LoginResponse> bindingPhone(@RequestBody @Validated IosBindingPhoneRequest request) {
        if (iosService.bindingPhone(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed("绑定失败");
    }
}
