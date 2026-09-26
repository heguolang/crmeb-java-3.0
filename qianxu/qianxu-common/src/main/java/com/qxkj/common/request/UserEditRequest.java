package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
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
@ApiModel(value="UserEditRequest对象", description="修改个人资料")
public class UserEditRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户昵称")
    @NotBlank(message = "请填写用户昵称")
    @Length(max = 255, message = "用户昵称不能超过255个字符")
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    @NotBlank(message = "请上传用户头像")
    @Length(max = 255, message = "用户头像不能超过255个字符")
    private String avatar;
}
