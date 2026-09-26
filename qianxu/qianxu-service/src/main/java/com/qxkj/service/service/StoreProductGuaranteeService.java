package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.product.StoreProductGuarantee;
import com.qxkj.common.request.StoreProductGuaranteeRequest;
import com.qxkj.common.response.StoreProductGuaranteeResponse;

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
public interface StoreProductGuaranteeService extends IService<StoreProductGuarantee> {

    /**
     * 保障服务列表
     *
     * @param isShow 是否显示
     * @return List
     */
    List<StoreProductGuaranteeResponse> getAdminList(Boolean isShow);

    /**
     * 新增保障服务
     * @param request 新增参数
     * @return Boolean
     */
    Boolean add(StoreProductGuaranteeRequest request);

    /**
     * 删除保障服务
     * @param id 保障服务ID
     * @return Boolean
     */
    Boolean delete(Integer id);

    /**
     * 修改保障服务
     * @param request 修改参数
     * @return Boolean
     */
    Boolean edit(StoreProductGuaranteeRequest request);

    /**
     * 修改保障服务显示状态
     * @param id 保障服务ID
     * @return Boolean
     */
    Boolean updateShowStatus(Integer id);


    /**
     * 保障服务列表
     * @return List
     */
    List<StoreProductGuarantee> findByIdList(List<Integer> gidList);
}
