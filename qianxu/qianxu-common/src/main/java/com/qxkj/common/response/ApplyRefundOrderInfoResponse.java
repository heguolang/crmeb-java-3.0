package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="ApplyRefundOrderInfoResponse对象", description="申请退款订单响应对象")
public class ApplyRefundOrderInfoResponse implements Serializable {

    private static final long serialVersionUID = 1387727608277207652L;

    @ApiModelProperty(value = "订单id")
    private Integer id;

    @ApiModelProperty(value = "订单编号")
    private String orderId;

    @ApiModelProperty(value = "支付状态")
    private Boolean paid;

    @ApiModelProperty(value = "支付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "订单商品总数")
    private Integer totalNum;

    @ApiModelProperty(value = "订单详情对象列表")
    private List<OrderInfoResponse> orderInfoList;

}
