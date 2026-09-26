package com.qxkj.common.vo;

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
@ApiModel(value="WeChatMiniAuthorizeVo对象", description="微信小程序用户授权返回数据")
public class WeChatMiniAuthorizeVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "会话密钥")
    @TableField(value = "session_key")
    private String sessionKey;

    @ApiModelProperty(value = "用户唯一标识")
    @TableField(value = "openid")
    private String openId;

    @ApiModelProperty(value = "用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台帐号下会返回")
    @TableField(value = "unionid")
    private String unionId;

    @ApiModelProperty(value = "错误码")
    @TableField(value = "errcode")
    private String errCode;

    @ApiModelProperty(value = "错误信息")
    @TableField(value = "errmsg")
    private String errMsg;
}
