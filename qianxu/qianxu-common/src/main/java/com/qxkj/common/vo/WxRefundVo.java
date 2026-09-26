
package com.qxkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="WxRefundVo", description="调用微信退款所需要的参数")
public class WxRefundVo {

    @ApiModelProperty(value = "appId，公众号名称，由商户传入", required = true)
    private String appid;

    @ApiModelProperty(value = "直连商户的商户号，由微信支付生成并下发", required = true)
    private String mch_id;

    @ApiModelProperty(value = "随机字符串，不长于32位", required = true)
    private String nonce_str;

    @ApiModelProperty(value = "签名", required = true)
    private String sign;

    @ApiModelProperty(value = "签名类型，目前支持HMAC-SHA256和MD5，默认为MD5")
    private String sign_type = "MD5";

    @ApiModelProperty(value = "微信支付订单号:微信生成的订单号，在支付通知中有返回")
    private String transaction_id;

    @ApiModelProperty(value = "商户系统内部的订单号,32个字符内、可包含字母, 其他说明见商户订单号", required = true)
    private String out_trade_no;

    @ApiModelProperty(value = "商户退款单号,同一退款单号多次请求只退一笔。")
    private String out_refund_no;

    @ApiModelProperty(value = "订单总金额，单位为分", required = true)
    private int total_fee;

    @ApiModelProperty(value = "退款金额，单位为分", required = true)
    private int refund_fee;

    @ApiModelProperty(value = "退款货币种类:符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型")
    private String refund_fee_type = "CNY";

    @ApiModelProperty(value = "退款结果通知url")
    private String notify_url;

}

