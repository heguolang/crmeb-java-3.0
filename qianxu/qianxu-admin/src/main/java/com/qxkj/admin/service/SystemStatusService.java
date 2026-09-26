package com.qxkj.admin.service;


import com.qxkj.common.response.SystemStatusResponse;

public interface SystemStatusService {


    /**
     * 查询java运行环境提供的系统相关数据
     * @return 查询结果  SystemStatusResponse
     */
    SystemStatusResponse getInfo();
}
