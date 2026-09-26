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
@ApiModel(value="AliPayJsResultVo对象", description="支付宝调起支付参数对象")
public class AliPayJsResultVo {

    @ApiModelProperty(value = "支付宝id")
    private String appId;

    @ApiModelProperty(value = "接口名称")
    private String method;

    @ApiModelProperty(value = "仅支持JSON")
    private String format = "JSON";

    @ApiModelProperty(value = "编码格式")
    private String charset;

    @ApiModelProperty(value = "签名算法类型：RSA,RSA2")
    private String signType;

    @ApiModelProperty(value = "支付签名")
    private String sign;

    @ApiModelProperty(value = "发送请求时间，格式'yyyy-MM-dd HH:mm:ss'")
    private String timestamp;

    @ApiModelProperty(value = "调用的接口版本")
    private String version = "1.0";

    @ApiModelProperty(value = "异步回调地址")
    private String notifyUrl;

    @ApiModelProperty(value = "业务请求参数集合")
    private String bizContent;
}
