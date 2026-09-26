package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.system.SystemFormTemp;
import com.qxkj.common.request.SystemFormCheckRequest;
import com.qxkj.common.request.SystemFormTempRequest;
import com.qxkj.common.request.SystemFormTempSearchRequest;

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
public interface SystemFormTempService extends IService<SystemFormTemp> {

    /**
     * 列表
     * @param request 请求参数
     * @param pageParamRequest 分页类参数
     * @return List<SystemFormTemp>
     */
    List<SystemFormTemp> getList(SystemFormTempSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 验证item规则
     * @param systemFormCheckRequest SystemFormCheckRequest 表单数据提交
     */
    void checkForm(SystemFormCheckRequest systemFormCheckRequest);

    /**
     * 新增表单模板
     * @param systemFormTempRequest 新增参数
     */
    Boolean add(SystemFormTempRequest systemFormTempRequest);

    /**
     * 修改表单模板
     * @param id integer id
     * @param systemFormTempRequest 修改参数
     */
    Boolean edit(Integer id, SystemFormTempRequest systemFormTempRequest);

    /**
     * 通过名称查询详情
     * @param name 表单名称
     * @return SystemFormTemp
     */
    SystemFormTemp getOneByName(String name);
}
