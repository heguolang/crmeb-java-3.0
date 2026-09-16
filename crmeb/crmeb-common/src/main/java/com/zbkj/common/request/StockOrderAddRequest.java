package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 订货下单请求
 */
@Data
@ApiModel(value = "StockOrderAddRequest对象", description = "订货下单请求")
public class StockOrderAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品明细")
    @Valid
    @NotEmpty(message = "请选择订货商品")
    private List<StockOrderItem> items;

    @ApiModelProperty(value = "付款方式：1=微信线上支付 2=后台记账欠款")
    private Integer payType = 2;

    @ApiModelProperty(value = "订单备注")
    private String mark;

    @Data
    @ApiModel(value = "StockOrderItem对象", description = "订货商品项")
    public static class StockOrderItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "商品ID")
        @NotNull(message = "商品不能为空")
        private Integer productId;

        @ApiModelProperty(value = "数量")
        @NotNull(message = "数量不能为空")
        private Integer num;
    }
}
