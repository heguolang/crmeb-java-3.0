package com.qxkj.admin.controller;

import com.qxkj.common.response.*;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.UserStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("api/admin/statistics/user")
@Api(tags = "用户统计")
public class UserStatisticsController {

    @Autowired
    private UserStatisticsService statisticsService;

    @PreAuthorize("hasAuthority('admin:statistics:user:total:data')")
    @ApiOperation(value = "用户总数据")
    @RequestMapping(value = "/total/data", method = RequestMethod.GET)
    public CommonResult<UserTotalResponse> getTotalDate() {
        return CommonResult.success(statisticsService.getTotalDate());
    }

    @PreAuthorize("hasAuthority('admin:statistics:user:overview')")
    @ApiOperation(value = "用户概览")
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public CommonResult<UserOverviewResponse> getOverview(@RequestParam(value = "dateLimit", defaultValue = "") String dateLimit) {
        return CommonResult.success(statisticsService.getOverview(dateLimit));
    }

    @PreAuthorize("hasAuthority('admin:statistics:user:sex')")
    @ApiOperation(value = "用户性别数据")
    @RequestMapping(value = "/sex", method = RequestMethod.GET)
    public CommonResult<List<UserSexDataResponse>> getSexData() {
        return CommonResult.success(statisticsService.getSexData());
    }

    @PreAuthorize("hasAuthority('admin:statistics:user:channel')")
    @ApiOperation(value = "用户渠道数据")
    @RequestMapping(value = "/channel", method = RequestMethod.GET)
    public CommonResult<List<UserChannelDataResponse>> getChannelData() {
        return CommonResult.success(statisticsService.getChannelData());
    }

    @PreAuthorize("hasAuthority('admin:statistics:user:area')")
    @ApiOperation(value = "用户区域数据")
    @RequestMapping(value = "/area", method = RequestMethod.GET)
    public CommonResult<List<UserAreaDataResponse>> getAreaData() {
        return CommonResult.success(statisticsService.getAreaData());
    }

    @PreAuthorize("hasAuthority('admin:statistics:user:overview:list')")
    @ApiOperation(value = "用户概览列表")
    @RequestMapping(value = "/overview/list", method = RequestMethod.GET)
    public CommonResult<List<UserOverviewDateResponse>> getOverviewList(@RequestParam(value = "dateLimit", defaultValue = "") String dateLimit) {
        return CommonResult.success(statisticsService.getOverviewList(dateLimit));
    }
}
