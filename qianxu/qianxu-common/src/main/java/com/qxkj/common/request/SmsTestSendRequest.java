package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 后台短信测试发送请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SmsTestSendRequest对象", description = "后台短信测试发送请求")
public class SmsTestSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "手机号不能为空")
    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @NotBlank(message = "短信内容不能为空")
    @ApiModelProperty(value = "短信内容", required = true)
    private String content;
}
