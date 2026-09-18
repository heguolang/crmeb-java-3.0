package com.zbkj.service.service;

import com.zbkj.common.request.StockPayRequest;

import java.util.HashMap;

/**
 * 订货系统-订货单支付服务
 * 复用商城支付底座：余额支付走用户余额扣减，微信支付走统一下单（公众号/小程序 JSAPI）
 */
public interface StockPayService {

    /**
     * 订货单支付
     *
     * @param uid     支付用户
     * @param request orderNo + payType(yue/weixin) + payChannel(public/routine)
     * @param ip      客户端IP
     * @return yue: {payType, status} / weixin: {payType, jsConfig}
     */
    HashMap<String, Object> pay(Integer uid, StockPayRequest request, String ip);
}
