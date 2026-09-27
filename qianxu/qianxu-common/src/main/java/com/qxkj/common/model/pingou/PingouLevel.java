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
 * 拼购等级设置表（全局默认分享奖励阶梯）
 * 活动未单独设置分享奖励阶梯时，结算按本表等级比例兜底：
 * 直推人数达到 min_direct_num（含）时，按满足条件的最高一档 reward_rate% 发放。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_pingou_level")
@ApiModel(value = "PingouLevel对象", description = "拼购等级设置表（全局默认分享奖励阶梯）")
public class PingouLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "等级ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "等级名称（如 A级）")
    private String name;

    @ApiModelProperty(value = "直推人数下限（含）")
    private Integer minDirectNum;

    @ApiModelProperty(value = "分享奖励比例（%，按被推荐人参与金额计算）")
    private BigDecimal rewardRate;

    @ApiModelProperty(value = "排序（越大越靠前）")
    private Integer sort;

    @ApiModelProperty(value = "是否删除：1=是 0=否")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
