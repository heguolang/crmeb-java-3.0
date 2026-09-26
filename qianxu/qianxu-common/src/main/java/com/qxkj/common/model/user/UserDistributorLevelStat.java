package com.qxkj.common.model.user;

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
 * 分销商等级统计表
 * 持久化累加，口径统一为「支付成功」，退款时回退（与分销佣金结算口径一致）。
 * 人数类指标不落表，判定时实时统计（与团队等级同一策略）。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_user_distributor_level_stat")
@ApiModel(value = "UserDistributorLevelStat对象", description = "分销商等级统计")
public class UserDistributorLevelStat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "累计商城总消费额（本人）")
    private BigDecimal totalConsumeAmount;

    @ApiModelProperty(value = "直推商城消费总额（一级下级订单）")
    private BigDecimal directConsumeAmount;

    @ApiModelProperty(value = "直推会员商城消费总额（一级下级且会员等级>0）")
    private BigDecimal directUserConsumeAmount;

    @ApiModelProperty(value = "团队商品总消费额（整条推荐链下级订单）")
    private BigDecimal teamProductAmount;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
