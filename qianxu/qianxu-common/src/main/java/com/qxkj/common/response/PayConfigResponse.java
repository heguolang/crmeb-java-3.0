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
@ApiModel(value = "PayConfigResponse对象", description = "支付配置响应对象")
public class PayConfigResponse implements Serializable {

    private static final long serialVersionUID = 7282892323898493847L;

    @ApiModelProperty(value = "余额支付")
    private Boolean yuePayStatus;

    @ApiModelProperty(value = "微信支付")
    private Boolean payWechatOpen;

    @ApiModelProperty(value = "支付宝支付")
    private Boolean aliPayStatus;

    @ApiModelProperty(value = "用户余额")
    private BigDecimal userBalance;
}
