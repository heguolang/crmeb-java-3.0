package com.zbkj.admin.controller;

import com.zbkj.common.model.log.AdminLoginLog;
import com.zbkj.common.model.log.SensitiveMethodLog;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.AdminLoginLogSearchRequest;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.AdminLoginLogService;
import com.zbkj.service.service.SensitiveMethodLogService;
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
 * 日志控制器
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
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

    /**
     * 删除管理员登录日志
     */
    @PreAuthorize("hasAuthority('admin:log:login:delete')")
    @ApiOperation(value = "删除管理员登录日志")
    @ApiImplicitParam(name = "id", value = "日志id", dataType = "integer")
    @RequestMapping(value = "/login/delete", method = RequestMethod.GET)
    public CommonResult<String> deleteLoginLog(@RequestParam(value = "id") Integer id) {
        if (adminLoginLogService.deleteById(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 清空管理员登录日志
     */
    @PreAuthorize("hasAuthority('admin:log:login:delete')")
    @ApiOperation(value = "清空管理员登录日志")
    @RequestMapping(value = "/login/clear", method = RequestMethod.GET)
    public CommonResult<String> clearLoginLog() {
        if (adminLoginLogService.clearAll()) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 清空敏感操作日志
     */
    @PreAuthorize("hasAuthority('admin:log:sensitive:delete')")
    @ApiOperation(value = "清空敏感操作日志")
    @RequestMapping(value = "/sensitive/clear", method = RequestMethod.GET)
    public CommonResult<String> clearSensitiveLog() {
        if (sensitiveMethodLogService.clearAll()) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

}
