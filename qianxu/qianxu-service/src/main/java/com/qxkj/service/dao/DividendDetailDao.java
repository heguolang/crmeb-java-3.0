package com.qxkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qxkj.common.model.dividend.DividendDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 全球分红发放明细 Dao
 */
public interface DividendDetailDao extends BaseMapper<DividendDetail> {

    /**
     * 统计期内的分红基数（订单口径：已支付、未删除、支付时间在期内）
     * baseType=1 订单总业绩：Σ pay_price
     * baseType=2 订单利润：Σ pay_num × (订单行单价 - 当前SKU成本价)
     */
    @Select("SELECT COALESCE(SUM(o.pay_price), 0) AS baseAmount, COUNT(*) AS orderCount " +
            "FROM eb_store_order o " +
            "WHERE o.paid = 1 AND o.is_del = 0 " +
            "AND o.pay_time >= #{startTime} AND o.pay_time < #{endTime}")
    Map<String, Object> statPerformance(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select("SELECT COALESCE(SUM(oi.pay_num * (oi.price - COALESCE(sku.cost, 0))), 0) AS baseAmount, " +
            "COUNT(DISTINCT o.id) AS orderCount " +
            "FROM eb_store_order o " +
            "JOIN eb_store_order_info oi ON oi.order_id = o.id " +
            "LEFT JOIN eb_store_product_attr_value sku ON sku.id = oi.attr_value_id " +
            "WHERE o.paid = 1 AND o.is_del = 0 " +
            "AND o.pay_time >= #{startTime} AND o.pay_time < #{endTime}")
    Map<String, Object> statProfit(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
