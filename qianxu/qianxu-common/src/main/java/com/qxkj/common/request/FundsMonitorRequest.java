package com.qxkj.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qxkj.common.annotation.StringContains;
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
@ApiModel(value="FundsMonitorRequest对象", description="资金监控")
public class FundsMonitorRequest extends UserCommonSearchRequest implements Serializable {

    private static final long serialVersionUID = 3362714265772774491L;

    //@ApiModelProperty(value = "搜索关键字")
    //private String keywords;

    @ApiModelProperty(value = "添加时间")
    private String dateLimit;

    @ApiModelProperty(value = "明细类型:recharge-充值支付，admin-后台操作，productRefund商品退款，payProduct购买商品，transferIn佣金转入，exchange-换货差价，order-订单佣金，withdraw-佣金提现，yue-佣金转余额，stock-订货奖金，sign-签到奖励，reward-活动奖励，deduct-消费抵扣")
    @StringContains(limitValues = {"recharge", "admin", "productRefund", "payProduct", "transferIn", "exchange",
            "order", "withdraw", "yue", "stock", "sign", "reward", "deduct"}, message = "请选择正确的明细类型")
    private String title;

    @ApiModelProperty(value = "账户类型:all-全部（默认），now_money-余额，integral-积分，brokerage_price-佣金")
    private String category;

    @ApiModelProperty(value = "关联单号（订单号/换货单号等，模糊匹配）")
    private String linkId;

}
