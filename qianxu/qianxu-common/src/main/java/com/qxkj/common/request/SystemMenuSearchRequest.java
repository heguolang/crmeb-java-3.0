package com.qxkj.common.request;

import com.qxkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
@ApiModel(value="SystemMenuSearchRequest对象", description="菜单搜索Request对象")
public class SystemMenuSearchRequest {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "菜单名称")
    private String name;

    @ApiModelProperty(value = "菜单类型:M-目录，C-菜单，A-按钮")
    @StringContains(limitValues = {"M","C","A"}, message = "未知的菜单类型")
    private String menuType;
}
