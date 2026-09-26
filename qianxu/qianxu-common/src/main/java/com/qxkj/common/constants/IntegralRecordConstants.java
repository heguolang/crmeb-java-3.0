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
public class IntegralRecordConstants {

    /** 佣金记录类型—增加 */
    public static final Integer INTEGRAL_RECORD_TYPE_ADD = 1;

    /** 佣金记录类型—扣减 */
    public static final Integer INTEGRAL_RECORD_TYPE_SUB = 2;

    /** 佣金记录状态—创建 */
    public static final Integer INTEGRAL_RECORD_STATUS_CREATE = 1;

    /** 佣金记录状态—冻结期 */
    public static final Integer INTEGRAL_RECORD_STATUS_FROZEN = 2;

    /** 佣金记录状态—完成 */
    public static final Integer INTEGRAL_RECORD_STATUS_COMPLETE = 3;

    /** 佣金记录状态—失效（订单退款） */
    public static final Integer INTEGRAL_RECORD_STATUS_INVALIDATION = 4;

    /** 佣金记录关联类型—订单 */
    public static final String INTEGRAL_RECORD_LINK_TYPE_ORDER = "order";

    /** 佣金记录关联类型—签到 */
    public static final String INTEGRAL_RECORD_LINK_TYPE_SIGN = "sign";

    /** 佣金记录关联类型—系统后台 */
    public static final String INTEGRAL_RECORD_LINK_TYPE_SYSTEM = "system";

    /** 佣金记录标题—用户订单付款成功 */
    public static final String BROKERAGE_RECORD_TITLE_ORDER = "用户订单付款成功";

    /** 佣金记录标题—签到经验奖励 */
    public static final String BROKERAGE_RECORD_TITLE_SIGN = "签到积分奖励";

    /** 佣金记录标题—后台积分操作 */
    public static final String BROKERAGE_RECORD_TITLE_SYSTEM = "后台积分操作";

    /** 佣金记录标题—订单退款 */
    public static final String BROKERAGE_RECORD_TITLE_REFUND = "订单退款";
}
