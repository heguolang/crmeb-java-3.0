package com.qxkj.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.request.onepass.OnePassLoginRequest;
import com.qxkj.common.request.onepass.OnePassShipmentCancelOrderRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.OnePassService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RestController
@RequestMapping("api/admin/pass")
@Api(tags = "一号通服务")
public class OnePassController {

    @Autowired
    private OnePassService onePassService;


    @PreAuthorize("hasAuthority('admin:pass:appsave')")
    @ApiOperation(value = "一号通 应用保存")
    @RequestMapping(value = "/appsave", method = RequestMethod.POST)
    public CommonResult<Boolean> saveOnePassApplication(@Validated @RequestBody OnePassLoginRequest request) {
        return CommonResult.success(onePassService.saveOnePassApplicationInfo(request));
    }

    @PreAuthorize("hasAuthority('admin:pass:appget')")
    @ApiOperation(value = "一号通 应用详情获取")
    @RequestMapping(value = "/appget", method = RequestMethod.GET)
    public CommonResult<OnePassLoginRequest> getOnePassApplication() {
        return CommonResult.success(onePassService.getOnePassApplicationInfo());
    }

    @PreAuthorize("hasAuthority('admin:pass:shipment:cancel')")
    @ApiOperation(value = "一号通 取消商家寄件")
    @RequestMapping(value = "/shipment/cancel", method = RequestMethod.POST)
    public CommonResult<JSONObject> onePassShipmentCancel(OnePassShipmentCancelOrderRequest request) {
        return CommonResult.success(onePassService.shipmentCancelOrder(request));
    }

    @PreAuthorize("hasAuthority('admin:pass:shipment:express')")
    @ApiOperation(value = "一号通 商家寄件 快递列表")
    @RequestMapping(value = "/shipment/express", method = RequestMethod.GET)
    public CommonResult<JSONObject> onePassShipmentExpress() {
        return CommonResult.success(onePassService.shipmentComs());
    }

    @PreAuthorize("hasAuthority('admin:pass:shipment:callback')")
    @ApiOperation(value = "一号通 商家寄件 回调地址")
    @RequestMapping(value = "/shipment/callback", method = RequestMethod.POST)
    public CommonResult<Boolean> onePassShipmentCallBack(String type, String data) {
        return CommonResult.success(onePassService.shipmentCallBackMethod(type, data));
    }

}
