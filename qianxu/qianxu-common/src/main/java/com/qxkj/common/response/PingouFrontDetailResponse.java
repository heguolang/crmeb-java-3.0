package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 拼购活动前端详情
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PingouFrontDetailResponse对象", description = "拼购活动前端详情")
public class PingouFrontDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动ID")
    private Integer id;

    @ApiModelProperty(value = "活动名称")
    private String name;

    @ApiModelProperty(value = "活动图片")
    private String image;

    @ApiModelProperty(value = "拼购商品ID")
    private Integer productId;

    @ApiModelProperty(value = "拼购商品名称")
    private String productName;

    @ApiModelProperty(value = "拼购商品售价")
    private BigDecimal productPrice;

    @ApiModelProperty(value = "活动开始时间")
    private String startTime;

    @ApiModelProperty(value = "活动结束时间")
    private String endTime;

    @ApiModelProperty(value = "成团人数")
    private Integer totalNum;

    @ApiModelProperty(value = "中奖人数")
    private Integer winNum;

    @ApiModelProperty(value = "未中奖补贴佣金（每人，元）")
    private BigDecimal refundSubsidy;

    @ApiModelProperty(value = "分享奖励低比例（‰）")
    private BigDecimal shareRateLow;

    @ApiModelProperty(value = "分享奖励高比例（‰）")
    private BigDecimal shareRateHigh;

    @ApiModelProperty(value = "高比例直推人数阈值")
    private Integer shareRateThreshold;

    @ApiModelProperty(value = "每人限参与次数（0=不限）")
    private Integer limitNum;

    @ApiModelProperty(value = "累计开团次数")
    private Integer teamCount;

    @ApiModelProperty(value = "当前进行中团期ID")
    private Integer currentTeamId;

    @ApiModelProperty(value = "当前团第几期")
    private Integer currentTeamNo;

    @ApiModelProperty(value = "当前团已参团人数")
    private Integer currentJoinNum;

    @ApiModelProperty(value = "当前团剩余名额")
    private Integer currentRemainNum;

    @ApiModelProperty(value = "最近已开奖团成员（含中奖情况，最多两期）")
    private List<PingouTeamListResponse> recentTeams;
}
