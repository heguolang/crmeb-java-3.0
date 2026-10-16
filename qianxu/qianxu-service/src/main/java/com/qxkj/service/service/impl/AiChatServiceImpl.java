package com.qxkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.config.DeepSeekConfig;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.request.AiChatMessageRequest;
import com.qxkj.common.request.AiChatRequest;
import com.qxkj.common.response.AiChatResponse;
import com.qxkj.service.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek 对话实现
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_HISTORY = 20;
    private static final int MAX_MESSAGE_LENGTH = 4000;

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        if (!deepSeekConfig.isEnabled()) {
            throw new QianxuException("AI助手未启用");
        }
        if (StrUtil.isBlank(deepSeekConfig.getApiKey())) {
            throw new QianxuException("未配置 DeepSeek API Key");
        }
        String message = StrUtil.trim(request.getMessage());
        if (StrUtil.isBlank(message)) {
            throw new QianxuException("消息不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new QianxuException("单条消息过长，请控制在 " + MAX_MESSAGE_LENGTH + " 字以内");
        }

        JSONArray messages = buildMessages(request.getHistory(), message);
        JSONObject body = new JSONObject();
        body.put("model", deepSeekConfig.getModel());
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.7);

        try {
            HttpResponse response = HttpRequest.post(deepSeekConfig.getApiUrl())
                    .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(body.toJSONString())
                    .timeout(deepSeekConfig.getTimeoutMs())
                    .execute();

            String respBody = response.body();
            if (!response.isOk()) {
                log.error("DeepSeek HTTP {} body={}", response.getStatus(), respBody);
                throw new QianxuException(parseDeepSeekError(respBody));
            }

            JSONObject json = JSON.parseObject(respBody);
            if (json == null) {
                throw new QianxuException("DeepSeek 返回为空");
            }
            if (json.containsKey("error")) {
                throw new QianxuException(parseDeepSeekError(respBody));
            }

            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new QianxuException("DeepSeek 未返回有效内容");
            }
            JSONObject choice = choices.getJSONObject(0);
            JSONObject msg = choice.getJSONObject("message");
            String reply = msg == null ? null : msg.getString("content");
            if (StrUtil.isBlank(reply)) {
                throw new QianxuException("DeepSeek 回复为空");
            }
            return new AiChatResponse().setReply(reply.trim());
        } catch (QianxuException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 DeepSeek 失败", e);
            throw new QianxuException("AI 服务暂时不可用：" + e.getMessage());
        }
    }

    private JSONArray buildMessages(List<AiChatMessageRequest> history, String message) {
        JSONArray messages = new JSONArray();
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", deepSeekConfig.getSystemPrompt());
        messages.add(system);

        List<AiChatMessageRequest> safeHistory = history == null ? new ArrayList<>() : history;
        int start = Math.max(0, safeHistory.size() - MAX_HISTORY);
        for (int i = start; i < safeHistory.size(); i++) {
            AiChatMessageRequest item = safeHistory.get(i);
            if (item == null || StrUtil.isBlank(item.getContent())) {
                continue;
            }
            String role = StrUtil.blankToDefault(item.getRole(), "user");
            if (!"user".equals(role) && !"assistant".equals(role) && !"system".equals(role)) {
                role = "user";
            }
            // 历史里不再重复塞 system，避免冲掉系统提示
            if ("system".equals(role)) {
                continue;
            }
            JSONObject m = new JSONObject();
            m.put("role", role);
            m.put("content", StrUtil.sub(item.getContent().trim(), 0, MAX_MESSAGE_LENGTH));
            messages.add(m);
        }

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", message);
        messages.add(user);
        return messages;
    }

    private String parseDeepSeekError(String respBody) {
        try {
            JSONObject json = JSON.parseObject(respBody);
            if (json == null) {
                return "DeepSeek 调用失败";
            }
            Object err = json.get("error");
            if (err instanceof JSONObject) {
                String msg = ((JSONObject) err).getString("message");
                if (StrUtil.isNotBlank(msg)) {
                    return "DeepSeek：" + msg;
                }
            } else if (err instanceof String && StrUtil.isNotBlank((String) err)) {
                return "DeepSeek：" + err;
            }
            String message = json.getString("message");
            if (StrUtil.isNotBlank(message)) {
                return "DeepSeek：" + message;
            }
        } catch (Exception ignore) {
            // ignore
        }
        return "DeepSeek 调用失败，请检查 API Key 或网络";
    }
}
