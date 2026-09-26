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
@ApiModel(value="TradingDataResponse对象", description="交易概览数据对象")
public class TradingDataResponse implements Serializable {

    private static final long serialVersionUID = -6332062115310922579L;

    @ApiModelProperty(value = "营业额")
    private BigDecimal turnover;

    @ApiModelProperty(value = "营业额环比")
    private String turnoverRatio;

    @ApiModelProperty(value = "商品支付金额")
    private BigDecimal proPayAmount;

    @ApiModelProperty(value = "商品支付金额环比")
    private String proPayAmountRatio;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal rechargeAmount;

    @ApiModelProperty(value = "充值金额环比")
    private String rechargeAmountRatio;

    @ApiModelProperty(value = "支出金额")
    private BigDecimal payoutAmount;

    @ApiModelProperty(value = "支出金额环比")
    private String payoutAmountRatio;

    @ApiModelProperty(value = "余额支付金额")
    private BigDecimal balanceAmount;

    @ApiModelProperty(value = "余额支付金额月环比")
    private String balanceAmountRatio;

    @ApiModelProperty(value = "支付佣金金额")
    private BigDecimal payoutBrokerageAmount;

    @ApiModelProperty(value = "支付佣金金额环比")
    private String payoutBrokerageAmountRatio;

    @ApiModelProperty(value = "商品退款金额")
    private BigDecimal proRefundAmount;

    @ApiModelProperty(value = "商品退款金额环比")
    private String proRefundAmountRatio;

}
