package com.qxkj.service.service.impl;

import com.qxkj.common.constants.Constants;
import com.qxkj.common.response.HomeOperatingDataResponse;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.common.response.HomeRateResponse;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
public class HomeServiceImpl implements HomeService {

    private static final Logger logger = LoggerFactory.getLogger(HomeServiceImpl.class);

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserVisitRecordService userVisitRecordService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private UserRechargeService userRechargeService;

    @Autowired
    private UserBrokerageRecordService brokerageRecordService;

    @Autowired
    private UserExtractService userExtractService;

    /**
     * 用户曲线图
     * @author Mr.Zhang
     * @since 2020-05-16
     */
    @Override
    public Map<Object, Object> chartUser() {
        return dataFormat(userService.getAddUserCountGroupDate(Constants.SEARCH_DATE_LATELY_30), Constants.SEARCH_DATE_LATELY_30);
    }

    /**
     * 订单量趋势
     * @author Mr.Zhang
     * @since 2020-05-16
     */
    @Override
    public Map<String, Object> chartOrder() {
        Map<String, Object> map = new HashMap<>();

        List<StoreOrder> list = storeOrderService.getOrderGroupByDate(Constants.SEARCH_DATE_LATELY_30, Constants.NUM_TEN);

        map.put("quality",
                dataFormat(getOrderCountGroupByDate(list), Constants.SEARCH_DATE_LATELY_30)
        );
        map.put("price",
                dataFormat(getOrderPriceGroupByDate(list), Constants.SEARCH_DATE_LATELY_30)
        );
        return map;
    }

    /**
     * 按开始结束时间查询每日新增订单数量
     * @param list List<StoreOrder> 时间范围
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return HashMap<String, Object>
     */
    private Map<Object, Object> getOrderCountGroupByDate(List<StoreOrder> list) {
        Map<Object, Object> map = new HashMap<>();

        if (list.size() < 1) {
            return map;
        }

        for (StoreOrder storeOrder : list) {
            map.put(storeOrder.getOrderId(), storeOrder.getId());
        }

        return map;
    }

    /**
     * 按开始结束时间查询每日新增订单销售额
     * @param list List<StoreOrder> 时间范围
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return HashMap<String, Object>
     */
    private Map<Object, Object> getOrderPriceGroupByDate(List<StoreOrder> list) {
        Map<Object, Object> map = new HashMap<>();

        if (list.size() < 1) {
            return map;
        }

        for (StoreOrder storeOrder : list) {
            map.put(storeOrder.getOrderId(), storeOrder.getPayPrice());
        }

        return map;
    }

    /**
     * 日期和数量格式化
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return Map<String, Integer>
     */
    private Map<Object, Object> dataFormat(Map<Object, Object> countGroupDate, String dateLimit) {
        Map<Object, Object> map = new LinkedHashMap<>();
        List<String> listDate = QianxuDateUtil.getListDate(dateLimit);

        String[] weekList = new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        int i = 0;
        for (String date : listDate) {
            Object count = 0;
            if (countGroupDate.containsKey(date)) {
                count = countGroupDate.get(date);
            }
            String key;

            //周格式化
            switch(dateLimit) {
                //格式化周
                case Constants.SEARCH_DATE_WEEK:
                case Constants.SEARCH_DATE_PRE_WEEK:
                    key = weekList[i];
                    break;
                    //格式化月
                case Constants.SEARCH_DATE_PRE_MONTH:
                case Constants.SEARCH_DATE_MONTH:
                    key = i + 1 + "";
                    break;
                    //默认显示两位日期
                default:
                    key = date.substring(5, 10);
            }
            map.put(key, count);
            i++;
        }
        return map;
    }

