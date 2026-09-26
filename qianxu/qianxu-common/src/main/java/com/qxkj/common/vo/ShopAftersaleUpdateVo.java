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
public class ShopAftersaleUpdateVo {

    /** 商家自定义订单ID */
    @TableField(value = "out_order_id")
    private String outOrderId;

    /** 商家自定义售后ID */
    @TableField(value = "out_aftersale_id")
    private String outAftersaleId;

    /** 用户的openid */
    private String openid;

    /** 0:未受理,1:用户取消,2:商家受理中,3:商家逾期未处理,4:商家拒绝退款,5:商家拒绝退货退款,6:待买家退货,7:退货退款关闭,8:待商家收货,11:商家退款中,12:商家逾期未退款,13:退款完成,14:退货退款完成 */
    private Integer status;

    /** 0:售后未结束, 1:售后结束且订单状态流转 */
    @TableField(value = "finish_all_aftersale")
    private Integer finishAllAftersale;

}
