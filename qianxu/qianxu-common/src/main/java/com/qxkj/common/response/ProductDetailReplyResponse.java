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
@ApiModel(value="ProductDetailReplyResponse对象", description="商品详情评论响应对象")
public class ProductDetailReplyResponse implements Serializable {

    private static final long serialVersionUID = 8822745472328151094L;

    @ApiModelProperty(value = "评论总数")
    private Integer sumCount;

    @ApiModelProperty(value = "好评率")
    private String replyChance;

    @ApiModelProperty(value = "最后一条评论信息")
    private ProductReplyResponse productReply;
}
