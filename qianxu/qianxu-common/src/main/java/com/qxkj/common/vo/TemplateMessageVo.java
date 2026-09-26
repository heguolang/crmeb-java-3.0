package com.qxkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;

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
@ApiModel(value="SendTemplateMessageVo对象", description="微信模板发送类")
public class TemplateMessageVo {
    @ApiModelProperty(value = "OPENID", required = true)
    private String touser;

    @ApiModelProperty(value = "模板ID", required = true)
    private String template_id;

    @ApiModelProperty(value = "模板跳转链接（海外帐号没有跳转能力）")
    private String url;

    @ApiModelProperty(value = "发送内容")
    private HashMap<String, SendTemplateMessageItemVo> data;
}
