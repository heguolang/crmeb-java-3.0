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
@ApiModel(value="OrderDataResponse对象", description="订单数量响应对象")
public class OrderDataResponse implements Serializable {

    private static final long serialVersionUID = 1387727608277207652L;

    @ApiModelProperty(value = "已完成订单数量")
    private Integer completeCount;

    @ApiModelProperty(value = "待核销订单数量")
    private Integer evaluatedCount;

//    @ApiModelProperty(value = "用户昵称")
//    private Integer verificationCount;

    @ApiModelProperty(value = "支付订单总数")
    private Integer orderCount;

    @ApiModelProperty(value = "待收货订单数量")
    private Integer receivedCount;

    @ApiModelProperty(value = "退款订单数量")
    private Integer refundCount;

    @ApiModelProperty(value = "总消费钱数")
    private BigDecimal sumPrice;

    @ApiModelProperty(value = "未支付订单数量")
    private Integer unPaidCount;

    @ApiModelProperty(value = "待发货订单数量")
    private Integer unShippedCount;
}
