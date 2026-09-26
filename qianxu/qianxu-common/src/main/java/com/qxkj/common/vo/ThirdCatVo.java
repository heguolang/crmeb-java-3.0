package com.qxkj.common.vo;

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
public class ThirdCatVo {

    /** 类目ID */
    private Integer thirdCatId;

    /** 类目名称 */
    private String thirdCatName;

    /** 类目资质 */
    private String qualification;

    /** 类目资质类型,0:不需要,1:必填,2:选填 */
    private Integer qualificationType;

    /** 商品资质 */
    private String productQualification;

    /** 商品资质类型,0:不需要,1:必填,2:选填 */
    private Integer productQualificationType;

}
