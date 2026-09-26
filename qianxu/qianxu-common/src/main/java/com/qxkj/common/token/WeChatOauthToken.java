package com.qxkj.common.token;

import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(value="WeChatAuthorizeLoginGetOpenIdResponse对象", description="微信开放平台获取accessToken对象")
public class WeChatOauthToken implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "accessToken接口调用凭证")
    @TableField(value = "access_token")
    private String accessToken;

    @ApiModelProperty(value = "access_token 接口调用凭证超时时间，单位（秒）")
    @TableField(value = "expires_in")
    private String expiresIn;

    @ApiModelProperty(value = "用户刷新access_token")
    @TableField(value = "refresh_token")
    private String refreshToken;

    @ApiModelProperty(value = "用户OpenId")
    @TableField(value = "openid")
    private String openId;

    @ApiModelProperty(value = "用户授权的作用域，使用逗号（,）分隔")
    private String scope;

}
