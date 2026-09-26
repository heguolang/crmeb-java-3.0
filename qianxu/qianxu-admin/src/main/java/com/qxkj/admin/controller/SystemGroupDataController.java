package com.qxkj.admin.controller;

import com.qxkj.common.model.system.SystemGroupData;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemGroupDataRequest;
import com.qxkj.common.request.SystemGroupDataSearchRequest;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemGroupDataService;
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
@RequestMapping("api/admin/system/group/data")
@Api(tags = "设置 -- 组合数据 -- 详情")
public class SystemGroupDataController {

    @Autowired
    private SystemGroupDataService systemGroupDataService;

    /**
     * 分页组合数据详情
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:group:data:list')")
    @ApiOperation(value = "分页组合数据详情")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemGroupData>> getList(@Validated SystemGroupDataSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<SystemGroupData> systemGroupDataCommonPage = CommonPage.restPage(systemGroupDataService.getList(request, pageParamRequest));
        return CommonResult.success(systemGroupDataCommonPage);
    }

    /**
     * 新增组合数据详情
     * @param systemGroupDataRequest SystemFormCheckRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:system:group:data:save')")
    @ApiOperation(value = "新增")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增组合数据")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated SystemGroupDataRequest systemGroupDataRequest) {
        if (systemGroupDataService.create(systemGroupDataRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除组合数据详情表
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:group:data:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除组合数据")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (systemGroupDataService.removeById(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改组合数据详情表
     * @param id integer id
     * @param request 修改参数
     */
    @PreAuthorize("hasAuthority('admin:system:group:data:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改组合数据")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated SystemGroupDataRequest request) {
        if (systemGroupDataService.update(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 组合数据详情信息
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:group:data:info')")
    @ApiOperation(value = "组合数据详情信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemGroupData> info(@RequestParam(value = "id") Integer id) {
        SystemGroupData systemGroupData = systemGroupDataService.getById(id);
        return CommonResult.success(systemGroupData);
    }
}



