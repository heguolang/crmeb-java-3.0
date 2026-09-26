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
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserExtractCashResponse对象", description="提现用户信息响应对象")
public class UserExtractCashResponse implements Serializable {
    public UserExtractCashResponse(){}
    public UserExtractCashResponse(String minPrice, BigDecimal commissionCount, BigDecimal brokenCommission, String brokenDay) {
        this.minPrice = minPrice;
        this.commissionCount = commissionCount;
        this.brokenCommission = brokenCommission;
        this.brokenDay = brokenDay;
    }

    private static final long serialVersionUID=1L;

//    @ApiModelProperty(value = "提现银行")
//    private List<String> extractBank;

    @ApiModelProperty(value = "提现最低金额")
    private String minPrice;

    @ApiModelProperty(value = "可提现佣金")
    private BigDecimal commissionCount;

    @ApiModelProperty(value = "冻结佣金")
    private BigDecimal brokenCommission;

    @ApiModelProperty(value = "冻结天数")
    private String brokenDay;

    @ApiModelProperty(value = "可提现余额")
    private BigDecimal balanceCount;

    @ApiModelProperty(value = "提现类别：brokerage/balance")
    private String extractCategory;

    @ApiModelProperty(value = "功能是否开启")
    private Boolean extractSwitch;

    @ApiModelProperty(value = "可提现星期提示，如：周一至周五")
    private String extractWeekdaysTip;

    @ApiModelProperty(value = "可提现时段提示，如：09:00-18:00")
    private String extractTimeTip;

    @ApiModelProperty(value = "当前是否在可提现时间内")
    private Boolean extractTimeAllowed;
}
