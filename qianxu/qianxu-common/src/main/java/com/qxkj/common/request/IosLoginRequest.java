package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

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
@ApiModel(value="IosLoginRequest对象", description="ios登录请求体")
public class IosLoginRequest {

    @ApiModelProperty(value = "iosToken", required = true)
//    @NotBlank(message = "identityToken不能为空")
    private String identityToken;

    @ApiModelProperty(value = "App服务商唯一用户标识", required = true)
    @NotBlank(message = "openId不能为空")
    private String openId;

    @ApiModelProperty(value = "Ios用户电子邮箱")
//    @NotBlank(message = "email不能为空")
    private String email;
}
