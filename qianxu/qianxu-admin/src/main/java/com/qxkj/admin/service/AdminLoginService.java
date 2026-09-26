package com.qxkj.admin.service;

import com.qxkj.common.request.LoginAdminUpdatePasswordRequest;
import com.qxkj.common.request.LoginAdminUpdateRequest;
import com.qxkj.common.request.SystemAdminLoginRequest;
import com.qxkj.common.response.MenusResponse;
import com.qxkj.common.response.SystemAdminResponse;
import com.qxkj.common.response.SystemLoginResponse;

import java.util.List;
import java.util.Map;

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
public interface AdminLoginService {

    /**
     * PC登录
     */
    SystemLoginResponse login(SystemAdminLoginRequest request, String ip);

    /**
     * 用户登出
     */
    Boolean logout();

    /**
     * 获取登录页图片
     * @return Map
     */
    Map<String, Object> getLoginPic();

    /**
     * 获取管理员可访问目录
     * @return List<MenusResponse>
     */
    List<MenusResponse> getMenus();

    /**
     * 根据Token获取对应用户信息
     */
    SystemAdminResponse getInfoByToken();

    /**
     * 账号登录检测
     * @param account 账号
     * @return 账号错误登录次数
     */
    Integer accountDetection(String account);

    /**
     * 修改登录用户信息
     *
     * @param request 请求参数
     * @return Boolean
     */
    Boolean loginAdminUpdate(LoginAdminUpdateRequest request);


    /**
     * 修改登录用户密码
     */
    Boolean loginAdminUpdatePwd(LoginAdminUpdatePasswordRequest request);
}
