package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemStoreStaffRequest;
import com.qxkj.common.response.SystemStoreStaffResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.system.SystemStoreStaff;

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
public interface SystemStoreStaffService extends IService<SystemStoreStaff> {

    /**
     * 分页显示门店核销员列表
     * @param storeId 门店id
     * @param pageParamRequest 分页参数
     */
    PageInfo<SystemStoreStaffResponse> getList(Integer storeId, PageParamRequest pageParamRequest);

    /**
     *      添加核销员 唯一验证
     * @param request  当前添加参数
     * @return                  添加结果
     */
    Boolean saveUnique(SystemStoreStaffRequest request);

    /**
     * 更新核销员信息
     * @param id 核销员id
     * @param systemStoreStaffRequest 更新参数
     */
    Boolean edit(Integer id, SystemStoreStaffRequest systemStoreStaffRequest);

    /**
     * 修改核销员状态
     * @param id 核销员id
     * @param status 状态
     * @return Boolean
     */
    Boolean updateStatus(Integer id, Integer status);

    Boolean deleteByUid(Integer uid);
}
