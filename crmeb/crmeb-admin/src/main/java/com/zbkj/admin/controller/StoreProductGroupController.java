package com.zbkj.admin.controller;

import com.zbkj.common.annotation.LogControllerAnnotation;
import com.zbkj.common.enums.MethodType;
import com.zbkj.common.model.product.StoreProductGroup;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StoreProductGroupBatchBindRequest;
import com.zbkj.common.request.StoreProductGroupBatchUnbindRequest;
import com.zbkj.common.request.StoreProductGroupRequest;
import com.zbkj.common.request.StoreProductGroupSearchRequest;
import com.zbkj.common.result.CommonResult;
import com.zbkj.service.service.StoreProductGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * 商品分组 后台控制器
 */
@Slf4j
@RestController
@RequestMapping("api/admin/store/product/group")
@Api(tags = "商品 -- 商品分组")
public class StoreProductGroupController {

    @Autowired
    private StoreProductGroupService storeProductGroupService;

    @PreAuthorize("hasAuthority('admin:store:product:group:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreProductGroup>> getList(@Validated StoreProductGroupSearchRequest request,
                                                               @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(storeProductGroupService.getAdminList(request, pageParamRequest));
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:list')")
    @ApiOperation(value = "启用分组简表（装修选分组）")
    @RequestMapping(value = "/simple/list", method = RequestMethod.GET)
    public CommonResult<List<StoreProductGroup>> simpleList() {
        return CommonResult.success(storeProductGroupService.getEnabledSimpleList());
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult<StoreProductGroup> info(@RequestParam Integer id) {
        return CommonResult.success(storeProductGroupService.getInfo(id));
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:save')")
    @ApiOperation(value = "新增")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.ADD, description = "新增商品分组")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated StoreProductGroupRequest request) {
        if (storeProductGroupService.create(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:update')")
    @ApiOperation(value = "修改")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "修改商品分组")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestParam Integer id, @RequestBody @Validated StoreProductGroupRequest request) {
        if (storeProductGroupService.edit(id, request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:update')")
    @ApiOperation(value = "更新状态")
    @RequestMapping(value = "/status", method = RequestMethod.POST)
    public CommonResult<String> status(@RequestParam Integer id, @RequestParam Boolean status) {
        if (storeProductGroupService.updateStatus(id, status)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:delete')")
    @ApiOperation(value = "删除")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.DELETE, description = "删除商品分组")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public CommonResult<String> delete(@RequestParam Integer id) {
        if (storeProductGroupService.deleteGroup(id)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:update')")
    @ApiOperation(value = "批量将商品加入分组")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "批量将商品加入分组")
    @RequestMapping(value = "/batch/bind", method = RequestMethod.POST)
    public CommonResult<String> batchBind(@RequestBody @Validated StoreProductGroupBatchBindRequest request) {
        if (storeProductGroupService.batchBindProducts(request.getProductIds(), request.getGroupIds())) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:update')")
    @ApiOperation(value = "批量将商品移出分组")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "批量将商品移出分组")
    @RequestMapping(value = "/batch/unbind", method = RequestMethod.POST)
    public CommonResult<String> batchUnbind(@RequestBody @Validated StoreProductGroupBatchUnbindRequest request) {
        if (storeProductGroupService.batchUnbindProducts(request.getProductIds(), request.getGroupIds())) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:config')")
    @ApiOperation(value = "获取全局配置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> getConfig() {
        return CommonResult.success(storeProductGroupService.getConfig());
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:config')")
    @ApiOperation(value = "保存全局配置")
    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "保存商品分组全局配置")
    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public CommonResult<String> updateConfig(@RequestBody HashMap<String, Object> config) {
        if (storeProductGroupService.updateConfig(config)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:store:product:group:update')")
    @ApiOperation(value = "获取分组装修页ID（不存在则创建）")
    @RequestMapping(value = "/theme/{id}", method = RequestMethod.POST)
    public CommonResult<Integer> theme(@PathVariable(value = "id") Integer id) {
        return CommonResult.success(storeProductGroupService.ensureTheme(id));
    }
}
