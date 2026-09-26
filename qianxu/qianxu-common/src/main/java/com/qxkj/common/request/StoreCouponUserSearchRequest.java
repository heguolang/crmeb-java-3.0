package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

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
@TableName("eb_store_coupon_user")
@ApiModel(value="StoreCouponUser对象", description="优惠券领取搜索")
public class StoreCouponUserSearchRequest extends UserCommonSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    //@ApiModelProperty(value = "优惠券所属用户")
    //private Integer uid;
    //
    //@ApiModelProperty(value = "优惠券id")
    //private Integer couponId;

    @ApiModelProperty(value = "优惠券名称")
    private String name;

    @ApiModelProperty(value = "状态（0：未使用，1：已使用, 2:已过期）")
    private Integer status;

    //@ApiModelProperty(value = "最低消费多少金额可用优惠券")
    //private BigDecimal minPrice;


}
