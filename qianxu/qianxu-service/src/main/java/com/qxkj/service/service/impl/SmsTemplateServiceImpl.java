package com.qxkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.sms.SmsTemplate;
import com.qxkj.service.dao.SmsTemplateDao;
import com.qxkj.service.service.SmsTemplateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Service
public class SmsTemplateServiceImpl extends ServiceImpl<SmsTemplateDao, SmsTemplate> implements SmsTemplateService {

    @Resource
    private SmsTemplateDao dao;

    /**
     * 获取详情
     * @param id 模板id
     * @return SmsTemplate
     */
    @Override
    public SmsTemplate getDetail(Integer id) {
        SmsTemplate smsTemplate = getById(id);
        if (ObjectUtil.isNull(smsTemplate)) {
            throw new QianxuException("短信模板不存在");
        }
        return smsTemplate;
    }
}

