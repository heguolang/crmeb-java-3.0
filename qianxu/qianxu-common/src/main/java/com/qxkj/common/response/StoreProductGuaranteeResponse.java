package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="StoreProductGuaranteeResponse对象", description="商品保障服务响应对象")
public class StoreProductGuaranteeResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "保障条款Id")
    private Integer id;

    @ApiModelProperty(value = "保障条款名称")
    private String name;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "条款内容")
    private String content;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "显示状态")
    private Boolean isShow;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "使用商品数")
    private Integer proNum;
}
