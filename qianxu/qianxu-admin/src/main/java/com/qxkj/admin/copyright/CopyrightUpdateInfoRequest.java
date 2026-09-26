package com.qxkj.admin.copyright;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

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
@ApiModel(value = "CopyrightUpdateInfoRequest对象", description = "编辑公司版权信息请求对象")
public class CopyrightUpdateInfoRequest {

    @ApiModelProperty(value = "公司信息")
    @NotBlank(message = "公司信息不能为空")
    private String companyName;

    @ApiModelProperty(value = "公司图片")
//    @NotBlank(message = "公司版权图片不能为空")
    private String companyImage;
}
