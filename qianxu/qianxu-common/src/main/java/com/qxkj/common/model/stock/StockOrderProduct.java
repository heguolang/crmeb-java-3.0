package com.qxkj.common.model.stock;

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
 * 订货系统-订货订单明细
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_order_product")
@ApiModel(value = "StockOrderProduct对象", description = "订货系统-订货订单明细")
public class StockOrderProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订货订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "规格标识（空=商品级）")
    private String skuKey;

    @ApiModelProperty(value = "商品名称（冗余）")
    private String productName;

    @ApiModelProperty(value = "商品图（冗余）")
    private String image;

    @ApiModelProperty(value = "数量")
    private Integer num;

    @ApiModelProperty(value = "下单者拿货价（单价）")
    private BigDecimal price;

    @ApiModelProperty(value = "上级拿货价（单价，0=上级为总部无差价）")
    private BigDecimal parentPrice;

    @ApiModelProperty(value = "小计 = num * price")
    private BigDecimal totalPrice;
}
