package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;
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
@ApiModel(value = "ShippingTemplatesRegionResponse对象", description = "运费模板区域响应对象")
public class ShippingTemplatesRegionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "城市名称描述")
    private String title;

    @ApiModelProperty(value = "首件", required = true, example = "0.1")
    @DecimalMin(value = "0.1", message = "首件金额不能低于0.1")
    private BigDecimal first;

    @ApiModelProperty(value = "首件运费", required = true, example = "0.1")
    @DecimalMin(value = "0.1", message = "首件运费金额不能低于0.1")
    private BigDecimal firstPrice;

    @ApiModelProperty(value = "续件", required = true, example = "0.1")
    @DecimalMin(value = "0.1", message = "续件不能低于0.1")
    private BigDecimal renewal;

    @ApiModelProperty(value = "续件运费", required = true, example = "0.1")
    @DecimalMin(value = "0.1", message = "续件运费金额不能低于0.1")
    private BigDecimal renewalPrice;

    @ApiModelProperty(value = "分组唯一值")
    private String uniqid;
}
