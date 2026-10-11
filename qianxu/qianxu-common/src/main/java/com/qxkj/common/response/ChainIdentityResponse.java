package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("链动身份信息")
public class ChainIdentityResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("0无 1代理 2老板")
    private Integer identity;
    @ApiModelProperty("身份名称")
    private String identityName;
    @ApiModelProperty("安置上级UID")
    private Integer chainParentId;
    @ApiModelProperty("邀请人UID")
    private Integer chainInviteUid;
    @ApiModelProperty("直推有效代理数")
    private Integer effectiveDirectCount;
    @ApiModelProperty("升老板所需人数")
    private Integer bossNeedCount;
    @ApiModelProperty("距离老板还差几人")
    private Integer bossRemainCount;
    @ApiModelProperty("今日佣金")
    private BigDecimal todayAmount;
    @ApiModelProperty("累计佣金")
    private BigDecimal totalAmount;
    @ApiModelProperty("冻结中")
    private BigDecimal freezeAmount;
}
