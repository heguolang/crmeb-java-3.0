package com.qxkj.front.controller;

import com.github.pagehelper.PageInfo;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.BargainFrontRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.*;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.StoreBargainService;
import com.qxkj.service.service.StoreBargainUserHelpService;
import com.qxkj.service.service.StoreBargainUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
@RequestMapping("api/front/bargain")
@Api(tags = "砍价商品")
public class BargainController {

    @Autowired
    private StoreBargainService storeBargainService;

    @Autowired
    private StoreBargainUserService storeBargainUserService;

    @Autowired
    private StoreBargainUserHelpService storeBargainUserHelpService;

    /**
     * 砍价首页信息
     */
    @ApiOperation(value = "砍价首页信息")
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public CommonResult<BargainIndexResponse> index(){
        return CommonResult.success(storeBargainService.getIndexInfo());
    }

    /**
     * 砍价商品列表header
     */
    @ApiOperation(value = "砍价商品列表header")
    @RequestMapping(value = "/header", method = RequestMethod.GET)
    public CommonResult<BargainHeaderResponse> header(){
        return CommonResult.success(storeBargainService.getHeader());
    }

    /**
     * 砍价商品列表
     * @return 砍价商品列表
     */
    @ApiOperation(value = "砍价商品列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<PageInfo<StoreBargainDetailResponse>> list(@ModelAttribute PageParamRequest pageParamRequest){
        return CommonResult.success(storeBargainService.getH5List(pageParamRequest));
    }

    /**
     * 获取用户砍价信息
     */
    @ApiOperation(value = "获取用户砍价信息")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public CommonResult<BargainUserInfoResponse> getBargainUserInfo(@ModelAttribute @Validated BargainFrontRequest bargainFrontRequest) {
        return CommonResult.success(storeBargainUserService.getBargainUserInfo(bargainFrontRequest));
    }

    /**
     * 砍价商品详情
     */
    @ApiOperation(value = "砍价商品详情")
    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public CommonResult<BargainDetailH5Response> detail(@PathVariable(value = "id") Integer id) {
        BargainDetailH5Response h5Detail = storeBargainService.getH5Detail(id);
        return CommonResult.success(h5Detail);
    }

    /**
     * 创建砍价活动
     */
    @ApiOperation(value = "创建砍价活动")
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public CommonResult<Map<String, Object>> start(@RequestBody @Validated BargainFrontRequest bargainFrontRequest) {
        return CommonResult.success(storeBargainService.start(bargainFrontRequest));
    }

    /**
     * 砍价
     */
    @ApiOperation(value = "砍价")
    @RequestMapping(value = "/help", method = RequestMethod.POST)
    public CommonResult<Map<String, Object>> help(@RequestBody @Validated BargainFrontRequest bargainFrontRequest) {
        return CommonResult.success(storeBargainUserHelpService.help(bargainFrontRequest));
    }

    /**
     * 砍价记录
     */
    @ApiOperation(value = "砍价记录")
    @RequestMapping(value = "/record", method = RequestMethod.GET)
    public CommonResult<CommonPage<BargainRecordResponse>> recordList(@ModelAttribute PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(storeBargainUserService.getRecordList(pageParamRequest)));
    }

}
