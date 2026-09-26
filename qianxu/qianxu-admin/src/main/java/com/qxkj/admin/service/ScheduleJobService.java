package com.qxkj.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.admin.model.ScheduleJob;
import com.qxkj.common.request.ScheduleJobRequest;

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
public interface ScheduleJobService extends IService<ScheduleJob> {

    /**
     * 所有定时任务列表
     */
    List<ScheduleJob> getAll();

    /**
     * 添加定时任务
     *
     * @param request 参数
     */
    Boolean add(ScheduleJobRequest request);

    /**
     * 定时任务编辑
     *
     * @param request 编辑参数
     */
    Boolean edit(ScheduleJobRequest request);

    /**
     * 暂停定时任务
     *
     * @param jobId 定时任务ID
     */
    Boolean suspend(Integer jobId);

    /**
     * 启动定时任务
     *
     * @param jobId 定时任务ID
     */
    Boolean start(Integer jobId);

    /**
     * 删除定时任务
     *
     * @param jobId 定时任务ID
     */
    Boolean delete(Integer jobId);

    /**
     * 立即触发定时任务（一次）
     *
     * @param jobId 定时任务ID
     */
    Boolean trig(Integer jobId);
}
