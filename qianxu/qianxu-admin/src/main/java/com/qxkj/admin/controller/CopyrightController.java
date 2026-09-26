package com.qxkj.admin.controller;

import com.qxkj.admin.copyright.CopyrightInfoResponse;
import com.qxkj.admin.copyright.CopyrightUpdateInfoRequest;
import com.qxkj.admin.service.CopyrightService;
import com.qxkj.common.result.CommonResult;
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
@RequestMapping("api/admin/copyright")
@Api(tags = "版权控制器")
public class CopyrightController {

    @Autowired
    private CopyrightService copyrightService;

    @PreAuthorize("hasAuthority('admin:copyright:get:info')")
    @ApiOperation(value = "获取公司版权信息")
    @RequestMapping(value = "/get/info", method = RequestMethod.GET)
    public CommonResult<CopyrightInfoResponse> getInfo() {
        return CommonResult.success(copyrightService.getInfo());
    }

    @PreAuthorize("hasAuthority('admin:copyright:update:company:info')")
    @ApiOperation(value = "编辑公司名称与图片")
    @RequestMapping(value = "/update/company/info", method = RequestMethod.POST)
    public CommonResult<Object> updateCompanyInfo(@RequestBody @Validated CopyrightUpdateInfoRequest request) {
        if (copyrightService.updateCompanyInfo(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }
}
