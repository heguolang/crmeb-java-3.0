package com.qxkj.admin.controller;

import com.qxkj.common.model.record.ShoppingProductDayRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.ProductRankingRequest;
import com.qxkj.common.response.ProductRankingResponse;
import com.qxkj.common.response.ShoppingProductDataResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.ProductStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("api/admin/statistics/product")
@Api(tags = "商品统计")
public class ProductStatisticsController {

    @Autowired
    private ProductStatisticsService statisticsService;

    @PreAuthorize("hasAuthority('admin:statistics:product:data')")
    @ApiOperation(value = "商品统计数据")
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public CommonResult<ShoppingProductDataResponse> getDataByDate(@RequestParam(value = "dateLimit") String dateLimit) {
        return CommonResult.success(statisticsService.getDataByDate(dateLimit));
    }

    @PreAuthorize("hasAuthority('admin:statistics:product:ranking')")
    @ApiOperation(value = "商品排行榜")
    @RequestMapping(value = "/ranking", method = RequestMethod.GET)
    public CommonResult<CommonPage<ProductRankingResponse>> getRanking(@Validated ProductRankingRequest request) {
        return CommonResult.success(statisticsService.getRanking(request));
    }

    @PreAuthorize("hasAuthority('admin:statistics:product:trend')")
    @ApiOperation(value = "商品趋势数据")
    @RequestMapping(value = "/trend", method = RequestMethod.GET)
    public CommonResult<List<ShoppingProductDayRecord>> getTrendDataByDate(@RequestParam(value = "dateLimit") String dateLimit) {
        return CommonResult.success(statisticsService.getTrendDataByDate(dateLimit));
    }
}
