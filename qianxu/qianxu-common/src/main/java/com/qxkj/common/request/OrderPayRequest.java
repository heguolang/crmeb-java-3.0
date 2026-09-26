package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

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
@ApiModel(value="OrderPayRequest对象", description="订单支付")
public class OrderPayRequest {

    @ApiModelProperty(value = "订单id")
//    @NotNull(message = "订单id不能为空")
    private String uni;

    @ApiModelProperty(value = "订单编号")
    @NotNull(message = "订单编号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "支付类型：weixin-微信支付，yue-余额支付，offline-线下支付，alipay-支付包支付")
    @NotNull(message = "支付类型不能为空")
    private String payType;

    @ApiModelProperty(value = "支付渠道:weixinh5-微信H5支付，public-公众号支付，routine-小程序支付，weixinAppIos-微信appios支付，weixinAppAndroid-微信app安卓支付,alipay-支付包支付，appAliPay-App支付宝支付")
    @NotNull(message = "支付渠道不能为空")
    private String payChannel;

    @ApiModelProperty(value = "支付平台")
    private String from;

    @ApiModelProperty(value = "下单时小程序的场景值")
    private Integer scene;
}
