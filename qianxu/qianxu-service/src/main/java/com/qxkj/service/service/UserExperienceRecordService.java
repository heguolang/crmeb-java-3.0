package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.model.user.UserExperienceRecord;

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
public interface UserExperienceRecordService extends IService<UserExperienceRecord> {

    /**
     * 获取用户经验列表（移动端）
     * @param userId 用户id
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserExperienceRecord> getH5List(Integer userId, PageParamRequest pageParamRequest);

    /**
     * 通过订单编号获取记录
     * @param orderNo 订单编号
     * @param uid uid
     * @return UserExperienceRecord
     */
    UserExperienceRecord getByOrderNoAndUid(String orderNo, Integer uid);

    /**
     * 通过订单编号和关联类型获取记录
     */
    UserExperienceRecord getByOrderNoAndUidAndLinkType(String orderNo, Integer uid, String linkType);

    /**
     * 统计用户交易完成计单记录数
     */
    Integer countCompleteOrderByUid(Integer uid);

    /**
     * 统计用户已付款累计消费金额（经验值）
     */
    Integer sumPaidConsumptionByUid(Integer uid);

    /**
     * 统计用户交易完成累计消费金额（经验值）
     */
    Integer sumCompleteConsumptionByUid(Integer uid);
}
