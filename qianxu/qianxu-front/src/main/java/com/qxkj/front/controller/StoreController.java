package com.qxkj.front.controller;


import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.StoreNearRequest;
import com.qxkj.common.response.StoreNearResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController("StoreController")
@RequestMapping("api/front/store")
@Api(tags = "提货点")
public class StoreController {
    @Autowired
    private SystemStoreService systemStoreService;

    /**
     * 附近的提货点
     */
    @ApiOperation(value = "附近的提货点")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public CommonResult<StoreNearResponse> register(@Validated StoreNearRequest request, @Validated PageParamRequest pageParamRequest){
        return CommonResult.success(systemStoreService.getNearList(request, pageParamRequest));
    }
}



