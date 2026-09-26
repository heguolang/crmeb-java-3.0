package com.qxkj.common.model.wechat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

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
@TableName("eb_wechat_qrcode")
@ApiModel(value="WechatQrcode对象", description="微信二维码管理表")
public class WechatQrcode implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "微信二维码ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "二维码类型")
    private String thirdType;

    @ApiModelProperty(value = "用户id")
    private Integer thirdId;

    @ApiModelProperty(value = "二维码参数")
    private String ticket;

    @ApiModelProperty(value = "二维码有效时间")
    private Integer expireSeconds;

    @ApiModelProperty(value = "状态")
    private Boolean status;

    @ApiModelProperty(value = "微信访问url")
    private String url;

    @ApiModelProperty(value = "微信二维码url")
    private String qrcodeUrl;

    @ApiModelProperty(value = "被扫的次数")
    private Integer scan;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;


}
