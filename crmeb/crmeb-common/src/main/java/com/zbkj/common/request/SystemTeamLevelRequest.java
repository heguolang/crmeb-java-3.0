package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 团队等级请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SystemTeamLevelRequest对象", description = "团队等级")
public class SystemTeamLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团队等级id")
    private Integer id;

    @ApiModelProperty(value = "团队等级名称")
    @NotBlank(message = "团队等级名称不能为空")
    @Length(max = 50, message = "团队等级名称不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "团队等级序号")
    @NotNull(message = "团队等级序号不能为空")
    @Min(value = 1, message = "团队等级序号最小为1")
    private Integer grade;

    @ApiModelProperty(value = "自购订单金额门槛(元)", example = "1000.00")
    @NotNull(message = "自购订单金额不能为空")
    @DecimalMin(value = "0", message = "自购订单金额不能小于0")
    private BigDecimal selfOrderAmount;

    @ApiModelProperty(value = "团队订单金额门槛(元)", example = "5000.00")
    @NotNull(message = "团队订单金额不能为空")
    @DecimalMin(value = "0", message = "团队订单金额不能小于0")
    private BigDecimal teamOrderAmount;

    @ApiModelProperty(value = "直推订单金额门槛(元)", example = "2000.00")
    @NotNull(message = "直推订单金额不能为空")
    @DecimalMin(value = "0", message = "直推订单金额不能小于0")
    private BigDecimal directOrderAmount;

    @ApiModelProperty(value = "直推订单统计时机：1=支付成功，2=订单完成", example = "2")
    private Integer directOrderTriggerType;

    @ApiModelProperty(value = "自购与团队条件关系：1=与，2=或", example = "1")
    @NotNull(message = "自购与团队条件关系不能为空")
    @Min(value = 1, message = "条件关系参数不合法")
    @Max(value = 2, message = "条件关系参数不合法")
    private Integer selfTeamRelation;

    @ApiModelProperty(value = "团队与直推条件关系：1=与，2=或", example = "1")
    @NotNull(message = "团队与直推条件关系不能为空")
    @Min(value = 1, message = "条件关系参数不合法")
    @Max(value = 2, message = "条件关系参数不合法")
    private Integer teamDirectRelation;

    @ApiModelProperty(value = "直推金额与直推分销商等级人数条件关系：1=与，2=或", example = "1")
    @NotNull(message = "直推金额与直推分销商等级人数条件关系不能为空")
    @Min(value = 1, message = "条件关系参数不合法")
    @Max(value = 2, message = "条件关系参数不合法")
    private Integer directLevelRelation;

    @ApiModelProperty(value = "直推分销商等级人数-目标分销商等级id，0=未启用", example = "0")
    @NotNull(message = "目标用户等级不能为空")
    @Min(value = 0, message = "目标用户等级参数不合法")
    private Integer directLevelId;

    @ApiModelProperty(value = "直推达到目标用户等级的人数门槛，0=未启用", example = "0")
    @NotNull(message = "直推分销商等级人数门槛不能为空")
    @Min(value = 0, message = "直推分销商等级人数门槛不能小于0")
    private Integer directLevelCount;

    @ApiModelProperty(value = "直推分销商等级人数与团队分销商等级人数条件关系：1=与，2=或", example = "1")
    @NotNull(message = "直推分销商等级人数与团队分销商等级人数条件关系不能为空")
    @Min(value = 1, message = "条件关系参数不合法")
    @Max(value = 2, message = "条件关系参数不合法")
    private Integer teamLevelRelation;

    @ApiModelProperty(value = "团队分销商等级人数-目标分销商等级id，0=未启用", example = "0")
    @NotNull(message = "团队目标用户等级不能为空")
    @Min(value = 0, message = "团队目标用户等级参数不合法")
    private Integer teamLevelId;

    @ApiModelProperty(value = "团队中达到目标用户等级的人数门槛，0=未启用", example = "0")
    @NotNull(message = "团队分销商等级人数门槛不能为空")
    @Min(value = 0, message = "团队分销商等级人数门槛不能小于0")
    private Integer teamLevelCount;

    @ApiModelProperty(value = "自购订单统计时机：1=支付成功，2=订单完成", example = "2")
    private Integer selfOrderTriggerType;

    @ApiModelProperty(value = "团队订单统计时机：1=支付成功，2=订单完成", example = "2")
    private Integer teamOrderTriggerType;

    @ApiModelProperty(value = "等级权益描述")
    @Length(max = 500, message = "权益描述不能超过500个字符")
    private String description;

    @ApiModelProperty(value = "等级图标")
    private String icon;

    @ApiModelProperty(value = "是否显示")
    @NotNull(message = "是否显示不能为空")
    private Boolean isShow;

    @ApiModelProperty(value = "团队等级配置")
    @Valid
    private SystemTeamLevelConfigRequest config;
}
