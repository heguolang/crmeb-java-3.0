package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ApiModel("链动身份信息")
@Data
public class ChainIdentityResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("0无 1代理 2老板")
    private Integer identity;
    @ApiModelProperty("身份名称（后台可配级别名称）")
    private String identityName;
    @ApiModelProperty("安置上级ID")
    private Integer chainParentId;
    @ApiModelProperty("邀请人ID")
    private Integer chainInviteUid;
    @ApiModelProperty("直推有效代理数")
    private Integer effectiveDirectCount;
    @ApiModelProperty("升老板所需人数N")
    private Integer bossNeedCount;
    @ApiModelProperty("距离老板还差几人")
    private Integer bossRemainCount;

    @ApiModelProperty("老板帮扶是否已解锁")
    private Boolean helpUnlocked;
    @ApiModelProperty("帮扶解锁条件N：每位留人代理团队有效订单数")
    private Integer helpUnlockNeedOrders;
    @ApiModelProperty("帮扶解锁条件M：本人团队有效订单数")
    private Integer helpUnlockNeedTeamOrders;
    @ApiModelProperty("帮扶进度：已达标的留人座位数")
    private Integer helpUnlockedSeatCount;
    @ApiModelProperty("帮扶进度：留人座位总数N")
    private Integer helpNeedSeatCount;
    @ApiModelProperty("本人团队有效订单数")
    private Integer selfTeamOrders;
    @ApiModelProperty("帮扶基金冻结金额")
    private BigDecimal helpFundAmount;
    @ApiModelProperty("留人座位明细（升老板前的前N名直推）")
    private List<KeptSeat> keptSeats;

    @ApiModelProperty("团队总人数（含本人）")
    private Integer teamSize;
    @ApiModelProperty("团队中已晋升老板数")
    private Integer teamBossCount;
    @ApiModelProperty("直推人数")
    private Integer directCount;

    @ApiModelProperty("今日佣金")
    private BigDecimal todayAmount;
    @ApiModelProperty("累计佣金")
    private BigDecimal totalAmount;
    @ApiModelProperty("冻结中（含帮扶基金）")
    private BigDecimal freezeAmount;
    @ApiModelProperty("可提现佣金")
    private BigDecimal withdrawable;

    @Data
    @ApiModel("留人座位")
    public static class KeptSeat implements Serializable {
        private static final long serialVersionUID = 1L;
        @ApiModelProperty("会员ID")
        private Integer uid;
        @ApiModelProperty("昵称")
        private String nickname;
        @ApiModelProperty("头像")
        private String avatar;
        @ApiModelProperty("身份 1代理 2老板")
        private Integer identity;
        @ApiModelProperty("身份名称")
        private String identityName;
        @ApiModelProperty("该座位团队有效订单数")
        private Integer teamOrders;
        @ApiModelProperty("是否已晋升老板")
        private Boolean upgraded;
        @ApiModelProperty("该座位是否已达标（升老板 且 团队订单≥N）")
        private Boolean qualified;
    }
}
