package com.qxkj.common.response;

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
public class StoreStaffTopDetail {
    private Integer completeCount; // 订单完成订单数量
    private Integer evaluatedCount;// 待评价订单数量
    private Integer monthCount;
    private BigDecimal monthPrice;
    private Integer orderCount;
    private Integer proCount;
    private BigDecimal proPrice;
    private Integer receivedCount;
    private Integer refundCount;
    private BigDecimal sumPrice;
    private Integer todayCount;
    private BigDecimal todayPrice;
    private Integer unpaidCount;
    private Integer unshippedCount;
    private Integer verificationCount;// 待核销数量
}
