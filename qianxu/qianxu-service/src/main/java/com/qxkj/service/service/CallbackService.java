package com.qxkj.service.service;

import javax.servlet.http.HttpServletRequest;

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
public interface CallbackService {
    /**
     * 微信支付回调
     * @param xmlInfo 微信回调json
     * @return String
     */
    String weChat(String xmlInfo);

    /**
     * 支付宝支付回调
     */
    String aliPay(HttpServletRequest request);

    /**
     * 微信退款回调
     * @param request 微信回调json
     * @return String
     */
    String weChatRefund(String request);
}
