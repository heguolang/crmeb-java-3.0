package com.qxkj.common.response;

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
@ApiModel(value="UserSpreadPeopleResponse对象", description="推广用户")
public class UserSpreadPeopleResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "一级推广人人数")
    private Integer total = 0;

    @ApiModelProperty(value = "二级推广人人数")
    private Integer totalLevel = 0;

    @ApiModelProperty(value = "推广人列表")
    private List<UserSpreadPeopleItemResponse> spreadPeopleList;

    @ApiModelProperty(value = "推广人总人数")
    private Integer count = 0;
}
