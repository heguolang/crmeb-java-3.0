package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 后台设置/修改区域代理请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AgentAdminRequest对象", description = "后台设置/修改区域代理请求")
public class AgentAdminRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键（修改时必传）")
    private Integer id;

    @ApiModelProperty(value = "代理用户UID")
    @NotNull(message = "请选择代理用户")
    private Integer uid;

    @ApiModelProperty(value = "代理级别：1=省级 2=市级 3=区级")
    @NotNull(message = "请选择代理级别")
    @Min(value = 1, message = "代理级别不正确")
    @Max(value = 3, message = "代理级别不正确")
    private Integer level;

    @ApiModelProperty(value = "省")
    @NotBlank(message = "请选择省份")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区/县")
    private String district;

    @ApiModelProperty(value = "奖励比例（%）")
    @NotNull(message = "请填写奖励比例")
    @DecimalMin(value = "0.01", message = "奖励比例必须大于0")
    @DecimalMax(value = "100", message = "奖励比例不能超过100")
    private BigDecimal ratio;

    @ApiModelProperty(value = "状态：0=待审核 1=已通过 2=已拒绝")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String applyMark;
}
