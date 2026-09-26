package com.qxkj.front.controller;

import com.qxkj.common.model.wechat.TemplateMessage;
import com.qxkj.common.request.RegisterAppWxRequest;
import com.qxkj.common.request.RegisterThirdUserRequest;
import com.qxkj.common.request.WxBindingPhoneRequest;
import com.qxkj.common.response.LoginResponse;
import com.qxkj.common.response.WeChatJsSdkConfigResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.front.service.UserCenterService;
import com.qxkj.service.service.SystemNotificationService;
import com.qxkj.service.service.WechatNewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
@RestController("WeChatFrontController")
@RequestMapping("api/front/wechat")
@Api(tags = "微信 -- 开放平台")
public class WeChatController {

    @Autowired
    private UserCenterService userCenterService;

    @Autowired
    private WechatNewService wechatNewService;

    @Autowired
    private SystemNotificationService systemNotificationService;

    /**
     * 通过微信code登录
     */
    @ApiOperation(value = "微信登录公众号授权登录")
    @RequestMapping(value = "/authorize/login", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "spread_spid", value = "推荐人id", dataType = "Integer"),
            @ApiImplicitParam(name = "code", value = "code码", dataType = "String", required = true)
    })
    public CommonResult<LoginResponse> login(@RequestParam(value = "spread_spid", defaultValue = "0", required = false) Integer spreadUid,
                                             @RequestParam(value = "code") String code){
        return CommonResult.success(userCenterService.weChatAuthorizeLogin(code, spreadUid));
    }

    /**
     * 微信登录小程序授权登录
     */
    @ApiOperation(value = "微信登录小程序授权登录")
    @RequestMapping(value = "/authorize/program/login", method = RequestMethod.POST)
    public CommonResult<LoginResponse> programLogin(@RequestParam String code, @RequestBody @Validated RegisterThirdUserRequest request){
        return CommonResult.success(userCenterService.weChatAuthorizeProgramLogin(code, request));
    }

    /**
     * 微信登录App授权登录
     */
    @ApiOperation(value = "微信登录App授权登录")
    @RequestMapping(value = "/authorize/app/login", method = RequestMethod.POST)
    public CommonResult<LoginResponse> appLogin(@RequestBody @Validated RegisterAppWxRequest request){
        return CommonResult.success(userCenterService.appLogin(request));
    }

    /**
     * 微信注册绑定手机号
     */
    @ApiOperation(value = "微信注册绑定手机号")
    @RequestMapping(value = "/register/binding/phone", method = RequestMethod.POST)
    public CommonResult<LoginResponse> registerBindingPhone(@RequestBody @Validated WxBindingPhoneRequest request){
        return CommonResult.success(userCenterService.registerBindingPhone(request));
    }

    /**
     * 获取微信公众号js配置
     */
    @ApiOperation(value = "获取微信公众号js配置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    @ApiImplicitParam(name = "url", value = "页面地址url")
    public CommonResult<WeChatJsSdkConfigResponse> configJs(@RequestParam(value = "url") String url){
        return CommonResult.success(wechatNewService.getJsSdkConfig(url));
    }

    /**
     * 小程序获取授权logo
     */
    @ApiOperation(value = "小程序获取授权logo")
    @RequestMapping(value = "/getLogo", method = RequestMethod.GET)
    public CommonResult<Map<String, String>> getLogo(){
        Map<String, String> map = new HashMap<>();
        map.put("logoUrl", userCenterService.getLogo());
        return CommonResult.success(map);
    }

    /**
     * 订阅消息模板列表
     */
    @ApiOperation(value = "订阅消息模板列表")
    @RequestMapping(value = "/program/my/temp/list", method = RequestMethod.GET)
    @ApiImplicitParam(name = "type", value = "支付之前：beforePay|支付成功：afterPay|申请退款：refundApply|充值之前：beforeRecharge|创建砍价：createBargain|参与拼团：pink|取消拼团：cancelPink")
    public CommonResult<List<TemplateMessage>> programMyTempList(@RequestParam(name = "type") String type){
        return CommonResult.success(systemNotificationService.getMiniTempList(type));
    }
}



