package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qxkj.common.constants.AliyunSmsConstants;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.constants.SmsConstants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.sms.SmsTemplate;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SmsApplyTempRequest;
import com.qxkj.common.request.SmsModifySignRequest;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.common.utils.RedisUtil;
import com.qxkj.common.utils.ValidateFormUtil;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.service.service.AliyunSmsClient;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 短信服务实现 —— 阿里云短信（支持模拟发送）
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
    private SmsTemplateService smsTemplateService;

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
        throw new QianxuException("请前往阿里云短信控制台申请模板，审核通过后将模板 CODE 写入本地短信模板配置");
    }

    @Override
    public MyRecord applys(Integer type, PageParamRequest pageParamRequest) {
        MyRecord myRecord = new MyRecord();
        myRecord.set("count", 0);
        myRecord.set("data", CollUtil.newArrayList());
        myRecord.set("msg", "模板申请请在阿里云短信控制台完成");
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

    private Boolean sendByTemplate(String phone, String templateCode, HashMap<String, Object> map) {
        if (StrUtil.isBlank(phone) || StrUtil.isBlank(templateCode)) {
            return false;
        }
        try {
            aliyunSmsClient.send(phone, templateCode, new JSONObject(map));
            return true;
        } catch (Exception e) {
            logger.error("发送短信失败 phone={}, template={}, err={}", phone, templateCode, e.getMessage());
            return false;
        }
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
