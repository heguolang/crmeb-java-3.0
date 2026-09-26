package com.qxkj.common.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
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
@Data
public class StoreOrderStatisticsResponse {
    @ApiModelProperty(value = "订单数图标数据")
    private List<StoreOrderStatisticsChartItemResponse> chart; // 订单数图标数据

    @ApiModelProperty(value = "时间区间增长率")
    private Integer growthRate; // 时间区间增长率

    @ApiModelProperty(value = "同比")
    private String increaseTime;

    @ApiModelProperty(value = "同比上个时间区间增长营业额 1=增长，2=减少")
    private Integer increaseTimeStatus; // 同比上个时间区间增长营业额 1=增长，2=减少

    @ApiModelProperty(value = "时间区间订单数")
    private BigDecimal time;
}
