package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.AdminIntegralSearchRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.UserIntegralRecordResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.user.UserIntegralRecord;

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
public interface UserIntegralRecordService extends IService<UserIntegralRecord> {

    /**
     * 根据订单编号、uid获取记录列表
     * @param orderNo 订单编号
     * @param uid 用户uid
     * @return 记录列表
     */
    List<UserIntegralRecord> findListByOrderIdAndUid(String orderNo, Integer uid);

    /**
     * 积分解冻
     */
    void integralThaw();

    /**
     * PC后台列表
     * @param request 搜索条件
     * @return 记录列表
     */
    PageInfo<UserIntegralRecordResponse> findAdminList(AdminIntegralSearchRequest request);

    /**
     * 根据类型条件计算积分总数
     * @param uid 用户uid
     * @param type 类型：1-增加，2-扣减
     * @param date 日期
     * @param linkType 关联类型
     * @return 积分总数
     */
    Integer getSumIntegral(Integer uid, Integer type, String date, List<String> linkTypeList);

    /**
     * H5用户积分列表
     * @param uid 用户uid
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserIntegralRecord> findUserIntegralRecordList(Integer uid, PageParamRequest pageParamRequest);

    /**
     * 获取用户冻结的积分
     * @param uid 用户uid
     * @return 积分数量
     */
    Integer getFrozenIntegralByUid(Integer uid);
}
