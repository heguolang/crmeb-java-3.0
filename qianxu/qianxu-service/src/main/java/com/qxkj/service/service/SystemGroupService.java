package com.qxkj.service.service;

import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.system.SystemGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.SystemGroupRequest;
import com.qxkj.common.request.SystemGroupSearchRequest;

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
public interface SystemGroupService extends IService<SystemGroup> {

    /**
     * 分页显示组合数据表
     * @param request 搜索条件
     * @param pageParamRequest 分页参数
     */
    List<SystemGroup> getList(SystemGroupSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增组合数据
     * @param systemGroupRequest 新增参数
     */
    Boolean add(SystemGroupRequest systemGroupRequest);

    /**
     * 删除组合数据表
     * @param id Integer
     */
    Boolean delete(Integer id);

    /**
     * 修改组合数据表
     * @param id integer id
     * @param systemGroupRequest 修改参数
     */
    Boolean edit(Integer id, SystemGroupRequest systemGroupRequest);
}
