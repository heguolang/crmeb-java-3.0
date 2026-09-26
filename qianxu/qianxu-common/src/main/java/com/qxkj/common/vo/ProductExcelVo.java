package com.qxkj.common.vo;

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
@ApiModel(value="ProductExcelVo对象", description = "产品导出")
public class ProductExcelVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商品名称")
    private String storeName;

//    @ApiModelProperty(value = "商品简介")
//    private String storeInfo;

    @ApiModelProperty(value = "商品分类")
    private String cateName;

    @ApiModelProperty(value = "价格")
    private String price;

    @ApiModelProperty(value = "库存")
    private String stock;

    @ApiModelProperty(value = "销量")
    private String sales;

    @ApiModelProperty(value = "浏览量")
    private String browse;
}
