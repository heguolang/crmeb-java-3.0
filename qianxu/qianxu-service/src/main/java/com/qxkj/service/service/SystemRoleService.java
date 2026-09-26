package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemRole;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.SystemRoleRequest;
import com.qxkj.common.request.SystemRoleSearchRequest;
import com.qxkj.common.response.RoleInfoResponse;
import com.qxkj.common.vo.CategoryTreeVo;

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
public interface SystemRoleService extends IService<SystemRole> {

    /**
     * 获取所有角色
     * @return List
     */
    List<SystemRole> getAllList();

    /**
     * 列表
     * @param request 请求参数
     * @param pageParamRequest 分页类参数
     * @return List<SystemRole>
     */
    List<SystemRole> getList(SystemRoleSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 根据id集合获取对应权限列表
     * @param ids id集合
     * @return 对应的权限列表
     */
    List<SystemRole> getListInIds(List<Integer> ids);

    /**
     * 管理员菜单权限
     */
    List<CategoryTreeVo> menu();

    /**
     * 修改身份状态
     */
    Boolean updateStatus(Integer id, Boolean status);

    /**
     * 添加身份
     * @param systemRoleRequest 身份参数
     * @return Boolean
     */
    Boolean add(SystemRoleRequest systemRoleRequest);

    /**
     * 修改身份管理表
     * @param systemRoleRequest 修改参数
     */
    Boolean edit(SystemRoleRequest systemRoleRequest);

    /**
     * 删除角色
     * @param id 角色id
     * @return Boolean
     */
    Boolean delete(Integer id);

    /**
     * 获取角色详情
     * @param id 角色id
     * @return RoleInfoResponse
     */
    RoleInfoResponse getInfo(Integer id);
}
