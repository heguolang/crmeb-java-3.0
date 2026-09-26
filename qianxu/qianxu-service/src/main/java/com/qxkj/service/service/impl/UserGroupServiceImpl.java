package com.qxkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.user.UserGroup;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserGroupRequest;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.service.dao.UserGroupDao;
import com.qxkj.service.service.UserGroupService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
@Service
public class UserGroupServiceImpl extends ServiceImpl<UserGroupDao, UserGroup> implements UserGroupService {

    @Resource
    private UserGroupDao dao;

    /**
    * 列表
    * @param pageParamRequest 分页类参数
    * @return List<UserGroup>
    */
    @Override
    public List<UserGroup> getList(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        return dao.selectList(null);
    }

    /**
     * 根据id in，返回字符串拼接
     * @param groupIdValue String 分组id
     * @return List<UserTag>
     */
    @Override
    public String getGroupNameInId(String groupIdValue) {
        LambdaQueryWrapper<UserGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(UserGroup::getId, QianxuUtil.stringToArray(groupIdValue)).orderByDesc(UserGroup::getId);
        List<UserGroup> userTags = dao.selectList(lambdaQueryWrapper);
        if(null == userTags){
            return "无";
        }

        return userTags.stream().map(UserGroup::getGroupName).distinct().collect(Collectors.joining(","));
    }

    /**
     * 新增用户分组
     * @param userGroupRequest 分组参数
     */
    @Override
    public Boolean create(UserGroupRequest userGroupRequest) {
        if (isExistName(userGroupRequest.getGroupName(), null)) {
            throw new QianxuException("分组名称已存在");
        }
        UserGroup userGroup = new UserGroup();
        BeanUtils.copyProperties(userGroupRequest, userGroup);
        return save(userGroup);
    }

    /**
     * 是否存在用户分组
     * @param groupName 分组名称
     * @param id 分组ID
     */
    private Boolean isExistName(String groupName, Integer id) {
        LambdaQueryWrapper<UserGroup> lqw = new LambdaQueryWrapper<>();
        lqw.select(UserGroup::getId);
        lqw.eq(UserGroup::getGroupName, groupName);
        if (ObjectUtil.isNotNull(id)) {
            lqw.ne(UserGroup::getId, id);
        }
        lqw.last(" limit 1");
        UserGroup userGroup = dao.selectOne(lqw);
        return ObjectUtil.isNotNull(userGroup);
    }

    /**
     * 修改用户分组
     * @param id 分组id
     * @param userGroupRequest 修改参数
     */
    @Override
    public Boolean edit(Integer id, UserGroupRequest userGroupRequest) {
        if (isExistName(userGroupRequest.getGroupName(), id)) {
            throw new QianxuException("分组名称已存在");
        }
        UserGroup userGroup = new UserGroup();
        BeanUtils.copyProperties(userGroupRequest, userGroup);
        userGroup.setId(id);
        return updateById(userGroup);
    }

}

