package com.zbkj.common.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 等级统计幂等流水表
 * 同一订单在同一模块同一场景下只允许统计一次，防止队列重投导致的重复累加/重复回退。
 * module：TEAM=团队等级，DISTRIBUTOR=分销商等级
 * scene ：PAID=支付成功，COMPLETE=订单完成，REFUND=退款回退
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_level_stat_order_log")
@ApiModel(value = "LevelStatOrderLog对象", description = "等级统计幂等流水")
public class LevelStatOrderLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "模块：TEAM / DISTRIBUTOR")
    private String module;

    @ApiModelProperty(value = "场景：PAID / COMPLETE / REFUND")
    private String scene;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
