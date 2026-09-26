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
public class ShopAftersaleVo {

    /** 发起售后的订单ID */
    @TableField(value = "order_id")
    private Integer order_id;

    /** 商家自定义订单ID */
    @TableField(value = "out_order_id")
    private String outOrderId;

    /** 商家自定义售后ID，与aftersale_id二选一 */
    @TableField(value = "out_aftersale_id")
    private String outAftersaleId;

    /** 商家小程序该售后单的页面path，不存在则使用订单path */
    private String path;

    /** 用户的openid */
    private String openid;

    /** 售后类型，1:退款,2:退款退货,3:换货 */
    private Integer type;

    /** 发起申请时间，yyyy-MM-dd HH:mm:ss */
    @TableField(value = "create_time")
    private String createTime;

    /** 0:未受理,1:用户取消,2:商家受理中,3:商家逾期未处理,4:商家拒绝退款,5:商家拒绝退货退款,6:待买家退货,7:退货退款关闭,8:待商家收货,11:商家退款中,12:商家逾期未退款,13:退款完成,14:退货退款完成 */
    private String status;

    /** 退货相关商品列表 */
    @TableField(value = "product_infos")
    private List<AftersaleProductInfoVo> product_infos;

}
