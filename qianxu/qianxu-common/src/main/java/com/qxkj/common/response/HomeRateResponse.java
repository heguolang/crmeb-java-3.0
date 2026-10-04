package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="HomeRateResponse对象", description="主页统计数据对象")
public class HomeRateResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "今日销售额")
    private Object sales;

    @ApiModelProperty(value = "昨日销售额")
    private Object yesterdaySales;

    @ApiModelProperty(value = "今日访问量")
    private Object pageviews;

    @ApiModelProperty(value = "昨日访问量")
    private Object yesterdayPageviews;

    @ApiModelProperty(value = "今日订单量")
    private Object orderNum;

    @ApiModelProperty(value = "昨日订单量")
    private Object yesterdayOrderNum;

    @ApiModelProperty(value = "今日新增用户")
    private Object newUserNum;

    @ApiModelProperty(value = "昨日新增用户")
    private Object yesterdayNewUserNum;

    // ==================== 业绩统计（2026-10-04 新增，纯增量）====================
    // 口径说明：全部只统计商城订单 eb_store_order，且只算 paid=1（已付款）的订单。
    // ⚠️ 时间维度统一用 create_time，**不要用 pay_time** —— 未付款订单的 pay_time 为 NULL，
    //    用 pay_time 会把「当天已下单但未付款」的订单整批漏掉，账目对不上。
    // 原有 sales/yesterdaySales/pageviews/... 八个字段与算法**一行未改**，保证老接口行为不变。

    @ApiModelProperty(value = "总业绩GMV（累计已付款订单实付金额，单位元）")
    private BigDecimal totalGmv;

    @ApiModelProperty(value = "本月GMV（自然月，按 create_time 的 %Y-%m 统计，单位元）")
    private BigDecimal monthGmv;

    @ApiModelProperty(value = "上月GMV（环比基数，单位元）")
    private BigDecimal lastMonthGmv;

    @ApiModelProperty(value = "本年GMV（按 create_time 的年份统计，单位元）")
    private BigDecimal yearGmv;

    @ApiModelProperty(value = "实付净额（GMV 减退款金额，单位元）")
    private BigDecimal netGmv;

    @ApiModelProperty(value = "优惠让利合计（优惠券+抵扣，单位元）")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "退款金额合计（单位元）")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款单数（refund_status in 1,2,3）")
    private Integer refundCount;

    @ApiModelProperty(value = "退款率（百分比，两位小数）")
    private BigDecimal refundRate;

    @ApiModelProperty(value = "改价单数（is_alter_price=1，需人工确认）")
    private Integer alterPriceCount;

    @ApiModelProperty(value = "累计订单量（已付款）")
    private Integer totalOrderNum;

    @ApiModelProperty(value = "本月订单量（已付款）")
    private Integer monthOrderNum;

    @ApiModelProperty(value = "上月订单量（环比基数）")
    private Integer lastMonthOrderNum;

    @ApiModelProperty(value = "客单价（GMV / 订单量，单位元）")
    private BigDecimal avgOrderPrice;

    @ApiModelProperty(value = "上月客单价（环比基数，单位元）")
    private BigDecimal lastMonthAvgOrderPrice;

    @ApiModelProperty(value = "累计支付用户数（distinct uid）")
    private Integer paidUserCount;

    @ApiModelProperty(value = "上月GMV环比增长率（百分比，可为负）")
    private BigDecimal monthGmvRatio;

    @ApiModelProperty(value = "本月订单量环比增长率（百分比，可为负）")
    private BigDecimal monthOrderRatio;

}
