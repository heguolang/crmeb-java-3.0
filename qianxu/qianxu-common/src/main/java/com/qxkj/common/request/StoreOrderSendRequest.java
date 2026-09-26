package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

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
@ApiModel(value="StoreOrderSendRequest对象", description="订单发货对象")
public class StoreOrderSendRequest {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单id")
    private Integer id;

    @ApiModelProperty(value = "订单编号")
    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "类型， express 发货，send 送货，fictitious虚拟", allowableValues = "express,send,fictitious", required = true)
    @NotBlank(message = "请选择类型")
    private String deliveryType;

    @ApiModelProperty(value = "快递公司名,发货类型必传")
    private String expressName;

    @ApiModelProperty(value = "快递公司编码,发货类型必传")
    private String expressCode;

    @ApiModelProperty(value = "快递单号,发货类型必传")
    private String expressNumber;

    @ApiModelProperty(value = "发货记录类型，1=手动填写快递单号")
    private String expressRecordType;

    @ApiModelProperty(value = "送货人姓名,送货类型必传")
    private String deliveryName;

    @ApiModelProperty(value = "送货人电话,送货类型必传")
    private String deliveryTel;
}
