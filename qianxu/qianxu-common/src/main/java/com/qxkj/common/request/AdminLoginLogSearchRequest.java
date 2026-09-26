package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("eb_admin_login_log")
@ApiModel(value="AdminLoginLogSearchRequest对象", description="管理员登录日志搜索请求对象")
public class AdminLoginLogSearchRequest extends PageParamRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "登录账号")
    private String adminAccount;

    @ApiModelProperty(value = "状态 1成功 0失败, 不传为全部")
    private Integer status;

    @ApiModelProperty(value = "登录IP")
    private String ip;

    @ApiModelProperty(value = "时间范围")
    private String dateLimit;
}
