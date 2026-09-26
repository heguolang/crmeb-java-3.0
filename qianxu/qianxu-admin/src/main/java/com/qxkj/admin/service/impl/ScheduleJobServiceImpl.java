package com.qxkj.admin.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.admin.dao.ScheduleJobDao;
import com.qxkj.admin.model.ScheduleJob;
import com.qxkj.admin.quartz.ScheduleConstants;
import com.qxkj.admin.quartz.ScheduleManager;
import com.qxkj.admin.service.ScheduleJobService;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.request.ScheduleJobRequest;
import org.quartz.CronTrigger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobDao, ScheduleJob> implements ScheduleJobService {

    @Resource
    private ScheduleJobDao dao;

    @Autowired
    private ScheduleManager scheduleManager;

    /**
     * 项目启动时，初始化定时器
     */
    @PostConstruct
    public void init() {
        getAll().forEach(scheduleJob -> {
            CronTrigger trigger = scheduleManager.getCronTrigger(scheduleJob);
            // 如果定时任务不存在，则创建定时任务
            if (trigger == null) {
                scheduleManager.createScheduleJob(scheduleJob);
            } else if (ScheduleConstants.NORMAL.equals(scheduleJob.getStatus())) {
                scheduleManager.resumeJob(scheduleJob);
            } else if (ScheduleConstants.PAUSE.equals(scheduleJob.getStatus())) {
                scheduleManager.pauseJob(scheduleJob);
            }
        });
    }

    /**
     * 获取所有的job
     *
     * @return List<ScheduleJob>
     */
    @Order
    public List<ScheduleJob> getAll() {
        LambdaQueryWrapper<ScheduleJob> lqw = Wrappers.lambdaQuery();
        lqw.eq(ScheduleJob::getIsDelte, false);
        lqw.orderByDesc(ScheduleJob::getJobId);
        return dao.selectList(lqw);
    }

    /**
     * 添加定时任务
     *
     * @param request 参数
     */
    @Override
    public Boolean add(ScheduleJobRequest request) {
        ScheduleJob scheduleJob = new ScheduleJob();
        BeanUtils.copyProperties(request, scheduleJob);
        scheduleJob.setJobId(null);
        scheduleJob.setStatus(ScheduleConstants.PAUSE);
        scheduleJob.setIsDelte(false);
        boolean save = save(scheduleJob);
        if (save) {
            scheduleManager.createScheduleJob(scheduleJob);
        }
        return save;
    }

    /**
     * 定时任务编辑
     *
     * @param request 编辑参数
     */
    @Override
    public Boolean edit(ScheduleJobRequest request) {
        if (ObjectUtil.isNull(request.getJobId())) {
            throw new QianxuException("定时任务ID不能为空");
        }
        ScheduleJob scheduleJob = getByIdException(request.getJobId());
        if (scheduleJob.getStatus().equals(ScheduleConstants.NORMAL)) {
            throw new QianxuException("请先暂停定时任务");
        }
        BeanUtils.copyProperties(request, scheduleJob);
        boolean update = updateById(scheduleJob);
        if (update) {
            scheduleManager.updateScheduleJob(scheduleJob);
        }
        return update;
    }

    /**
     * 暂停定时任务
     *
     * @param jobId 定时任务ID
     */
    @Override
    public Boolean suspend(Integer jobId) {
        ScheduleJob scheduleJob = getByIdException(jobId);
        if (scheduleJob.getStatus().equals(ScheduleConstants.PAUSE)) {
            throw new QianxuException("定时任务已暂停，请勿重复操作");
        }
        scheduleJob.setStatus(ScheduleConstants.PAUSE);
        boolean update = updateById(scheduleJob);
        if (update) {
            scheduleManager.pauseJob(scheduleJob);
        }
        return update;
    }

    /**
     * 启动定时任务
     *
     * @param jobId 定时任务ID
     */
    @Override
    public Boolean start(Integer jobId) {
        ScheduleJob scheduleJob = getByIdException(jobId);
        if (scheduleJob.getStatus().equals(ScheduleConstants.NORMAL)) {
            throw new QianxuException("定时任务已启动，请勿重复操作");
        }
        scheduleJob.setStatus(ScheduleConstants.NORMAL);
        boolean update = updateById(scheduleJob);
        if (update) {
            scheduleManager.resumeJob(scheduleJob);
        }
        return update;
    }

    /**
     * 删除定时任务
     *
     * @param jobId 定时任务ID
     */
    @Override
    public Boolean delete(Integer jobId) {
        ScheduleJob scheduleJob = getByIdException(jobId);
        if (scheduleJob.getStatus().equals(ScheduleConstants.NORMAL)) {
            throw new QianxuException("请先暂停定时任务");
        }
        scheduleJob.setIsDelte(true);
        boolean delete = updateById(scheduleJob);
        if (delete) {
            scheduleManager.deleteScheduleJob(scheduleJob);
        }
        return delete;
    }

    /**
     * 立即触发定时任务（一次）
     *
     * @param jobId 定时任务ID
     */
    @Override
    public Boolean trig(Integer jobId) {
        ScheduleJob scheduleJob = getByIdException(jobId);
        scheduleManager.run(scheduleJob);
        return Boolean.TRUE;
    }

    private ScheduleJob getByIdException(Integer jobId) {
        ScheduleJob scheduleJob = getById(jobId);
        if (ObjectUtil.isNull(scheduleJob) || scheduleJob.getIsDelte()) {
            throw new QianxuException("定时任务不存在");
        }
        return scheduleJob;
    }
}

