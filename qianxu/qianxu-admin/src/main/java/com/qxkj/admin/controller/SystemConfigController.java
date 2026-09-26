package com.qxkj.admin.controller;

import com.qxkj.common.model.system.SystemConfig;
import com.qxkj.common.request.SaveConfigRequest;
import com.qxkj.common.request.SystemFormCheckRequest;
import com.qxkj.common.response.AdminSiteLogoResponse;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


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
@RequestMapping("api/admin/system/config")
@Api(tags = "设置 -- Config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @PreAuthorize("hasAuthority('admin:system:config:info')")
    @ApiOperation(value = "表单详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<HashMap<String, String>> info(@RequestParam(value = "formId") Integer formId) {
        return CommonResult.success(systemConfigService.info(formId));
    }

    @PreAuthorize("hasAuthority('admin:system:config:save:form')")
    @ApiOperation(value = "整体保存表单数据")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "保存系统配置")
    @RequestMapping(value = "/save/form", method = RequestMethod.POST)
    public CommonResult<String> saveFrom(@RequestBody @Validated SystemFormCheckRequest systemFormCheckRequest) {
        if (systemConfigService.saveForm(systemFormCheckRequest)) {
            return CommonResult.success("表单保存成功");
        }
        return CommonResult.failed("表单保存失败");
    }

    @PreAuthorize("hasAuthority('admin:system:config:upload:type')")
    @ApiOperation(value = "获取文件存储类型")
    @RequestMapping(value = "/get/upload/type", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getFileUploadType() {
        return CommonResult.success(systemConfigService.getFileUploadType());
    }

//    @PreAuthorize("hasAuthority('admin:system:config:site:logo')")
    @ApiOperation(value = "获取管理端logo")
    @RequestMapping(value = "/get/site/logo", method = RequestMethod.GET)
    public CommonResult<AdminSiteLogoResponse> getSiteLogo() {
        return CommonResult.success(systemConfigService.getSiteLogo());
    }

    @PreAuthorize("hasAuthority('admin:system:config:tx:map:key')")
    @ApiOperation(value = "获取腾讯地图key")
    @RequestMapping(value = "/get/tx/map/key", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getTxMapKey() {
        return CommonResult.success(systemConfigService.getTxMapKey());
    }

    @PreAuthorize("hasAuthority('admin:system:config:home:page:list:style')")
    @ApiOperation(value = "获取移动端首页列表样式")
    @RequestMapping(value = "/get/home/page/list/style", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getHomePageSaleListStyle() {
        return CommonResult.success(systemConfigService.getHomePageSaleListStyle());
    }

    @PreAuthorize("hasAuthority('admin:system:config:mini:download:url')")
    @ApiOperation(value = "获取小程序下载地址")
    @RequestMapping(value = "/get/mini/download/url", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getMiniDownloadUrl() {
        return CommonResult.success(systemConfigService.getMiniDownloadUrl());
    }

    @PreAuthorize("hasAuthority('admin:system:config:home:page:list:style:save')")
    @ApiOperation(value = "保存移动端首页列表样式")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "保存移动端首页列表样式")
    @RequestMapping(value = "/save/home/page/list/style", method = RequestMethod.POST)
    public CommonResult<String> saveHomePageSaleListStyle(@RequestBody SaveConfigRequest request) {
        if (systemConfigService.saveHomePageSaleListStyle(request)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

    @PreAuthorize("hasAuthority('admin:system:config:auth:host:get')")
    @ApiOperation(value = "获取授权地址")
    @RequestMapping(value = "/get/auth/host", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getAuthHost() {
        return CommonResult.success(systemConfigService.getAuthHost());
    }

    @PreAuthorize("hasAuthority('admin:system:config:change:color:get')")
    @ApiOperation(value = "获取主题色")
    @RequestMapping(value = "/get/change/color", method = RequestMethod.GET)
    public CommonResult<SystemConfig> getChangeColor() {
        return CommonResult.success(systemConfigService.getChangeColor());
    }

    @PreAuthorize("hasAuthority('admin:system:config:change:color:save')")
    @ApiOperation(value = "保存主题色")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "保存主题色")
    @RequestMapping(value = "/save/change/color", method = RequestMethod.POST)
    public CommonResult<String> saveChangeColor(@RequestBody SaveConfigRequest request) {
        if (systemConfigService.saveChangeColor(request)) {
            return CommonResult.success("保存成功");
        }
        return CommonResult.failed("保存失败");
    }

    @PreAuthorize("hasAuthority('admin:system:config:clear:cache')")
    @ApiOperation(value = "清除config缓存")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "清除系统配置缓存")
    @RequestMapping(value = "/clear/cache", method = RequestMethod.POST)
    public CommonResult<String> clearCache() {
        if (systemConfigService.clearCache()) {
            return CommonResult.success("清除成功");
        }
        return CommonResult.failed("清除失败");
    }
}



