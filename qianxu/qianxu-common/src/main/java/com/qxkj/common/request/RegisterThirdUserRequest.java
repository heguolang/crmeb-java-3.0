package com.qxkj.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@ApiModel(value="RegisterThirdUserRequest对象", description="三方用户注册对象")
public class RegisterThirdUserRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户昵称", required = true)
    private String nickName;

    @ApiModelProperty(value = "性别", required = true)
    private String sex;

    @ApiModelProperty(value = "用户个人资料填写的省份")
    private String province;

    @ApiModelProperty(value = "普通用户个人资料填写的城市")
    private String city;

    @ApiModelProperty(value = "国家，如中国为CN")
    private String country;

    @ApiModelProperty(value = "微信小程序用户头像", required = true)
    private String avatar;

    @ApiModelProperty(value = "推广人id")
    @JsonProperty(value = "spread_spid", defaultValue = "0")
    private Integer spreadPid;

    @ApiModelProperty(value = "微信公众号用户头像", required = true)
    private String headimgurl;

    @ApiModelProperty(value = "用户类型:wechat-公众号，routine-小程序，h5-H5,iosWx-苹果微信，androidWx-安卓微信")
    private String type;

    @ApiModelProperty(value = "用户openId")
    private String openId;
}
