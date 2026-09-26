package com.qxkj.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.user.UserTag;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.UserTagRequest;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.service.dao.UserTagDao;
import com.qxkj.service.service.UserService;
import com.qxkj.service.service.UserTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UserTagServiceImpl extends ServiceImpl<UserTagDao, UserTag> implements UserTagService {

    @Resource
    private UserTagDao dao;

    @Autowired
    private UserService userService;

    /**
    * 列表
    * @param pageParamRequest 分页类参数
    * @return List<UserTag>
    */
    @Override
    public List<UserTag> getList(PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        return dao.selectList(null);
    }

    /**
     * 根据id in 返回name字符串
     * @param tagIdValue String 标签id
     * @return List<UserTag>
     */
    @Override
    public String getGroupNameInId(String tagIdValue) {
        LambdaQueryWrapper<UserTag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(UserTag::getId, QianxuUtil.stringToArray(tagIdValue)).orderByDesc(UserTag::getId);
        List<UserTag> userTags = dao.selectList(lambdaQueryWrapper);
        if (null == userTags){
            return "无";
        }

        return userTags.stream().map(UserTag::getName).distinct().collect(Collectors.joining(","));
    }

    /**
     * 新增用户标签
     * @param userTagRequest 标签参数
     */
    @Override
    public Boolean create(UserTagRequest userTagRequest) {
        if (isExistName(userTagRequest.getName(), null)) {
            throw new QianxuException("标签名称已存在");
        }
        UserTag userTag = new UserTag();
        BeanUtils.copyProperties(userTagRequest, userTag);
        return save(userTag);
    }

    /**
     * 是否存在用户标签
     * @param tagName 标签名称
     * @param id 标签ID
     */
    private Boolean isExistName(String tagName, Integer id) {
        LambdaQueryWrapper<UserTag> lqw = new LambdaQueryWrapper<>();
        lqw.select(UserTag::getId);
        lqw.eq(UserTag::getName, tagName);
        if (ObjectUtil.isNotNull(id)) {
            lqw.ne(UserTag::getId, id);
        }
        lqw.last(" limit 1");
        UserTag userTag = dao.selectOne(lqw);
        return ObjectUtil.isNotNull(userTag);
    }

    /**
     * 删除用户标签
     * @param id 标签id
     */
    @Override
    public Boolean delete(Integer id) {
        boolean remove = removeById(id);
        if (remove) {
            userService.clearGroupByGroupId(id.toString());
        }
        return remove;
    }

    /**
     * 修改用户标签
     * @param id 标签id
     * @param userTagRequest 标签参数
     */
    @Override
    public Boolean updateTag(Integer id, UserTagRequest userTagRequest) {
        if (isExistName(userTagRequest.getName(), id)) {
            throw new QianxuException("标签名称已存在");
        }
        UserTag userTag = new UserTag();
        BeanUtils.copyProperties(userTagRequest, userTag);
        userTag.setId(id);
        return updateById(userTag);
    }

}

