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
public class ShopSpuCommonVo {

    /** 交易组件平台内部商品ID */
    @TableField(value = "product_id")
    private Integer productId;

    /** 商家自定义商品ID */
    @TableField(value = "out_product_id")
    private String outProductId;

    /** 默认0:获取线上数据, 1:获取草稿数据 */
    @TableField(value = "need_edit_spu")
    private Integer needEditSpu = 0;
}
