package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 会员端申请区域代理请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AgentApplyRequest对象", description = "会员端申请区域代理请求")
public class AgentApplyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "申请级别：1=省级 2=市级 3=区级")
    @NotNull(message = "请选择申请级别")
    @Min(value = 1, message = "申请级别不正确")
    @Max(value = 3, message = "申请级别不正确")
    private Integer level;

    @ApiModelProperty(value = "省")
    @NotBlank(message = "请选择省份")
    private String province;

    @ApiModelProperty(value = "市")
    private String city;

    @ApiModelProperty(value = "区/县")
    private String district;

    @ApiModelProperty(value = "申请说明")
    private String applyMark;
}
