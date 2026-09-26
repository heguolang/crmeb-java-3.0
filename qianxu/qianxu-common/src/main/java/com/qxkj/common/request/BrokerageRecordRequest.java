package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

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
@ApiModel(value="BrokerageRecordRequest对象", description="佣金记录请求对象")
public class BrokerageRecordRequest extends UserCommonSearchRequest implements Serializable {

    private static final long serialVersionUID = 3362714265772774491L;

    @ApiModelProperty(value = "类型：1-订单返佣，2-申请提现，3-提现失败，4-提现成功，5-佣金转余额")
    @Range(min = 1, max = 5, message = "未知的类型")
    private Integer type;


}
