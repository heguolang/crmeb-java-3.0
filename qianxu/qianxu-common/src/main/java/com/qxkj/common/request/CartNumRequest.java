package com.qxkj.common.request;

import com.qxkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.*;
import java.io.Serializable;

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
@ApiModel(value="CartRequest对象", description="购物车数量请求对象")
public class CartNumRequest implements Serializable {

    private static final long serialVersionUID = -1186533756329913311L;

    @ApiModelProperty(value = "数量类型：total-商品数量，sum-购物数量", required = true)
    @NotEmpty(message = "数量类型不能为空")
    @StringContains(limitValues = {"total", "sum"}, message = "无效数量类型")
    private String type;

    @ApiModelProperty(value = "商品类型：true-有效商品，false-无效商品", required = true)
    @NotNull(message = "商品类型不能为空")
    private Boolean numType;

}
