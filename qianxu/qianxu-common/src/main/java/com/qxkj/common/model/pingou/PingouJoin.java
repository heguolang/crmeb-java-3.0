package com.qxkj.common.model.pingou;

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
 * 拼购参与明细表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_pingou_join")
@ApiModel(value = "PingouJoin对象", description = "拼购参与明细表")
public class PingouJoin implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "参与ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "团期ID")
    private Integer teamId;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "参与用户UID")
    private Integer uid;

    @ApiModelProperty(value = "订单主键ID")
    private Integer orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "拼购商品ID")
    private Integer productId;

    @ApiModelProperty(value = "参与金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "直接推荐人UID")
    private Integer spreadUid;

    @ApiModelProperty(value = "是否中奖：1=中奖 0=未中奖")
    private Integer isWin;

    @ApiModelProperty(value = "未中奖奖励发放状态：0=未发放 1=已发放")
    private Integer rewardStatus;

    @ApiModelProperty(value = "已退本金")
    private BigDecimal refundPrice;

    @ApiModelProperty(value = "已发补贴佣金")
    private BigDecimal subsidyPrice;

    @ApiModelProperty(value = "本次分享奖励比例（‰）")
    private BigDecimal shareRate;

    @ApiModelProperty(value = "已发分享奖励")
    private BigDecimal sharePrice;

    @ApiModelProperty(value = "分享奖励发放状态：0=未发放 1=已发放 2=无推荐人不发")
    private Integer shareStatus;

    @ApiModelProperty(value = "参与时间")
    private Date createTime;
}
