package com.qxkj.front.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserCollectAllRequest;
import com.qxkj.common.request.UserCollectRequest;
import com.qxkj.common.response.UserRelationResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreProductRelationService;
import io.swagger.annotations.Api;
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
@RequestMapping("api/front/collect")
@Api(tags = "用户 -- 点赞/收藏")
public class UserCollectController {

    @Autowired
    private StoreProductRelationService storeProductRelationService;

    /**
     * 我的收藏列表
     */
    @ApiOperation(value = "我的收藏列表")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserRelationResponse>> getList(@Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(storeProductRelationService.getUserList(pageParamRequest)));
    }

    /**
     * 添加收藏产品
     * @param request StoreProductRelationRequest 新增参数
     */
    @ApiOperation(value = "添加收藏产品")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<String> save(@RequestBody @Validated UserCollectRequest request) {
        if (storeProductRelationService.add(request)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 添加收藏产品
     * @param request UserCollectAllRequest 新增参数
     */
    @ApiOperation(value = "批量收藏")
    @RequestMapping(value = "/all", method = RequestMethod.POST)
    public CommonResult<String> all(@RequestBody @Validated UserCollectAllRequest request) {
        if (storeProductRelationService.all(request)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 取消收藏产品
     */
    @ApiOperation(value = "取消收藏产品")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult<String> delete(@RequestBody String requestJson) {
        if (storeProductRelationService.delete(requestJson)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }

    /**
     * 取消收藏产品(通过商品)
     */
    @ApiOperation(value = "取消收藏产品(通过商品)")
    @RequestMapping(value = "/cancel/{proId}", method = RequestMethod.POST)
    public CommonResult<String> cancel(@PathVariable Integer proId) {
        if (storeProductRelationService.deleteByProIdAndUid(proId)) {
            return CommonResult.success();
        } else {
            return CommonResult.failed();
        }
    }
}



