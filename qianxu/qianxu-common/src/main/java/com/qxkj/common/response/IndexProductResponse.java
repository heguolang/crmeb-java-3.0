package com.qxkj.common.response;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="IndexProductResponse对象", description="首页商品对象")
public class IndexProductResponse {


    @ApiModelProperty(value = "商品id")
    private Integer id;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "商品名称")
    private String storeName;

    @ApiModelProperty(value = "商品价格")
    private BigDecimal price;

    @ApiModelProperty(value = "市场价")
    private BigDecimal otPrice;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "虚拟销量")
    private Integer ficti;

    @ApiModelProperty(value = "好评率")
    private String positiveRatio;

    @ApiModelProperty(value = "评论数量")
    private Integer replyNum;

    @ApiModelProperty(value = "单位名")
    private String unitName;

    @ApiModelProperty(value = "活动显示排序0=默认，1=秒杀，2=砍价，3=拼团")
    private String activity;

    @ApiModelProperty(value = "为移动端特定参数")
    private ProductActivityItemResponse activityH5;

    @ApiModelProperty(value = "购物车商品数量")
    private Integer cartNum;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "展示图")
    private String flatPattern;

    @ApiModelProperty(value = "活动边框 列表中是边框 详情中是背景图")
    @TableField(exist = false)
    private String activityStyle;
}
