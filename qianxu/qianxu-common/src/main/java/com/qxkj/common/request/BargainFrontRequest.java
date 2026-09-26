package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

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
@ApiModel(value="BargainFrontRequest对象", description="砍价公共请求对象")
public class BargainFrontRequest {

    @ApiModelProperty(value = "砍价商品ID", required = true)
    @NotNull(message = "砍价商品编号不能为空")
    private Integer bargainId;

    @ApiModelProperty(value = "用户砍价活动ID")
    private Integer bargainUserId;

    @ApiModelProperty(value = "用户砍价活动Uid")
    private Integer bargainUserUid;
}
