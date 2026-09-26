package com.qxkj.common.response;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
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
public class SystemAdminResponse implements Serializable {

    private Integer id;

    private String account;

//    private String pwd;

    private String realName;

    private String roles;

    private String roleNames;

    private String lastIp;

    private Date lastTime;

    private Integer addTime;

    private Integer loginCount;

    private Integer level;

    private Boolean status;

//    private Boolean isDel;

    private String Token;

    @ApiModelProperty(value = "手机号码")
    private String phone;

    @ApiModelProperty(value = "是否接收短信")
    private Boolean isSms;

    @ApiModelProperty(value = "权限标识数组")
    private List<String> permissionsList;
}
