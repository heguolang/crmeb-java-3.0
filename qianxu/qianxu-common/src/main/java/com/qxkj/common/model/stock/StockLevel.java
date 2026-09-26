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

    @ApiModelProperty(value = "启用条件：自购消费满额自动升级")
    private Boolean condSelfBuy;

    @ApiModelProperty(value = "自购消费门槛（元）")
    private BigDecimal selfBuyAmount;

    @ApiModelProperty(value = "启用条件：直推订单总业绩")
    private Boolean condDirect;

    @ApiModelProperty(value = "直推订单总业绩门槛（元）")
    private BigDecimal directOrderAmount;

    @ApiModelProperty(value = "启用条件：团队伞下业绩")
    private Boolean condTeam;

    @ApiModelProperty(value = "团队伞下业绩门槛（元）")
    private BigDecimal teamAmount;

    @ApiModelProperty(value = "启用条件：购买指定产品升级")
    private Boolean condProduct;

    @ApiModelProperty(value = "指定升级产品ID，英文逗号分隔")
    private String upgradeProductIds;

    @ApiModelProperty(value = "条件组合：0=满足任一(或) 1=全部满足(与)")
    private Integer conditionLogic;

    @ApiModelProperty(value = "平级奖比例(%)")
    private BigDecimal peerRate;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;
}
