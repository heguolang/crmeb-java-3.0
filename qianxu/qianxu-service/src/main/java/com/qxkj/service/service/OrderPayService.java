package com.qxkj.service.service;

import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.request.OrderPayRequest;
import com.qxkj.common.response.OrderPayResultResponse;
import com.qxkj.common.response.PayConfigResponse;

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
public interface OrderPayService{


    /**
     * 获取支付配置
     */
    PayConfigResponse getPayConfig();

    /**
     * 支付成功处理
     * @param storeOrder 订单
     */
    Boolean paySuccess(StoreOrder storeOrder);

    /**
     * 订单支付
     * @param orderPayRequest 支付参数
     * @param ip    ip
     * @return OrderPayResultResponse
     */
    OrderPayResultResponse payment(OrderPayRequest orderPayRequest, String ip);
}
