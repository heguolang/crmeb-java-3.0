package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.log.SensitiveMethodLog;
import com.qxkj.common.request.PageParamRequest;

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
public interface SensitiveMethodLogService extends IService<SensitiveMethodLog> {

    /**
     * 添加敏感记录
     * @param methodLog 记录信息
     */
    void addLog(SensitiveMethodLog methodLog);

    /**
     * 分页列表
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<SensitiveMethodLog> getPageList(PageParamRequest pageParamRequest);

    /**
     * 清空全部操作日志
     * @return Boolean
     */
    Boolean clearAll();
}