    /**
     * 日期和数量格式化
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return Map<String, Integer>
     */
    private Map<Object, Object> dataFormatYear(Map<Object, Object> countGroupDate, String dateLimit) {
        Map<Object, Object> map = new LinkedHashMap<>();
        List<Object> listDate = new ArrayList<>();
        String year = "";
        if (dateLimit.equals(Constants.SEARCH_DATE_YEAR)) {
            year = QianxuDateUtil.nowYear();
        }

        if (dateLimit.equals(Constants.SEARCH_DATE_PRE_YEAR)) {
            year = QianxuDateUtil.lastYear();
        }

        //处理年
        //12个月份数据
        for (int i = 1; i <= 12; i++) {
            String month = i + "";
            if (i < 10) {
                month = "0" + i;
            }
            listDate.add(year + "-" + month);
        }

        String[] monthList = new String[]{"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月" , "十一月", "十二月" };

        int i = 0;
        for (Object date : listDate) {
            Object count = 0;
            if (countGroupDate.containsKey(date)) {
                count = countGroupDate.get(date);
            }
            map.put(monthList[i], count);
            i++;
        }
        return map;
    }

    /**
     * 用户购买统计
     * @author Mr.Zhang
     * @since 2020-05-16
     */
    @Override
    public Map<String, Integer> chartUserBuy() {
        Map<String, Integer> map = new HashMap<>();
        //未消费用户
        map.put("zero", userService.getCountByPayCount(Constants.NUM_ZERO, Constants.NUM_ZERO));

        //消费一次用户
        map.put("one", userService.getCountByPayCount(Constants.NUM_ONE, Constants.NUM_ONE));

        //留存客户
        map.put("history", userService.getCountByPayCount(Constants.NUM_TWO, Constants.NUM_THREE));

        //回流客户
        map.put("back", userService.getCountByPayCount(Constants.NUM_ONE, Constants.EXPORT_MAX_LIMIT));

        return map;
    }

    /**
     * 周订单量趋势
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> chartOrderInWeek() {
        return returnOrderDate(Constants.SEARCH_DATE_WEEK, Constants.SEARCH_DATE_PRE_WEEK, Constants.NUM_TEN);


    }

    /**
     * 月订单量趋势
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> chartOrderInMonth() {
        return returnOrderDate(Constants.SEARCH_DATE_MONTH, Constants.SEARCH_DATE_PRE_MONTH, Constants.NUM_TEN);
    }

    /**
     * 年订单量趋势
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> chartOrderInYear() {
        Map<String, Object> map = new HashMap<>();

        //查询本年订单量
        List<StoreOrder> list = storeOrderService.getOrderGroupByDate(Constants.SEARCH_DATE_YEAR, Constants.NUM_SEVEN);

        map.put("quality",
                dataFormatYear(getOrderCountGroupByDate(list), Constants.SEARCH_DATE_YEAR)
        );
        map.put("price",
                dataFormatYear(getOrderPriceGroupByDate(list), Constants.SEARCH_DATE_YEAR)
        );

        //查询上年订单量
        List<StoreOrder> preList = storeOrderService.getOrderGroupByDate(Constants.SEARCH_DATE_PRE_YEAR, Constants.NUM_SEVEN);

        map.put("preQuality",
                dataFormatYear(getOrderCountGroupByDate(preList), Constants.SEARCH_DATE_PRE_YEAR)
        );
        map.put("prePrice",
                dataFormatYear(getOrderPriceGroupByDate(preList), Constants.SEARCH_DATE_PRE_YEAR)
        );

        return map;
    }

    /**
     * 首页数据
     * 今日/昨日
     * 销售额
     * 用户访问量
     * 订单量
     * 新增用户
     * @return HomeRateResponse
     */
    @Override
    public HomeRateResponse indexDate() {
        String today = cn.hutool.core.date.DateUtil.date().toString("yyyy-MM-dd");
        String yesterday = cn.hutool.core.date.DateUtil.yesterday().toString("yyyy-MM-dd");
        HomeRateResponse response = new HomeRateResponse();
        // ↓↓↓ 原有 4 个指标：算法与字段一字未改，保持向后兼容 ↓↓↓
        response.setSales(storeOrderService.getPayOrderAmountByDate(today));
        response.setYesterdaySales(storeOrderService.getPayOrderAmountByDate(yesterday));
        response.setPageviews(userVisitRecordService.getPageviewsByDate(today));
        response.setYesterdayPageviews(userVisitRecordService.getPageviewsByDate(yesterday));
        response.setOrderNum(storeOrderService.getOrderNumByDate(today));
        response.setYesterdayOrderNum(storeOrderService.getOrderNumByDate(yesterday));
        response.setNewUserNum(userService.getRegisterNumByDate(today));
        response.setYesterdayNewUserNum(userService.getRegisterNumByDate(yesterday));

        // ↓↓↓ 2026-10-04 新增：业绩统计（纯增量，老字段不受影响）↓↓↓
        fillPerformance(response);
        return response;
    }

