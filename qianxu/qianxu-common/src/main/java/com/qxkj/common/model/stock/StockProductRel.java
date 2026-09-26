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
import java.util.Date;

/**
 * 订货系统-参与订货的商品（加入制，人工添加后才在订货模块可见）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_product_rel")
@ApiModel(value = "StockProductRel对象", description = "订货系统-参与订货的商品关联")
public class StockProductRel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商品ID（eb_store_product.id）")
    private Integer productId;

    @ApiModelProperty(value = "是否支持虚拟库存下单")
    private Boolean supportVirtual;

    @ApiModelProperty(value = "是否支持实体库存下单")
    private Boolean supportPhysical;

    @ApiModelProperty(value = "添加时间")
    private Date createTime;
}
