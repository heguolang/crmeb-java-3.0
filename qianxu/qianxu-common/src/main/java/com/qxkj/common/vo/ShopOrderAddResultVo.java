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
public class ShopOrderAddResultVo {

    /** 交易组件平台订单ID */
    @TableField(value = "order_id")
    private Long orderId;

    /** 交易组件平台订单ID */
    @TableField(value = "out_order_id")
    private String outOrderId;

    /** 拉起收银台的ticket */
    private String ticket;

    /** ticket有效截止时间 */
    @TableField(value = "ticket_expire_time")
    private String ticketExpireTime;

    /** 订单最终价格（单位：分） */
    @TableField(value = "final_price")
    private Long finalPrice;
}
