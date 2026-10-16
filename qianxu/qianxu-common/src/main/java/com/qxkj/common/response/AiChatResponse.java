package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI 对话响应
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "AiChatResponse", description = "AI对话响应")
public class AiChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "助手回复")
    private String reply;
}
