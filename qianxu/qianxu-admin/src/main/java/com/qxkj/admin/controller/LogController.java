package com.qxkj.admin.controller;

import com.qxkj.common.model.log.AdminLoginLog;
import com.qxkj.common.model.log.SensitiveMethodLog;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.AdminLoginLogSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.AdminLoginLogService;
import com.qxkj.service.service.SensitiveMethodLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("api/admin/log")
@Api(tags = "日志管理")
public class LogController {

    @Autowired
    private SensitiveMethodLogService sensitiveMethodLogService;

    @Autowired
    private AdminLoginLogService adminLoginLogService;

    /**
     * 敏感操作日志列表
     */
    @PreAuthorize("hasAuthority('admin:log:sensitive:list')")
    @ApiOperation(value = "敏感操作日志列表")
    @RequestMapping(value = "/sensitive/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SensitiveMethodLog>> getList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(sensitiveMethodLogService.getPageList(pageParamRequest)));
    }

    /**
     * 管理员登录日志列表
     */
    @PreAuthorize("hasAuthority('admin:log:login:list')")
    @ApiOperation(value = "管理员登录日志列表")
    @RequestMapping(value = "/login/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<AdminLoginLog>> getLoginList(@Validated AdminLoginLogSearchRequest request) {
        return CommonResult.success(CommonPage.restPage(adminLoginLogService.getPageList(request)));
    }

    /**
     * 管理员登录日志详情
     */
    @PreAuthorize("hasAuthority('admin:log:login:info')")
    @ApiOperation(value = "管理员登录日志详情")
    @ApiImplicitParam(name = "id", value = "日志id", dataType = "integer")
    @RequestMapping(value = "/login/info", method = RequestMethod.GET)
    public CommonResult<AdminLoginLog> getLoginInfo(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(adminLoginLogService.getById(id));
    }

    // 汪总要求（2026-09-18）：移除「清空日志」与登录日志单条删除，日志只保留查询
}
