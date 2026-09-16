package com.zbkj.common.model.stock;

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
 * 订货系统-代理层级配置
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_level")
@ApiModel(value = "StockLevel对象", description = "订货系统-代理层级配置")
public class StockLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "层级名称")
    private String name;

    @ApiModelProperty(value = "排序（小=高层级）")
    private Integer sort;

    @ApiModelProperty(value = "默认拿货折扣%")
    private BigDecimal discount;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;
}
