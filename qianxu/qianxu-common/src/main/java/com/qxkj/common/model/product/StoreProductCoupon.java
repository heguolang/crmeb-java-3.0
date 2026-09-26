package com.qxkj.common.model.product;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("eb_store_product_coupon")
@ApiModel(value="StoreProductCoupon对象", description="")
public class StoreProductCoupon implements Serializable {

    private static final long serialVersionUID=1L;

    public StoreProductCoupon() {
    }

    public StoreProductCoupon(Integer productId, Integer issueCouponId, Integer addTime) {
        this.productId = productId;
        this.issueCouponId = issueCouponId;
        this.addTime = addTime;
    }

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商品id")
    private Integer productId;

    @ApiModelProperty(value = "优惠劵id")
    private Integer issueCouponId;

    @ApiModelProperty(value = "添加时间")
    private Integer addTime;


}
