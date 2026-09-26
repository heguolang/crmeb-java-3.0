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
import java.util.Date;

/**
 * 订货系统-换货设置（精确到规格）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_exchange_config")
@ApiModel(value = "StockExchangeConfig对象", description = "订货系统-换货设置")
public class StockExchangeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "规格key（空=该商品整品）")
    private String skuKey;

    @ApiModelProperty(value = "是否允许换货")
    private Boolean enable;

    @ApiModelProperty(value = "换入商品最低价（目标只能>=原价，此值可再抬高门槛）")
    private BigDecimal minTargetPrice;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
