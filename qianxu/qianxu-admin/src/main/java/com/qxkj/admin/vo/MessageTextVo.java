package com.qxkj.admin.vo;

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
@ApiModel(value="MessageTextVo对象", description="微信消息文本模板")
public class MessageTextVo extends BaseMessageVo {
    public MessageTextVo() {}
    public MessageTextVo(String toUserName, String fromUserName, String content) {
        super();
        ToUserName = toUserName;
        FromUserName = fromUserName;
        Content = content;
    }

    @ApiModelProperty(value = "文本消息内容")
    private String Content;
}
