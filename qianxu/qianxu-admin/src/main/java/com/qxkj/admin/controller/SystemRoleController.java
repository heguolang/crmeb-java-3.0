package com.qxkj.admin.controller;

import com.qxkj.common.model.system.SystemRole;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemRoleRequest;
import com.qxkj.common.request.SystemRoleSearchRequest;
import com.qxkj.common.response.RoleInfoResponse;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


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
@RequestMapping("api/admin/system/role")
@Api(tags = "设置 -- 权限管理 -- 身份管理")
public class SystemRoleController {

    @Autowired
    private SystemRoleService systemRoleService;

    /**
     * 分页显示身份管理表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:role:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemRole>> getList(@Validated SystemRoleSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<SystemRole> systemRoleCommonPage = CommonPage.restPage(systemRoleService.getList(request, pageParamRequest));
        return CommonResult.success(systemRoleCommonPage);
    }

    /**
     * 新增身份
     * @param systemRoleRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:system:role:save')")
    @ApiOperation(value = "新增身份")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增身份")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated SystemRoleRequest systemRoleRequest) {
        if (systemRoleService.add(systemRoleRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除身份管理表
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:role:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除身份")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (systemRoleService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改身份管理表
     * @param systemRoleRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:system:role:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改身份")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated SystemRoleRequest systemRoleRequest) {
        if (systemRoleService.edit(systemRoleRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 查询身份详情
     * @param id String
     */
    @PreAuthorize("hasAuthority('admin:system:role:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info/{id}", method = RequestMethod.GET)
    public CommonResult<RoleInfoResponse> info(@PathVariable Integer id) {
        return CommonResult.success(systemRoleService.getInfo(id));
   }

    /**
     * 修改身份状态
     */
    @PreAuthorize("hasAuthority('admin:system:role:update:status')")
    @ApiOperation(value = "修改身份状态")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改身份状态")
    @RequestMapping(value = "/updateStatus", method = RequestMethod.GET)
    public CommonResult<Object> updateStatus(@Validated @RequestParam(value = "id") Integer id, @Validated @RequestParam(value = "status") Boolean status) {
        if (systemRoleService.updateStatus(id, status)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }
}



