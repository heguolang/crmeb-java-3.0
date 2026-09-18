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

    @ApiModelProperty(value = "收货地址ID（系统收货地址簿 eb_user_address）")
    @NotNull(message = "请选择收货地址")
    private Integer addressId;

    @ApiModelProperty(value = "库存类型：1=实体 2=虚拟（二期启用虚拟，默认实体）")
    private Integer stockType;

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

        @ApiModelProperty(value = "规格标识（可选，空=商品级）")
        private String skuKey;
    }
}
