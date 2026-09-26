package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.user.UserAddress;
import com.qxkj.common.request.UserAddressRequest;

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
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 用户地址列表
     * @param pageParamRequest 分页参数
     * @return List<UserAddress>
     */
    List<UserAddress> getList(PageParamRequest pageParamRequest);

    /**
     * 添加用户地址
     * @param request 地址请求参数
     * @return UserAddress
     */
    UserAddress create(UserAddressRequest request);

    /**
     * 设置默认地址
     * @param id 地址id
     * @return Boolean
     */
    Boolean def(Integer id);

    /**
     * 删除用户地址
     * @param id 地址id
     * @return Boolean
     */
    Boolean delete(Integer id);

    UserAddress getDefault();

    UserAddress getById(Integer addressId);

    /**
     * 获取地址详情
     * @param id 地址id
     * @return UserAddress
     */
    UserAddress getDetail(Integer id);

    /**
     * 获取默认地址
     * @return UserAddress
     */
    UserAddress getDefaultByUid(Integer uid);
}
