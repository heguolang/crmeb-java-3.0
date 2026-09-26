package com.qxkj.common.response;

import com.qxkj.common.model.user.User;
import lombok.Data;

import java.io.Serializable;
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
@Data
public class GoPinkResponse implements Serializable {

    private static final long serialVersionUID = 1962453930812558918L;

    /** 拼团剩余人数 **/
    private Integer count;

    /** 当前拼团订单号 **/
    private String currentPinkOrder;

    /** 判断拼团是否完成 **/
    private Integer isOk;

    /** 团员列表 **/
    private List<StorePinkResponse> pinkAll;

    /** 拼团是否成功  0未成功 1成功 **/
    private Integer pinkBool;

    /** 当前用户是否在团内  0未在 1在 **/
    private Integer userBool;

    /** 团长信息 **/
    private StorePinkResponse pinkT;

    /** 当前拼团信息 **/
    private StoreCombinationResponse storeCombination;

    /** 当前用户信息 **/
    private User userInfo;
}
