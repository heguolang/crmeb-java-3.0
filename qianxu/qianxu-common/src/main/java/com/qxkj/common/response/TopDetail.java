package com.qxkj.common.response;

import com.qxkj.common.model.user.User;
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
public class TopDetail {

    private User user;
    // 余额
    private BigDecimal balance;
    // 积分
    private Integer integralCount;
    // 总计订单
    private Integer allOrderCount;
    // 本月订单
    private Integer mothOrderCount;
    // 总消费金额
    private BigDecimal allConsumeCount;
    // 本月消费金额
    private BigDecimal mothConsumeCount;
}
