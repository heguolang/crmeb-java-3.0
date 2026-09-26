package com.qxkj.common.response;

import com.qxkj.common.model.user.UserBrokerageRecord;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
@ApiModel(value = "SpreadCommissionDetailResponse对象", description = "推广佣金明细响应对象")
public class SpreadCommissionDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public SpreadCommissionDetailResponse() {}

    public SpreadCommissionDetailResponse(String date, List<UserBrokerageRecord> list) {
        this.date = date;
        this.list = list;
    }

    @ApiModelProperty(value = "月份")
    private String date;

    @ApiModelProperty(value = "数据")
    private List<UserBrokerageRecord> list;


}
