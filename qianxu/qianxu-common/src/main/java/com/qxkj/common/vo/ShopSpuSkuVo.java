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
public class ShopSpuSkuVo {

    /** 商家自定义商品ID */
    @TableField(value = "out_product_id")
    private String outProductId;

    /** 商家自定义skuID */
    @TableField(value = "out_sku_id")
    private String outSkuId;

    /** sku小图 */
    @TableField(value = "thumb_img")
    private String thumbImg;

    /** 售卖价格,以分为单位 */
    @TableField(value = "sale_price")
    private Long salePrice;

    /** 市场价格,以分为单位 */
    @TableField(value = "market_price")
    private Long marketPrice;

    /** 库存 */
    @TableField(value = "stock_num")
    private Integer stockNum;

    /** 条形码 */
    private String barcode;

    /** 商品编码 */
    @TableField(value = "sku_code")
    private String skuCode;

    /** 销售属性（自定义） */
    @TableField(value = "sku_attrs")
    private List<ShopSpuSkuAttrVo> skuAttrs;
}
