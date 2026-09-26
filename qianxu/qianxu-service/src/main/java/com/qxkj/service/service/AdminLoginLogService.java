package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.log.AdminLoginLog;
import com.qxkj.common.request.AdminLoginLogSearchRequest;

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
public interface AdminLoginLogService extends IService<AdminLoginLog> {

    /**
     * 添加登录日志
     * @param loginLog 日志信息
     */
    void addLog(AdminLoginLog loginLog);

    /**
     * 分页列表
     * @param request 搜索条件
     * @return PageInfo
     */
    PageInfo<AdminLoginLog> getPageList(AdminLoginLogSearchRequest request);

    /**
     * 删除日志
     * @param id 日志id
     * @return Boolean
     */
    Boolean deleteById(Integer id);

    /**
     * 清空全部登录日志
     * @return Boolean
     */
    Boolean clearAll();
}
