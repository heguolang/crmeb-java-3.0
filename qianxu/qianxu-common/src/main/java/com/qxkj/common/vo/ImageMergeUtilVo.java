package com.qxkj.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
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
public class ImageMergeUtilVo {

    @ApiModelProperty(value = "图片地址", required = true)
    @NotBlank(message = "图片地址必须填写")
    private String path; //地址

    @ApiModelProperty(value = "x轴", required = true)
    @Min(value = 0, message = "x轴至少为0")
    private int x; //x轴

    @ApiModelProperty(value = "y轴", required = true)
    @Min(value = 0, message = "y轴至少为0")
    private int y; //y轴
}
