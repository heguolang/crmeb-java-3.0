package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@TableName("eb_system_form_temp")
@ApiModel(value="SystemFormTempRequest对象", description="表单模板")
public class SystemFormTempRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "表单名称", required = true)
    @NotBlank(message = "请填写表单名称")
    @Length(max = 500, message = "表单名称长度不能超过500个字符")
    private String name;

    @ApiModelProperty(value = "表单简介", required = true)
    @NotBlank(message = "请填写表单简介")
    @Length(max = 500, message = "表单简介长度不能超过500个字符")
    private String info;

    @ApiModelProperty(value = "表单内容", required = true)
    @NotBlank(message = "请填写表单内容")
    private String content;

}
