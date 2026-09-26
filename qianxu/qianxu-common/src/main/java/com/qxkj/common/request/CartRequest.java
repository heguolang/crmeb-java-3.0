package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
@ApiModel(value="CartRequest对象", description="购物车")
public class CartRequest {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品id不能为空")
    private Integer productId;

    @ApiModelProperty(value = "商品属性 -- attr 对象的id")
    @NotBlank(message = "商品属性id不能为空")
    private String productAttrUnique;

    @ApiModelProperty(value = "商品数量", required = true)
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量不能小于1")
    @Max(value = 99, message = "单个商品数量不能大于99")
    private Integer cartNum;
}
