package com.qxkj.common.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qxkj.common.utils.CustomerBigDecimalSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="SpreadOrderResponse对象", description="推广订单响应体")
public class SpreadOrderResponse {

    @ApiModelProperty(value = "订单ID")
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "用户姓名")
    private String realName;

    @ApiModelProperty(value = "用户电话")
    private String userPhone;

    @JsonSerialize(using = CustomerBigDecimalSerialize.class)
    @ApiModelProperty(value = "佣金金额")
    private BigDecimal price;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
