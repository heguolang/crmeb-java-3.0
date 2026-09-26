package com.qxkj.service.dao;

import com.qxkj.common.model.order.StoreOrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

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
public interface StoreOrderInfoDao extends BaseMapper<StoreOrderInfo> {

    /**
     * 根据时间、商品id获取销售件数
     * @param date 时间，格式'yyyy-MM-dd'
     * @param proId 商品id
     */
    Integer getSalesNumByDateAndProductId(@Param("date") String date, @Param("proId")  Integer proId);

    /**
     * 根据时间、商品id获取销售额
     * @param date 时间，格式'yyyy-MM-dd'
     * @param proId 商品id
     */
    BigDecimal getSalesByDateAndProductId(@Param("date") String date, @Param("proId")  Integer proId);
}
