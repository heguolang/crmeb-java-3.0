package com.qxkj.front.controller;


import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemCityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RestController("CityFrontController")
@RequestMapping("api/front/city")
@Api(tags = "城市服务")
public class CityController {

    @Autowired
    private SystemCityService systemCityService;

    /**
     * 城市服务树形结构数据
     */
    @ApiOperation(value = "树形结构")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<Object> register(){
        return CommonResult.success(systemCityService.getListTree());
    }
}



