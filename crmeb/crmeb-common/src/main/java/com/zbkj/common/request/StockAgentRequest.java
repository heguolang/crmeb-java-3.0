package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 订货代理 后台新增/修改请求
 */
@Data
@ApiModel(value = "StockAgentRequest对象", description = "订货代理新增/修改请求")
public class StockAgentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "代理ID（修改时必传）")
    private Integer id;

    @ApiModelProperty(value = "用户UID")
    @NotNull(message = "用户UID不能为空")
    private Integer uid;

    @ApiModelProperty(value = "层级ID")
    @NotNull(message = "层级不能为空")
    private Integer levelId;

    @ApiModelProperty(value = "上级代理ID（0=上级为总部）")
    private Integer parentId;

    @ApiModelProperty(value = "状态：0=禁用 1=启用")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String mark;
}
