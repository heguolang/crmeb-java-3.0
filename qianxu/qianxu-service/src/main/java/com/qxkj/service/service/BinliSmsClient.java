package com.qxkj.service.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.BinliSmsConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.utils.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 滨礼短信平台客户端。
 *
 * 账号与接口密码存放于系统配置（binli_sms_account / binli_sms_pwd），后台可改；
 * 调用时 pwd 取 MD5 32 位加密值。
 * 未配置账号密码或开启 mock（binli_sms_mock != "0"）时只打日志并视为发送成功，便于本地联调。
 */
@Component
public class BinliSmsClient {

    private static final Logger logger = LoggerFactory.getLogger(BinliSmsClient.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    /**
     * 是否处于模拟模式（未配置账号密码，或 mock 开关未显式置 0）
     */
    public boolean isMock() {
        String account = getAccount();
        String pwd = getPwd();
        if (StrUtil.isBlank(account) || StrUtil.isBlank(pwd)) {
            return true;
        }
        String mock = systemConfigService.getValueByKey(BinliSmsConstants.CONFIG_MOCK);
        return !"0".equals(mock);
    }

    public String getAccount() {
        return StrUtil.trim(systemConfigService.getValueByKey(BinliSmsConstants.CONFIG_ACCOUNT));
    }

    public String getPwd() {
        return StrUtil.trim(systemConfigService.getValueByKey(BinliSmsConstants.CONFIG_PWD));
    }

    /**
     * 1、发送文字短信（多个号码逗号分割，单批最大 3000）
     *
     * @return 批次 taskId；模拟模式返回 MOCK 前缀的假 taskId
     */
    public String sendText(String mobiles, String content) {
        return sendText(mobiles, content, null);
    }

    /**
     * 1、发送文字短信（可自定义批次 taskId）
     */
    public String sendText(String mobiles, String content, String taskId) {
        if (StrUtil.isBlank(mobiles) || StrUtil.isBlank(content)) {
            throw new QianxuException("手机号或短信内容不能为空");
        }
        if (isMock()) {
            logger.info("[滨礼短信-模拟] mobiles={}, content={}", mobiles, content);
            return mockTaskId();
        }
        JSONObject params = baseParams();
        params.put("mobiles", mobiles);
        params.put("content", content);
        if (StrUtil.isNotBlank(taskId)) {
            params.put("taskId", taskId);
        }
        JSONObject resp = post(BinliSmsConstants.API_SEND, params);
        JSONObject data = resp.getJSONObject("data");
        String realTaskId = data == null ? null : data.getString("taskId");
        logger.info("[滨礼短信] mobiles={}, taskId={}, result={}", mobiles, realTaskId, resp.getString("code"));
        return realTaskId;
    }

    /**
     * 2、点对点批量发送（每个号码内容可以不同）
     *
     * @param dataList 每项含 mobile / content / taskId(可选)
     * @return [{mobile, taskId}] 列表
     */
    public List<JSONObject> sendOneToOne(List<JSONObject> dataList, String extNo) {
        if (dataList == null || dataList.isEmpty()) {
            throw new QianxuException("下发数据列表不能为空");
        }
        if (isMock()) {
            logger.info("[滨礼短信-模拟-点对点] data={}", dataList);
            List<JSONObject> result = new ArrayList<>();
            for (JSONObject item : dataList) {
                JSONObject one = new JSONObject();
                one.put("mobile", item.getString("mobile"));
                one.put("taskId", mockTaskId());
                result.add(one);
            }
            return result;
        }
        JSONObject params = baseParams();
        params.put("data", dataList);
        if (StrUtil.isNotBlank(extNo)) {
            params.put("extNo", extNo);
        }
        JSONObject resp = post(BinliSmsConstants.API_BATCH_ONE_TO_ONE, params);
        JSONArray data = resp.getJSONArray("data");
        List<JSONObject> result = new ArrayList<>();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                result.add(data.getJSONObject(i));
            }
        }
        return result;
    }

    /**
     * 3、语音呼叫
     *
     * @return 批次 taskId
     */
    public String sendVoice(String mobiles, String content) {
        if (StrUtil.isBlank(mobiles) || StrUtil.isBlank(content)) {
            throw new QianxuException("手机号或语音内容不能为空");
        }
        if (isMock()) {
            logger.info("[滨礼语音-模拟] mobiles={}, content={}", mobiles, content);
            return mockTaskId();
        }
        JSONObject params = baseParams();
        params.put("mobiles", mobiles);
        params.put("content", content);
        JSONObject resp = post(BinliSmsConstants.API_VOICE_SEND, params);
        JSONObject data = resp.getJSONObject("data");
        return data == null ? null : data.getString("taskId");
    }

    /**
     * 4、发送统计（只统计当天数据且一小时统计一次，勿频繁调用）
     *
     * @param taskId     选填；不为空时 createDate 必填
     * @param createDate 格式 yyyy-MM-dd
     */
    public JSONObject queryTotal(String taskId, String createDate) {
        JSONObject params = baseParams();
        if (StrUtil.isNotBlank(taskId)) {
            params.put("taskId", taskId);
            params.put("createDate", createDate);
        } else if (StrUtil.isNotBlank(createDate)) {
            params.put("createDate", createDate);
        }
        JSONObject resp = post(BinliSmsConstants.API_QUERY_TOTAL, params);
        return resp.getJSONObject("data");
    }

    /**
     * 5、状态查询（文字短信/语音）
     *
     * @return [{taskId, status, times, mobile, extNo}]
     */
    public List<JSONObject> pullReport() {
        JSONObject resp = post(BinliSmsConstants.API_REPORT, baseParams());
        return toList(resp.get("data"));
    }

    /**
     * 6、上行查询（文字短信）
     *
     * @return [{taskId, mobile, content, times, extNo}]
     */
    public List<JSONObject> pullMo() {
        JSONObject resp = post(BinliSmsConstants.API_MO, baseParams());
        return toList(resp.get("data"));
    }

    /**
     * 7、余额查询
     *
     * @return {balance, payment, paymentName}
     */
    public JSONObject queryBalance() {
        JSONObject resp = post(BinliSmsConstants.API_QUERY_BALANCE, baseParams());
        return resp.getJSONObject("data");
    }

    // ------------------------------------------------------------------ 内部

    private JSONObject baseParams() {
        String account = getAccount();
        String pwd = getPwd();
        if (StrUtil.isBlank(account) || StrUtil.isBlank(pwd)) {
            throw new QianxuException("滨礼短信账号未配置，请在后台短信设置中填写账号和接口密码");
        }
        JSONObject params = new JSONObject();
        params.put("account", account);
        params.put("pwd", SecureUtil.md5(pwd));
        return params;
    }

    private JSONObject post(String api, JSONObject params) {
        try {
            JSONObject resp = restTemplateUtil.postJsonDataAndReturnJson(BinliSmsConstants.API_HOST + api, params);
            if (resp == null) {
                throw new QianxuException("滨礼短信接口无响应");
            }
            String code = resp.getString("code");
            if (!"00".equals(code)) {
                logger.error("[滨礼短信] api={}, code={}, message={}", api, code, resp.getString("message"));
                throw new QianxuException("滨礼短信接口失败[" + code + "]：" + resp.getString("message"));
            }
            return resp;
        } catch (QianxuException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[滨礼短信] api={} 调用异常", api, e);
            throw new QianxuException("滨礼短信接口调用异常：" + e.getMessage());
        }
    }

    /**
     * 滨礼文档中 data 的 JSON 示例不规范（data: {[...]}），做兼容解析：
     * 数组直接返回；对象则尝试取内部的数组字段。
     */
    private List<JSONObject> toList(Object data) {
        List<JSONObject> list = new ArrayList<>();
        if (data == null) {
            return list;
        }
        if (data instanceof JSONArray) {
            JSONArray arr = (JSONArray) data;
            for (int i = 0; i < arr.size(); i++) {
                list.add(arr.getJSONObject(i));
            }
        } else if (data instanceof JSONObject) {
            JSONObject obj = (JSONObject) data;
            for (String key : obj.keySet()) {
                Object inner = obj.get(key);
                if (inner instanceof JSONArray) {
                    JSONArray arr = (JSONArray) inner;
                    for (int i = 0; i < arr.size(); i++) {
                        list.add(arr.getJSONObject(i));
                    }
                    break;
                }
            }
        }
        return list;
    }

    private String mockTaskId() {
        return "MOCK" + System.currentTimeMillis();
    }
}
