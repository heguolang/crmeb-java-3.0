package com.qxkj.service.service;

import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.user.UserTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.UserTagRequest;

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
public interface UserTagService extends IService<UserTag> {

    /**
     * 用户标签列表
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserTag> getList(PageParamRequest pageParamRequest);

    String getGroupNameInId(String tagIdValue);

    /**
     * 新增用户标签
     * @param userTagRequest 标签参数
     */
    Boolean create(UserTagRequest userTagRequest);

    /**
     * 删除用户标签
     * @param id 标签id
     */
    Boolean delete(Integer id);

    /**
     * 修改用户标签
     * @param id 标签id
     * @param userTagRequest 标签参数
     */
    Boolean updateTag(Integer id, UserTagRequest userTagRequest);
}
