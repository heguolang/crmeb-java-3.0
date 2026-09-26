package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@ApiModel(value="ShoppingProductDataResponse对象", description="商城商品统计数据对象")
public class ShoppingProductDataResponse implements Serializable {

    private static final long serialVersionUID = -2853994865375523003L;

    @ApiModelProperty(value = "新增商品数量")
    private Integer newProductNum;

    @ApiModelProperty(value = "新增商品数量环比")
    private String newProductNumRatio;

    @ApiModelProperty(value = "浏览量")
    private Integer pageView;

    @ApiModelProperty(value = "浏览量环比")
    private String pageViewRatio;

    @ApiModelProperty(value = "收藏量")
    private Integer collectNum;

    @ApiModelProperty(value = "收藏量环比")
    private String collectNumRatio;

    @ApiModelProperty(value = "加购件数")
    private Integer addCartNum;

    @ApiModelProperty(value = "加购件数环比")
    private String addCartNumRatio;

    @ApiModelProperty(value = "交易总件数")
    private Integer orderProductNum;

    @ApiModelProperty(value = "交易总件数环比")
    private String orderProductNumRatio;

    @ApiModelProperty(value = "交易成功件数")
    private Integer orderSuccessProductNum;

    @ApiModelProperty(value = "交易成功件数环比")
    private String orderSuccessProductNumRatio;

}
