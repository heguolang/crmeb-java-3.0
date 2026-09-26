package com.qxkj.admin.controller;

import com.qxkj.admin.model.ScheduleJob;
import com.qxkj.admin.model.ScheduleJobLog;
import com.qxkj.admin.service.ScheduleJobLogService;
import com.qxkj.admin.service.ScheduleJobService;
import com.qxkj.common.annotation.LogControllerAnnotation;
import com.qxkj.common.enums.MethodType;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.ScheduleJobLogSearchRequest;
import com.qxkj.common.request.ScheduleJobRequest;
import com.qxkj.common.result.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
@RestController
@RequestMapping("api/admin/schedule/job")
@Api(tags = "定时任务控制器")
public class ScheduleJobController {

    @Autowired
    private ScheduleJobService scheduleJobService;
    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    @PreAuthorize("hasAuthority('admin:schedule:job:list')")
    @ApiOperation(value = "定时任务列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<ScheduleJob>> getList() {
        return CommonResult.success(scheduleJobService.getAll());
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "添加定时任务")
    @PreAuthorize("hasAuthority('admin:schedule:job:add')")
    @ApiOperation(value = "添加定时任务")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult<String> add(@RequestBody @Validated ScheduleJobRequest request) {
        if (scheduleJobService.add(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "定时任务编辑")
    @PreAuthorize("hasAuthority('admin:schedule:job:update')")
    @ApiOperation(value = "定时任务编辑")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult<String> update(@RequestBody @Validated ScheduleJobRequest request) {
        if (scheduleJobService.edit(request)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "暂停定时任务")
    @PreAuthorize("hasAuthority('admin:schedule:job:suspend')")
    @ApiOperation(value = "暂停定时任务")
    @RequestMapping(value = "/suspend/{jobId}", method = RequestMethod.POST)
    public CommonResult<String> suspend(@PathVariable(value = "jobId") Integer jobId) {
        if (scheduleJobService.suspend(jobId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "启动定时任务")
    @PreAuthorize("hasAuthority('admin:schedule:job:start')")
    @ApiOperation(value = "启动定时任务")
    @RequestMapping(value = "/start/{jobId}", method = RequestMethod.POST)
    public CommonResult<String> start(@PathVariable(value = "jobId") Integer jobId) {
        if (scheduleJobService.start(jobId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "删除定时任务")
    @PreAuthorize("hasAuthority('admin:schedule:job:delete')")
    @ApiOperation(value = "删除定时任务")
    @RequestMapping(value = "/delete/{jobId}", method = RequestMethod.POST)
    public CommonResult<String> delete(@PathVariable(value = "jobId") Integer jobId) {
        if (scheduleJobService.delete(jobId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @LogControllerAnnotation(intoDB = true, methodType = MethodType.UPDATE, description = "立即执行定时任务（一次）")
    @PreAuthorize("hasAuthority('admin:schedule:job:trig')")
    @ApiOperation(value = "立即执行定时任务（一次）")
    @RequestMapping(value = "/trig/{jobId}", method = RequestMethod.POST)
    public CommonResult<String> trig(@PathVariable(value = "jobId") Integer jobId) {
        if (scheduleJobService.trig(jobId)) {
            return CommonResult.success();
        }
        return CommonResult.failed();
    }

    @PreAuthorize("hasAuthority('admin:schedule:job:log:list')")
    @ApiOperation(value = "定时任务日志分页列表")
    @RequestMapping(value = "/log/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<ScheduleJobLog>> getLogList(@Validated ScheduleJobLogSearchRequest request,
                                                               @Validated PageParamRequest pageParamRequest) {
        return CommonResult.success(CommonPage.restPage(scheduleJobLogService.findLogPageList(request, pageParamRequest)));
    }
}



