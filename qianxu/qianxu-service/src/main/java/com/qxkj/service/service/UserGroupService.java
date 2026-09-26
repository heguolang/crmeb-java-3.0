package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.user.UserGroup;
import com.qxkj.common.request.UserGroupRequest;

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
public interface UserGroupService extends IService<UserGroup> {

    /**
     * 会员-分组列表
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserGroup> getList(PageParamRequest pageParamRequest);

    String getGroupNameInId(String groupIdValue);

    /**
     * 新增用户分组
     * @param userGroupRequest 分组参数
     */
    Boolean create(UserGroupRequest userGroupRequest);

    /**
     * 修改用户分组
     * @param id 分组id
     * @param userGroupRequest 修改参数
     */
    Boolean edit(Integer id, UserGroupRequest userGroupRequest);
}
