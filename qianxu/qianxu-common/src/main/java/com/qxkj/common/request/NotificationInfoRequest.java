package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
@ApiModel(value="NotificationInfoRequest对象", description="系统通知详情请求对象")
public class NotificationInfoRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "通知id")
    @NotNull(message = "通知id不能为空")
    private Integer id;

    @ApiModelProperty(value = "wechat-公众号模板消息，routine-小程序订阅消息，sms-短信")
    @NotEmpty(message = "详情类型不能为空")
    private String detailType;

}