    /**
     * 填充业绩统计字段（首页 8 张卡的数据源）
     *
     * 口径（用户已确认）：**只算商城订单 eb_store_order，且只算 paid=1 已付款订单**。
     * 时间维度：累计不限 / 本月与本年按 create_time；环比基准为「上月同期」。
     *
     * 三个周期各查一次聚合，避免为了出图反复打库：
     *   累计 = startDate=null, endDate=null
     *   本月 = 当月 1 号 ~ 今天
     *   上月同期 = 上月 1 号 ~ 上月与今天同号（**不是上月全月**，否则月中环比恒降）
     *   本年 = 1 月 1 日 ~ 今天
     */
    private void fillPerformance(HomeRateResponse response) {
        try {
            // ---- 累计 ----
            Map<String, Object> total = storeOrderService.getHomePerformance(null, null);
            BigDecimal totalGmv = toDec(total, "totalGmv");
            Integer totalOrderNum = toInt(total, "orderNum");
            BigDecimal refundAmount = toDec(total, "refundAmount");
            BigDecimal discountAmount = toDec(total, "discountAmount");

            response.setTotalGmv(totalGmv);
            response.setTotalOrderNum(totalOrderNum);
            response.setRefundAmount(refundAmount);
            response.setDiscountAmount(discountAmount);
            response.setRefundCount(toInt(total, "refundCount"));
            response.setAlterPriceCount(toInt(total, "alterPriceNum"));
            response.setPaidUserCount(toInt(total, "paidUserCount"));
            // 净业绩 = GMV - 退款。理论上不会为负（退款必属某笔订单），
            // 但历史数据脏或退款金额被改大时可能出现负数，负数会显示成 "-¥xxx" 更要命，
            // 所以这里做一次兜底钳制为 0，并在注释里留痕。
            response.setNetGmv(maxZero(totalGmv.subtract(refundAmount)));
            response.setAvgOrderPrice(divide(totalGmv, totalOrderNum));
            // 退款率 = 退款单数 / 总单数 × 100，保留两位
            response.setRefundRate(totalOrderNum == null || totalOrderNum == 0
                    ? BigDecimal.ZERO
                    : toDec(total, "refundCount")
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalOrderNum), 2, java.math.RoundingMode.HALF_UP));

            // ---- 本月 / 上月同期（环比基数）----
            String today = cn.hutool.core.date.DateUtil.date().toString("yyyy-MM-dd");
            String monthStart = today.substring(0, 7) + "-01";
            // 环比基数取「上月同期」而不是「上月全月」——
            // 本月只过了 10 天却跟上月 30 天比，环比会天然偏低（月中恒降），数字没法看。
            // 同期口径 = 上月 1 号 ~ 上月与今天相同的日号，跨月边界（如今天 31 号、上月 30 天）
            // 由 offsetMonth 自然收敛到上月的最后一天，不会溢出。
            String lastMonthStart = cn.hutool.core.date.DateUtil.offsetMonth(
                    cn.hutool.core.date.DateUtil.parse(monthStart), -1).toString("yyyy-MM-dd");
            String lastMonthEnd = cn.hutool.core.date.DateUtil.offsetMonth(
                    cn.hutool.core.date.DateUtil.parse(today), -1).toString("yyyy-MM-dd");

            Map<String, Object> month = storeOrderService.getHomePerformance(monthStart, today);
            Map<String, Object> lastMonth = storeOrderService.getHomePerformance(lastMonthStart, lastMonthEnd);

            BigDecimal monthGmv = toDec(month, "totalGmv");
            Integer monthOrderNum = toInt(month, "orderNum");
            BigDecimal lastMonthGmv = toDec(lastMonth, "totalGmv");
            Integer lastMonthOrderNum = toInt(lastMonth, "orderNum");

