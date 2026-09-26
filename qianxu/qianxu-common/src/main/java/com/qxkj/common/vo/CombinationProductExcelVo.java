package com.qxkj.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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
public class CombinationProductExcelVo {

    @ApiModelProperty(value = "编号")
    private Integer id;

    @ApiModelProperty(value = "拼团名称")
    private String title;

    @ApiModelProperty(value = "原价")
    private BigDecimal otPrice;

    @ApiModelProperty(value = "拼团价")
    private BigDecimal price;

    @ApiModelProperty(value = "库存剩余")
    private Integer quotaShow;

    @ApiModelProperty(value = "拼团人数")
    private Integer countPeople;

    @ApiModelProperty(value = "参与人数")
    private Integer countPeopleAll;

    @ApiModelProperty(value = "成团数量")
    private Integer countPeoplePink;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "商品状态")
    private String isShow;

    @ApiModelProperty(value = "拼团结束时间")
    private String stopTime;
}
