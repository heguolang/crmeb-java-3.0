package com.qxkj.common.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
public class ShopUploadImgRequest {

    @ApiModelProperty(value = "0:media_id(目前只用于品牌申请品牌和类目)，1：返回链接, 2：返回微信支付media_id图片要求")
    private Integer respType;

    @ApiModelProperty(value = "0:图片流(resp_type=2需指定filename)，1:图片url")
    private Integer uploadType;

    @ApiModelProperty(value = "upload_type=1时必传")
    private String imgUrl;
}
