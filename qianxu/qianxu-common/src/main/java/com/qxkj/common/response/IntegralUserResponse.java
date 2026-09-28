package com.qxkj.common.response;

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
@ApiModel(value="IntegralUserResponse对象", description="用户积分响应对象")
public class IntegralUserResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户剩余积分")
    private Integer integral;

    @ApiModelProperty(value = "累计总积分")
    private Integer sumIntegral;

    @ApiModelProperty(value = "累计抵扣积分")
    private Integer deductionIntegral;

//    @ApiModelProperty(value = "今日获得累计积分")
//    private Integer nowIntegral;

    @ApiModelProperty(value = "冻结的积分")
    private Integer frozenIntegral;

}
