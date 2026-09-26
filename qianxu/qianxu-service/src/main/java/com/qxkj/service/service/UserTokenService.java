package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.user.UserToken;

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
public interface UserTokenService extends IService<UserToken> {

    /**
     * 获取UserToken
     * @param token 微信为openid
     * @param type 类型
     * @return UserToken
     */
    UserToken getByOpenidAndType(String token, int type);

    void bind(String openId, int type, Integer uid);

    UserToken getTokenByUserId(Integer userId, int type);

    /**
     * 通过用户id删除
     * @param uid 用户ID
     */
    Boolean deleteByUid(Integer uid);
}
