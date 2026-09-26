package com.qxkj.admin.vo;

import com.qxkj.common.constants.WeChatConstants;
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
@ApiModel(value="MessageMediaVo对象", description="微信消息 图片/语音 模板")
public class MessageImageVo extends BaseMessageVo {
    public MessageImageVo() {}
    public MessageImageVo(String toUserName, String fromUserName, MessageImageItemVo image) {
        super();
        ToUserName = toUserName;
        FromUserName = fromUserName;
        MsgType = WeChatConstants.WE_CHAT_MESSAGE_RESP_MESSAGE_TYPE_IMAGE;
        Image = image;
    }

    @ApiModelProperty(value = "通过素材管理中的接口上传多媒体文件，得到的id。")
    private MessageImageItemVo Image;
}
