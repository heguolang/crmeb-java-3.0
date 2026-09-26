package com.qxkj.service.service;

import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.request.StoreOrderRefundRequest;

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
public interface AliPayService {


    /**
     * 查询支付结果
     * @param orderNo 订单编号
     * @return
     */
    Boolean queryPayResult(String orderNo);

    void refund(StoreOrderRefundRequest request, StoreOrder storeOrder);

    /**
     * 查询退款
     * @param orderNo 订单编号
     */
    Boolean queryRefund(String orderNo);
}
