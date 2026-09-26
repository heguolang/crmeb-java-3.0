package com.qxkj.common.vo;

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
@ApiModel(value="PublicMyTemplateVo对象", description="微信公众号私有模板消息Vo对象")
public class PublicMyTemplateVo {

    @ApiModelProperty(value = "模板ID")
    private String template_id;

    @ApiModelProperty(value = "模板ID")
    private String title;

    @ApiModelProperty(value = "一级行业")
    private String primary_industry;

    @ApiModelProperty(value = "二级行业")
    private String deputy_industry;

    @ApiModelProperty(value = "模板内容")
    private String content;

    @ApiModelProperty(value = "模板示例")
    private String example;
}
