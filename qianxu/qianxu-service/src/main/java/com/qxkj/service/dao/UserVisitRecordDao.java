package com.qxkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.model.record.UserVisitRecord;
import org.apache.ibatis.annotations.Param;

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
public interface UserVisitRecordDao extends BaseMapper<UserVisitRecord> {

    /**
     * 获取活跃用户数
     * @param date 日期
     * @return Integer
     */
    Integer getActiveUserNumByDate(@Param("date") String date);

    /**
     * 通过时间段获取活跃用户数
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    Integer getActiveUserNumByPeriod(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
