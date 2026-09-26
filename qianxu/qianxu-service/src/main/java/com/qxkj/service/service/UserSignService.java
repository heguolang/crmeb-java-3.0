package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.UserSignInfoResponse;
import com.qxkj.common.vo.SystemGroupDataSignConfigVo;
import com.qxkj.common.model.user.UserSign;
import com.qxkj.common.vo.UserSignMonthVo;
import com.qxkj.common.vo.UserSignVo;

import java.util.HashMap;
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
public interface UserSignService extends IService<UserSign> {

    /**
     * 用户积分列表
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserSignVo> getList(PageParamRequest pageParamRequest);

    List<UserSign> getListByCondition(UserSign sign, PageParamRequest pageParamRequest);

    /**
     * 签到
     * @return SystemGroupDataSignConfigVo
     */
    SystemGroupDataSignConfigVo sign();

    /**
     * 今日记录详情
     * @return HashMap
     */
    HashMap<String, Object> get();

    /**
     * 签到配置
     * @return List<SystemGroupDataSignConfigVo>
     */
    List<SystemGroupDataSignConfigVo> getSignConfig();

    /**
     * 积分月度列表
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserSignMonthVo> getListGroupMonth(PageParamRequest pageParamRequest);

    /**
     * 获取用户签到信息
     * @return UserSignInfoResponse
     */
    UserSignInfoResponse getUserSignInfo();
}
