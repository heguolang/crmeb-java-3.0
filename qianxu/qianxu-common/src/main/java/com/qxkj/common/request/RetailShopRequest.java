package com.qxkj.common.request;

import com.qxkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
public class RetailShopRequest {

    public RetailShopRequest() {
    }

    @ApiModelProperty(value = "是否启用分销:1-启用，0-禁止")
    @NotNull(message = "是否启用分销 不能为空")
    private Integer brokerageFuncStatus;

//    @ApiModelProperty(value = "分销模式：1-指定分销，2-人人分销，3-满额分销")
//    @NotNull(message = "分销模式 不能为空")
//    private String storeBrokerageStatus;

    @ApiModelProperty(value = "分销额度：-1-关闭，0--用户购买金额大于等于设置金额时，用户自动成为分销员")
    @NotNull(message = "分销额度 不能为空")
    @Min(value = -1, message = "分销额度,不能小于-1")
    private Integer storeBrokerageQuota;

    @ApiModelProperty(value = "一级返佣比例（已废弃：改由「会员返佣配置」按会员等级设置，字段仅保留兼容）")
    private Integer storeBrokerageRatio;

    @ApiModelProperty(value = "二级返佣比例（已废弃：改由「会员返佣配置」按会员等级设置，字段仅保留兼容）")
    private Integer storeBrokerageTwo;

    @ApiModelProperty(value = "分销关系绑定:0-所有用户，1-新用户")
    @NotNull(message = "分销关系绑定 不能为空")
    private Integer brokerageBindind;

    @ApiModelProperty(value = "用户提现最低金额")
    @NotNull(message = "用户提现最低金额 不能为空")
    @DecimalMin(value = "0", message = "用户提现最低金额最小为0")
    private BigDecimal userExtractMinPrice;

    @ApiModelProperty(value = "提现银行")
    @NotNull(message = "提现银行 不能为空")
    private String userExtractBank;

    @ApiModelProperty(value = "冻结时间")
    @NotNull(message = "冻结时间 不能为空")
    @Min(value = 0, message = "冻结时间最少为0天")
    private Integer extractTime;

    @ApiModelProperty(value = "是否展示分销气泡：0-展示，1-展示")
    @NotNull(message = "是否展示分销气泡 不能为空")
    @Range(min = 0, max = 1, message = "是否展示分销气泡只能选择0-1")
    private Integer storeBrokerageIsBubble;

    @ApiModelProperty(value = "分销佣金分账节点:pay:订单支付后，receipt:订单收货后，complete:订单完成后", required = true)
    @NotBlank(message = "分销佣金分账节点不能为空")
    @StringContains(limitValues = {"pay", "receipt", "complete"}, message = "未知的分销佣金分账节点")
    private String storeBrokerageShareNode;

    @ApiModelProperty(value = "用户首次注册是否默认成为推广员：0-否，1-是")
    @Range(min = 0, max = 1, message = "注册默认推广员只能选择0-1")
    private Integer registerDefaultIsPromoter;

    @ApiModelProperty(value = "用户首次注册默认会员等级ID，0表示不设置")
    @Min(value = 0, message = "注册默认会员等级不能小于0")
    private Integer registerDefaultUserLevel;

    @ApiModelProperty(value = "用户首次注册默认分销商等级ID（eb_distributor_level），0表示不设置")
    @Min(value = 0, message = "注册默认分销商等级不能小于0")
    private Integer registerDefaultDistributorLevel;

    @ApiModelProperty(value = "分销佣金到账方式：1-支付订单到账，2-订单完成到账")
    @Range(min = 1, max = 2, message = "分销佣金到账方式只能为1或2")
    private Integer brokerageCreditTiming;

}
