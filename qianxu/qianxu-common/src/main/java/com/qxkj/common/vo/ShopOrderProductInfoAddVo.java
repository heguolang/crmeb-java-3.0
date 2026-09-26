package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.models.auth.In;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

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
public class ShopOrderProductInfoAddVo {

    /** 商家自定义商品ID */
    @TableField(value = "out_product_id")
    private String outProductId;

    /** 商家自定义商品skuID，可填空字符串（如果这个product_id下没有sku） */
    @TableField(value = "out_sku_id")
    private String outSkuId;

    /** 购买的数量 */
    @TableField(value = "product_cnt")
    private Integer productCnt;

    /** 生成订单时商品的售卖价（单位：分），可以跟上传商品接口的价格不一致 */
    @TableField(value = "sale_price")
    private Long salePrice;

    /** 生成订单时商品的头图 */
    @TableField(value = "head_img")
    private String headImg;

    /** 生成订单时商品的标题 */
    private String title;

    /** 绑定的小程序商品路径 */
    private String path;
}
