package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
@ApiModel(value="UserSpreadOrderItemResponse对象", description="推广订单信息")
public class UserSpreadOrderItemResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "推广条数")
    private Integer count = 0;

    @ApiModelProperty(value = "推广年月")
    private String time;

    @ApiModelProperty(value = "推广订单信息")
    private List<UserSpreadOrderItemChildResponse> child = new ArrayList<>();
}
