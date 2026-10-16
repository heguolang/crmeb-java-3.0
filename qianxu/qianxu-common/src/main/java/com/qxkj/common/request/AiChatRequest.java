package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * AI 对话请求
 */
@Data
@ApiModel(value = "AiChatRequest", description = "AI对话请求")
public class AiChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "消息不能为空")
    @ApiModelProperty(value = "当前用户消息", required = true)
    private String message;

    @ApiModelProperty(value = "历史消息（可选，建议最多保留最近 20 条）")
    private List<AiChatMessageRequest> history;
}
