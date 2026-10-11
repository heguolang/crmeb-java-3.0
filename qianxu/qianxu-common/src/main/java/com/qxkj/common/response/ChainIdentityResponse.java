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
    @ApiModelProperty("老板帮扶是否已解锁")
    private Boolean helpUnlocked;
    @ApiModelProperty("帮扶解锁所需：每位代理直推礼包单数X")
    private Integer helpUnlockNeedOrders;
    @ApiModelProperty("帮扶进度：已达标的直推代理位数")
    private Integer helpUnlockedSeatCount;
    @ApiModelProperty("帮扶进度：需要达标的直推代理位数N")
    private Integer helpNeedSeatCount;
    @ApiModelProperty("帮扶基金冻结金额（见单未解锁部分）")
    private BigDecimal helpFundAmount;
    @ApiModelProperty("今日佣金")
    private BigDecimal todayAmount;
    @ApiModelProperty("累计佣金")
    private BigDecimal totalAmount;
    @ApiModelProperty("冻结中")
    private BigDecimal freezeAmount;
}
