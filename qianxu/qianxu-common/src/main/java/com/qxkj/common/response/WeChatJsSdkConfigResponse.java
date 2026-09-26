package com.qxkj.common.response;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@ApiModel(value="WeChatJsSdkConfigResponse对象", description="微信公众号js-sdk响应对象对象")
public class WeChatJsSdkConfigResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "url")
    private String url;

    @ApiModelProperty(value = "jsApiTicket")
    private String jsApiTicket;

    @ApiModelProperty(value = "nonceStr")
    private String nonceStr;

    @ApiModelProperty(value = "timestamp")
    private Long timestamp;

    @ApiModelProperty(value = "signature")
    private String signature;

    @ApiModelProperty(value = "jsApiList")
    private List<String> jsApiList;

    @ApiModelProperty(value = "debug")
    private Boolean debug;

    @ApiModelProperty(value = "appid")
    private String appId;
}
