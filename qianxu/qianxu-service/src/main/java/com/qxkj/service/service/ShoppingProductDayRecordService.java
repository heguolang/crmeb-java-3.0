package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.record.ShoppingProductDayRecord;

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
public interface ShoppingProductDayRecordService extends IService<ShoppingProductDayRecord> {

    /**
     * 根据日期获取
     * @param date 日期
     * @return ShoppingProductDayRecord
     */
    ShoppingProductDayRecord getByDate(String date);

    /**
     * 获取时间区间的数据
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return ShoppingProductDayRecord
     */
    ShoppingProductDayRecord getByTimeInterval(String startDate, String endDate);

    /**
     * 根据时间范围返回趋势数据
     * @param startDate 开始日期,格式yyyy-MM-dd
     * @param endDate 结束日期,格式yyyy-MM-dd
     * @return List<ShoppingProductDayRecord>
     */
    List<ShoppingProductDayRecord> findTrendDataOfBetween(String startDate, String endDate);
}
