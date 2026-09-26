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
public class UserLevelConstants {

    /**
     * ---------------------------------------
     * --------系统用户等级常量------------------
     * ---------------------------------------
     */
    /** 系统用户等级规则 */
    public static final String SYSTEM_USER_LEVEL_RULE = "userLevelRule";

    /** 是否启用经验参与等级升级（开启后支持 upgradeType=1/3 的消费金额升级） */
    public static final Boolean EXPERIENCE_UPGRADE_ENABLED = true;

    /** 签到是否触发等级升级 */
    public static final Boolean SIGN_LEVEL_UPGRADE_ENABLED = false;

    /** 升级条件：累计消费金额（对应 experience 门槛，1元=1经验） */
    public static final Integer UPGRADE_TYPE_CONSUMPTION = 1;

    /** 升级条件：累计订单数 */
    public static final Integer UPGRADE_TYPE_ORDER_COUNT = 2;

    /** 升级条件：累计消费金额 + 累计订单数（同时满足） */
    public static final Integer UPGRADE_TYPE_BOTH = 3;

    /** 默认等级赠送积分 */
    public static final Integer DEFAULT_GIVE_INTEGRAL = 0;

    /** 消费金额统计时机：已付款 */
    public static final Integer CONSUMPTION_TRIGGER_PAID = 1;

    /** 消费金额统计时机：交易完成 */
    public static final Integer CONSUMPTION_TRIGGER_COMPLETE = 2;

    /** 订单数统计时机：已付款 */
    public static final Integer ORDER_COUNT_TRIGGER_PAID = 1;

    /** 订单数统计时机：交易完成 */
    public static final Integer ORDER_COUNT_TRIGGER_COMPLETE = 2;

}
