package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
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
@ApiModel(value="StoreProductReplyCommentRequest对象", description="回复商品评论对象")
public class StoreProductReplyCommentRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "评论id", required = true)
    @NotNull(message = "评论id不能为空")
    private Integer ids;

    @ApiModelProperty(value = "管理员回复内容", required = true)
    @NotBlank(message = "请填写评论内容")
    private String merchantReplyContent;
}
