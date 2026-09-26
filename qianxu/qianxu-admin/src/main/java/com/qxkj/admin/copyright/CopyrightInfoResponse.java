package com.qxkj.admin.copyright;

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
@ApiModel(value = "CopyrightInfoResponse对象", description = "版权信息响应对象")
public class CopyrightInfoResponse {

    @ApiModelProperty(value = "管理端API域名")
    private String domainUrl;

    @ApiModelProperty(value = "项目版本号")
    private String version;

    @ApiModelProperty(value = "版权标签")
    private Integer label;

    @ApiModelProperty(value = "授权码")
    private String authCode;

    @ApiModelProperty(value = "版权状态：-2=API域名未配置 -1=未提交，0-待审核，1-授权成功，2-审核失败")
    private Integer status;

    @ApiModelProperty(value = "公司信息")
    private String companyName;

    @ApiModelProperty(value = "公司图片")
    private String companyImage;

    @ApiModelProperty(value = "版权码")
    private String copyright;
}
