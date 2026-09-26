package com.qxkj.admin.controller;


import com.qxkj.common.response.TradeDataResponse;
import com.qxkj.common.response.TradingDataResponse;
import com.qxkj.common.response.TrandeTrendDateResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.TradeStatisticsService;
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
@RequestMapping("api/admin/statistics/trade")
@Api(tags = "交易统计")
public class TradeStatisticsController {

    @Autowired
    private TradeStatisticsService statisticsService;

    @PreAuthorize("hasAuthority('admin:statistics:trade:data')")
    @ApiOperation(value = "交易统计数据")
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public CommonResult<TradeDataResponse> getData() {
        return CommonResult.success(statisticsService.getData());
    }

    @PreAuthorize("hasAuthority('admin:statistics:trade:overview')")
    @ApiOperation(value = "交易概览")
    @RequestMapping(value = "/overview", method = RequestMethod.GET)
    public CommonResult<TradingDataResponse> getOverview(@RequestParam(value = "dateLimit", defaultValue = "") String dateLimit) {
        return CommonResult.success(statisticsService.getOverview(dateLimit));
    }

    @PreAuthorize("hasAuthority('admin:statistics:trade:trend')")
    @ApiOperation(value = "交易趋势数据")
    @RequestMapping(value = "/trend", method = RequestMethod.GET)
    public CommonResult<List<TrandeTrendDateResponse>> getTrendDataByDate(@RequestParam(value = "dateLimit") String dateLimit) {
        return CommonResult.success(statisticsService.getTrendDataByDate(dateLimit));
    }
}
