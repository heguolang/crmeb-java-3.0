package com.qxkj.common.model.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
@TableName("eb_system_notification")
@ApiModel(value="SystemNotification对象", description="通知设置表")
public class SystemNotification implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "标识")
    private String mark;

    @ApiModelProperty(value = "通知类型")
    private String type;

    @ApiModelProperty(value = "通知场景说明")
    private String description;

    @ApiModelProperty(value = "公众号模板消息（0：不存在，1：开启，2：关闭）")
    private Integer isWechat;

    @ApiModelProperty(value = "模板消息id")
    private Integer wechatId;

    @ApiModelProperty(value = "小程序订阅消息（0：不存在，1：开启，2：关闭）")
    private Integer isRoutine;

    @ApiModelProperty(value = "订阅消息id")
    private Integer routineId;

    @ApiModelProperty(value = "发送短信（0：不存在，1：开启，2：关闭）")
    private Integer isSms;

    @ApiModelProperty(value = "短信id")
    private Integer smsId;

    @ApiModelProperty(value = "发送类型（1：用户，2：管理员）")
    private Integer sendType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
