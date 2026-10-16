package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 对话单条消息
 */
@Data
@ApiModel(value = "AiChatMessageRequest", description = "AI对话消息")
public class AiChatMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "角色：user / assistant / system")
    private String role;

    @ApiModelProperty(value = "内容")
    private String content;
}
