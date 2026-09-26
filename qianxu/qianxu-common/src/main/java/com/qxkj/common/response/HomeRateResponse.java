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
@ApiModel(value="HomeRateResponse对象", description="主页统计数据对象")
public class HomeRateResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "今日销售额")
    private Object sales;

    @ApiModelProperty(value = "昨日销售额")
    private Object yesterdaySales;

    @ApiModelProperty(value = "今日访问量")
    private Object pageviews;

    @ApiModelProperty(value = "昨日访问量")
    private Object yesterdayPageviews;

    @ApiModelProperty(value = "今日订单量")
    private Object orderNum;

    @ApiModelProperty(value = "昨日订单量")
    private Object yesterdayOrderNum;

    @ApiModelProperty(value = "今日新增用户")
    private Object newUserNum;

    @ApiModelProperty(value = "昨日新增用户")
    private Object yesterdayNewUserNum;


}
