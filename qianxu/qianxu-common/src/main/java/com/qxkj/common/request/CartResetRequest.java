package com.qxkj.common.request;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
public class CartResetRequest {

    @ApiModelProperty(value = "购物车id")
    @NotNull(message = "id 不能为空")
    private Long id;

    @ApiModelProperty(value = "购物车数量")
    @NotNull(message = "num 不能为空")
    private Integer num;

    @ApiModelProperty(value = "商品id")
    @NotNull(message = "productId 不能为空")
    private Integer productId;

    @ApiModelProperty(value = "AttrValue Id")
    @NotNull(message = "unique 不能为空")
    private Integer unique;
}
