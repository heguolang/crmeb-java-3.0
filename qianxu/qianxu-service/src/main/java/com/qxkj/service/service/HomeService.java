package com.qxkj.service.service;

import com.qxkj.common.response.HomeOperatingDataResponse;
import com.qxkj.common.response.HomeRateResponse;

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
public interface HomeService{

    /**
     * 用户曲线图
     */
    Map<Object, Object> chartUser();

    /**
     * 30天订单量趋势
     */
    Map<String, Object> chartOrder();

    /**
     * 用户购买统计
     */
    Map<String, Integer> chartUserBuy();

    /**
     * 周订单量趋势
     */
    Map<String, Object> chartOrderInWeek();

    /**
     * 月订单量趋势
     */
    Map<String, Object> chartOrderInMonth();

    /**
     * 年订单量趋势
     */
    Map<String, Object> chartOrderInYear();

    /**
     * 首页数据
     * @return HomeRateResponse
     */
    HomeRateResponse indexDate();

    /**
     * 经营数据
     * @return HomeOperatingDataResponse
     */
    HomeOperatingDataResponse operatingData();
}
