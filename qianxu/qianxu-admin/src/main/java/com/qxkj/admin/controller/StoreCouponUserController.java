package com.qxkj.admin.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.StoreCouponUserRequest;
import com.qxkj.common.request.StoreCouponUserSearchRequest;
import com.qxkj.common.response.StoreCouponUserResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreCouponUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("api/admin/marketing/coupon/user")
@Api(tags = "营销 -- 优惠券 -- 领取记录")
public class StoreCouponUserController {

    @Autowired
    private StoreCouponUserService storeCouponUserService;

    /**
     * 分页显示优惠券发放记录表
     * @param request 搜索条件
     */
    @PreAuthorize("hasAuthority('admin:coupon:user:list')")
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<StoreCouponUserResponse>> getList(@Validated StoreCouponUserSearchRequest request) {
        CommonPage<StoreCouponUserResponse> storeCouponUserCommonPage = CommonPage.restPage(storeCouponUserService.getList(request));
        return CommonResult.success(storeCouponUserCommonPage);
    }

    /**
     * 领券
     * @param storeCouponUserRequest 新增参数
     */
    @PreAuthorize("hasAuthority('admin:coupon:user:receive')")
    @ApiOperation(value = "领券")
    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public CommonResult<String> receive(@Validated StoreCouponUserRequest storeCouponUserRequest) {
        if(storeCouponUserService.receive(storeCouponUserRequest)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }
}



