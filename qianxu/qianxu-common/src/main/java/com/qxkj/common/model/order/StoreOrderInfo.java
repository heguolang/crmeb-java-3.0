package com.qxkj.common.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("eb_store_order_info")
@ApiModel(value="StoreOrderInfo对象", description="订单购物详情表")
public class StoreOrderInfo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订单id")
    private Integer orderId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "购买东西的详细信息")
    private String info;

    @ApiModelProperty(value = "唯一id")
    @TableField(value = "`unique`")
    private String unique;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "规格属性id")
    private Integer attrValueId;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "sku")
    private String sku;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "购买数量")
    private Integer payNum;

    @ApiModelProperty(value = "重量")
    private BigDecimal weight;

    @ApiModelProperty(value = "体积")
    private BigDecimal volume;

    @ApiModelProperty(value = "获得积分")
    private Integer giveIntegral;

    @ApiModelProperty(value = "是否评价")
    private Boolean isReply;

    @ApiModelProperty(value = "是否单独分佣")
    private Boolean isSub;

    @ApiModelProperty(value = "会员价")
    private BigDecimal vipPrice;

    @ApiModelProperty(value = "商品类型:0-普通，1-秒杀，2-砍价，3-拼团，4-视频号")
    private Integer productType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
