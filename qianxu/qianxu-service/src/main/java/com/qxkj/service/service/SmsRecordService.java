package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.sms.SmsRecord;
import com.qxkj.common.vo.MyRecord;

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
public interface SmsRecordService extends IService<SmsRecord> {

    /**
     * 保存短信记录
     * @param smsRecord 待保存短信记录
     * @return 保存结果
     */
    boolean save(SmsRecord smsRecord);

    /**
     * 后台分页查询发送记录
     *
     * @param phone     手机号（模糊，可空）
     * @param status    回执状态：DELIVRD=成功，其他=失败，空=全部
     * @param keywords  内容关键词（模糊，可空）
     */
    MyRecord getList(String phone, String status, String keywords, com.qxkj.common.request.PageParamRequest pageParamRequest);

    /**
     * 从滨礼拉取状态报告并更新本地记录
     *
     * @return 更新的记录条数
     */
    Integer syncStatusFromBinli();

    /**
     * 处理滨礼状态推送（单个号码回执）
     *
     * @param taskId 批次 id
     * @param mobile 手机号
     * @param status 状态码（DELIVRD=成功）
     * @param times  回执时间 yyyy-MM-dd HH:mm:ss
     */
    void handleStatusPush(String taskId, String mobile, String status, String times);
}
