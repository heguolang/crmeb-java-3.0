package com.qxkj.common.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
@ApiModel(value="StoreProductRelationRequest对象", description="商品点赞和收藏表")
public class UserCollectAllRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "商品ID")
    @JsonProperty("id")
    @Size(min = 1, message = "请选择产品")
    private Integer[] productId;

    @ApiModelProperty(value = "产品类型|store=普通产品,product_seckill=秒杀产品(默认 普通产品 store)")
    @NotBlank(message = "请选择产品类型")
    private String category;
}
