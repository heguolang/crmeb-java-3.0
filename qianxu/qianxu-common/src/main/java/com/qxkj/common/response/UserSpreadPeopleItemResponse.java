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
@ApiModel(value="UserSpreadPeopleItemResponse对象", description="推广人信息")
public class UserSpreadPeopleItemResponse implements Serializable {

    private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "用户编号")
    private Integer uid;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "添加时间")
    private String time;

    @ApiModelProperty(value = "推广人数")
    private Integer childCount;

    @ApiModelProperty(value = "订单数量")
    private Integer orderCount;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal numberCount;

}
