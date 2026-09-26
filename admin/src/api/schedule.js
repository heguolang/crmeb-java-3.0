// +----------------------------------------------------------------------
// | 黔序商城 [ 黔序科技，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
// +----------------------------------------------------------------------
// | Author: 贵州黔序科技有限公司
// +----------------------------------------------------------------------

import request from '@/utils/request';

// 定时任务列表
export function jobList() {
  return request({
    url: '/admin/schedule/job/list',
    method: 'get',
  });
}

// 定时任务日志分页列表
export function jobLogList(data) {
  return request({
    url: '/admin/schedule/job/log/list',
    method: 'get',
    params: { ...data },
  });
}

// 添加定时任务
export function scheduleJobAdd(pram) {
  const data = {
    jobId: pram.jobId,
    beanName: pram.beanName,
    cronExpression: pram.cronExpression,
    methodName: pram.methodName,
    params: pram.params,
    remark: pram.remark,
  };
  return request({
    url: '/admin/schedule/job/add',
    method: 'post',
    data: data,
  });
}

// 删除定时任务
export function scheduleJobDelete(id) {
  return request({
    url: `/admin/schedule/job/delete/${id}`,
    method: 'post',
  });
}

// 启动定时任务
export function scheduleJobStart(id) {
  return request({
    url: `/admin/schedule/job/start/${id}`,
    method: 'post',
  });
}

// 暂停定时任务
export function scheduleJobSuspend(id) {
  return request({
    url: `/admin/schedule/job/suspend/${id}`,
    method: 'post',
  });
}

// 立即执行定时任务（一次）暂停定时任务
export function scheduleJobTrig(id) {
  return request({
    url: `/admin/schedule/job/trig/${id}`,
    method: 'post',
  });
}

// 定时任务编辑
export function scheduleJobUpdate(pram) {
  const data = {
    jobId: pram.jobId,
    beanName: pram.beanName,
    cronExpression: pram.cronExpression,
    methodName: pram.methodName,
    params: pram.params,
    remark: pram.remark,
  };
  return request({
    url: '/admin/schedule/job/update',
    method: 'post',
    data: { ...data },
  });
}
