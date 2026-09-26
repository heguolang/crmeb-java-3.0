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
 * 订货系统-换货可选目标（源商品/规格 → 可换入的目标商品/规格）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_exchange_target")
@ApiModel(value = "StockExchangeTarget对象", description = "订货系统-换货可选目标")
public class StockExchangeTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "源商品ID")
    private Integer productId;

    @ApiModelProperty(value = "源规格key（空=整品）")
    private String skuKey;

    @ApiModelProperty(value = "可换入的商品ID")
    private Integer targetProductId;

    @ApiModelProperty(value = "可换入的规格key（空=整品）")
    private String targetSkuKey;

    @ApiModelProperty(value = "目标商品名称（冗余）")
    private String targetProductName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
