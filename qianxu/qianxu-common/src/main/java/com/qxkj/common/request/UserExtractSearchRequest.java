package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@TableName("eb_user_extract")
@ApiModel(value="UserExtract对象", description="用户提现表")
public class UserExtractSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "搜索关键字")
    private String keywords;

    @ApiModelProperty(value = "bank = 银行卡 alipay = 支付宝 weixin = 微信")
    private String extractType;

    @ApiModelProperty(value = "brokerage = 佣金 balance = 余额")
    private String extractCategory;

    @ApiModelProperty(value = "-1 未通过 0 审核中 1 已提现")
    private Integer status;

    @ApiModelProperty(value = "today,yesterday,lately7,lately30,month,year,/yyyy-MM-dd hh:mm:ss,yyyy-MM-dd hh:mm:ss/")
    private String dateLimit;

}
