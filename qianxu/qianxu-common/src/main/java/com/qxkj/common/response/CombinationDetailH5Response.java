package com.qxkj.common.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
@ApiModel(value="CombinationDetailH5Response对象", description="拼团商品移动端对象")
public class CombinationDetailH5Response implements Serializable {

    private static final long serialVersionUID = -885733985825623484L;

    @ApiModelProperty(value = "拼团商品ID")
    private Integer id;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "推荐图")
    private String image;

    @ApiModelProperty(value = "轮播图")
    private String sliderImage;

    @ApiModelProperty(value = "活动标题")
    private String storeName;

    @ApiModelProperty(value = "参团人数")
    private Integer people;

//    @ApiModelProperty(value = "简介")
//    private String storeInfo;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "单位名")
    private String unitName;

    @ApiModelProperty(value = "限购总数")
    private Integer quota;

    @ApiModelProperty(value = "限量总数显示")
    private Integer quotaShow;

    @ApiModelProperty(value = "原价")
    private BigDecimal otPrice;

    @ApiModelProperty(value = "每个订单可购买数量")
    private Integer onceNum;

    @ApiModelProperty(value = "虚拟销量")
    private Integer ficti;

    @ApiModelProperty(value = "商品详情")
    private String content;
}
