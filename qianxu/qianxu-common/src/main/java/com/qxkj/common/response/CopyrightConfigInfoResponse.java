package com.qxkj.common.response;

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
@ApiModel(value = "CopyrightConfigInfoResponse对象", description = "授权信息响应对象")
public class CopyrightConfigInfoResponse implements Serializable {

    private static final long serialVersionUID = -4585094537501770138L;

    @ApiModelProperty(value = "公司信息")
    private String companyName;

    @ApiModelProperty(value = "公司图片")
    private String companyImage;

    @ApiModelProperty(value = "未登录访问首页提示登录弹窗开关 1-开启 0-关闭")
    private String loginNoticeSwitch;

    @ApiModelProperty(value = "未登录访问首页提示登录弹窗文案")
    private String loginNoticeText;
}
