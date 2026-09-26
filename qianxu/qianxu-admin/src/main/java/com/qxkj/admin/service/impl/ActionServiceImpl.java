package com.qxkj.admin.service.impl;

import com.qxkj.admin.service.ActionService;
import com.qxkj.common.model.log.SensitiveMethodLog;
import com.qxkj.service.service.SensitiveMethodLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
public class ActionServiceImpl implements ActionService {

    @Autowired
    private SensitiveMethodLogService sensitiveMethodLogService;

    /**
     * 添加敏感记录
     * @param methodLog 记录信息
     */
    @Async
    @Override
    public void addSensitiveLog(SensitiveMethodLog methodLog) {
        sensitiveMethodLogService.addLog(methodLog);
    }
}
