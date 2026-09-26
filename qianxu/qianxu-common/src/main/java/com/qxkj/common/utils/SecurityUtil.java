package com.qxkj.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.qxkj.common.exception.QianxuException;
import com.qxkj.common.vo.LoginUserVo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
public class SecurityUtil {

    /**
     * 获取管理员信息（从security中）
     */
    public static LoginUserVo getLoginUserVo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUserVo loginUserVo = (LoginUserVo) authentication.getPrincipal();
        if (ObjectUtil.isNull(loginUserVo)) {
            throw new QianxuException("登录信息已过期，请重新登录");
        }
        return loginUserVo;
    }

}
