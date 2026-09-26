package com.qxkj.common.model.agent;

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
 * 区域代理奖励明细表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_agent_reward")
@ApiModel(value = "AgentReward对象", description = "区域代理奖励明细表")
public class AgentReward implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Integer STATUS_WAIT = 1;
    public static final Integer STATUS_CREDITED = 2;
    public static final Integer STATUS_INVALID = 3;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "代理ID")
    private Integer agentId;

    @ApiModelProperty(value = "代理用户UID")
    private Integer uid;

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "订单实付金额")
    private BigDecimal orderPayPrice;

    @ApiModelProperty(value = "奖励比例（%）")
    private BigDecimal ratio;

    @ApiModelProperty(value = "奖励金额")
    private BigDecimal rewardPrice;

    @ApiModelProperty(value = "命中的代理区域")
    private String regionName;

    @ApiModelProperty(value = "关联佣金记录ID")
    private Integer recordId;

    @ApiModelProperty(value = "状态：1=待入账 2=已入账 3=已失效")
    private Integer status;

    @ApiModelProperty(value = "入账时间")
    private Date creditTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "代理昵称")
    @TableField(exist = false)
    private String nickname;
}
