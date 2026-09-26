package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
@ApiModel(value="MonitorResponse对象", description="资金流水对象")
public class MonitorResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "账单id（来源表 id）")
    private Integer id;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "0 = 支出 1 = 获得")
    private int pm;

    @ApiModelProperty(value = "账单标题")
    private String title;

    @ApiModelProperty(value = "明细数字（积分时为整数）")
    private BigDecimal number;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "用户手机号")
    private String phone;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "关联id（订单号等）")
    private String linkId;

    @ApiModelProperty(value = "明细种类：now_money-余额 / integral-积分 / brokerage_price-佣金")
    private String category;

    @ApiModelProperty(value = "剩余（变动后余额）")
    private BigDecimal balance;

    @ApiModelProperty(value = "来源表：bill / integral")
    private String sourceTable;

}