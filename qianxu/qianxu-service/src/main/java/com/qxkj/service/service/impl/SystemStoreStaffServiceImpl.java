package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.constants.Constants;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.request.SystemStoreStaffRequest;
import com.qxkj.common.response.SystemStoreStaffResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.system.SystemStore;
import com.qxkj.common.model.system.SystemStoreStaff;
import com.qxkj.common.model.user.User;
import com.qxkj.service.dao.SystemStoreStaffDao;
import com.qxkj.service.service.SystemStoreService;
import com.qxkj.service.service.SystemStoreStaffService;
import com.qxkj.service.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SystemStoreStaffServiceImpl extends ServiceImpl<SystemStoreStaffDao, SystemStoreStaff> implements SystemStoreStaffService {

    @Resource
    private SystemStoreStaffDao dao;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemStoreService systemStoreService;
    
    /**
     * 列表
     * @param storeId 门店id
     * @param pageParamRequest 分页类参数
     * @return List<SystemStoreStaff>
     */
    @Override
    public PageInfo<SystemStoreStaffResponse> getList(Integer storeId, PageParamRequest pageParamRequest) {
        Page<SystemStore> systemStorePage = PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<SystemStoreStaff> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (storeId > 0) {
            lambdaQueryWrapper.eq(SystemStoreStaff::getStoreId, storeId);
        }
        ArrayList<SystemStoreStaffResponse> systemStoreStaffResponseArrayList = new ArrayList<>();
        List<SystemStoreStaff> systemStoreStaffList = dao.selectList(lambdaQueryWrapper);
        if (CollUtil.isEmpty(systemStoreStaffList)) {
            return new PageInfo<>();
        }

        //用户信息
        List<Integer> userIdList = systemStoreStaffList.stream().map(SystemStoreStaff::getUid).collect(Collectors.toList());
        HashMap<Integer, User> userList = null;
        if (userIdList.size() >= 1) {
            userList = userService.getMapListInUid(userIdList);
        }
        //门店信息
        List<Integer> storeIdList = systemStoreStaffList.stream().map(SystemStoreStaff::getStoreId).collect(Collectors.toList());
        HashMap<Integer, SystemStore> storeList = null;
        if (storeIdList.size() >= 1) {
            storeList = systemStoreService.getMapInId(storeIdList);
        }
        for (SystemStoreStaff systemStoreStaff : systemStoreStaffList) {
            SystemStoreStaffResponse systemStoreStaffResponse = new SystemStoreStaffResponse();
            BeanUtils.copyProperties(systemStoreStaff, systemStoreStaffResponse);
            if (CollUtil.isNotEmpty(userList) && userList.containsKey(systemStoreStaff.getUid())) {
                systemStoreStaffResponse.setUser(userList.get(systemStoreStaff.getUid()));
            }
            if (CollUtil.isNotEmpty(storeList) && storeList.containsKey(systemStoreStaff.getStoreId())) {
                systemStoreStaffResponse.setSystemStore(storeList.get(systemStoreStaff.getStoreId()));
            }
            systemStoreStaffResponseArrayList.add(systemStoreStaffResponse);
        }
        return CommonPage.copyPageInfo(systemStorePage, systemStoreStaffResponseArrayList);
    }

    /**
     * 查询核销员列表
     * @param userIds uidList
     * @return storeList
     */
    private List<SystemStoreStaff> getByAdminUserIds(List<Integer> userIds) {
        LambdaQueryWrapper<SystemStoreStaff> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(SystemStoreStaff::getUid, userIds);
        List<SystemStoreStaff> existStaffs = dao.selectList(lambdaQueryWrapper);
        return existStaffs.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 添加核销员 唯一验证
     *
     * @param request 当前添加参数
     * @return 添加结果
     */
    @Override
    public Boolean saveUnique(SystemStoreStaffRequest request) {
        List<Integer> userIds = new ArrayList<>();
        userIds.add(request.getUid());
        List<SystemStoreStaff> existStaffs = getByAdminUserIds(userIds);
        if (CollUtil.isNotEmpty(existStaffs) && existStaffs.size() > 0) throw new QianxuException(Constants.RESULT_VERIFICATION_USER_EXIST);
        SystemStoreStaff systemStoreStaff = new SystemStoreStaff();
        BeanUtils.copyProperties(request, systemStoreStaff);
        return dao.insert(systemStoreStaff) > 0;
    }

    /**
     * 更新核销员信息
     * @param id 核销员id
     * @param systemStoreStaffRequest 更新参数
     */
    @Override
    public Boolean edit(Integer id, SystemStoreStaffRequest systemStoreStaffRequest) {
        SystemStoreStaff systemStoreStaff = new SystemStoreStaff();
        BeanUtils.copyProperties(systemStoreStaffRequest, systemStoreStaff);
        systemStoreStaff.setId(id);
        systemStoreStaff.setUpdateTime(DateUtil.date());
        return updateById(systemStoreStaff);
    }

    /**
     * 修改核销员状态
     * @param id 核销员id
     * @param status 状态
     * @return Boolean
     */
    @Override
    public Boolean updateStatus(Integer id, Integer status) {
        SystemStoreStaff systemStoreStaff = getById(id);
        if (ObjectUtil.isNull(systemStoreStaff)) {
            throw new QianxuException("核销员不存在");
        }
        if (systemStoreStaff.getStatus().equals(status)) {
            return true;
        }
        systemStoreStaff.setStatus(status);
        systemStoreStaff.setUpdateTime(DateUtil.date());
        return updateById(systemStoreStaff);
    }

    @Override
    public Boolean deleteByUid(Integer uid) {
        LambdaUpdateWrapper<SystemStoreStaff> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(SystemStoreStaff::getUid, uid);
        return remove(wrapper);
    }
}

