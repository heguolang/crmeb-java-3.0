package com.qxkj.common.model.user;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("eb_user_level")
@ApiModel(value="UserLevel对象", description="用户等级记录表")
public class UserLevel implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "用户uid")
    private Integer uid;

    @ApiModelProperty(value = "等级vip")
    private Integer levelId;

    @ApiModelProperty(value = "会员等级")
    private Integer grade;

    @ApiModelProperty(value = "0:禁止,1:正常")
    private Boolean status;

    @ApiModelProperty(value = "备注")
    private String mark;

    @ApiModelProperty(value = "是否已通知")
    private Boolean remind;

    @ApiModelProperty(value = "是否删除,0=未删除,1=删除")
    private Boolean isDel;

    @ApiModelProperty(value = "享受折扣")
    private Integer discount;

    @ApiModelProperty(value = "等级赠送积分")
    private Integer giveIntegral;

    @ApiModelProperty(value = "创建时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

}
