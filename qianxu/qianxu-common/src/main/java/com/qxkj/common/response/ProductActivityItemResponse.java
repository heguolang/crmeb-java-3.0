package com.qxkj.common.response;

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
public class ProductActivityItemResponse {

    @ApiModelProperty(value = "参与活动id")
    private Integer id;

    @ApiModelProperty(value = "活动结束时间")
    private Integer time;

    @ApiModelProperty(value = "活动参与类型:1=秒杀，2=砍价，3=拼团")
    private String type;
}
