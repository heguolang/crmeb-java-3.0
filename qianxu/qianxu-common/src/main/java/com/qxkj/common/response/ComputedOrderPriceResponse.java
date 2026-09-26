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
@ApiModel(value="ComputedOrderPriceResponse对象", description="计算订单价格响应对象")
public class ComputedOrderPriceResponse implements Serializable {

    private static final long serialVersionUID = 7282892323898493847L;

    @ApiModelProperty(value = "优惠券优惠金额")
    private BigDecimal couponFee;

    @ApiModelProperty(value = "积分抵扣金额")
    private BigDecimal deductionPrice;

    @ApiModelProperty(value = "运费金额")
    private BigDecimal freightFee;

    @ApiModelProperty(value = "实际支付金额")
    private BigDecimal payFee;

    @ApiModelProperty(value = "商品总金额")
    private BigDecimal proTotalFee;

    @ApiModelProperty(value = "剩余积分")
    private Integer surplusIntegral;

    @ApiModelProperty(value = "是否使用积分")
    private Boolean useIntegral;

    @ApiModelProperty(value = "使用的积分")
    private Integer usedIntegral;
}
