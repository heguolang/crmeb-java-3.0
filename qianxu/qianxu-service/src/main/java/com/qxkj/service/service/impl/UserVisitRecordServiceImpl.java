package com.qxkj.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qxkj.common.model.record.UserVisitRecord;
import com.qxkj.service.dao.UserVisitRecordDao;
import com.qxkj.service.service.UserVisitRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
@Service
public class UserVisitRecordServiceImpl extends ServiceImpl<UserVisitRecordDao, UserVisitRecord> implements UserVisitRecordService {

    @Resource
    private UserVisitRecordDao dao;

    /**
     * 通过日期获取浏览量
     * @param date 日期
     * @return Integer
     */
    @Override
    public Integer getPageviewsByDate(String date) {
        QueryWrapper<UserVisitRecord> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.eq("date", date);
        return dao.selectCount(wrapper);
    }

    /**
     * 通过时间段获取浏览量
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    @Override
    public Integer getPageviewsByPeriod(String startDate, String endDate) {
        QueryWrapper<UserVisitRecord> wrapper = new QueryWrapper<>();
        wrapper.select("id");
        wrapper.between("date", startDate, endDate);
        return dao.selectCount(wrapper);
    }

    /**
     * 通过日期获取活跃用户数
     * @param date 日期
     * @return Integer
     */
    @Override
    public Integer getActiveUserNumByDate(String date) {
        return dao.getActiveUserNumByDate(date);
    }

    /**
     * 通过时间段获取活跃用户数
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    @Override
    public Integer getActiveUserNumByPeriod(String startDate, String endDate) {
        return dao.getActiveUserNumByPeriod(startDate, endDate);
    }
}

