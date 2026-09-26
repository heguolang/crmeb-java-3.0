package com.qxkj.admin.controller;

import com.qxkj.common.model.system.SystemGroup;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemGroupRequest;
import com.qxkj.common.request.SystemGroupSearchRequest;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemGroupService;
import io.swagger.annotations.Api;
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
@RequestMapping("api/admin/system/group")
@Api(tags = "设置 -- 组合数据")
public class SystemGroupController {

    @Autowired
    private SystemGroupService systemGroupService;

    /**
     * 分页显示组合数据表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:group:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemGroup>> getList(@Validated SystemGroupSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<SystemGroup> systemGroupCommonPage = CommonPage.restPage(systemGroupService.getList(request, pageParamRequest));
        return CommonResult.success(systemGroupCommonPage);
    }

    /**
     * 新增组合数据
     * @param systemGroupRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:system:group:save')")
    @ApiOperation(value = "新增")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增组合数据分组")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@Validated SystemGroupRequest systemGroupRequest) {
        if (systemGroupService.add(systemGroupRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除组合数据表
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:group:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除组合数据分组")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (systemGroupService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改组合数据表
     * @param id integer id
     * @param systemGroupRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:system:group:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改组合数据分组")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @Validated SystemGroupRequest systemGroupRequest) {
        if (systemGroupService.edit(id, systemGroupRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 查询组合数据表信息
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:group:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemGroup> info(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(systemGroupService.getById(id));
   }
}



