package com.qxkj.common.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="CreateOrderH5SceneInfoVo对象", description="上报支付的场景信息")
public class CreateOrderH5SceneInfoVo {
    public CreateOrderH5SceneInfoVo() {
    }

    public CreateOrderH5SceneInfoVo(CreateOrderH5SceneInfoDetailVo h5Info) {
        this.h5_info = h5Info;
    }

    @ApiModelProperty(value = "h5_info", required = true)
    @JsonProperty(value = "h5_info")
    private CreateOrderH5SceneInfoDetailVo h5_info;
}
