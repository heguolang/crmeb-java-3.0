package com.qxkj.service.service;

import com.qxkj.common.page.CommonPage;
import com.qxkj.common.model.record.ShoppingProductDayRecord;
import com.qxkj.common.request.ProductRankingRequest;
import com.qxkj.common.response.ProductRankingResponse;
import com.qxkj.common.response.ShoppingProductDataResponse;

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
public interface ProductStatisticsService {

    /**
     * 根据日期获取统计数据(商城维度)
     * @param dateLimit 日期参数
     * @return ShoppingProductDataResponse
     */
    ShoppingProductDataResponse getDataByDate(String dateLimit);

    /**
     * 获取商品排行榜
     * @param request 查询参数
     * @return CommonPage
     */
    CommonPage<ProductRankingResponse> getRanking(ProductRankingRequest request);

    /**
     * 商品趋势数据
     * @param dateLimit 日期参数
     * @return List
     */
    List<ShoppingProductDayRecord> getTrendDataByDate(String dateLimit);
}
