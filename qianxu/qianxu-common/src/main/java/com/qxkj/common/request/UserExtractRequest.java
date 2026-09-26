package com.qxkj.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@ApiModel(value = "UserExtractRequest对象", description = "用户提现")
public class UserExtractRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "姓名")
    @NotBlank(message = "提现用户名称必须填写")
    @Length(max = 64, message = "提现用户名称不能超过64个字符")
    @JsonProperty(value = "name")
    private String realName;

    @ApiModelProperty(value = "提现方式| alipay=支付宝,bank=银行卡,weixin=微信", allowableValues = "range[alipay,weixin,bank]")
    @NotBlank(message = "请选择提现方式， 支付宝|微信|银行卡")
    private String extractType;

    @ApiModelProperty(value = "提现类别| brokerage=佣金,balance=余额，默认佣金")
    private String extractCategory;

    @ApiModelProperty(value = "银行卡")
    @JsonProperty(value = "cardum")
    private String bankCode;

    @ApiModelProperty(value = "提现银行名称")
    private String bankName;

    @ApiModelProperty(value = "支付宝账号")
    private String alipayCode;

    @ApiModelProperty(value = "提现金额")
    @JsonProperty(value = "money")
    @NotNull(message = "请输入提现金额")
    @DecimalMin(value = "0.01", message = "提现金额不能小于0.01")
    private BigDecimal extractPrice;

    @ApiModelProperty(value = "微信号")
    private String wechat;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "微信收款码")
    private String qrcodeUrl;
}
