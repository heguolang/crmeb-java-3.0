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
public class ShopOrderPriceInfoVo {

    /** 该订单最终的金额（单位：分） */
    @TableField(value = "order_price")
    private Long orderPrice;

    /** 运费（单位：分） */
    private Long freight;

    /** 优惠金额（单位：分） */
    @TableField(value = "discounted_price")
    private Long discountedPrice;

    /** 附加金额（单位：分） */
    @TableField(value = "additional_price")
    private Long additionalPrice;

    /** 附加金额备注 */
    @TableField(value = "additional_remarks")
    private String additional_remarks;
}
