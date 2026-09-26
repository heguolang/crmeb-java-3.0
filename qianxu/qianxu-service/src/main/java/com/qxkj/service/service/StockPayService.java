package com.qxkj.service.service;

import com.qxkj.common.request.StockPayRequest;

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

    /** 换货差价微信支付：返回 jsConfig（需后台开启 stock_exchange_diff_wechat 开关） */
    HashMap<String, Object> payExchangeDiffWeixin(Integer uid, Integer exchangeId, String channel, String ip);
}
