package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qxkj.common.annotation.StringContains;
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
@TableName("eb_user")
@ApiModel(value="User对象", description="用户表")
public class UserSearchRequest extends UserCommonSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "时间")
    private String dateLimit;

    @ApiModelProperty(value = "用户分组")
    private String groupId;

    @ApiModelProperty(value = "用户标签")
    private String labelId;

    @ApiModelProperty(value = "用户登陆类型，h5 = h5， wechat = wechat，routine = routine", allowableValues = "range[h5,wechat,routine]")
    @StringContains(limitValues = {"h5","wechat","routine"}, message = "请选择正确的用户登录类型")
    private String userType;

    @ApiModelProperty(value = "状态是否正常， 0 = 禁止， 1 = 正常")
    private Boolean status = null;

    @ApiModelProperty(value = "是否为推广员， 0 = 禁止， 1 = 正常")
    private Boolean isPromoter = null;

    @ApiModelProperty(value = "消费情况")
    private String payCount;

    @ApiModelProperty(value = "等级")
    private String level;

    //时间类型
    @ApiModelProperty(value = "访问情况： 1 = 首次， 2 = 访问过， 3 = 未访问", allowableValues = "range[0,1,2,3]")
    private Integer accessType = 0;

    @ApiModelProperty(value = "国家，中国CN，其他OTHER")
    private String country;

    @ApiModelProperty(value = "省份")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "性别，0未知，1男，2女，3保密")
    private String sex;
}
