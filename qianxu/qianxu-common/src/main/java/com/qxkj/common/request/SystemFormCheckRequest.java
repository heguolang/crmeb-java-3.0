package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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
@ApiModel(value="SystemFormCheckRequest对象", description="整体保存表单数据")
public class SystemFormCheckRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "表单名称", required = true)
    @Min(value = 0, message = "请选择表单")
    private Integer id;

    @ApiModelProperty(value = "排序", required = true)
    private Integer sort;

    @ApiModelProperty(value = "状态（1：开启；0：关闭；）")
    private Boolean status;

    @ApiModelProperty(value = "字段值列表", required = true)
    @NotEmpty(message = "fields 至少要有一组数据")
    @Size(min = 1, message = "fields 至少要有一组数据")
    private List<SystemFormItemCheckRequest> fields;

}
