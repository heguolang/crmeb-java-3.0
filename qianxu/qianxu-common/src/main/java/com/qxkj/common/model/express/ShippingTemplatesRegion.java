package com.qxkj.common.model.express;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("eb_shipping_templates_region")
@ApiModel(value="ShippingTemplatesRegion对象", description="")
public class ShippingTemplatesRegion implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "模板ID")
    private Integer tempId;

    @ApiModelProperty(value = "城市ID")
    private Integer cityId;

    @ApiModelProperty(value = "描述")
    private String title;

    @ApiModelProperty(value = "首件")
    private BigDecimal first;

    @ApiModelProperty(value = "首件运费")
    private BigDecimal firstPrice;

    @ApiModelProperty(value = "续件")
    private BigDecimal renewal;

    @ApiModelProperty(value = "续件运费")
    private BigDecimal renewalPrice;

    @ApiModelProperty(value = "计费方式 1按件数 2按重量 3按体积")
    private Integer type;

    @ApiModelProperty(value = "分组唯一值")
    private String uniqid;

    @ApiModelProperty(value = "是否无效")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;


}
