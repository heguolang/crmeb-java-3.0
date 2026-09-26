package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(value="StoreOrderInfoVo对象", description="订单购物详情表")
public class StoreOrderInfoOldVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "订单id")
    private Integer orderId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "购买东西的详细信息")
    private OrderInfoDetailVo info;

    @ApiModelProperty(value = "唯一id")
    @TableField(value = "`unique`")
    private String unique;



}
