package com.qxkj.admin.controller;

import com.qxkj.common.model.coupon.StoreCoupon;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SearchAndPageRequest;
import com.qxkj.common.request.StoreCouponRequest;
import com.qxkj.common.request.StoreCouponSearchRequest;
import com.qxkj.common.response.StoreCouponInfoResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreCouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
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
@RequestMapping("api/admin/marketing/coupon")
@Api(tags = "营销 -- 优惠券")
public class StoreCouponController {

    @Autowired
    private StoreCouponService storeCouponService;

    /**
     * 分页显示优惠券表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    @PreAuthorize("hasAuthority('admin:coupon:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreCoupon>> getList(@Validated StoreCouponSearchRequest request, @Validated PageParamRequest pageParamRequest) {
        CommonPage<StoreCoupon> storeCouponCommonPage = CommonPage.restPage(storeCouponService.getList(request, pageParamRequest));
        return CommonResult.success(storeCouponCommonPage);
    }

    /**
     * 保存优惠券表
     * @param request StoreCouponRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:coupon:save')")
    @ApiOperation(value = "新增")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated StoreCouponRequest request) {
        if (storeCouponService.create(request)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 是否有效
     * @param id integer id
     */
    @PreAuthorize("hasAuthority('admin:coupon:update:status')")
    @ApiOperation(value = "修改")
    @RequestMapping(value = "/update/status", method = RequestMethod.POST)
    public CommonResult<String> updateStatus(@RequestParam Integer id, @RequestParam Boolean status) {
        if (storeCouponService.updateStatus(id, status)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 详情
     * @param id integer id
     */
    @PreAuthorize("hasAuthority('admin:coupon:info')")
    @ApiOperation(value = "详情")
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ApiImplicitParam(name="id", value="优惠券ID", required = true)
    public CommonResult<StoreCouponInfoResponse> info(@RequestParam Integer id) {
        return CommonResult.success(storeCouponService.info(id));
    }

    /**
     * 发送优惠券列表
     * @param searchAndPageRequest 搜索分页参数
     */
    @PreAuthorize("hasAuthority('admin:coupon:send:list')")
    @ApiOperation(value = "发送优惠券列表")
    @RequestMapping(value = "/send/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreCoupon>>  getSendList(@Validated SearchAndPageRequest searchAndPageRequest) {
        CommonPage<StoreCoupon> storeCouponCommonPage = CommonPage.restPage(storeCouponService.getSendList(searchAndPageRequest));
        return CommonResult.success(storeCouponCommonPage);
    }

    /**
     * 删除优惠券
     * @param id 优惠券id
     */
    @PreAuthorize("hasAuthority('admin:coupon:delete')")
    @ApiOperation(value = "删除优惠券")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<StoreCouponInfoResponse> delete(@RequestParam Integer id) {
        if (storeCouponService.delete(id)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed("删除失败");
        }
    }
}



