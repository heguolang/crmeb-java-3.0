package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemMenu;
import com.qxkj.common.request.SystemMenuRequest;
import com.qxkj.common.request.SystemMenuSearchRequest;
import com.qxkj.common.vo.MenuCheckVo;

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
public interface SystemMenuService extends IService<SystemMenu> {

    /**
     * 通过权限获取管理员可访问目录
     * @return List<SystemMenu>
     */
    List<SystemMenu> findCatalogueByPermission(List<String> permissionsList);

    /**
     * 获取所有菜单
     * @return List<SystemMenu>
     */
    List<SystemMenu> findAllCatalogue();

    /**
     * 菜单列表
     * @param request 请求参数
     */
    List<SystemMenu> getAdminList(SystemMenuSearchRequest request);

    /**
     * 新增菜单
     * @param systemMenuRequest 菜单参数
     * @return Boolean
     */
    Boolean add(SystemMenuRequest systemMenuRequest);

    /**
     * 根据id删除菜单
     * @param id 菜单id
     * @return Boolean
     */
    Boolean deleteById(Integer id);

    /**
     * 修改菜单
     * @param systemMenuRequest 菜单参数
     * @return Boolean
     */
    Boolean edit(SystemMenuRequest systemMenuRequest);

    /**
     * 获取菜单详情
     * @param id 菜单id
     * @return SystemMenu
     */
    SystemMenu getInfo(Integer id);

    /**
     * 修改菜单显示状态
     * @param id 菜单id
     * @return Boolean
     */
    Boolean updateShowStatus(Integer id);

    /**
     * 获取菜单缓存列表
     * @return List
     */
    List<SystemMenu> getCacheList();

    /**
     * 菜单缓存树
     * @return List
     */
    List<MenuCheckVo> getCacheTree();

    /**
     * 获取所有权限
     * @return List
     */
    List<SystemMenu> getAllPermissions();

    /**
     * 通过用户id获取权限
     */
    List<SystemMenu> findPermissionByUserId(Integer userId);

    /**
     * 获取用户路由
     * @param userId 用户id
     * @return List
     */
    List<SystemMenu> getMenusByUserId(Integer userId);
}
