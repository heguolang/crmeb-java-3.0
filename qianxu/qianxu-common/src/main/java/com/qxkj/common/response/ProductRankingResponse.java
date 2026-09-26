package com.qxkj.common.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qxkj.common.utils.CustomerBigDecimalSerialize;
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
@ApiModel(value="ProductRankingResponse对象", description="商品排行响应对象")
public class ProductRankingResponse implements Serializable {

    private static final long serialVersionUID = 3362714265772774491L;

    @ApiModelProperty(value = "顺序")
    private Integer sort;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "浏览量")
    private Integer pageView;

    @ApiModelProperty(value = "收藏量")
    private Integer collectNum;

    @ApiModelProperty(value = "加购件数")
    private Integer addCartNum;

    @ApiModelProperty(value = "下单商品数（销售件数）")
    private Integer salesNum;

    @JsonSerialize(using = CustomerBigDecimalSerialize.class)
    @ApiModelProperty(value = "销售额")
    private BigDecimal salesAmount;

    @ApiModelProperty(value = "商品名称")
    private String proName;

    @ApiModelProperty(value = "商品图片")
    private String image;
}
