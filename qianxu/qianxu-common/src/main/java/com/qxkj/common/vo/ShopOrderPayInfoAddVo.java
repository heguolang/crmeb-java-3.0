package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

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
@Data
public class ShopOrderPayInfoAddVo {

    /** 支付方式（目前只有"微信支付"） */
    @TableField(value = "pay_method")
    private String payMethod;

    /** 支付方式，0，微信支付，1: 货到付款，99: 其他（默认0） */
    @TableField(value = "pay_method_type")
    private Integer payMethodType = 0;

    /** 预支付ID */
    @TableField(value = "prepay_id")
    private String prepayId;

    /** 预付款时间（拿到prepay_id的时间） */
    @TableField(value = "prepay_time")
    private String prepayTime;
}
