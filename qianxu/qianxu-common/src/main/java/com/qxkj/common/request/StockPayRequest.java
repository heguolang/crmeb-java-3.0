package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 订货单支付请求
 */
@Data
@ApiModel(value = "StockPayRequest对象", description = "订货单支付请求")
public class StockPayRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订货单号")
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @ApiModelProperty(value = "支付方式：yue=余额 weixin=微信")
    @NotBlank(message = "请选择支付方式")
    private String payType;

    @ApiModelProperty(value = "微信支付渠道：public=公众号 routine=小程序（仅 weixin 时必传）")
    private String payChannel;
}
