package com.qxkj.service.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.qxkj.common.constants.AliyunSmsConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.utils.RestTemplateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云短信发送客户端。
 * 未配置密钥或开启 mock 时仅打日志并视为成功，便于本地联调。
 */
@Component
public class AliyunSmsClient {

    private static final Logger logger = LoggerFactory.getLogger(AliyunSmsClient.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    /**
     * 发送短信
     *
     * @param phone          手机号
     * @param templateCode   阿里云模板 CODE
     * @param templateParam  模板参数 JSON 对象（可为 null）
     */
    public void send(String phone, String templateCode, JSONObject templateParam) {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(templateCode)) {
            throw new QianxuException("手机号或短信模板不能为空");
        }
        String accessKeyId = systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_ACCESS_KEY_ID);
        String accessKeySecret = systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_ACCESS_KEY_SECRET);
        String signName = systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_SIGN_NAME);
        if (StrUtil.isBlank(signName)) {
            signName = AliyunSmsConstants.DEFAULT_SIGN_NAME;
        }
        String mock = systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_MOCK);
        boolean useMock = !"0".equals(mock) || StrUtil.isBlank(accessKeyId) || StrUtil.isBlank(accessKeySecret);

        String paramJson = templateParam == null ? "{}" : templateParam.toJSONString();
        if (useMock) {
            logger.info("[阿里云短信-模拟] phone={}, sign={}, template={}, param={}", phone, signName, templateCode, paramJson);
            return;
        }
        sendReal(phone, signName, templateCode, paramJson, accessKeyId, accessKeySecret);
    }

    private void sendReal(String phone, String signName, String templateCode, String paramJson,
                          String accessKeyId, String accessKeySecret) {
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("AccessKeyId", accessKeyId);
            params.put("Action", AliyunSmsConstants.API_ACTION_SEND);
            params.put("Format", "JSON");
            params.put("PhoneNumbers", phone);
            params.put("RegionId", resolveRegion());
            params.put("SignName", signName);
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("SignatureNonce", UUID.randomUUID().toString());
            params.put("SignatureVersion", "1.0");
            params.put("TemplateCode", templateCode);
            params.put("TemplateParam", paramJson);
            params.put("Timestamp", gmtTimestamp());
            params.put("Version", AliyunSmsConstants.API_VERSION);
            params.put("Signature", sign("GET", params, accessKeySecret));

            StringBuilder url = new StringBuilder(AliyunSmsConstants.API_HOST).append("?");
            for (Map.Entry<String, String> e : params.entrySet()) {
                url.append(percentEncode(e.getKey())).append("=").append(percentEncode(e.getValue())).append("&");
            }
            url.setLength(url.length() - 1);

            JSONObject json = restTemplateUtil.getData(url.toString());
            logger.info("[阿里云短信] phone={}, template={}, result={}", phone, templateCode, json);
            if (json == null || !"OK".equalsIgnoreCase(json.getString("Code"))) {
                String msg = json == null ? "无响应" : json.getString("Message");
                throw new QianxuException("阿里云短信发送失败：" + msg);
            }
        } catch (QianxuException e) {
            throw e;
        } catch (Exception e) {
            logger.error("阿里云短信发送异常", e);
            throw new QianxuException("阿里云短信发送异常：" + e.getMessage());
        }
    }

    private String resolveRegion() {
        String region = systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_REGION_ID);
        return StrUtil.isBlank(region) ? AliyunSmsConstants.DEFAULT_REGION : region;
    }

    private static String gmtTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date());
    }

    private static String sign(String method, Map<String, String> params, String accessKeySecret) throws Exception {
        StringBuilder sorted = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            sorted.append("&").append(percentEncode(e.getKey())).append("=").append(percentEncode(e.getValue()));
        }
        String stringToSign = method + "&" + percentEncode("/") + "&" + percentEncode(sorted.substring(1));
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private static String percentEncode(String value) throws UnsupportedEncodingException {
        return value == null ? null : URLEncoder.encode(value, "UTF-8")
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }
}
