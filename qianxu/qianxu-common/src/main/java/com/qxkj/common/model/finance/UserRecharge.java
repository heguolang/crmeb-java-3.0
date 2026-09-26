package com.qxkj.common.model.finance;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("eb_user_recharge")
@ApiModel(value="UserRecharge对象", description="用户充值表")
public class UserRecharge implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "充值用户UID")
    private Integer uid;

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal price;

    @ApiModelProperty(value = "购买赠送金额")
    private BigDecimal givePrice;

    @ApiModelProperty(value = "充值类型")
    private String rechargeType;

    @ApiModelProperty(value = "是否充值")
    private Boolean paid;

    @ApiModelProperty(value = "充值支付时间")
    private Date payTime;

    @ApiModelProperty(value = "充值时间")
    private Date createTime;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundPrice;

    @ApiModelProperty(value = "是否上传微信发货管理")
    private Boolean isWechatShipping;

    @ApiModelProperty(value = "支付服务方订单号")
    private String outTradeNo;
}
