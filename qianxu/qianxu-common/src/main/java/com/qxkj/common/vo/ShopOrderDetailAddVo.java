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
public class ShopOrderDetailAddVo {

    /** 商品详情数组 */
    @TableField(value = "product_infos")
    private List<ShopOrderProductInfoAddVo> productInfos;

    /** 支付详情数组 */
    @TableField(value = "pay_info")
    private ShopOrderPayInfoAddVo payInfo;

    /** 价格详情数组 */
    @TableField(value = "price_info")
    private ShopOrderPriceInfoVo priceInfo;
}
