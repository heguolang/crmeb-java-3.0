package com.qxkj.front.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserCouponReceiveRequest;
import com.qxkj.common.response.StoreCouponUserResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreCouponUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("api/front/coupon")
@Api(tags = "营销 -- 优惠券")
public class UserCouponController {

    @Autowired
    private StoreCouponUserService storeCouponUserService;

    /**
     * 我的优惠券
     */
    @ApiOperation(value = "我的优惠券")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name="type", value="类型，usable-可用，unusable-不可用", required = true),
            @ApiImplicitParam(name="page", value="页码", required = true),
            @ApiImplicitParam(name="limit", value="每页数量", required = true)
    })
    public CommonResult<CommonPage<StoreCouponUserResponse>> getList(@RequestParam(value = "type") String type,
                                                                     @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(storeCouponUserService.getMyCouponList(type, pageParamRequest));
    }

    /**
     * 领券
     * @param request UserCouponReceiveRequest 新增参数
     */
    @ApiOperation(value = "领券")
    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public CommonResult<String> receive(@RequestBody @Validated UserCouponReceiveRequest request) {
        if (storeCouponUserService.receiveCoupon(request)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

}



