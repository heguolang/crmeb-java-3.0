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
@ApiModel(value="StoreBargainDetailResponse对象", description="砍价商品详情响应对象（列表）")
public class StoreBargainDetailResponse implements Serializable {

    private static final long serialVersionUID = 969438774401700566L;

    @ApiModelProperty(value = "砍价商品ID")
    private Integer id;

    @ApiModelProperty(value = "关联商品ID")
    private Integer productId;

    @ApiModelProperty(value = "砍价活动名称")
    private String title;

    @ApiModelProperty(value = "砍价活动图片")
    private String image;

    @ApiModelProperty(value = "砍价开启时间")
    private Long startTime;

    @ApiModelProperty(value = "砍价结束时间")
    private Long stopTime;

    @ApiModelProperty(value = "砍价商品最低价")
    private BigDecimal minPrice;

    @ApiModelProperty(value = "限购总数")
    private Integer quota;
}
