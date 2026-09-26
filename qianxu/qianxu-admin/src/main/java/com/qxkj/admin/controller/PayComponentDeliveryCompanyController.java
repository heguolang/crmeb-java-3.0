package com.qxkj.admin.controller;

import com.qxkj.common.model.wechat.video.PayComponentDeliveryCompany;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.PayComponentDeliveryCompanyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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
@RequestMapping("api/admin/pay/component/delivery/company")
@Api(tags = "自定义交易组件—组件快递公司")
public class PayComponentDeliveryCompanyController {

    @Autowired
    private PayComponentDeliveryCompanyService payComponentDeliveryCompanyService;

    /**
     * 获取组件快递
     */
    @PreAuthorize("hasAuthority('admin:pay:component:delivery:company:get')")
    @ApiOperation(value = "获取组件快递（测试用，前端不调用）")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public CommonResult<Object> get(){
        payComponentDeliveryCompanyService.updateData();
        return CommonResult.success();
    }

    /**
     * 获取组件快递列表
     */
    @PreAuthorize("hasAuthority('admin:pay:component:delivery:company:get:list')")
    @ApiOperation(value = "获取组件快递")
    @RequestMapping(value = "/get/list", method = RequestMethod.GET)
    public CommonResult<List<PayComponentDeliveryCompany>> getList(){
        return CommonResult.success(payComponentDeliveryCompanyService.getList());
    }
}



