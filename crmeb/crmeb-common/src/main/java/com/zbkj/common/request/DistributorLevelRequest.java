package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分销商等级 保存/更新请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DistributorLevelRequest对象", description = "分销商等级保存请求")
public class DistributorLevelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分销等级名称", required = true, example = "金牌分销商")
    @NotBlank(message = "请填写分销等级名称")
    @Size(max = 50, message = "分销等级名称不能超过50个字符")
    private String name;

    @ApiModelProperty(value = "等级权重，数值越大等级越高", required = true, example = "1")
    @NotNull(message = "请填写等级权重")
    @Min(value = 1, message = "等级权重不能小于1")
    @Max(value = 9999, message = "等级权重不能大于9999")
    private Integer grade;

    @ApiModelProperty(value = "自购返佣比例(%)", required = true, example = "3")
    @NotNull(message = "请填写自购返佣比例")
    @Min(value = 0, message = "自购返佣比例不能小于0")
    @Max(value = 100, message = "自购返佣比例不能大于100")
    private Integer selfBrokerageRate;

    @ApiModelProperty(value = "一级返佣比例(%)", required = true, example = "8")
    @NotNull(message = "请填写一级返佣比例")
    @Min(value = 0, message = "一级返佣比例不能小于0")
    @Max(value = 100, message = "一级返佣比例不能大于100")
    private Integer brokerageRateOne;

    @ApiModelProperty(value = "二级返佣比例(%)", required = true, example = "4")
    @NotNull(message = "请填写二级返佣比例")
    @Min(value = 0, message = "二级返佣比例不能小于0")
    @Max(value = 100, message = "二级返佣比例不能大于100")
    private Integer brokerageRateTwo;

    // ==================== 升级条件 ====================

    @ApiModelProperty(value = "升级条件-直推会员人数(人)", example = "0")
    @Min(value = 0, message = "直推会员人数不能小于0")
    private Integer directUserCount;

    @ApiModelProperty(value = "直推会员人数条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer directUserRelation;

    @ApiModelProperty(value = "升级条件-团队会员人数(人)", example = "0")
    @Min(value = 0, message = "团队会员人数不能小于0")
    private Integer teamUserCount;

    @ApiModelProperty(value = "团队会员人数条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer teamUserRelation;

    @ApiModelProperty(value = "升级条件-直推指定等级ID，0=未启用", example = "0")
    @Min(value = 0, message = "直推指定等级ID不能小于0")
    private Integer directLevelId;

    @ApiModelProperty(value = "升级条件-直推指定等级人数(人)", example = "0")
    @Min(value = 0, message = "直推指定等级人数不能小于0")
    private Integer directLevelCount;

    @ApiModelProperty(value = "直推指定等级条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer directLevelRelation;

    @ApiModelProperty(value = "升级条件-累计商城总消费额(元)", example = "800.00")
    @Min(value = 0, message = "累计商城总消费额不能小于0")
    private BigDecimal totalConsumeAmount;

    @ApiModelProperty(value = "累计商城总消费额条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer totalConsumeRelation;

    @ApiModelProperty(value = "升级条件-总充值额(元)", example = "0.00")
    @Min(value = 0, message = "总充值额不能小于0")
    private BigDecimal totalRechargeAmount;

    @ApiModelProperty(value = "总充值额条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer totalRechargeRelation;

    @ApiModelProperty(value = "升级条件-团队商品总消费额(元)", example = "0.00")
    @Min(value = 0, message = "团队商品总消费额不能小于0")
    private BigDecimal teamProductAmount;

    @ApiModelProperty(value = "团队商品总消费额条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer teamProductRelation;

    @ApiModelProperty(value = "升级条件-直推商城消费总额(元)", example = "0.00")
    @Min(value = 0, message = "直推商城消费总额不能小于0")
    private BigDecimal directConsumeAmount;

    @ApiModelProperty(value = "直推商城消费总额条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer directConsumeRelation;

    @ApiModelProperty(value = "升级条件-直推会员商城消费总额(元)", example = "0.00")
    @Min(value = 0, message = "直推会员商城消费总额不能小于0")
    private BigDecimal directUserConsumeAmount;

    @ApiModelProperty(value = "直推会员商城消费总额条件关系：1=与，2=或", example = "1")
    @Min(value = 1, message = "条件关系只能是1(与)或2(或)")
    @Max(value = 2, message = "条件关系只能是1(与)或2(或)")
    private Integer directUserConsumeRelation;

    // ==================== 状态 ====================

    @ApiModelProperty(value = "是否显示：true=显示，false=隐藏", example = "true")
    private Boolean isShow;
}
