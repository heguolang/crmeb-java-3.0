package com.qxkj.common.request;

import com.qxkj.common.constants.Constants;
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
public class SearchAndPageRequest {

    @ApiModelProperty(value = "搜索关键字")
    private String keywords;

    @ApiModelProperty(value = "页码", example= Constants.DEFAULT_PAGE + "")
    private int page = Constants.DEFAULT_PAGE;

    @ApiModelProperty(value = "每页数量", example = Constants.DEFAULT_LIMIT + "")
    private int limit = Constants.DEFAULT_LIMIT;

    @ApiModelProperty(value = "优惠券类型:1-手动领取,3-赠送券")
    private Integer type;
}
