package com.qxkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@ApiModel(value="SystemConfigFormItemVo对象", description="item对象")
public class SystemConfigFormItemConfigVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "")
    private String label;

    @ApiModelProperty(value = "")
    private String showLabel;

    @ApiModelProperty(value = "")
    private String changeTag;

    @ApiModelProperty(value = "")
    private String labelWidth;

    @ApiModelProperty(value = "")
    private String tag;

    @ApiModelProperty(value = "")
    private String tagIcon;

    @ApiModelProperty(value = "")
    private String span;

    @ApiModelProperty(value = "")
    private String layout;

    @ApiModelProperty(value = "")
    private Boolean required;

    @ApiModelProperty(value = "验证规则")
    private List<SystemConfigFormItemConfigRegListVo> regList;

    @ApiModelProperty(value = "")
    private String document;

    @ApiModelProperty(value = "")
    private String formId;

    @ApiModelProperty(value = "")
    private String renderKey;

    @ApiModelProperty(value = "")
    private String defaultValue;


}