            response.setMonthGmv(monthGmv);
            response.setMonthOrderNum(monthOrderNum);
            response.setLastMonthGmv(lastMonthGmv);
            response.setLastMonthOrderNum(lastMonthOrderNum);
            response.setLastMonthAvgOrderPrice(divide(lastMonthGmv, lastMonthOrderNum));
            response.setMonthGmvRatio(storeOrderService.calcGrowthRate(monthGmv, lastMonthGmv));
            response.setMonthOrderRatio(storeOrderService.calcGrowthRate(
                    new BigDecimal(monthOrderNum == null ? 0 : monthOrderNum),
                    new BigDecimal(lastMonthOrderNum == null ? 0 : lastMonthOrderNum)));

            // ---- 本年 ----
            String yearStart = today.substring(0, 4) + "-01-01";
            response.setYearGmv(toDec(storeOrderService.getHomePerformance(yearStart, today), "totalGmv"));
        } catch (Exception e) {
            // 首页业绩统计是「锦上添花」，绝不能因为它把整个首页接口打挂（旧 4 个指标要照常返回）
            logger.error("[首页业绩统计] 计算失败，首页将只显示原有 4 个指标", e);        }
    }

    /** 聚合结果取 BigDecimal，null / 非数字给 0 */
    private BigDecimal toDec(Map<String, Object> row, String key) {
        if (row == null) {
            return BigDecimal.ZERO;
        }
        Object v = row.get(key);
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** 聚合结果取 Integer，null / 非数字给 0 */
    private Integer toInt(Map<String, Object> row, String key) {
        if (row == null) {
            return 0;
        }
        Object v = row.get(key);
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return new BigDecimal(v.toString()).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 除法防零：分母为 0 时给 0，不要返回 null 或抛 ArithmeticException */
    private BigDecimal divide(BigDecimal a, Integer b) {
        if (a == null || b == null || b == 0) {
            return BigDecimal.ZERO;
        }
        return a.divide(new BigDecimal(b), 2, java.math.RoundingMode.HALF_UP);
    }

    /** 负数钳制为 0 */
    private BigDecimal maxZero(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        return v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
    }

    /**
     * 经营数据：
     * 1.待发货订单，2.退款中订单，3.待核销订单，4.库存预警
     * 5.上架商品数，6.库存中商品数，7.提现待审核，8.账户充值
     * @return HomeOperatingDataResponse
     */
    @Override
    public HomeOperatingDataResponse operatingData() {
        HomeOperatingDataResponse response = new HomeOperatingDataResponse();
        response.setNotShippingOrderNum(storeOrderService.getNotShippingNum());
        response.setRefundingOrderNum(storeOrderService.getRefundingNum());
        response.setNotWriteOffOrderNum(storeOrderService.getNotWriteOffNum());
        response.setVigilanceInventoryNum(storeProductService.getVigilanceInventoryNum());
        response.setOnSaleProductNum(storeProductService.getOnSaleNum());
        response.setNotSaleProductNum(storeProductService.getNotSaleNum());
        response.setNotAuditNum(userExtractService.getNotAuditNum());
        BigDecimal totalRechargeAmount = userRechargeService.getTotalPrice();
        BigDecimal BrokerageToYueAmount = brokerageRecordService.getTotalYuePrice();
        response.setTotalRechargeAmount(totalRechargeAmount.add(BrokerageToYueAmount));
        return response;
    }

    /**
     * 组装订单统计返回数据
     * @author Mr.Zhang
     * @since 2020-05-16
     * @return Map<String, Object>
     */
    private Map<String, Object> returnOrderDate(String dateLimit, String preDateLimit, int leftTime) {
        Map<String, Object> map = new HashMap<>();

        //查询本周周订单量
        List<StoreOrder> list = storeOrderService.getOrderGroupByDate(dateLimit, leftTime);

        map.put("quality",
                dataFormat(getOrderCountGroupByDate(list), dateLimit)
        );
        map.put("price",
                dataFormat(getOrderPriceGroupByDate(list), dateLimit)
        );

        //查询上周周订单量
        List<StoreOrder> preList = storeOrderService.getOrderGroupByDate(preDateLimit, leftTime);

        map.put("preQuality",
                dataFormat(getOrderCountGroupByDate(preList), preDateLimit)
        );
        map.put("prePrice",
                dataFormat(getOrderPriceGroupByDate(preList), preDateLimit)
        );

        return map;
    }

}
