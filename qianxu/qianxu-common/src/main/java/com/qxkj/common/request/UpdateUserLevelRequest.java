package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="UpdateUserLevelRequest", description="更新用户会员等级对象")
public class UpdateUserLevelRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "uid")
    @NotNull(message = "用户id不能为空")
    private Integer uid;

    @ApiModelProperty(value = "会员等级")
    @NotNull(message = "等级id不能为空")
    private Integer levelId;

    @ApiModelProperty(value = "是否扣减积分，true-扣减，false-不扣减")
    private Boolean isSub;

}
