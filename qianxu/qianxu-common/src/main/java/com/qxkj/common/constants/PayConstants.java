package com.qxkj.common.constants;

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
public class PayConstants {

    //支付方式
    public static final String PAY_TYPE_WE_CHAT = "weixin"; //微信支付
    public static final String PAY_TYPE_YUE = "yue"; //余额支付
    public static final String PAY_TYPE_OFFLINE = "offline"; //线下支付
    public static final String PAY_TYPE_ALI_PAY = "alipay"; //支付宝
    public static final String PAY_TYPE_ZERO_PAY = "zeroPay"; // 零元付

    //支付渠道
    public static final String PAY_CHANNEL_WE_CHAT_H5 = "weixinh5"; //H5唤起微信支付
    public static final String PAY_CHANNEL_WE_CHAT_PUBLIC = "public"; //公众号
    public static final String PAY_CHANNEL_WE_CHAT_PROGRAM = "routine"; //小程序
    public static final String PAY_CHANNEL_WE_CHAT_APP_IOS = "weixinAppIos"; //微信App支付ios
    public static final String PAY_CHANNEL_WE_CHAT_APP_ANDROID = "weixinAppAndroid"; //微信App支付android

    public static final String PAY_CHANNEL_ALI_PAY = "alipay"; //支付宝支付
    public static final String PAY_CHANNEL_ALI_APP_PAY = "appAliPay"; //支付宝App支付

    public static final String WX_PAY_TRADE_TYPE_JS = "JSAPI";
    public static final String WX_PAY_TRADE_TYPE_H5 = "MWEB";

    //微信支付接口请求地址
    public static final String WX_PAY_API_URL = "https://api.mch.weixin.qq.com/";
    // 微信统一预下单
    public static final String WX_PAY_API_URI = "pay/unifiedorder";
    // 微信查询订单
    public static final String WX_PAY_ORDER_QUERY_API_URI = "pay/orderquery";
    // 微信支付回调地址
    public static final String WX_PAY_NOTIFY_API_URI = "/api/admin/payment/callback/wechat";
    // 微信退款回调地址
    public static final String WX_PAY_REFUND_NOTIFY_API_URI = "/api/admin/payment/callback/wechat/refund";

    public static final String WX_PAY_SIGN_TYPE_MD5 = "MD5";
    public static final String WX_PAY_SIGN_TYPE_SHA256 = "HMAC-SHA256";

    public static final String PAY_BODY = "黔序商城-订单支付";
    public static final String FIELD_SIGN = "sign";

    // 公共号退款
    public static final String WX_PAY_REFUND_API_URI= "secapi/pay/refund";
}
