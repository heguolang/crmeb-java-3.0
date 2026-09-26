package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
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
@ApiModel(value="UserBalanceResponse对象", description="用户资金统计")
public class UserBalanceResponse implements Serializable {
    public UserBalanceResponse(){}
    public UserBalanceResponse(BigDecimal nowMoney, BigDecimal recharge, BigDecimal orderStatusSum) {
        this.nowMoney = nowMoney;
        this.recharge = recharge;
        this.orderStatusSum = orderStatusSum;
    }

    private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "当前总资金")
    private BigDecimal nowMoney;

    @ApiModelProperty(value = "累计充值")
    private BigDecimal recharge;

    @ApiModelProperty(value = "累计消费")
    private BigDecimal orderStatusSum;

}
