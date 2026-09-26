package com.qxkj.admin.controller;

import cn.hutool.core.util.ObjectUtil;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.system.SystemFormTemp;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemFormTempRequest;
import com.qxkj.common.request.SystemFormTempSearchRequest;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.result.SystemConfigResultCode;
import com.qxkj.service.service.SystemFormTempService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;


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
@RequestMapping("api/admin/system/form/temp")
@Api(tags = "设置 -- 表单模板")
public class SystemFormTempController {

    @Autowired
    private SystemFormTempService systemFormTempService;

    /**
     * 分页显示表单模板
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:system:form:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemFormTemp>> getList(@Validated SystemFormTempSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<SystemFormTemp> systemFormTempCommonPage = CommonPage.restPage(systemFormTempService.getList(request, pageParamRequest));
        return CommonResult.success(systemFormTempCommonPage);
    }

    /**
     * 新增表单模板
     * @param systemFormTempRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:system:form:save')")
    @ApiOperation(value = "新增")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增表单模板")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated SystemFormTempRequest systemFormTempRequest) {
        if (systemFormTempService.add(systemFormTempRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 修改表单模板
     * @param id integer id
     * @param systemFormTempRequest 修改参数
     */
    @PreAuthorize("hasAuthority('admin:system:form:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改表单模板")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated SystemFormTempRequest systemFormTempRequest) {
        if (systemFormTempService.edit(id, systemFormTempRequest)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    /**
     * 查询表单模板信息
     * @param id Integer
     */
    @PreAuthorize("hasAuthority('admin:system:form:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemFormTemp> info(@RequestParam(value = "id") Integer id) {
        SystemFormTemp systemFormTemp = systemFormTempService.getById(id);
        return CommonResult.success(systemFormTemp);
   }

    @PreAuthorize("hasAuthority('admin:system:form:name:info')")
    @ApiOperation(value = "通过名称查询详情")
    @RequestMapping(value = "/name/info", method = RequestMethod.GET)
    @ApiImplicitParam(name="name", value="表单模板Name", required = true)
    public SystemFormTemp nameInfo(@NotBlank(message = "name不能为空") @RequestParam(value = "name") String name) {
        SystemFormTemp temp = systemFormTempService.getOneByName(name);
        if (ObjectUtil.isNull(temp)) {
            throw new QianxuException(SystemConfigResultCode.FORM_TEMP_NOT_EXIST);
        }
        return temp;
    }

}



