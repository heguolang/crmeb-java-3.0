package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
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
@ApiModel(value="OrderProductReplyResponse对象", description="评价商品页响应对象")
public class OrderProductReplyResponse implements Serializable {

    private static final long serialVersionUID = -1926585407216207845L;

    @ApiModelProperty(value = "购买数量")
    private Integer cartNum;

    @ApiModelProperty(value = "价格")
    private BigDecimal truePrice;

    @ApiModelProperty(value = "商品名称")
    private String storeName;

    @ApiModelProperty(value = "图片")
    private String image;

    @ApiModelProperty(value = "商品编号")
    private Integer productId;

    @ApiModelProperty(value = "规格sku")
    private String sku;

}
