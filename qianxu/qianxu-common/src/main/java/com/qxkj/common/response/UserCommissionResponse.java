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
@ApiModel(value="UserCommissionResponse对象", description="推广佣金明细")
public class UserCommissionResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "昨天的佣金")
    private BigDecimal lastDayCount = BigDecimal.ZERO;

    @ApiModelProperty(value = "累计提现金额")
    private BigDecimal extractCount = BigDecimal.ZERO;

    @ApiModelProperty(value = "当前佣金")
    private BigDecimal commissionCount = BigDecimal.ZERO;

    @ApiModelProperty(value = "分销级别名称，无则为「未获得」")
    private String distributorLevelName = "未获得";

    @ApiModelProperty(value = "团队奖级别名称，无则为「未获得」")
    private String teamLevelName = "未获得";
}
