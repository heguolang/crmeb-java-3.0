package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.constants.BinliSmsConstants;
import com.qxkj.common.model.sms.SmsRecord;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.utils.BinliSmsStatusUtil;
import com.qxkj.common.vo.MyRecord;
import com.qxkj.service.dao.SmsRecordDao;
import com.qxkj.service.service.BinliSmsClient;
import com.qxkj.service.service.SmsRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

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

    private static final Logger logger = LoggerFactory.getLogger(SmsRecordServiceImpl.class);

    @Resource
    private SmsRecordDao dao;

    @Autowired
    private BinliSmsClient binliSmsClient;

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

    @Override
    public MyRecord getList(String phone, String status, String keywords, PageParamRequest pageParamRequest) {
        LambdaQueryWrapper<SmsRecord> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(phone)) {
            wrapper.like(SmsRecord::getPhone, phone.trim());
        }
        if (StrUtil.isNotBlank(status)) {
            if (BinliSmsConstants.STATUS_DELIVRD.equals(status)) {
                wrapper.eq(SmsRecord::getStatus, BinliSmsConstants.STATUS_DELIVRD);
            } else {
                // 非 DELIVRD 的都算失败：有回执但状态不是 DELIVRD
                wrapper.isNotNull(SmsRecord::getStatus)
                        .ne(SmsRecord::getStatus, "")
                        .ne(SmsRecord::getStatus, BinliSmsConstants.STATUS_DELIVRD);
            }
        }
        if (StrUtil.isNotBlank(keywords)) {
            wrapper.like(SmsRecord::getContent, keywords.trim());
        }
        wrapper.orderByDesc(SmsRecord::getId);
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        List<SmsRecord> list = dao.selectList(wrapper);
        MyRecord myRecord = new MyRecord();
        if (CollUtil.isEmpty(list)) {
            return myRecord.set("count", 0).set("data", CollUtil.newArrayList());
        }
        // PageHelper 分页时取真实总数，而非当页条数
        long total = list instanceof com.github.pagehelper.Page
                ? ((com.github.pagehelper.Page<SmsRecord>) list).getTotal()
                : list.size();
        myRecord.set("data", list);
        myRecord.set("count", total);
        return myRecord;
    }

    @Override
    public Integer syncStatusFromBinli() {
        List<JSONObject> reports = binliSmsClient.pullReport();
        int updated = 0;
        for (JSONObject item : reports) {
            String taskId = item.getString("taskId");
            String mobile = item.getString("mobile");
            if (StrUtil.isBlank(taskId) || StrUtil.isBlank(mobile)) {
                continue;
            }
            if (applyStatus(taskId, mobile, item.getString("status"), item.getString("times"))) {
                updated++;
            }
        }
        logger.info("[滨礼短信] 状态同步完成，共 {} 条回执，更新 {} 条记录", reports.size(), updated);
        return updated;
    }

    @Override
    public void handleStatusPush(String taskId, String mobile, String status, String times) {
        if (StrUtil.isBlank(taskId) || StrUtil.isBlank(mobile)) {
            return;
        }
        applyStatus(taskId, mobile, status, times);
    }

    /**
     * 按 taskId + 手机号定位记录并更新回执状态；状态未变化时跳过（幂等）
     *
     * @return 是否有更新
     */
    private boolean applyStatus(String taskId, String mobile, String status, String times) {
        List<SmsRecord> records = dao.selectList(new LambdaQueryWrapper<SmsRecord>()
                .eq(SmsRecord::getTaskId, taskId)
                // 手机号列可能存逗号分割的批量号码，用包含匹配
                .like(SmsRecord::getPhone, mobile)
                .last(" limit 10"));
        if (CollUtil.isEmpty(records)) {
            return false;
        }
        boolean changed = false;
        for (SmsRecord record : records) {
            if (status != null && status.equals(record.getStatus())) {
                continue;
            }
            record.setStatus(status);
            record.setStatusDesc(resolveStatusDesc(status));
            record.setStatusTime(times);
            record.setResultcode(BinliSmsStatusUtil.isSuccess(status) ? 100 : 130);
            record.setMemo(BinliSmsStatusUtil.isSuccess(status)
                    ? "发送成功" : "发送失败：" + resolveStatusDesc(status) + "(" + status + ")");
            dao.updateById(record);
            changed = true;
        }
        return changed;
    }

    private String resolveStatusDesc(String status) {
        return BinliSmsStatusUtil.resolveDesc(status);
    }
}
