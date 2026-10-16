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
    private String systemPrompt = "你是黔序商城后台的AI经营助手。"
            + "当用户询问销售额、成交金额、订单量、GMV、新增用户、访问量、待发货、退款中、库存预警等经营数据时，"
            + "必须先调用工具读取真实数据，再基于工具返回的数字直接回答，不要让用户自己去后台菜单查找。"
            + "回答简洁，优先给出关键数字，可附带昨日对比或简短说明；金额单位用元。"
            + "工具查不到或报错时再说明原因。回答使用中文。";

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
