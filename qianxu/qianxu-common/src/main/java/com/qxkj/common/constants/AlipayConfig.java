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
public class AlipayConfig {

    // 商户appid
    public static String APPID = "ali_pay_appid";

    // 私钥 pkcs8格式的
    public static String RSA_PRIVATE_KEY = "ali_pay_private_key";

    // 服务器异步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "ali_pay_notifu_url";

    // 页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public static String return_url = "ali_pay_return_url";
    // (充值)页面跳转同步通知页面路径 需http://或者https://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问 商户可以自定义同步跳转地址
    public static String recharge_return_url = "ali_pay_recharge_return_url";

    // 用户付款中途退出返回商户网站的地址
    public static String quit_url = "ali_pay_quit_url";
    // （充值）用户付款中途退出返回商户网站的地址
    public static String recharge_quit_url = "ali_pay_recharge_quit_url";


    // 请求网关地址
    public static String URL = "https://openapi.alipay.com/gateway.do";

    // 编码
    public static String CHARSET = "UTF-8";

    // 返回格式
    public static String FORMAT = "json";

    // 支付宝公钥
    public static String ALIPAY_PUBLIC_KEY_2 = "ali_pay_public_key2";
    public static String ALIPAY_PUBLIC_KEY = "ali_pay_public_key";
    // 日志记录目录
    public static String LOG_PATH = "/log";
    // RSA2
    public static String SIGNTYPE = "RSA2";

    // 是否开启支付宝支付
    public static String ALIPAY_IS_OPEN = "ali_pay_is_open";
}
