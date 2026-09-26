package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

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
@TableName("eb_wechat_reply")
@ApiModel(value="WechatReplyRequest对象", description="微信关键字回复表")
public class WechatReplyRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "关键字id")
    private Integer id;

    @ApiModelProperty(value = "关键字")
    @NotBlank(message = "请填写关键字, 关注 = subscribe， 默认 = default")
    private String keywords;

    @ApiModelProperty(value = "回复类型 text=文本  image =图片  news =图文  voice =音频"  , example = "text")
    @NotBlank(message = "请选择回复类型")
    private String type;

    @ApiModelProperty(value = "回复数据")
    @NotBlank(message = "请填写回复数据")
    private String data;

    @ApiModelProperty(value = "回复状态 0=不可用  1 =可用", example = "1")
    @NotNull(message = "请选择回复状态")
    private Boolean status;
}
