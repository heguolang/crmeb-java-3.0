package com.qxkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
@ApiModel(value="WxPayJsResultVo对象", description="微信调起支付参数对象")
public class WxPayJsResultVo {

    @ApiModelProperty(value = "微信分配的小程序ID")
    private String appId;

    @ApiModelProperty(value = "随机字符串，不长于32位")
    private String nonceStr;

    @ApiModelProperty(value = "统一下单接口返回的 prepay_id 参数值")
    private String packages;

    @ApiModelProperty(value = "签名类型，默认为MD5，支持HMAC-SHA256和MD5。")
    private String signType;

    @ApiModelProperty(value = "时间戳从1970年1月1日00:00:00至今的秒数,即当前的时间")
    private String timeStamp;

    @ApiModelProperty(value = "支付签名")
    private String paySign;

    @ApiModelProperty(value = "H5支付跳转链接")
    private String mwebUrl;

    @ApiModelProperty(value = "微信商户号")
    private String partnerid;

    @ApiModelProperty(value = "拉起收银台的ticket")
    private String ticket;
}
