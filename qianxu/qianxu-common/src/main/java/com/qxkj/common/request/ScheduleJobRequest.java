package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value = "ScheduleJobRequest对象", description = "定时任务请求对象")
public class ScheduleJobRequest implements Serializable {

    private static final long serialVersionUID = -452373239606480650L;

    @ApiModelProperty(value = "任务id,编辑时必传")
    private Integer jobId;

    @ApiModelProperty(value = "spring bean名称", required = true)
    @NotBlank(message = "spring bean名称不能为空")
    private String beanName;

    @ApiModelProperty(value = "方法名", required = true)
    @NotBlank(message = "方法名不能为空")
    private String methodName;

    @ApiModelProperty(value = "参数")
    private String params;

    @ApiModelProperty(value = "cron表达式", required = true)
    @NotBlank(message = "cron表达式不能为空")
    private String cronExpression;

    @ApiModelProperty(value = "备注")
    private String remark;
}
