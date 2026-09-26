package com.qxkj.common.vo;

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
@ApiModel(value="MenuCheckVo对象", description="菜单待选中Vo对象")
public class MenuCheckVo implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "菜单ID")
    private Integer id;

    @ApiModelProperty(value = "父级ID")
    private Integer pid;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "icon")
    private String icon;

    @ApiModelProperty(value = "是否选中")
    private Boolean checked;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "子对象列表")
    private List<MenuCheckVo> childList;
}
