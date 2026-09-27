package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 拼购团记录搜索请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PingouTeamSearchRequest对象", description = "拼购团记录搜索请求")
public class PingouTeamSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动ID（不传查全部）")
    private Integer activityId;

    @ApiModelProperty(value = "状态：null=全部 0=进行中 1=已成团（已开奖）")
    private Integer status;
}
