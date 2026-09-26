package com.qxkj.admin.controller;

import com.qxkj.common.model.system.SystemStoreStaff;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemStoreStaffRequest;
import com.qxkj.common.response.SystemStoreStaffResponse;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.SystemStoreStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("api/admin/system/store/staff")
@Api(tags = "设置 -- 提货点 -- 核销员")
public class SystemStoreStaffController {

    @Autowired
    private SystemStoreStaffService systemStoreStaffService;

    /**
     * 分页显示门店核销员列表
     * @param storeId 门店id
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:staff:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemStoreStaffResponse>> getList(@RequestParam(name = "storeId", required = false, defaultValue = "0") Integer storeId,
                                                                      @ModelAttribute PageParamRequest pageParamRequest) {
        CommonPage<SystemStoreStaffResponse> systemStoreStaffCommonPage =
                CommonPage.restPage(systemStoreStaffService.getList(storeId, pageParamRequest));
        return CommonResult.success(systemStoreStaffCommonPage);
    }

    /**
     * 新增门店店员表
     * @param systemStoreStaffRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:system:staff:save')")
    @ApiOperation(value = "新增")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增门店店员")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @ModelAttribute SystemStoreStaffRequest systemStoreStaffRequest) {
        if (systemStoreStaffService.saveUnique(systemStoreStaffRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 删除门店店员表
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:staff:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除门店店员")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam(value = "id") Integer id) {
        if (systemStoreStaffService.removeById(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改门店店员表
     * @param id integer id
     * @param systemStoreStaffRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:system:staff:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改门店店员")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @ModelAttribute SystemStoreStaffRequest systemStoreStaffRequest) {
        if (systemStoreStaffService.edit(id, systemStoreStaffRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改门店店员表
     * @param id integer id
     * @param status 状态
     */
    @PreAuthorize("hasAuthority('admin:system:staff:update:status')")
    @ApiOperation(value = "修改状态")
    @RequestMapping(value = "/update/status", method = RequestMethod.GET)
    public CommonResult<String> updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        if (systemStoreStaffService.updateStatus(id, status)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 查询门店店员表信息
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:staff:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemStoreStaff> info(@RequestParam(value = "id") Integer id) {
        return CommonResult.success(systemStoreStaffService.getById(id));
   }
}



