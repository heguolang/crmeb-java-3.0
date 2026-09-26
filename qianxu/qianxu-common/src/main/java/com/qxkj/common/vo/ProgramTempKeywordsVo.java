package com.qxkj.common.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@ApiModel(value="ProgramTempKeywordsVo对象", description="小程序关键词")
public class ProgramTempKeywordsVo {
    @ApiModelProperty(value = "关键词 id，选用模板时需要")
    private Integer kid;

    @ApiModelProperty(value = "关键词内容")
    private String name;

    @ApiModelProperty(value = "关键词内容对应的示例")
    private String example;

    @ApiModelProperty(value = "参数类型")
    private String rule;

    @ApiModelProperty(value = "代码里组装数据的时候，需要用到这个key")
    @JsonIgnore
    private String key;

    @ApiModelProperty(value = "代码里组装数据的时候，需要用到这个key")
    @JsonIgnore
    private String sendKey;
}
