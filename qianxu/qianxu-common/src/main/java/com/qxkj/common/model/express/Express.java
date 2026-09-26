package com.qxkj.common.model.express;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("eb_express")
@ApiModel(value="Express对象", description="快递公司表")
public class Express implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "快递公司id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "快递公司简称")
    private String code;

    @ApiModelProperty(value = "快递公司全称")
    private String name;

    @ApiModelProperty(value = "是否需要月结账号")
    private Boolean partnerId;

    @ApiModelProperty(value = "是否需要月结密码")
    private Boolean partnerKey;

    @ApiModelProperty(value = "是否需要取件网店")
    private Boolean net;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "网点名称")
    private String netName;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否显示")
    private Boolean isShow;

    @ApiModelProperty(value = "是否可用")
    private Boolean status;
}
