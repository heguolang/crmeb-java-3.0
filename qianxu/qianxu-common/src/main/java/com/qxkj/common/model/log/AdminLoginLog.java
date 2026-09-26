package com.qxkj.common.model.log;

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
@TableName("eb_admin_login_log")
@ApiModel(value="AdminLoginLog对象", description="管理员登录日志表")
public class AdminLoginLog implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "管理员id（登录失败时为0）")
    private Integer adminId;

    @ApiModelProperty(value = "登录账号")
    private String adminAccount;

    @ApiModelProperty(value = "登录IP")
    private String ip;

    @ApiModelProperty(value = "登录地点")
    private String location;

    @ApiModelProperty(value = "浏览器")
    private String browser;

    @ApiModelProperty(value = "操作系统")
    private String os;

    @ApiModelProperty(value = "状态 1成功 0失败")
    private Integer status;

    @ApiModelProperty(value = "提示信息")
    private String msg;

    @ApiModelProperty(value = "登录时间")
    private Date createTime;
}
