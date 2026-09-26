package com.qxkj.common.response;

import com.qxkj.common.model.product.StoreProduct;
import com.qxkj.common.model.product.StoreProductAttr;
import com.qxkj.common.model.product.StoreProductGuarantee;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
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
@ApiModel(value="ProductDetailResponse对象", description="商品详情H5")
public class ProductDetailResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "产品属性")
    private List<StoreProductAttr> productAttr;

    @ApiModelProperty(value = "商品属性详情")
    private HashMap<String, StoreProductAttrValueResponse> productValue;

    @ApiModelProperty(value = "返佣金额区间")
    private String priceName;

    @ApiModelProperty(value = "为移动端特定参数 所有参与的活动")
    private List<ProductActivityItemResponse> activityAllH5;

    @ApiModelProperty(value = "商品信息")
    private StoreProduct productInfo;

    @ApiModelProperty(value = "收藏标识")
    private Boolean userCollect;

    @ApiModelProperty(value = "保障服务")
    private List<StoreProductGuarantee> guaranteeList;
}
