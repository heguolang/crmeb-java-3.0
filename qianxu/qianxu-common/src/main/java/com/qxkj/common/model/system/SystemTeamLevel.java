package com.qxkj.common.model.system;

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
 * 团队等级
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_system_team_level")
@ApiModel(value = "SystemTeamLevel对象", description = "团队等级")
public class SystemTeamLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "团队等级名称")
    private String name;

    @ApiModelProperty(value = "团队等级序号")
    private Integer grade;

    @ApiModelProperty(value = "自购订单金额门槛(元)")
    private BigDecimal selfOrderAmount;

    @ApiModelProperty(value = "团队订单金额门槛(元)")
    private BigDecimal teamOrderAmount;

    @ApiModelProperty(value = "直推订单金额门槛(元)")
    private BigDecimal directOrderAmount;

    @ApiModelProperty(value = "直推订单统计时机：1=支付成功，2=订单完成")
    private Integer directOrderTriggerType;

    @ApiModelProperty(value = "自购与团队条件关系：1=与，2=或")
    private Integer selfTeamRelation;

    @ApiModelProperty(value = "团队与直推条件关系：1=与，2=或")
    private Integer teamDirectRelation;

    @ApiModelProperty(value = "直推金额与直推分销商等级人数条件关系：1=与，2=或")
    private Integer directLevelRelation;

    @ApiModelProperty(value = "直推分销商等级人数-目标分销商等级id(来源分销商等级)，0=未启用")
    private Integer directLevelId;

    @ApiModelProperty(value = "直推达到目标用户等级的人数门槛，0=未启用")
    private Integer directLevelCount;

    @ApiModelProperty(value = "直推分销商等级人数与团队分销商等级人数条件关系：1=与，2=或")
    private Integer teamLevelRelation;

    @ApiModelProperty(value = "团队分销商等级人数-目标分销商等级id(来源分销商等级)，0=未启用")
    private Integer teamLevelId;

    @ApiModelProperty(value = "团队中达到目标用户等级的人数门槛，0=未启用")
    private Integer teamLevelCount;

    @ApiModelProperty(value = "自购订单统计时机：1=支付成功，2=订单完成")
    private Integer selfOrderTriggerType;

    @ApiModelProperty(value = "团队订单统计时机：1=支付成功，2=订单完成")
    private Integer teamOrderTriggerType;

    @ApiModelProperty(value = "等级权益描述")
    private String description;

    @ApiModelProperty(value = "等级图标")
    private String icon;

    @ApiModelProperty(value = "是否显示")
    private Boolean isShow;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "团队等级配置")
    @TableField(exist = false)
    private SystemTeamLevelConfig config;
}
