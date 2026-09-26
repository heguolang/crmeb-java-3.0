package com.qxkj.service.service;

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
public interface StatisticsTaskService {

    /**
     * 每天零点的自动统计
     */
    void autoStatistics();

    /**
     * 根据日期获取统计数据(商城维度)
     * @param dateLimit 日期参数
     * @return ShoppingProductDataResponse
     */
//    ShoppingProductDataResponse getDataByDate(String dateLimit);
}
