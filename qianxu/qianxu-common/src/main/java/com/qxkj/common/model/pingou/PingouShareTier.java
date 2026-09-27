package com.qxkj.common.model.pingou;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 拼购分享奖励阶梯
 * 直推人数达到 minDirectNum（含）时，推荐人按 rewardType 方式获得 rewardValue 奖励：
 * rewardType=1 按比例（参与金额 × rewardValue%）；rewardType=2 固定金额（元）。
 * 多阶梯时按直推人数匹配"满足条件的最高一档"。
 */
@Data
public class PingouShareTier implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "直推人数下限（含）")
    private Integer minDirectNum;

    @ApiModelProperty(value = "奖励类型：1=比例(%) 2=固定金额(元)")
    private Integer rewardType;

    @ApiModelProperty(value = "奖励值：比例时为百分比数值，固定金额时为元")
    private BigDecimal rewardValue;
}
