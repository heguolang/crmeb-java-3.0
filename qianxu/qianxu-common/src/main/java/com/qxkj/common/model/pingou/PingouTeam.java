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
import java.util.Date;

/**
 * 拼购团期表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_pingou_team")
@ApiModel(value = "PingouTeam对象", description = "拼购团期表")
public class PingouTeam implements Serializable {

    /** 状态：进行中 */
    public static final int STATUS_OPEN = 0;
    /** 状态：已完成（已开奖） */
    public static final int STATUS_FINISH = 1;

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "团期ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "活动ID")
    private Integer activityId;

    @ApiModelProperty(value = "第几期（该活动累计开团次数）")
    private Integer teamNo;

    @ApiModelProperty(value = "已参团人数")
    private Integer joinNum;

    @ApiModelProperty(value = "成团人数（快照）")
    private Integer totalNum;

    @ApiModelProperty(value = "中奖人数（快照）")
    private Integer winNum;

    @ApiModelProperty(value = "状态：0=进行中 1=已完成")
    private Integer status;

    @ApiModelProperty(value = "开团时间")
    private Date openTime;

    @ApiModelProperty(value = "开奖时间")
    private Date winTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
