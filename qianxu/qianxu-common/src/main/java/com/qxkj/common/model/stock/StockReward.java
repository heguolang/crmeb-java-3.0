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
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订货系统-奖金明细
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_stock_reward")
@ApiModel(value = "StockReward对象", description = "订货系统-奖金明细")
public class StockReward implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer TYPE_DIFF = 1;      // 差价奖励
    public static final Integer TYPE_LADDER = 2;    // 团队级差奖励
    public static final Integer TYPE_PEER = 3;      // 平级奖励
    public static final Integer TYPE_COST = 4;      // 货款成本回款（下级订单货款中的进货成本部分）

    public static final Integer SOURCE_ORDER = 1;   // 订货单
    public static final Integer SOURCE_EXCHANGE = 2; // 换货单

    public static final Integer STATUS_CREDITED = 1;    // 已入账
    public static final Integer STATUS_INVALID = -1;    // 已失效

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "得奖代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "奖励类型：1=差价 2=级差 3=平级")
    private Integer type;

    @ApiModelProperty(value = "业绩来源：1=订货单 2=换货单")
    private Integer source;

    @ApiModelProperty(value = "关联单号")
    private String orderNo;

    @ApiModelProperty(value = "产生业绩的下级用户UID")
    private Integer linkUid;

    @ApiModelProperty(value = "计算基数")
    private BigDecimal basePrice;

    @ApiModelProperty(value = "比例（%）")
    private BigDecimal rate;

    @ApiModelProperty(value = "奖励金额")
    private BigDecimal rewardPrice;

    @ApiModelProperty(value = "说明")
    private String mark;

    @ApiModelProperty(value = "状态：1=已入账 -1=已失效")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "得奖用户昵称")
    @TableField(exist = false)
    private String nickname;

    @ApiModelProperty(value = "得奖用户头像")
    @TableField(exist = false)
    private String avatar;

    @ApiModelProperty(value = "业绩产生用户头像（下单人）")
    @TableField(exist = false)
    private String linkAvatar;

    @ApiModelProperty(value = "业绩产生用户昵称（下单人）")
    @TableField(exist = false)
    private String linkNickname;

    @ApiModelProperty(value = "业绩产生用户手机号（下单人）")
    @TableField(exist = false)
    private String linkPhone;

    @ApiModelProperty(value = "关联订单商品明细摘要，如「某某商品×2」")
    @TableField(exist = false)
    private String productNames;
}
