package com.qxkj.common.model.system;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("eb_system_user_level")
@ApiModel(value="SystemUserLevel对象", description="用户等级表")
public class SystemUserLevel implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "会员名称")
    private String name;

    @ApiModelProperty(value = "购买金额|经验达到")
    private Integer experience;

    @ApiModelProperty(value = "升级条件类型：1=累计消费金额，2=累计订单数，3=累计消费金额+累计订单数")
    private Integer upgradeType;

    @ApiModelProperty(value = "消费金额统计时机：1=已付款，2=交易完成")
    private Integer consumptionTriggerType;

    @ApiModelProperty(value = "订单数统计时机：1=已付款，2=交易完成")
    private Integer orderCountTriggerType;

    @ApiModelProperty(value = "累计订单数升级门槛（upgradeType=2或3时使用）")
    private Integer upgradeValue;

    @ApiModelProperty(value = "等级赠送积分（每单固定赠送，手输多少送多少）")
    private Integer giveIntegral;

    @ApiModelProperty(value = "等级权益描述")
    private String description;

    @ApiModelProperty(value = "是否显示 1=显示,0=隐藏")
    private Boolean isShow;

    @ApiModelProperty(value = "会员等级")
    private Integer grade;

    @ApiModelProperty(value = "享受折扣")
    private Integer discount;

    @ApiModelProperty(value = "会员图标")
    private String icon;

    @ApiModelProperty(value = "是否删除.1=删除,0=未删除")
    private Boolean isDel;

    @ApiModelProperty(value = "创建时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "等级返佣配置")
    @TableField(exist = false)
    private SystemUserLevelBrokerage brokerage;
}
