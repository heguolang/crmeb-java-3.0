package com.qxkj.admin.controller;

import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.request.StoreProductGuaranteeRequest;
import com.qxkj.common.response.StoreProductGuaranteeResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreProductGuaranteeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@RequestMapping("api/admin/store/product/guarantee")
@Api(tags = "商品保障服务控制器")
public class StoreProductGuaranteeController {

    @Autowired
    private StoreProductGuaranteeService guaranteeService;

    @PreAuthorize("hasAuthority('admin:product:guarantee:list')")
    @ApiOperation(value = "保障服务列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<StoreProductGuaranteeResponse>> getList(@RequestParam(value = "isShow", defaultValue = "0") Boolean isShow) {
        return CommonResult.success(guaranteeService.getAdminList(isShow));
    }

    @PreAuthorize("hasAuthority('admin:product:guarantee:add')")
    @ApiOperation(value = "新增保障服务")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<String> add(@RequestBody @Validated StoreProductGuaranteeRequest request) {
        if (guaranteeService.add(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:product:guarantee:delete')")
    @ApiOperation(value = "删除保障服务")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult<String> delete(@PathVariable(value = "id") Integer id) {
        if (guaranteeService.delete(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:product:guarantee:update')")
    @ApiOperation(value = "修改保障服务")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated StoreProductGuaranteeRequest request) {
        if (guaranteeService.edit(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:product:guarantee:show:status')")
    @ApiOperation(value = "修改保障服务显示状态")
    @RequestMapping(value = "/update/show/{id}", method = RequestMethod.POST)
    public CommonResult<Object> updateShowStatus(@PathVariable(value = "id") Integer id) {
        if (guaranteeService.updateShowStatus(id)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

}
