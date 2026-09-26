package com.qxkj.service.service;

import com.qxkj.common.model.finance.UserRecharge;

import java.util.Map;

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
public interface WeChatPayService {

    /**
     * 查询支付结果
     * @param orderNo 订单编号
     * @return
     */
    Boolean queryPayResult(String orderNo);

    /**
     * 微信充值预下单接口
     * @param userRecharge 充值订单
     * @param clientIp      ip
     * @return 获取wechat.requestPayment()参数
     */
    Map<String, String> unifiedRecharge(UserRecharge userRecharge, String clientIp);
}
