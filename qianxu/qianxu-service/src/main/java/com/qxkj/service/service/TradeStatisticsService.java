package com.qxkj.service.service;

import com.qxkj.common.response.TradeDataResponse;
import com.qxkj.common.response.TradingDataResponse;
import com.qxkj.common.response.TrandeTrendDateResponse;

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
public interface TradeStatisticsService {

    /**
     * 交易统计数据
     * @return TradeDataResponse
     */
    TradeDataResponse getData();

    /**
     * 交易统计概览
     * @param dateLimit 时间参数
     * @return TradingDataResponse
     */
    TradingDataResponse getOverview(String dateLimit);

    /**
     * 交易趋势数据
     * @param dateLimit 时间参数
     * @return List
     */
    List<TrandeTrendDateResponse> getTrendDataByDate(String dateLimit);
}
