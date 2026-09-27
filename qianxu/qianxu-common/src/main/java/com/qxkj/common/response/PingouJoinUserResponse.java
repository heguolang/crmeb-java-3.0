package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 拼团成员（头像墙/中奖情况）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PingouJoinUserResponse对象", description = "拼团成员信息")
public class PingouJoinUserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "参与ID")
    private Integer id;

    @ApiModelProperty(value = "用户UID")
    private Integer uid;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "是否中奖：1=中奖 0=未中奖")
    private Integer isWin;

    @ApiModelProperty(value = "参与金额")
    private java.math.BigDecimal payPrice;

    @ApiModelProperty(value = "参与时间")
    private String createTime;
}
