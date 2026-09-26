package com.qxkj.common.response;

import com.qxkj.common.model.seckill.StoreSeckill;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

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
@ApiModel(value="SeckillIndexResponse对象", description="秒杀首页响应对象")
public class SeckillIndexResponse {

    @ApiModelProperty(value = "秒杀时段信息")
    private SecKillResponse secKillResponse;

    @ApiModelProperty(value = "秒杀商品信息")
    private List<StoreSeckill> productList;
}
