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
 * 订货系统-团队级差阶梯
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_ladder")
@ApiModel(value = "StockLadder对象", description = "订货系统-团队级差阶梯")
public class StockLadder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "团队业绩下限（含）")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "团队业绩上限（0=不限）")
    private BigDecimal maxAmount;

    @ApiModelProperty(value = "阶梯固定奖励金额（元）")
    private BigDecimal reward;

    @ApiModelProperty(value = "奖励比例（%，按团队业绩计算）")
    private BigDecimal rate;

    @ApiModelProperty(value = "排序")
    private Integer sort;
}
