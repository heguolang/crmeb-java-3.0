package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.system.SystemRoleMenu;
import com.qxkj.service.dao.SystemRoleMenuDao;
import com.qxkj.service.service.SystemRoleMenuService;
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
public class SystemRoleMenuServiceImpl extends ServiceImpl<SystemRoleMenuDao, SystemRoleMenu> implements SystemRoleMenuService {

    @Resource
    private SystemRoleMenuDao dao;

    /**
     * 通过角色id删除
     * @param rid 角色id
     */
    @Override
    public Boolean deleteByRid(Integer rid) {
        LambdaUpdateWrapper<SystemRoleMenu> luw = Wrappers.lambdaUpdate();
        luw.eq(SystemRoleMenu::getRid, rid);
        return dao.delete(luw) > 0;
    }

    /**
     * 通过角色id获取菜单列表
     * @param rid 角色id
     * @return List
     */
    @Override
    public List<Integer> getMenuListByRid(Integer rid) {
        LambdaQueryWrapper<SystemRoleMenu> lqw = Wrappers.lambdaQuery();
        lqw.select(SystemRoleMenu::getMenuId);
        lqw.eq(SystemRoleMenu::getRid, rid);
        List<SystemRoleMenu> roleMenuList = dao.selectList(lqw);
        return roleMenuList.stream().map(SystemRoleMenu::getMenuId).collect(Collectors.toList());
    }
}

