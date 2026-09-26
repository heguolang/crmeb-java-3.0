package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.record.TradingDayRecord;

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
public interface TradingDayRecordService extends IService<TradingDayRecord> {

    /**
     * 根据日期获取记录
     * @param date 日期，yyyy-MM-dd
     * @return TradingDayRecord
     */
    TradingDayRecord getByDate(String date);

    /**
     * 获取时间段内的数据
     * @param startDate 日期，yyyy-MM-dd
     * @param endDate 日期，yyyy-MM-dd
     * @return TradingDayRecord
     */
    TradingDayRecord getByTimeInterval(String startDate, String endDate);

    /**
     * 根据时间范围返回趋势数据
     * @param startDate 开始日期,格式yyyy-MM-dd
     * @param endDate 结束日期,格式yyyy-MM-dd
     * @return List<TradingDayRecord>
     */
    List<TradingDayRecord> findTrendDataOfBetween(String startDate, String endDate);
}
