package com.qxkj.common.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.HashMap;
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
@ApiModel(value="BargainRecordResponse对象", description="砍价记录响应对象")
public class BargainRecordResponse {

    @ApiModelProperty(value = "砍价商品ID")
    private Integer id;

    @ApiModelProperty(value = "关联商品ID")
    private Integer productId;

    @ApiModelProperty(value = "砍价活动名称")
    private String title;

    @ApiModelProperty(value = "砍价活动图片")
    private String image;

    @ApiModelProperty(value = "砍价结束时间")
    private Long stopTime;

    @ApiModelProperty(value = "用户砍价活动id")
    private Integer bargainUserId;

    @ApiModelProperty(value = "剩余金额")
    private BigDecimal surplusPrice;

    @ApiModelProperty(value = "状态 1参与中 2 活动结束参与失败 3活动结束参与成功")
    private Integer status;

    @ApiModelProperty(value = "是否取消")
    private Boolean isDel;

    @ApiModelProperty(value = "是否生成订单")
    private Boolean isOrder;

    @ApiModelProperty(value = "是否支付")
    private Boolean isPay;

    @ApiModelProperty(value = "订单号")
    private String orderNo;
}
