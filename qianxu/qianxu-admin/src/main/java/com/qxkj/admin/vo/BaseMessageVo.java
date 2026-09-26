package com.qxkj.admin.vo;

import com.qxkj.common.utils.QianxuDateUtil;
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
@ApiModel(value="BaseMessageVo对象", description="微信消息基础模板")
public class BaseMessageVo{
    @ApiModelProperty(value = "开发者微信号")
    protected String ToUserName;

    @ApiModelProperty(value = "发送方帐号（一个OpenID）")
    protected String FromUserName;

    @ApiModelProperty(value = "消息创建时间 （整型）")
    protected Long CreateTime = QianxuDateUtil.getTime();

    @ApiModelProperty(value = "消息类型，文本为text")
    protected String MsgType = "text";
}
