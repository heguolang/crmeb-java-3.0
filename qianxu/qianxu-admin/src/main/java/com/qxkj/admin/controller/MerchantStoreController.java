package com.qxkj.admin.controller;

import com.qxkj.common.model.merchant.MerchantStoreVerifyRecord;
import com.qxkj.common.model.system.SystemStore;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.MerchantStoreRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.MerchantStoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店系统控制器（后台）
 */
@Slf4j
@RestController
@RequestMapping("api/admin/merchantStore")
@Api(tags = "门店系统 -- 后台管理")
public class MerchantStoreController {

    @Autowired
    private MerchantStoreService merchantStoreService;

    @PreAuthorize("hasAuthority('admin:merchant:store:list')")
    @ApiOperation(value = "门店分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<SystemStore>> list(
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "status", required = false) Integer status,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(merchantStoreService.adminPage(keywords, status, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:merchant:store:list')")
    @ApiOperation(value = "门店详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<SystemStore> info(@RequestParam Integer id) {
        return CommonResult.success(merchantStoreService.detail(id));
    }

    @PreAuthorize("hasAuthority('admin:merchant:store:save')")
    @ApiOperation(value = "新增门店")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated MerchantStoreRequest request) {
        if (merchantStoreService.saveStore(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:merchant:store:update')")
    @ApiOperation(value = "编辑门店")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated MerchantStoreRequest request) {
        if (merchantStoreService.updateStore(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:merchant:store:update')")
    @ApiOperation(value = "启用/禁用门店")
    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public CommonResult<String> status(@RequestParam Integer id, @RequestParam Boolean isShow) {
        if (merchantStoreService.updateShow(id, isShow)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:merchant:store:delete')")
    @ApiOperation(value = "删除门店")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<String> delete(@RequestParam Integer id) {
        if (merchantStoreService.deleteStore(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:merchant:verify:list')")
    @ApiOperation(value = "门店核销记录分页")
    @RequestMapping(value = "/verify/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MerchantStoreVerifyRecord>> verifyList(
            @RequestParam(value = "storeId", required = false) Integer storeId,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(merchantStoreService.verifyRecords(storeId, orderNo, pageParamRequest));
    }
}
