package com.qxkj.common.request;


import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.management.MXBean;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
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
@TableName("eb_system_city")
@ApiModel(value="SystemCity对象", description="城市表")
public class SystemCityRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "父级id", required = true, example= "0")
    @NotNull(message = "父级id不能为空")  //不可为空
    @DecimalMin(value = "0", message = "父级id必须大于等于0") //数字最小值为0
    private Integer parentId;

    @ApiModelProperty(value = "名称")
    @NotNull(message = "城市名称不能为空")
    @Length(max = 100, message = "城市名称不能超过100个字符")
    private String name;

}
