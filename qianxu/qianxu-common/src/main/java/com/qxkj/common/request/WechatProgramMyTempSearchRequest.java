package com.qxkj.common.request;

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
@ApiModel(value="WechatProgramMyTempSearchRequest对象", description="小程序我的模板")
public class WechatProgramMyTempSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "模版标题")
    private String title;

    @ApiModelProperty(value = "我的模板id")
    private String tempId;

    @ApiModelProperty(value = "状态 0，禁用，1启用")
    private Boolean status;

    @ApiModelProperty(value = "应用场景")
    private String type;

    private Integer id;
}
