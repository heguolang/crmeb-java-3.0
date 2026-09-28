package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 拼购团记录列表项
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PingouTeamListResponse对象", description = "拼购团记录列表项")
public class PingouTeamListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团期ID")
    private Integer id;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "活动名称")
    private String activityName;

    @ApiModelProperty(value = "商品名称")
    private String productName;

    @ApiModelProperty(value = "商品图片")
    private String image;

    @ApiModelProperty(value = "第几期")
    private Integer teamNo;

    @ApiModelProperty(value = "已参团人数")
    private Integer joinNum;

    @ApiModelProperty(value = "成团人数")
    private Integer totalNum;

    @ApiModelProperty(value = "中奖人数")
    private Integer winNum;

    @ApiModelProperty(value = "剩余名额")
    private Integer remainNum;

    @ApiModelProperty(value = "状态：0=进行中 1=已成团")
    private Integer status;

    @ApiModelProperty(value = "开团时间")
    private String openTime;

    @ApiModelProperty(value = "开奖时间")
    private String winTime;

    @ApiModelProperty(value = "已参团成员")
    private List<PingouJoinUserResponse> joins;
}
