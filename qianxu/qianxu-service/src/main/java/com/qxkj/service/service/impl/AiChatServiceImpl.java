package com.qxkj.service.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.config.DeepSeekConfig;
import com.qxkj.common.constants.SysConfigConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.request.AiChatMessageRequest;
import com.qxkj.common.request.AiChatRequest;
import com.qxkj.common.response.AiChatResponse;
import com.qxkj.common.response.HomeOperatingDataResponse;
import com.qxkj.common.response.HomeRateResponse;
import com.qxkj.service.service.AiChatService;
import com.qxkj.service.service.HomeService;
import com.qxkj.service.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek 对话实现（支持工具调用，可读取首页经营数据）
 * API Key 等优先读「设置 → 第三方接口 → AI配置」，未配置时回退 application.yml
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_HISTORY = 20;
    private static final int MAX_MESSAGE_LENGTH = 4000;
    private static final int MAX_TOOL_ROUNDS = 3;

    private static final String TOOL_HOME_OVERVIEW = "get_home_overview";
    private static final String TOOL_OPERATING_DATA = "get_operating_data";

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Autowired
    private HomeService homeService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        RuntimeAiConfig cfg = resolveRuntimeConfig();
        if (!cfg.enabled) {
            throw new QianxuException("AI助手未启用，请在「设置 → 第三方接口设置 → AI配置」中开启");
        }
        if (StrUtil.isBlank(cfg.apiKey)) {
            throw new QianxuException("未配置 DeepSeek API Key，请在「设置 → 第三方接口设置 → AI配置」中填写");
        }
        String message = StrUtil.trim(request.getMessage());
        if (StrUtil.isBlank(message)) {
            throw new QianxuException("消息不能为空");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new QianxuException("单条消息过长，请控制在 " + MAX_MESSAGE_LENGTH + " 字以内");
        }

        JSONArray messages = buildMessages(request.getHistory(), message);
        try {
            for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                JSONObject msg = callDeepSeek(messages, true, cfg);
                JSONArray toolCalls = msg.getJSONArray("tool_calls");
                if (toolCalls == null || toolCalls.isEmpty()) {
                    String reply = msg.getString("content");
                    if (StrUtil.isBlank(reply)) {
                        throw new QianxuException("DeepSeek 回复为空");
                    }
                    return new AiChatResponse().setReply(reply.trim());
                }

                // 把 assistant 的 tool_calls 原样回传
                messages.add(msg);
                for (int i = 0; i < toolCalls.size(); i++) {
                    JSONObject call = toolCalls.getJSONObject(i);
                    String callId = call.getString("id");
                    JSONObject fn = call.getJSONObject("function");
                    String name = fn == null ? null : fn.getString("name");
                    String args = fn == null ? "{}" : fn.getString("arguments");
                    String toolResult = executeTool(name, args);

                    JSONObject toolMsg = new JSONObject();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", callId);
                    toolMsg.put("content", toolResult);
                    messages.add(toolMsg);
                }
            }

            // 工具轮次用尽后，再要一次纯文本回答
            JSONObject finalMsg = callDeepSeek(messages, false, cfg);
            String reply = finalMsg.getString("content");
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

    /**
     * 后台配置优先，yml 兜底
     */
    private RuntimeAiConfig resolveRuntimeConfig() {
        RuntimeAiConfig cfg = new RuntimeAiConfig();
        String enabled = systemConfigService.getValueByKey(SysConfigConstants.DEEPSEEK_ENABLED);
        if (StrUtil.isNotBlank(enabled)) {
            cfg.enabled = !"0".equals(enabled.trim()) && !"false".equalsIgnoreCase(enabled.trim());
        } else {
            cfg.enabled = deepSeekConfig.isEnabled();
        }
        cfg.apiKey = firstNonBlank(
                systemConfigService.getValueByKey(SysConfigConstants.DEEPSEEK_API_KEY),
                deepSeekConfig.getApiKey());
        cfg.model = firstNonBlank(
                systemConfigService.getValueByKey(SysConfigConstants.DEEPSEEK_MODEL),
                deepSeekConfig.getModel(),
                "deepseek-chat");
        cfg.apiUrl = firstNonBlank(
                systemConfigService.getValueByKey(SysConfigConstants.DEEPSEEK_API_URL),
                deepSeekConfig.getApiUrl(),
                "https://api.deepseek.com/chat/completions");
        return cfg;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (StrUtil.isNotBlank(v)) {
                return v.trim();
            }
        }
        return "";
    }

    private JSONObject callDeepSeek(JSONArray messages, boolean withTools, RuntimeAiConfig cfg) {
        JSONObject body = new JSONObject();
        body.put("model", cfg.model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.3);
        if (withTools) {
            body.put("tools", buildTools());
            body.put("tool_choice", "auto");
        }

        HttpResponse response = HttpRequest.post(cfg.apiUrl)
                .header("Authorization", "Bearer " + cfg.apiKey)
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
        if (msg == null) {
            throw new QianxuException("DeepSeek 未返回消息");
        }
        return msg;
    }

    private JSONArray buildTools() {
        JSONArray tools = new JSONArray();

        JSONObject home = new JSONObject();
        home.put("type", "function");
        JSONObject homeFn = new JSONObject();
        homeFn.put("name", TOOL_HOME_OVERVIEW);
        homeFn.put("description", "查询商城首页经营概览真实数据：今日/昨日销售额、今日/昨日订单量、今日/昨日新增用户、"
                + "今日/昨日访问量，以及累计GMV、本月GMV、本年GMV、退款、客单价、支付用户数等。"
                + "当用户询问今日成交金额、销售额、订单量、GMV、新增用户等经营数据时必须调用。");
        JSONObject homeParams = new JSONObject();
        homeParams.put("type", "object");
        homeParams.put("properties", new JSONObject());
        homeFn.put("parameters", homeParams);
        home.put("function", homeFn);
        tools.add(home);

        JSONObject op = new JSONObject();
        op.put("type", "function");
        JSONObject opFn = new JSONObject();
        opFn.put("name", TOOL_OPERATING_DATA);
        opFn.put("description", "查询经营待办与商品库存概况：待发货订单数、退款中订单数、待核销订单数、"
                + "库存预警商品数、上架/仓库商品数、提现待审核数、用户充值总金额。"
                + "当用户询问待发货、退款中、库存预警、待处理事项时调用。");
        JSONObject opParams = new JSONObject();
        opParams.put("type", "object");
        opParams.put("properties", new JSONObject());
        opFn.put("parameters", opParams);
        op.put("function", opFn);
        tools.add(op);

        return tools;
    }

    private String executeTool(String name, String argsJson) {
        try {
            if (TOOL_HOME_OVERVIEW.equals(name)) {
                HomeRateResponse data = homeService.indexDate();
                JSONObject out = new JSONObject(true);
                out.put("今日销售额_元", data.getSales());
                out.put("昨日销售额_元", data.getYesterdaySales());
                out.put("今日订单量", data.getOrderNum());
                out.put("昨日订单量", data.getYesterdayOrderNum());
                out.put("今日新增用户", data.getNewUserNum());
                out.put("昨日新增用户", data.getYesterdayNewUserNum());
                out.put("今日访问量", data.getPageviews());
                out.put("昨日访问量", data.getYesterdayPageviews());
                out.put("累计GMV_元", data.getTotalGmv());
                out.put("本月GMV_元", data.getMonthGmv());
                out.put("上月GMV_元", data.getLastMonthGmv());
                out.put("本年GMV_元", data.getYearGmv());
                out.put("实付净额_元", data.getNetGmv());
                out.put("退款金额_元", data.getRefundAmount());
                out.put("退款单数", data.getRefundCount());
                out.put("退款率_百分比", data.getRefundRate());
                out.put("累计已付订单量", data.getTotalOrderNum());
                out.put("本月订单量", data.getMonthOrderNum());
                out.put("客单价_元", data.getAvgOrderPrice());
                out.put("累计支付用户数", data.getPaidUserCount());
                out.put("口径说明", "销售额/订单量与后台首页一致；业绩GMV仅统计已付款商城订单。");
                return out.toJSONString();
            }
            if (TOOL_OPERATING_DATA.equals(name)) {
                HomeOperatingDataResponse data = homeService.operatingData();
                JSONObject out = new JSONObject(true);
                out.put("待发货订单数", data.getNotShippingOrderNum());
                out.put("退款中订单数", data.getRefundingOrderNum());
                out.put("待核销订单数", data.getNotWriteOffOrderNum());
                out.put("库存预警商品数", data.getVigilanceInventoryNum());
                out.put("上架商品数", data.getOnSaleProductNum());
                out.put("仓库中商品数", data.getNotSaleProductNum());
                out.put("提现待审核数", data.getNotAuditNum());
                out.put("用户充值总金额_元", data.getTotalRechargeAmount());
                return out.toJSONString();
            }
            JSONObject unknown = new JSONObject();
            unknown.put("error", "未知工具: " + name);
            unknown.put("arguments", argsJson);
            return unknown.toJSONString();
        } catch (Exception e) {
            log.error("执行 AI 工具失败 name={}", name, e);
            JSONObject err = new JSONObject();
            err.put("error", "查询失败: " + e.getMessage());
            return err.toJSONString();
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
            if (!"user".equals(role) && !"assistant".equals(role)) {
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

    private static class RuntimeAiConfig {
        private boolean enabled;
        private String apiKey;
        private String model;
        private String apiUrl;
    }
}
