package com.qxkj.service.service;

import com.qxkj.common.request.AiChatRequest;
import com.qxkj.common.response.AiChatResponse;

/**
 * AI 对话服务
 */
public interface AiChatService {

    /**
     * 调用 DeepSeek 完成一轮对话
     */
    AiChatResponse chat(AiChatRequest request);
}
