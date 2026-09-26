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
public class ShopCatDetailVo {

    /** 类目ID */
    @TableField(value = "third_cat_id")
    private Integer thirdCatId;

    /** 类目名称 */
    @TableField(value = "third_cat_name")
    private String thirdCatName;

    /** 类目资质 */
    private String qualification;

    /** 类目资质类型,0:不需要,1:必填,2:选填 */
    @TableField(value = "qualification_type")
    private Integer qualificationType;

    /** 商品资质 */
    @TableField(value = "product_qualification")
    private String productQualification;

    /** 商品资质类型,0:不需要,1:必填,2:选填 */
    @TableField(value = "product_qualification_type")
    private Integer productQualificationType;

    /** 二级类目ID */
    @TableField(value = "second_cat_id")
    private Integer secondCatId;

    /** 二级类目名称 */
    @TableField(value = "second_cat_name")
    private String secondCatName;

    /** 一级类目ID */
    @TableField(value = "first_cat_id")
    private Integer firstCatId;

    /** 一级类目名称 */
    @TableField(value = "first_cat_name")
    private String firstCatName;
}
