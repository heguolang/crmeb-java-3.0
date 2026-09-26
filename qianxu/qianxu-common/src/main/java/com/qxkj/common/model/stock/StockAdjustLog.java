package com.qxkj.common.model.stock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * 订货系统-订货商库存调整记录（后台「调整虚拟库存 / 调整实体库存」）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_adjust_log")
@ApiModel(value = "StockAdjustLog对象", description = "订货系统-订货商库存调整记录")
public class StockAdjustLog implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer STOCK_TYPE_PHYSICAL = 1; // 实体库存调整
    public static final Integer STOCK_TYPE_VIRTUAL = 2;  // 虚拟库存调整（仅留痕，不参与实体推导）

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订货商ID")
    private Integer agentId;

    @ApiModelProperty(value = "会员UID")
    private Integer uid;

    @ApiModelProperty(value = "商品ID")
    private Integer productId;

    @ApiModelProperty(value = "规格key（空=整品）")
    private String skuKey;

    @ApiModelProperty(value = "库存类型：1=实体 2=虚拟")
    private Integer stockType;

    @ApiModelProperty(value = "调整数量（正=增加，负=扣减）")
    private Integer num;

    @ApiModelProperty(value = "调整原因")
    private String mark;

    @ApiModelProperty(value = "关联下级UID：虚拟库存转卖给下级时的采购人（下单的下级会员UID），0=无")
    private Integer linkUid;

    @ApiModelProperty(value = "是否删除")
    private Integer isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    // ==================== 展示字段（不入库） ====================

    @ApiModelProperty(value = "采购人昵称（关联下级）")
    @TableField(exist = false)
    private String linkNickname;

    @ApiModelProperty(value = "采购人手机号（关联下级）")
    @TableField(exist = false)
    private String linkPhone;

    @ApiModelProperty(value = "采购人代理等级名（关联下级）")
    @TableField(exist = false)
    private String linkAgentName;

    @ApiModelProperty(value = "关联单号（虚拟库存转卖对应的下级订货单号）")
    @TableField(exist = false)
    private String orderNo;
}
