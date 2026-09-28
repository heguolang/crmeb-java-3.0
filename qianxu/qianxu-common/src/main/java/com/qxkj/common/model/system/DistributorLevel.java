package com.qxkj.common.model.system;

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
 * 分销商等级
 * 独立于会员等级（eb_system_user_level）的分销等级体系，
 * 包含等级名称、等级权重、返佣比例与升级条件配置。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_distributor_level")
@ApiModel(value = "DistributorLevel对象", description = "分销商等级")
public class DistributorLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "分销等级名称")
    private String name;

    @ApiModelProperty(value = "等级权重，数值越大等级越高")
    private Integer grade;

    @ApiModelProperty(value = "自购返佣比例(%)")
    private Integer selfBrokerageRate;

    @ApiModelProperty(value = "一级返佣比例(%)")
    private Integer brokerageRateOne;

    @ApiModelProperty(value = "二级返佣比例(%)")
    private Integer brokerageRateTwo;

    // ==================== 升级条件 ====================

    @ApiModelProperty(value = "升级条件-直推会员人数(人)")
    private Integer directUserCount;

    @ApiModelProperty(value = "直推会员人数条件关系：1=与，2=或")
    private Integer directUserRelation;

    @ApiModelProperty(value = "升级条件-团队会员人数(人)")
    private Integer teamUserCount;

    @ApiModelProperty(value = "团队会员人数条件关系：1=与，2=或")
    private Integer teamUserRelation;

    @ApiModelProperty(value = "升级条件-直推指定等级ID，0=未启用")
    private Integer directLevelId;

    @ApiModelProperty(value = "升级条件-直推指定等级人数(人)")
    private Integer directLevelCount;

    @ApiModelProperty(value = "直推指定等级条件关系：1=与，2=或")
    private Integer directLevelRelation;

    @ApiModelProperty(value = "升级条件-累计商城总消费额(元)")
    private BigDecimal totalConsumeAmount;

    @ApiModelProperty(value = "累计商城总消费额条件关系：1=与，2=或")
    private Integer totalConsumeRelation;

    @ApiModelProperty(value = "升级条件-总充值额(元)")
    private BigDecimal totalRechargeAmount;

    @ApiModelProperty(value = "总充值额条件关系：1=与，2=或")
    private Integer totalRechargeRelation;

    @ApiModelProperty(value = "升级条件-团队商品总消费额(元)")
    private BigDecimal teamProductAmount;

    @ApiModelProperty(value = "团队商品总消费额条件关系：1=与，2=或")
    private Integer teamProductRelation;

    @ApiModelProperty(value = "升级条件-直推商城消费总额(元)")
    private BigDecimal directConsumeAmount;

    @ApiModelProperty(value = "直推商城消费总额条件关系：1=与，2=或")
    private Integer directConsumeRelation;

    @ApiModelProperty(value = "升级条件-直推会员商城消费总额(元)，已下线，列保留不再使用")
    private BigDecimal directUserConsumeAmount;

    @ApiModelProperty(value = "直推会员商城消费总额条件关系：1=与，2=或，已下线，列保留不再使用")
    private Integer directUserConsumeRelation;

    @ApiModelProperty(value = "升级条件-下单指定商品ID，逗号分隔，空=未启用")
    private String orderProductIds;

    @ApiModelProperty(value = "下单指定商品条件关系：1=与，2=或")
    private Integer orderProductRelation;

    @ApiModelProperty(value = "下单指定商品达成方式：1=任买一件即可，2=需全部购买")
    private Integer orderProductMode;

    // ==================== 状态 ====================

    @ApiModelProperty(value = "是否显示：1=显示，0=隐藏")
    private Boolean isShow;

    @ApiModelProperty(value = "是否删除：0=否，1=是")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
