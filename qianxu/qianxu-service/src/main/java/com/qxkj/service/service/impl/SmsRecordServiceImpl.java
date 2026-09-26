package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.sms.SmsRecord;
import com.qxkj.service.dao.SmsRecordDao;
import com.qxkj.service.service.SmsRecordService;
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
public class SmsRecordServiceImpl extends ServiceImpl<SmsRecordDao, SmsRecord> implements SmsRecordService {

    @Resource
    private SmsRecordDao dao;

    /**
     * 短信发送记录保存
     *
     * @param smsRecord 待保存短信记录
     * @return 保存结果
     */
    @Override
    public boolean save(SmsRecord smsRecord) {
        return dao.insert(smsRecord) > 0;
    }
}

