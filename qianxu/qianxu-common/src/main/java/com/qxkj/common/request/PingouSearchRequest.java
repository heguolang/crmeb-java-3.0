package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 拼购活动搜索请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PingouSearchRequest对象", description = "拼购活动搜索请求")
public class PingouSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动名称/商品名称关键字")
    private String keywords;

    @ApiModelProperty(value = "状态：null=全部 1=启用 0=禁用")
    private Boolean status;
}
