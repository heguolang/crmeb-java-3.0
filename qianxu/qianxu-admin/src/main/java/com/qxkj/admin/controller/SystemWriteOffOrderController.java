package com.qxkj.admin.controller;

import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemWriteOffOrderSearchRequest;
import com.qxkj.common.response.SystemWriteOffOrderResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("api/admin/system/store/order")
@Api(tags = "设置 -- 提货点 -- 核销订单") //配合swagger使用
public class SystemWriteOffOrderController {

    @Autowired
    private StoreOrderService storeOrderService;

    /**
     * 分页显示订单表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:order:list')")
    @ApiOperation(value = "分页列表") //配合swagger使用
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public CommonResult<SystemWriteOffOrderResponse> getList(
            @Validated SystemWriteOffOrderSearchRequest request,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(storeOrderService.getWriteOffList(request, pageParamRequest));
    }
}



