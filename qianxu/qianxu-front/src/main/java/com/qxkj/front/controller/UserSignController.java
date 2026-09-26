package com.qxkj.front.controller;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.UserSignInfoResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.common.vo.SystemGroupDataSignConfigVo;
import com.qxkj.common.vo.UserSignMonthVo;
import com.qxkj.common.vo.UserSignVo;
import com.qxkj.service.service.UserSignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
@RequestMapping("api/front/user/sign")
@Api(tags = "用户 -- 签到")
public class UserSignController {

    @Autowired
    private UserSignService userSignService;

    /**
     * 签到列表
     * @param pageParamRequest 分页参数
     */
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserSignVo>> getList(@Validated PageParamRequest pageParamRequest) {
        CommonPage<UserSignVo> userSignCommonPage = CommonPage.restPage(userSignService.getList(pageParamRequest));
        return CommonResult.success(userSignCommonPage);
    }

    /**
     * 签到列表，年月纬度
     * @param pageParamRequest 分页参数
     */
    @ApiOperation(value = "分页列表")
    @RequestMapping(value = "/month", method = RequestMethod.GET)
    public CommonResult<CommonPage<UserSignMonthVo>>  getListGroupMonth(@Validated PageParamRequest pageParamRequest) {
        CommonPage<UserSignMonthVo> userSignCommonPage = CommonPage.restPage(userSignService.getListGroupMonth(pageParamRequest));
        return CommonResult.success(userSignCommonPage);
    }

    /**
     * 配置
     */
    @ApiOperation(value = "配置")
    @RequestMapping(value = "/config", method = RequestMethod.GET)
    public CommonResult<List<SystemGroupDataSignConfigVo>> config() {
        return CommonResult.success(userSignService.getSignConfig());
    }

    /**
     * 签到
     */
    @ApiOperation(value = "签到")
    @RequestMapping(value = "/integral", method = RequestMethod.GET)
    public CommonResult<SystemGroupDataSignConfigVo> info() {
        return CommonResult.success(userSignService.sign());
    }

    /**
     * 今日记录详情
     */
    @ApiOperation(value = "今日记录详情")
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public CommonResult<HashMap<String, Object>> get() {
        return CommonResult.success(userSignService.get());
    }

    /**
     * 签到用户信息
     */
    @ApiOperation(value = "签到用户信息")
    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public CommonResult<UserSignInfoResponse> getUserInfo() {
        return CommonResult.success(userSignService.getUserSignInfo());
    }
}



