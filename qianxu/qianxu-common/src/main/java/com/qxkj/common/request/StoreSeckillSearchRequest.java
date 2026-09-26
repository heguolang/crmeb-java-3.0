package com.qxkj.common.request;

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
public class StoreSeckillSearchRequest {

    @ApiModelProperty(value = "搜索关键字 商品id或者名称")
    private String keywords;

    @ApiModelProperty(value = "秒杀时段")
    private Integer timeId;

    @ApiModelProperty(value = "是否显示关键字 0/1")
    private Integer status;
}

