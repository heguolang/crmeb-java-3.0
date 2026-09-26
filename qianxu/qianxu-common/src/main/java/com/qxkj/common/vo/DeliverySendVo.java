package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

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
public class DeliverySendVo {

    /** 订单ID */
    @TableField(value = "order_id")
    private Integer orderId;

    /** 商家自定义订单ID，与 order_id 二选一 */
    @TableField(value = "out_order_id")
    private String outOrderId;

    /** 用户的openid */
    private String openid;

    /** 发货完成标志位, 0: 未发完, 1:已发完 */
    @TableField(value = "finish_all_delivery")
    private Integer finishSllDelivery;

    /** 快递信息,delivery_type=1时必填 */
    @TableField(value = "delivery_list")
    private List<DeliveryInfoVo> deliveryList;
}
