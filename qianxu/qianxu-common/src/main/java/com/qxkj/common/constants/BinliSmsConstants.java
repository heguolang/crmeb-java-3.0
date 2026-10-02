package com.qxkj.common.constants;

/**
 * 滨礼短信平台配置键与接口地址
 *
 * 账号 / 接口密码存放于系统配置表（eb_system_config），后台可改，不写死在代码里：
 *  - account：滨礼账号
 *  - pwd：接口密码（调用时 MD5 32 位加密后传输）
 *  - mock：1=模拟发送（只打日志，便于本地联调），0=真实发送
 */
public class BinliSmsConstants {

    /** 滨礼账号（系统配置键） */
    public static final String CONFIG_ACCOUNT = "binli_sms_account";
    /** 滨礼接口密码，明文存配置，调用时 MD5 加密（系统配置键） */
    public static final String CONFIG_PWD = "binli_sms_pwd";
    /** 是否模拟发送：1=模拟（不真实调用），0=真实发送 */
    public static final String CONFIG_MOCK = "binli_sms_mock";
    /** 验证码短信内容模板，{code}=验证码，{time}=有效分钟数 */
    public static final String CONFIG_VERIFY_CONTENT = "binli_sms_verify_content";

    public static final String DEFAULT_VERIFY_CONTENT = "【黔序】您的验证码是{code}，{time}分钟内有效，请勿泄露给他人。";

    /** 滨礼短信 OpenAPI 地址 */
    public static final String API_HOST = "http://120.26.69.132:7891";
    /** 1、发送接口（文字短信） */
    public static final String API_SEND = "/api/v1/send";
    /** 2、发送接口（文字短信-点对点批量发送） */
    public static final String API_BATCH_ONE_TO_ONE = "/api/v1/batchOneToOneSend";
    /** 3、发送接口（语音呼叫） */
    public static final String API_VOICE_SEND = "/api/v1/voice/send";
    /** 4、发送统计接口（文字短信/语音） */
    public static final String API_QUERY_TOTAL = "/api/v1/queryTotal";
    /** 5、状态查询接口（文字短信/语音） */
    public static final String API_REPORT = "/api/v1/report";
    /** 6、上行查询接口（文字短信） */
    public static final String API_MO = "/api/v1/mo";
    /** 7、用户余额查询接口（文字短信） */
    public static final String API_QUERY_BALANCE = "/api/v1/queryBalance";

    /** 状态报告：成功 */
    public static final String STATUS_DELIVRD = "DELIVRD";

    /** 发送渠道：滨礼 */
    public static final String CHANNEL_BINLI = "binli";
    /** 发送渠道：阿里云 */
    public static final String CHANNEL_ALIYUN = "aliyun";
}
