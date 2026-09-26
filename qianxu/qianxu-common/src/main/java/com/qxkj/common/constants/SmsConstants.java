package com.qxkj.common.constants;

/**
 * 短信业务常量（模板类型开关等）
 */
public class SmsConstants {

    /** 接口异常错误码 */
    public static final Integer SMS_ERROR_CODE = 400;

    /** 短信发送队列key */
    public static final String SMS_SEND_KEY = "sms_send_list";

    /** 手机验证码 redis key */
    public static final String SMS_VALIDATE_PHONE = "sms:validate:code:";
    public static final String SMS_VALIDATE_PHONE_NUM = "sms:validate:phone:";

    /** 验证码 */
    public static final String SMS_CONFIG_VERIFICATION_CODE = "verificationCode";
    /** 支付成功短信提醒 */
    public static final String SMS_CONFIG_LOWER_ORDER_SWITCH = "lowerOrderSwitch";
    /** 发货短信提醒 */
    public static final String SMS_CONFIG_DELIVER_GOODS_SWITCH = "deliverGoodsSwitch";
    /** 确认收货短信提醒 */
    public static final String SMS_CONFIG_CONFIRM_TAKE_OVER_SWITCH = "confirmTakeOverSwitch";
    /** 用户下单管理员短信提醒 */
    public static final String SMS_CONFIG_ADMIN_LOWER_ORDER_SWITCH = "adminLowerOrderSwitch";
    /** 支付成功管理员短信提醒 */
    public static final String SMS_CONFIG_ADMIN_PAY_SUCCESS_SWITCH = "adminPaySuccessSwitch";
    /** 用户确认收货管理员短信提醒 */
    public static final String SMS_CONFIG_ADMIN_REFUND_SWITCH = "adminRefundSwitch";
    /** 用户发起退款管理员短信提醒 */
    public static final String SMS_CONFIG_ADMIN_CONFIRM_TAKE_OVER_SWITCH = "adminConfirmTakeOverSwitch";
    /** 改价短信提醒 */
    public static final String SMS_CONFIG_PRICE_REVISION_SWITCH = "priceRevisionSwitch";
    /** 订单未支付 */
    public static final String SMS_CONFIG_ORDER_PAY_FALSE = "orderPayFalse";

    /** 验证码 */
    public static final int SMS_CONFIG_TYPE_VERIFICATION_CODE = 1;
    /** 支付成功短信提醒 */
    public static final int SMS_CONFIG_TYPE_LOWER_ORDER_SWITCH = 2;
    /** 发货短信提醒 */
    public static final int SMS_CONFIG_TYPE_DELIVER_GOODS_SWITCH = 3;
    /** 确认收货短信提醒 */
    public static final int SMS_CONFIG_TYPE_CONFIRM_TAKE_OVER_SWITCH = 4;
    /** 用户下单管理员短信提醒 */
    public static final int SMS_CONFIG_TYPE_ADMIN_LOWER_ORDER_SWITCH = 5;
    /** 支付成功管理员短信提醒 */
    public static final int SMS_CONFIG_TYPE_ADMIN_PAY_SUCCESS_SWITCH = 6;
    /** 用户确认收货管理员短信提醒 */
    public static final int SMS_CONFIG_TYPE_ADMIN_REFUND_SWITCH = 7;
    /** 用户发起退款管理员短信提醒 */
    public static final int SMS_CONFIG_TYPE_ADMIN_CONFIRM_TAKE_OVER_SWITCH = 8;
    /** 改价短信提醒 */
    public static final int SMS_CONFIG_TYPE_PRICE_REVISION_SWITCH = 9;
    /** 订单未支付 */
    public static final int SMS_CONFIG_TYPE_ORDER_PAY_FALSE = 10;
}
