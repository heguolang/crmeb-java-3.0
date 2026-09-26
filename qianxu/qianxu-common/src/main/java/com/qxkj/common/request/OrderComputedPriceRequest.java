package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
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
@ApiModel(value="OrderComputedPriceRequest对象", description="计算订单价格请求对象")
public class OrderComputedPriceRequest {

    @ApiModelProperty(value = "预下单订单号")
    @NotBlank(message = "预下单订单号不能为空")
    private String preOrderNo;

    @ApiModelProperty(value = "地址id")
    private Integer addressId;

    @ApiModelProperty(value = "优惠券id")
    private Integer couponId;

    @ApiModelProperty(value = "快递类型: 1-快递配送，2-到店自提")
    @NotNull(message = "快递类型不能为空")
    @Range(min = 1, max = 2, message = "未知的快递类型")
    private Integer shippingType;

    @ApiModelProperty(value = "是否使用积分")
    @NotNull(message = "是否使用积分不能为空")
    private Boolean useIntegral;

}
