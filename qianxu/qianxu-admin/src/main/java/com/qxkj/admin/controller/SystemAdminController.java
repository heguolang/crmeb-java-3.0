package com.qxkj.admin.controller;

import com.qxkj.common.model.system.SystemAdmin;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemAdminAddRequest;
import com.qxkj.common.request.SystemAdminRequest;
import com.qxkj.common.request.SystemAdminUpdateRequest;
import com.qxkj.common.response.SystemAdminResponse;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


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
@RequestMapping("api/admin/system/admin")
@Api(tags = "后台用户服务")
public class SystemAdminController {

    @Autowired
    private SystemAdminService systemAdminService;

    /**
     * 分页显示后台管理员表
     * @param systemAdminRequest 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:admin:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<SystemAdminResponse>> getList(@Validated SystemAdminRequest systemAdminRequest, PageParamRequest pageParamRequest) {
        CommonPage<SystemAdminResponse> systemAdminCommonPage = CommonPage.restPage(systemAdminService.getList(systemAdminRequest, pageParamRequest));
        return CommonResult.success(systemAdminCommonPage);
    }

    /**
     * 新增后台管理员
     * @param systemAdminAddRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:system:admin:save')")
    @ApiOperation(value = "新增后台管理员")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增后台管理员")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<SystemAdminResponse> save(@RequestBody SystemAdminAddRequest systemAdminAddRequest) {
        if (systemAdminService.saveAdmin(systemAdminAddRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed("添加管理员失败");
    }

    /**
     * 删除后台管理员表
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:admin:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除后台管理员")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (systemAdminService.removeById(id)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 修改后台管理员表
     * @param systemAdminRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:system:admin:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改后台管理员")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody SystemAdminUpdateRequest systemAdminRequest) {
        if (systemAdminService.updateAdmin(systemAdminRequest)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 后台管理员详情
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:admin:info')")
    @ApiOperation(value = "后台管理员详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemAdmin> info(@RequestParam(value = "id") @Valid Integer id) {
        return CommonResult.success(systemAdminService.getDetail(id));
    }

    /**
     * 修改后台管理员状态
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:admin:update:status')")
    @ApiOperation(value = "修改后台管理员状态")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改后台管理员状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.GET)
    public CommonResult<Object> updateStatus(@RequestParam(value = "id") @Valid Integer id, @RequestParam(value = "status") @Valid Boolean status) {
        if (systemAdminService.updateStatus(id, status)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    /**
     * 修改后台管理员是否接收状态
     */
    @PreAuthorize("hasAuthority('admin:system:admin:update:sms')")
    @ApiOperation(value = "修改后台管理员是否接收状态")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改后台管理员短信接收状态")
    @RequestMapping(value = "/update/isSms", method = RequestMethod.GET)
    public CommonResult<Object> updateIsSms(@RequestParam(value = "id") @Valid Integer id) {
        if (systemAdminService.updateIsSms(id)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }
}



