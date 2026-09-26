package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
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
@ApiModel(value="UserAddressCityRequest对象", description="用户地址详细对象")
public class UserAddressCityRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "收货人所在省", required = true)
    @NotBlank(message = "收货人所在省不能为空")
    private String province;

    @ApiModelProperty(value = "收货人所在市", required = true)
    @NotBlank(message = "收货人所在市不能为空")
    private String city;

    @ApiModelProperty(value = "城市id")
    private Integer cityId = 0;

    @ApiModelProperty(value = "收货人所在区", required = true)
    @NotBlank(message = "收货人所在区不能为空")
    private String district;

}
