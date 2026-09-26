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
@ApiModel(value="OrderInfoResponse对象", description="订单详情响应对象")
public class OrderInfoResponse implements Serializable {

    private static final long serialVersionUID=1L;

//    @ApiModelProperty(value = "订单id")
//    private Integer orderId;
    @ApiModelProperty(value = "attrId")
    private String attrId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

//    @ApiModelProperty(value = "购买东西的详细信息")
//    private StoreCartResponse info;

    @ApiModelProperty(value = "商品数量")
    private Integer cartNum;

//    @ApiModelProperty(value = "唯一id")
//    @TableField(value = "`unique`")
//    private String unique;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品名称")
    private String storeName;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "是否评价")
    private Integer isReply;

    @ApiModelProperty(value = "规格属性值")
    private String sku;
}
