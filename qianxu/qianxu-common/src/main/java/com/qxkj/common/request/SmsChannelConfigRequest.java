package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 短信通道配置请求（滨礼账号等）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SmsChannelConfigRequest对象", description = "短信通道配置请求")
public class SmsChannelConfigRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "发送通道：binli=滨礼（默认），aliyun=阿里云")
    private String channel;

    @NotBlank(message = "滨礼账号不能为空")
    @ApiModelProperty(value = "滨礼账号", required = true)
    private String account;

    @ApiModelProperty(value = "滨礼接口密码（传空表示不修改）")
    private String pwd;

    @ApiModelProperty(value = "是否模拟发送：1=模拟，0=真实发送")
    private String mock;

    @ApiModelProperty(value = "注册短信验证码开关：1=注册需校验验证码，0=不校验")
    private String registerCaptcha;

    @ApiModelProperty(value = "验证码短信内容模板，{code}=验证码 {time}=有效分钟数")
    private String verifyContent;
}
