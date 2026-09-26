package com.qxkj.common.response;


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
@ApiModel(value="SystemLoginResponse对象", description="PC登录返回对象")
public class SystemLoginResponse implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "管理员账号")
    private String account;

    @ApiModelProperty(value = "管理员昵称")
    private String realName;

    @ApiModelProperty(value = "token")
    private String Token;

    @ApiModelProperty(value = "是否接收短信")
    private Boolean isSms;
}
