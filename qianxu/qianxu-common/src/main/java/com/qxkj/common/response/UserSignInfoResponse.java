package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

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
@ApiModel(value="UserSignInfoResponse对象", description="用户签到信息响应对象")
public class UserSignInfoResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "连续签到天数")
    private Integer signNum;

    @ApiModelProperty(value = "累计签到次数")
    private Integer sumSignDay;

    @ApiModelProperty(value = "今天是否签到")
    private Boolean isDaySign;

    @ApiModelProperty(value = "昨天是否签到")
    private Boolean isYesterdaySign;

    @ApiModelProperty(value = "用户积分")
    private Integer integral;

}
