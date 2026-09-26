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
@ApiModel(value="UserTotalResponse对象", description="用户总数据对象")
public class UserTotalResponse implements Serializable {

    private static final long serialVersionUID = -6332062115310922579L;

    @ApiModelProperty(value = "累计用户数")
    private Integer userNum;

    @ApiModelProperty(value = "累计充值人数")
    private Integer rechargePeopleNum;

    @ApiModelProperty(value = "累计充值金额（佣金转余额也算）")
    private BigDecimal rechargeTotalAmount;

    @ApiModelProperty(value = "累计消费金额")
    private BigDecimal consumptionAmount;

}
