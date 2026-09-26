package com.qxkj.common.constants;

/**
 * 阿里云短信配置键与默认值
 */
public class AliyunSmsConstants {

    /** AccessKey ID */
    public static final String CONFIG_ACCESS_KEY_ID = "aliyun_sms_access_key_id";
    /** AccessKey Secret */
    public static final String CONFIG_ACCESS_KEY_SECRET = "aliyun_sms_access_key_secret";
    /** 短信签名 */
    public static final String CONFIG_SIGN_NAME = "aliyun_sms_sign_name";
    /** 地域，默认 cn-hangzhou */
    public static final String CONFIG_REGION_ID = "aliyun_sms_region_id";
    /** 是否模拟发送：1=模拟（不真实调用），0=真实发送 */
    public static final String CONFIG_MOCK = "aliyun_sms_mock";
    /** 验证码模板 CODE（如 SMS_154950909） */
    public static final String CONFIG_VERIFY_TEMPLATE_CODE = "aliyun_sms_verify_template_code";

    public static final String DEFAULT_REGION = "cn-hangzhou";
    public static final String DEFAULT_SIGN_NAME = "黔序商城";
    public static final String DEFAULT_VERIFY_TEMPLATE = "SMS_MOCK_VERIFY";

    /** 阿里云短信 OpenAPI 地址 */
    public static final String API_HOST = "https://dysmsapi.aliyuncs.com/";
    public static final String API_VERSION = "2017-05-25";
    public static final String API_ACTION_SEND = "SendSms";
}
