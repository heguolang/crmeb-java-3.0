package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
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
@ApiModel(value="ProductAddStockRequest对象", description="商品添加库存对象")
public class ProductAddStockRequest implements Serializable {

    private static final long serialVersionUID = -259657957101524526L;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Integer id;

    @Valid
    @ApiModelProperty(value = "商品规格属性列表", required = true)
    @NotEmpty(message = "商品规格属性列表不能为空")
    private List<ProductAttrValueAddStockRequest> attrValueList;

}
