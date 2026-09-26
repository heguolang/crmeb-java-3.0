package com.qxkj.common.constants;

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
public class UserConstants {

    /** 用户类型——H5 */
    public static final String USER_TYPE_H5 = "h5";
    /** 用户类型——公众号 */
    public static final String USER_TYPE_WECHAT = "wechat";
    /** 用户类型——小程序 */
    public static final String USER_TYPE_ROUTINE = "routine";

    /**
     * =========================================================
     * UserToken部分
     * =========================================================
     */
    /** 用户Token类型——公众号 */
    public static final Integer USER_TOKEN_TYPE_WECHAT = 1;
    /** 用户Token类型——小程序 */
    public static final Integer USER_TOKEN_TYPE_ROUTINE = 2;
    /** 用户Token类型——unionid */
    public static final Integer USER_TOKEN_TYPE_UNIONID = 3;

    /**
     * =========================================================
     * 用户搜索部分
     * =========================================================
     */

    /** 用户搜索类型——all */
    public static final String USER_SEARCH_TYPE_ALL = "all";
    /** 用户搜索类型——UID 精准匹配 */
    public static final String USER_SEARCH_TYPE_UID = "uid";
    /** 用户搜索类型——用户昵称 全模糊匹配 */
    public static final String USER_SEARCH_TYPE_NICKNAME = "nickname";
    /** 用户搜索类型——手机号 精准匹配 */
    public static final String USER_SEARCH_TYPE_PHONE = "phone";
}
