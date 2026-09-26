package com.qxkj.front.controller;

import com.qxkj.common.request.OrderPayRequest;
import com.qxkj.common.response.OrderPayResultResponse;
import com.qxkj.common.response.PayConfigResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.service.service.AliPayService;
import com.qxkj.service.service.OrderPayService;
import com.qxkj.service.service.WeChatPayService;
import io.swagger.annotations.Api;
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
@RestController
@RequestMapping("api/front/pay")
@Api(tags = "支付管理")
public class PayController {

    @Autowired
    private WeChatPayService weChatPayService;

    @Autowired
    private OrderPayService orderPayService;

    @Autowired
    private AliPayService aliPayService;


    @ApiOperation(value = "获取支付配置")
    @RequestMapping(value = "/get/config", method = RequestMethod.GET)
    public CommonResult<PayConfigResponse> getPayConfig() {
        return CommonResult.success(orderPayService.getPayConfig());
    }

    /**
     * 订单支付
     */
    @ApiOperation(value = "订单支付")
    @RequestMapping(value = "/payment", method = RequestMethod.POST)
    public CommonResult<OrderPayResultResponse> payment(@RequestBody @Validated OrderPayRequest orderPayRequest, HttpServletRequest request) {
        String ip = QianxuUtil.getClientIp(request);
        return CommonResult.success(orderPayService.payment(orderPayRequest, ip));
    }

    /**
     * 查询支付结果
     *
     * @param orderNo |订单编号|String|必填
     */
    @ApiOperation(value = "查询支付结果")
    @RequestMapping(value = "/queryPayResult", method = RequestMethod.GET)
    public CommonResult<Boolean> queryPayResult(@RequestParam String orderNo) {
        return CommonResult.success(weChatPayService.queryPayResult(orderNo));
    }

    /**
     * 查询支付结果(支付宝)
     *
     * @param orderNo |订单编号|String|必填
     */
    @ApiOperation(value = "查询支付结果(支付宝)")
    @RequestMapping(value = "/queryAliPayResult", method = RequestMethod.GET)
    public CommonResult<Boolean> queryAliPayResult(@RequestParam String orderNo) {
        return CommonResult.success(aliPayService.queryPayResult(orderNo));
    }
}
