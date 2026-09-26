package com.qxkj.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.model.system.SystemAdmin;
import com.qxkj.common.model.system.SystemRole;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemAdminAddRequest;
import com.qxkj.common.request.SystemAdminRequest;
import com.qxkj.common.request.SystemAdminUpdateRequest;
import com.qxkj.common.response.SystemAdminResponse;
import com.qxkj.common.utils.QianxuUtil;
import com.qxkj.common.utils.ValidateFormUtil;
import com.github.pagehelper.PageHelper;
import com.qxkj.service.dao.SystemAdminDao;
import com.qxkj.service.service.SystemAdminService;
import com.qxkj.service.service.SystemRoleService;
import org.apache.commons.lang3.StringUtils;
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
public class SystemAdminServiceImpl extends ServiceImpl<SystemAdminDao, SystemAdmin> implements SystemAdminService {

    @Resource
    private SystemAdminDao dao;

    @Autowired
    private SystemRoleService systemRoleService;

    /**
     * 后台管理员列表
     * @param request 请求参数
     * @param pageParamRequest 分页参数
     * @return List
     */
    @Override
    public List<SystemAdminResponse> getList(SystemAdminRequest request, PageParamRequest pageParamRequest) {
        PageHelper.startPage(pageParamRequest.getPage(), pageParamRequest.getLimit());

        //带SystemAdminRequest类的多条件查询
        LambdaQueryWrapper<SystemAdmin> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 隐藏系统运维账号（不在管理员列表展示）
        lambdaQueryWrapper.ne(SystemAdmin::getAccount, "qxtec");
        if (StrUtil.isNotBlank(request.getRoles())) {
            lambdaQueryWrapper.eq(SystemAdmin::getRoles, request.getRoles());
        }
        if (ObjectUtil.isNotNull(request.getStatus())) {
            lambdaQueryWrapper.eq(SystemAdmin::getStatus, request.getStatus());
        }
        if (StrUtil.isNotBlank(request.getRealName())) {
            lambdaQueryWrapper.and(i -> i.like(SystemAdmin::getRealName, request.getRealName())
                    .or().like(SystemAdmin::getAccount, request.getRealName()));
        }
        List<SystemAdmin> systemAdmins = dao.selectList(lambdaQueryWrapper);
        if (CollUtil.isEmpty(systemAdmins)) {
            return CollUtil.newArrayList();
        }
        List<SystemAdminResponse> systemAdminResponses = new ArrayList<>();
        List<SystemRole> roleList = systemRoleService.getAllList();
        for (SystemAdmin admin : systemAdmins) {
            SystemAdminResponse sar = new SystemAdminResponse();
            BeanUtils.copyProperties(admin, sar);
            sar.setLastTime(admin.getUpdateTime());
            if (StrUtil.isBlank(admin.getRoles())) continue;
            List<Integer> roleIds = QianxuUtil.stringToArrayInt(admin.getRoles());
            List<String> roleNames = new ArrayList<>();
            for (Integer roleId : roleIds) {
                List<SystemRole> hasRoles = roleList.stream().filter(e -> e.getId().equals(roleId)).collect(Collectors.toList());
                if (hasRoles.size()> 0) {
                    roleNames.add(hasRoles.stream().map(SystemRole::getRoleName).collect(Collectors.joining(",")));
                }
            }
            sar.setRoleNames(StringUtils.join(roleNames,","));
            systemAdminResponses.add(sar);
        }
        return systemAdminResponses;
    }

    /**
     * 新增管理员
     * @param systemAdminAddRequest 新增参数
     * @return Boolean
     */
    @Override
    public Boolean saveAdmin(SystemAdminAddRequest systemAdminAddRequest) {
        // 管理员名称唯一校验
        Integer result = checkAccount(systemAdminAddRequest.getAccount());
        if (result > 0) {
            throw new QianxuException("管理员已存在");
        }
        // 如果有手机号，校验手机号
        if (StrUtil.isNotBlank(systemAdminAddRequest.getPhone())) {
            ValidateFormUtil.isPhoneException(systemAdminAddRequest.getPhone());
        }

        SystemAdmin systemAdmin = new SystemAdmin();
        BeanUtils.copyProperties(systemAdminAddRequest, systemAdmin);

        String pwd = QianxuUtil.encryptPassword(systemAdmin.getPwd(), systemAdmin.getAccount());
        systemAdmin.setPwd(pwd);
        return save(systemAdmin);
    }

