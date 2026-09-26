package com.qxkj.admin.controller;

import com.qxkj.service.service.CallbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("api/admin/payment/callback")
@Api(tags = "支付回调")
public class CallbackController {

    @Autowired
    private CallbackService callbackService;

    /**
     * 微信支付回调
     */
    @ApiOperation(value = "微信支付回调")
    @RequestMapping(value = "/wechat", method = RequestMethod.POST)
    public String weChat(@RequestBody String  request) {
        System.out.println("微信支付回调 request ===> " + request);
        String response = callbackService.weChat(request);
        System.out.println("微信支付回调 response ===> " + response);
        return response;
    }

    /**
     * 支付宝支付回调
     */
    @ApiOperation(value = "支付宝支付回调 ")
    @RequestMapping(value = "/alipay", method = RequestMethod.POST)
    public String aliPay(HttpServletRequest request){
        //支付宝支付回调
        System.out.println("支付宝支付回调 request ===> " + request.getParameterMap());
        return callbackService.aliPay(request);
    }

    /**
     * 微信退款回调
     */
    @ApiOperation(value = "微信退款回调")
    @RequestMapping(value = "/wechat/refund", method = RequestMethod.POST)
    public String weChatRefund(@RequestBody String request) {
        System.out.println("微信退款回调 request ===> " + request);
        String response = callbackService.weChatRefund(request);
        System.out.println("微信退款回调 response ===> " + response);
        return response;
    }
}



