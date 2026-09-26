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
public class ShopOrderAddVo {

    /** 创建时间 */
    @TableField(value = "create_time")
    private String createTime;

    /** 商家自定义订单ID */
    @TableField(value = "out_order_id")
    private String outOrderId;

    /** 用户的openid */
    private String openid;

    /** 商家小程序该订单的页面path，用于微信侧订单中心跳转 */
    private String path;

    /** 下单时小程序的场景值，可通getLaunchOptionsSync或onLaunch/onShow拿到 */
    private Integer scene;

    /** 订单详情 */
    @TableField(value = "order_detail")
    private ShopOrderDetailAddVo orderDetail;

    /** 交付详情 */
    @TableField(value = "delivery_detail")
    private ShopOrderDeliveryDetailAddVo deliveryDetail;

    /** 地址详情 */
    @TableField(value = "address_info")
    private ShopOrderAddressInfoAddVo addressInfo;

    /** 用户id */
    private Integer outUserId;
}
