package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.Valid;
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
@ApiModel(value="SystemFormItemCheckRequest对象", description="表单字段明细")
public class SystemFormItemCheckRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "字段名称", required = true)
    @NotBlank(message = "请设置 SystemFormItemCheckRequest 对象的 name 属性")
    private String name;

    @ApiModelProperty(value = "字段值", required = true)
    private String value;

    @ApiModelProperty(value = "字段显示文字", required = true)
    private String title;

}
