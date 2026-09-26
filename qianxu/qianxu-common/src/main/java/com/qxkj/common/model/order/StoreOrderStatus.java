package com.qxkj.common.model.order;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("eb_store_order_status")
@ApiModel(value="StoreOrderStatus对象", description="订单操作记录表")
public class StoreOrderStatus implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单id")
    private Integer oid;

    @ApiModelProperty(value = "操作类型")
    private String changeType;

    @ApiModelProperty(value = "操作备注")
    private String changeMessage;

    @ApiModelProperty(value = "操作时间")
    private Date createTime;


}
