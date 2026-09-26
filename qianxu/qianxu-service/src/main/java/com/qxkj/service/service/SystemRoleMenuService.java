package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.system.SystemRoleMenu;

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
public interface SystemRoleMenuService extends IService<SystemRoleMenu> {

    /**
     * 通过角色id删除
     * @param rid 角色id
     */
    Boolean deleteByRid(Integer rid);

    /**
     * 通过角色id获取菜单列表
     * @param rid 角色id
     * @return List
     */
    List<Integer> getMenuListByRid(Integer rid);
}
