package com.qxkj.common.request;

import com.qxkj.common.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
public class StoreOrderStaticsticsRequest {
    @ApiModelProperty(value = "页码", example= Constants.DEFAULT_PAGE + "")
    private int page = Constants.DEFAULT_PAGE;

    @ApiModelProperty(value = "每页数量", example = Constants.DEFAULT_LIMIT + "")
    private int limit = Constants.DEFAULT_LIMIT;

    @ApiModelProperty(value = "today,yesterday,lately7,lately30,month,year,/yyyy-MM-dd hh:mm:ss,yyyy-MM-dd hh:mm:ss/")
    private String dateLimit;

    @JsonIgnore
    private String startTime;

    @JsonIgnore
    private String endTime;
}
