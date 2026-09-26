package com.qxkj.service.service;

import com.qxkj.common.response.*;

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
public interface UserStatisticsService {

    /**
     * 获取总数据
     * @return UserTotalResponse
     */
    UserTotalResponse getTotalDate();

    /**
     * 用户概览数据
     * @param dateLimit 时间参数
     * @return UserOverviewResponse
     */
    UserOverviewResponse getOverview(String dateLimit);

    /**
     * 获取用户性别数据
     * @return List
     */
    List<UserSexDataResponse> getSexData();

    /**
     * 获取用户渠道数据
     * @return List
     */
    List<UserChannelDataResponse> getChannelData();

    /**
     * 获取用户区域数据
     * @return List
     */
    List<UserAreaDataResponse> getAreaData();

    /**
     * 用户概览数据列表（导出使用）
     * @param dateLimit 时间参数
     * @return UserOverviewResponse
     */
    List<UserOverviewDateResponse> getOverviewList(String dateLimit);

}
