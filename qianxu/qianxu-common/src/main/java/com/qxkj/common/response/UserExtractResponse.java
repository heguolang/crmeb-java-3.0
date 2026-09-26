package com.qxkj.common.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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
public class UserExtractResponse {
    // 提现数据总额
    @ApiModelProperty(value = "体现数据总额")
    private BigDecimal extractCountPrice;
    // 提现次数
    @ApiModelProperty(value = "提现次数")
    private Integer extractCountNum;
    // 提现用户id
//    private Integer euid;
}
