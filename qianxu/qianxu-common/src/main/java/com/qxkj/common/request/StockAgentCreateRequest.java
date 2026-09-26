package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 订货代理 新增下级请求（代理端）
 */
@Data
@ApiModel(value = "StockAgentCreateRequest对象", description = "新增下级代理请求")
public class StockAgentCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "下级用户手机号（必须是已注册用户）")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @ApiModelProperty(value = "层级ID（须低于自己层级）")
    @NotNull(message = "层级不能为空")
    private Integer levelId;

    @ApiModelProperty(value = "备注")
    private String mark;
}
