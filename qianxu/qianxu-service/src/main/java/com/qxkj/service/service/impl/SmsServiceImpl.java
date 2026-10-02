package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qxkj.common.constants.AliyunSmsConstants;
import com.qxkj.common.constants.BinliSmsConstants;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SmsConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.sms.SmsRecord;
import com.qxkj.common.model.sms.SmsTemplate;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SmsApplyTempRequest;
import com.qxkj.common.request.SmsChannelConfigRequest;
import com.qxkj.common.request.SmsModifySignRequest;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.utils.ValidateFormUtil;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.service.service.AliyunSmsClient;
import com.qxkj.service.service.BinliSmsClient;
import com.qxkj.service.service.SmsRecordService;
import com.qxkj.service.service.SmsService;
import com.qxkj.service.service.SmsTemplateService;
import com.qxkj.service.service.SystemConfigService;
import com.qxkj.service.service.UserService;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 短信服务实现
 *
 * 双通道：binli=滨礼（默认，自由内容短信）、aliyun=阿里云（模板短信，回退保留）。
 * 通道由系统配置 sms_send_channel 控制。
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private AliyunSmsClient aliyunSmsClient;

    @Autowired
    private BinliSmsClient binliSmsClient;

    @Autowired
    private SmsTemplateService smsTemplateService;

    @Autowired
    private SmsRecordService smsRecordService;

    @Override
    public Boolean modifySign(SmsModifySignRequest request) {
        ValidateFormUtil.isPhoneException(request.getPhone());
        if (StrUtil.isBlank(request.getSign())) {
            throw new QianxuException("签名不能为空");
        }
        systemConfigService.updateOrSaveValueByName(AliyunSmsConstants.CONFIG_SIGN_NAME, request.getSign());
        return Boolean.TRUE;
    }

    @Override
    public MyRecord temps(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<SmsTemplate> list = smsTemplateService.list(new LambdaQueryWrapper<SmsTemplate>()
                .orderByDesc(SmsTemplate::getId));
        MyRecord myRecord = new MyRecord();
        if (CollUtil.isEmpty(list)) {
            return myRecord.set("count", 0).set("data", CollUtil.newArrayList());
        }
        List<MyRecord> recordList = list.stream().map(t -> {
            MyRecord record = new MyRecord();
            record.set("id", t.getId());
            record.set("temp_id", t.getTempId());
            record.set("title", t.getTitle());
            record.set("content", t.getContent());
            record.set("temp_type", t.getTempType());
            record.set("status", t.getStatus());
            record.set("type", resolveTempTypeName(t.getTempType()));
            return record;
        }).collect(Collectors.toList());
        return myRecord.set("count", recordList.size()).set("data", recordList);
    }

    @Override
    public Boolean applyTempMessage(SmsApplyTempRequest request) {
        throw new QianxuException("滨礼为自由内容短信，无需申请模板；如使用阿里云通道请前往阿里云短信控制台申请");
    }

    @Override
    public MyRecord applys(Integer type, PageParamRequest pageParamRequest) {
        MyRecord myRecord = new MyRecord();
        myRecord.set("count", 0);
        myRecord.set("data", CollUtil.newArrayList());
        myRecord.set("msg", "滨礼为自由内容短信，无需申请模板");
        return myRecord;
    }

    @Override
    public Boolean sendCommonCode(String phone) {
        ValidateFormUtil.isPhone(phone, "手机号码错误");
        if (redisUtil.exists(SmsConstants.SMS_VALIDATE_PHONE_NUM + phone)) {
            throw new QianxuException("您的短信发送过于频繁，请稍后再试");
        }
        String codeExpireStr = systemConfigService.getValueByKey(Constants.CONFIG_KEY_SMS_CODE_EXPIRE);
        if (StrUtil.isBlank(codeExpireStr) || Integer.parseInt(codeExpireStr) == 0) {
            codeExpireStr = Constants.NUM_FIVE + "";
        }
        Integer code = QianxuUtil.randomCount(111111, 999999);

        if (isBinliChannel()) {
            // 滨礼：自由内容验证码
            String contentTpl = systemConfigService.getValueByKey(BinliSmsConstants.CONFIG_VERIFY_CONTENT);
            if (StrUtil.isBlank(contentTpl)) {
                contentTpl = BinliSmsConstants.DEFAULT_VERIFY_CONTENT;
            }
            String content = contentTpl.replace("{code}", String.valueOf(code)).replace("{time}", codeExpireStr);
            try {
                binliSmsClient.sendText(phone, content);
            } catch (QianxuException e) {
                throw e;
            } catch (Exception e) {
                logger.error("发送验证码失败: {}", e.getMessage());
                throw new QianxuException("发送短信失败，请联系后台管理员");
            }
            saveBinliRecord(phone, content, "verify_code", null);
        } else {
            // 阿里云：模板验证码
            JSONObject param = new JSONObject();
            param.put("code", String.valueOf(code));
            param.put("time", codeExpireStr);

            String templateCode = systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_VERIFY_TEMPLATE_CODE);
            if (StrUtil.isBlank(templateCode)) {
                templateCode = AliyunSmsConstants.DEFAULT_VERIFY_TEMPLATE;
            }
            try {
                aliyunSmsClient.send(phone, templateCode, param);
            } catch (Exception e) {
                logger.error("发送验证码失败: {}", e.getMessage());
                throw new QianxuException("发送短信失败，请联系后台管理员");
            }
        }
        redisUtil.set(userService.getValidateCodeRedisKey(phone), code, Long.valueOf(codeExpireStr), TimeUnit.MINUTES);
        redisUtil.set(SmsConstants.SMS_VALIDATE_PHONE_NUM + phone, 1, 60L);
        return Boolean.TRUE;
    }

    @Override
    public Boolean sendPaySuccess(String phone, String orderNo, BigDecimal payPrice, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("pay_price", String.valueOf(payPrice));
        map.put("order_id", orderNo);
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public Boolean sendCreateOrderNotice(String phone, String orderNo, String realName, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("admin_name", realName);
        map.put("order_id", orderNo);
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public Boolean sendOrderPaySuccessNotice(String phone, String orderNo, String realName, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("admin_name", realName);
        map.put("order_id", orderNo);
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public Boolean sendOrderRefundApplyNotice(String phone, String orderNo, String realName, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("admin_name", realName);
        map.put("order_id", orderNo);
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public Boolean sendOrderReceiptNotice(String phone, String orderNo, String realName, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("admin_name", realName);
        map.put("order_id", orderNo);
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public Boolean sendOrderEditPriceNotice(String phone, String orderNo, BigDecimal price, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("order_id", orderNo);
        map.put("pay_price", String.valueOf(price));
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public Boolean sendOrderDeliverNotice(String phone, String nickName, String storeName, String orderNo, String templateCode) {
        HashMap<String, Object> map = CollUtil.newHashMap();
        map.put("nickname", nickName);
        map.put("store_name", storeName);
        map.put("order_id", orderNo);
        return sendByTemplate(phone, templateCode, map);
    }

    @Override
    public String sendContent(String phones, String content) {
        if (StrUtil.isBlank(phones) || StrUtil.isBlank(content)) {
            throw new QianxuException("手机号或短信内容不能为空");
        }
        String taskId = binliSmsClient.sendText(phones, content);
        saveBinliRecord(phones, content, "notice", taskId);
        return taskId;
    }

    @Override
    public MyRecord balance() {
        JSONObject data = binliSmsClient.queryBalance();
        MyRecord record = new MyRecord();
        record.set("balance", data == null ? null : data.get("balance"));
        record.set("payment", data == null ? null : data.get("payment"));
        record.set("paymentName", data == null ? null : data.get("paymentName"));
        record.set("mock", binliSmsClient.isMock());
        return record;
    }

    @Override
    public MyRecord getChannelConfig() {
        MyRecord record = new MyRecord();
        record.set("channel", resolveChannel());
        record.set("account", binliSmsClient.getAccount());
        record.set("pwd_set", StrUtil.isNotBlank(binliSmsClient.getPwd()));
        record.set("mock", "0".equals(systemConfigService.getValueByKey(BinliSmsConstants.CONFIG_MOCK)) ? "0" : "1");
        record.set("register_captcha", "0".equals(systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_REGISTER_CAPTCHA)) ? "0" : "1");
        record.set("verify_content", systemConfigService.getValueByKey(BinliSmsConstants.CONFIG_VERIFY_CONTENT));
        record.set("mock_aliyun", systemConfigService.getValueByKey(AliyunSmsConstants.CONFIG_MOCK));
        return record;
    }

    @Override
    public Boolean saveChannelConfig(SmsChannelConfigRequest request) {
        if (StrUtil.isNotBlank(request.getChannel())) {
            if (!BinliSmsConstants.CHANNEL_BINLI.equals(request.getChannel())
                    && !BinliSmsConstants.CHANNEL_ALIYUN.equals(request.getChannel())) {
                throw new QianxuException("发送通道仅支持 binli（滨礼）或 aliyun（阿里云）");
            }
            systemConfigService.updateOrSaveValueByName(SmsConstants.SMS_CONFIG_CHANNEL, request.getChannel());
        }
        if (StrUtil.isNotBlank(request.getAccount())) {
            systemConfigService.updateOrSaveValueByName(BinliSmsConstants.CONFIG_ACCOUNT, request.getAccount().trim());
        }
        if (StrUtil.isNotBlank(request.getPwd())) {
            systemConfigService.updateOrSaveValueByName(BinliSmsConstants.CONFIG_PWD, request.getPwd().trim());
        }
        if (StrUtil.isNotBlank(request.getMock())) {
            if (!"0".equals(request.getMock()) && !"1".equals(request.getMock())) {
                throw new QianxuException("模拟发送开关仅支持 0（真实发送）或 1（模拟）");
            }
            systemConfigService.updateOrSaveValueByName(BinliSmsConstants.CONFIG_MOCK, request.getMock());
        }
        if (StrUtil.isNotBlank(request.getRegisterCaptcha())) {
            if (!"0".equals(request.getRegisterCaptcha()) && !"1".equals(request.getRegisterCaptcha())) {
                throw new QianxuException("注册验证码开关仅支持 1（开启）或 0（关闭）");
            }
            systemConfigService.updateOrSaveValueByName(SmsConstants.SMS_CONFIG_REGISTER_CAPTCHA, request.getRegisterCaptcha());
        }
        if (request.getVerifyContent() != null) {
            systemConfigService.updateOrSaveValueByName(BinliSmsConstants.CONFIG_VERIFY_CONTENT, request.getVerifyContent().trim());
        }
        return Boolean.TRUE;
    }

    /**
     * 业务通知短信统一入口：按通道分流
     */
    private Boolean sendByTemplate(String phone, String templateCode, HashMap<String, Object> map) {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(templateCode)) {
            return false;
        }
        if (isBinliChannel()) {
            // 滨礼：取本地模板内容渲染 {$key} 占位符后自由发送
            String content = renderTemplateContent(templateCode, map);
            if (StrUtil.isBlank(content)) {
                logger.warn("滨礼通道未找到短信模板 {}，跳过发送 phone={}", templateCode, phone);
                return false;
            }
            try {
                binliSmsClient.sendText(phone, content);
                saveBinliRecord(phone, content, templateCode, null);
                return true;
            } catch (Exception e) {
                logger.error("发送短信失败 phone={}, template={}, err={}", phone, templateCode, e.getMessage());
                return false;
            }
        }
        // 阿里云：模板 + 参数
        try {
            aliyunSmsClient.send(phone, templateCode, new JSONObject((Map<String, Object>) map));
            return true;
        } catch (Exception e) {
            logger.error("发送短信失败 phone={}, template={}, err={}", phone, templateCode, e.getMessage());
            return false;
        }
    }

    /**
     * 按模板编号（temp_id 或 temp_key）取本地模板内容，替换 {$key} 占位符
     */
    private String renderTemplateContent(String templateCode, HashMap<String, Object> map) {
        List<SmsTemplate> templates = smsTemplateService.list(new LambdaQueryWrapper<SmsTemplate>()
                .and(w -> w.eq(SmsTemplate::getTempId, templateCode).or().eq(SmsTemplate::getTempKey, templateCode))
                .last(" limit 1"));
        if (CollUtil.isEmpty(templates)) {
            return null;
        }
        String content = templates.get(0).getContent();
        if (StrUtil.isBlank(content)) {
            return null;
        }
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    content = content.replace("{$" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }
            }
        }
        return content;
    }

    /**
     * 滨礼发送记录落库（失败不影响主流程）；多号码逗号分割时逐号码各存一条
     * （eb_sms_record.phone 为 char(11)，严格模式下整串写入会报截断错误）
     */
    private void saveBinliRecord(String phones, String content, String template, String taskId) {
        try {
            String account = binliSmsClient.getAccount();
            boolean mock = binliSmsClient.isMock();
            for (String phone : phones.split(",")) {
                SmsRecord record = new SmsRecord();
                record.setUid(account);
                record.setPhone(phone.trim());
                record.setContent(content);
                record.setTemplate(template);
                record.setResultcode(100);
                record.setMemo(mock ? "模拟发送" : "已提交滨礼");
                record.setTaskId(taskId);
                record.setChannel(BinliSmsConstants.CHANNEL_BINLI);
                smsRecordService.save(record);
            }
        } catch (Exception e) {
            logger.error("短信发送记录保存失败 phone={}", phones, e);
        }
    }

    /**
     * 当前发送通道是否滨礼（默认 binli，配置 aliyun 时走阿里云）
     */
    private boolean isBinliChannel() {
        return !BinliSmsConstants.CHANNEL_ALIYUN.equals(resolveChannel());
    }

    private String resolveChannel() {
        String channel = systemConfigService.getValueByKey(SmsConstants.SMS_CONFIG_CHANNEL);
        return StrUtil.isBlank(channel) ? BinliSmsConstants.CHANNEL_BINLI : channel;
    }

    private String resolveTempTypeName(Integer tempType) {
        if (tempType == null) {
            return "通知";
        }
        switch (tempType) {
            case 1:
                return "验证码";
            case 2:
                return "通知";
            case 3:
                return "营销短信";
            default:
                return "通知";
        }
    }
}
