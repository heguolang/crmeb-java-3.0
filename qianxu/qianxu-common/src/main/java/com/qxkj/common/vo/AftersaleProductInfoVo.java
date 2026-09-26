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
public class AftersaleProductInfoVo {

    /** 商家自定义商品ID */
    @TableField(value = "out_product_id")
    private String outProductId;

    /** 商家自定义sku ID, 如果没有则不填 */
    @TableField(value = "out_sku_id")
    private String outSkuId;

    /** 参与售后的商品数量 */
    @TableField(value = "product_cnt")
    private Integer productCnt;

}
