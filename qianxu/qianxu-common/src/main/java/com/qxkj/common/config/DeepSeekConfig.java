package com.qxkj.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek 对话配置（服务端保存 API Key，勿下发前端）
 */
@Configuration
@ConfigurationProperties(prefix = "qianxu.deepseek")
public class DeepSeekConfig {

    /** 是否启用 */
    private boolean enabled = true;

    /** API Key */
    private String apiKey = "";

    /** 接口地址 */
    private String apiUrl = "https://api.deepseek.com/chat/completions";

    /** 模型名 */
    private String model = "deepseek-chat";

    /** 系统提示词 */
    private String systemPrompt = "你是黔序商城后台的AI助手，帮助管理员解答经营、订单、商品、营销和系统使用问题。"
            + "回答简洁、可执行。当前版本不直接查询店铺数据库，涉及具体数据时请引导用户到对应后台菜单查看。"
            + "回答使用中文。";

    /** 超时毫秒 */
    private int timeoutMs = 120000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