    /**
     * 管理员名称唯一校验
     * @param account 管理员账号
     * @return Integer
     */
    private Integer checkAccount(String account) {
        LambdaQueryWrapper<SystemAdmin> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SystemAdmin::getAccount, account);
        return dao.selectCount(lambdaQueryWrapper);

    }

    /**
     * 更新管理员
     */
    @Override
    public Boolean updateAdmin(SystemAdminUpdateRequest systemAdminRequest) {
        getDetail(systemAdminRequest.getId());
        verifyAccount(systemAdminRequest.getId(), systemAdminRequest.getAccount());
        // 如果有手机号，校验手机号
        if (StrUtil.isNotBlank(systemAdminRequest.getPhone())) {
            ValidateFormUtil.isPhoneException(systemAdminRequest.getPhone());
        }
        SystemAdmin systemAdmin = new SystemAdmin();
        BeanUtils.copyProperties(systemAdminRequest, systemAdmin);
        systemAdmin.setPwd(null);
        if (StrUtil.isNotBlank(systemAdminRequest.getPwd())) {
            String pwd = QianxuUtil.encryptPassword(systemAdminRequest.getPwd(), systemAdminRequest.getAccount());
            systemAdmin.setPwd(pwd);
        }
        systemAdmin.setUpdateTime(DateUtil.date());
        return updateById(systemAdmin);
    }

    /**
     * 校验账号唯一性（管理员更新时）
     * @param id 管理员id
     * @param account 管理员账号
     */
    private void verifyAccount(Integer id, String account) {
        LambdaQueryWrapper<SystemAdmin> lqw = Wrappers.lambdaQuery();
        lqw.ne(SystemAdmin::getId, id);
        lqw.eq(SystemAdmin::getAccount, account);
        SystemAdmin systemAdmin = dao.selectOne(lqw);
        if (ObjectUtil.isNotNull(systemAdmin)) {
            throw new QianxuException("账号已存在");
        }
    }

    /**
     * 修改后台管理员状态
     * @param id 管理员id
     * @param status 状态
     * @return Boolean
     */
    @Override
    public Boolean updateStatus(Integer id, Boolean status) {
        SystemAdmin systemAdmin = getDetail(id);
        if (systemAdmin.getStatus().equals(status)) {
            return true;
        }
        systemAdmin.setStatus(status);
        systemAdmin.setUpdateTime(DateUtil.date());
        return updateById(systemAdmin);
    }

    /**
     * 根据idList获取Map
     * @param adminIdList id数组
     * @return HashMap
     */
    @Override
    public HashMap<Integer, SystemAdmin> getMapInId(List<Integer> adminIdList) {
        HashMap<Integer, SystemAdmin> map = new HashMap<>();
        if (adminIdList.size() < 1) {
            return map;
        }
        LambdaQueryWrapper<SystemAdmin> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.in(SystemAdmin::getId, adminIdList);
        List<SystemAdmin> systemAdminList = dao.selectList(lambdaQueryWrapper);
        if (systemAdminList.size() < 1) {
            return map;
        }
        for (SystemAdmin systemAdmin : systemAdminList) {
            map.put(systemAdmin.getId(), systemAdmin);
        }
        return map;
    }

    /**
     * 修改后台管理员是否接收状态
     * @param id 管理员id
     * @return Boolean
     */
    @Override
    public Boolean updateIsSms(Integer id) {
        SystemAdmin systemAdmin = getDetail(id);
        if (StrUtil.isBlank(systemAdmin.getPhone())) {
            throw new QianxuException("请先为管理员添加手机号!");
        }
        systemAdmin.setIsSms(!systemAdmin.getIsSms());
        systemAdmin.setUpdateTime(DateUtil.date());
        return updateById(systemAdmin);
    }

    /**
     * 获取可以接收短信的管理员
     * @return List
     */
    @Override
    public List<SystemAdmin> findIsSmsList() {
        LambdaQueryWrapper<SystemAdmin> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemAdmin::getStatus, true);
        lqw.eq(SystemAdmin::getIsDel, false);
        lqw.eq(SystemAdmin::getIsSms, true);
        List<SystemAdmin> list = dao.selectList(lqw);
        if (CollUtil.isEmpty(list)) {
            return list;
        }
        return list.stream().filter(i -> StrUtil.isNotBlank(i.getPhone())).collect(Collectors.toList());
    }

    /**
     * 管理员详情
     * @param id 管理员id
     * @return SystemAdmin
     */
    @Override
    public SystemAdmin getDetail(Integer id) {
        SystemAdmin systemAdmin = getById(id);
        if (ObjectUtil.isNull(systemAdmin) || systemAdmin.getIsDel()) {
            throw new QianxuException("管理员不存在");
        }
        return systemAdmin;
    }

    /**
     * 通过用户名获取用户
     * @param username 用户名
     * @return SystemAdmin
     */
    @Override
    public SystemAdmin selectUserByUserName(String username) {
        LambdaQueryWrapper<SystemAdmin> lqw = Wrappers.lambdaQuery();
        lqw.eq(SystemAdmin::getAccount, username);
        lqw.eq(SystemAdmin::getIsDel, false);
        return dao.selectOne(lqw);
    }

}

