package com.qxkj.common.response;

import com.qxkj.common.model.category.Category;
import com.qxkj.common.request.StoreCouponRequest;
import com.qxkj.common.model.product.StoreProduct;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="StoreCouponInfoResponse对象", description="优惠券详情")
public class StoreCouponInfoResponse implements Serializable {

    private static final long serialVersionUID=1L;

    public StoreCouponInfoResponse(StoreCouponRequest coupon, List<StoreProduct> product, List<Category> category) {
        this.coupon = coupon;
        this.product = product;
        this.category = category;
    }

    @ApiModelProperty(value = "优惠券发布id")
    private StoreCouponRequest coupon;

    @ApiModelProperty(value = "商品信息")
    private List<StoreProduct> product;

    @ApiModelProperty(value = "分类信息")
    private List<Category> category;

}
