package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.SpreadCommissionDetailResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.request.BrokerageRecordRequest;
import com.qxkj.common.request.RetailShopStairUserRequest;
import com.qxkj.common.request.TeamBrokerageRecordRequest;
import com.qxkj.common.model.user.UserBrokerageRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
public interface UserBrokerageRecordService extends IService<UserBrokerageRecord> {

    /**
     * 获取记录列表
     * @param linkId 关联id
     * @param linkType 关联类型
     * @return 记录列表
     */
    List<UserBrokerageRecord> findListByLinkIdAndLinkType(String linkId, String linkType);

    /**
     * 获取记录(订单不可用此方法)
     * @param linkId 关联id
     * @param linkType 关联类型
     * @return 记录列表
     */
    UserBrokerageRecord getByLinkIdAndLinkType(String linkId, String linkType);

    /**
     * 佣金解冻
     */
    void brokerageThaw();

    /**
     * 昨天得佣金
     * @param uid 用户uid
     */
    BigDecimal getYesterdayIncomes(Integer uid);

    /**
     * 获取佣金明细列表根据uid
     * @param uid uid
     * @param pageParamRequest 分页参数
     */
    PageInfo<SpreadCommissionDetailResponse> findDetailListByUid(Integer uid, PageParamRequest pageParamRequest);

    /**
     * 获取累计推广条数
     * @param uid 用户uid
     * @return Integer
     */
    Integer getSpreadCountByUid(Integer uid);

    /**
     * 获取推广记录列表
     * @param uid 用户uid
     * @param pageParamRequest 分页参数
     * @return List
     */
    List<UserBrokerageRecord> findSpreadListByUid(Integer uid, PageParamRequest pageParamRequest);

    /**
     * 获取推广记录列表
     * @param request 用户uid
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<UserBrokerageRecord> findAdminSpreadListByUid(RetailShopStairUserRequest request, PageParamRequest pageParamRequest);

    /**
     * 获取月份对应的推广订单数
     * @param uid 用户uid
     * @param monthList 月份列表
     * @return Map
     */
    Map<String, Integer> getSpreadCountByUidAndMonth(Integer uid, List<String> monthList);

    /**
     * 获取佣金排行榜（周、月）
     * @param type week、month
     * @return List
     */
    List<UserBrokerageRecord> getBrokerageTopByDate(String type);

    /**
     * 根据Uid和时间参数获取分佣记录列表
     * @param uid 用户uid
     * @return List
     */
    List<UserBrokerageRecord> getSpreadListByUid(Integer uid);

    /**
     * 佣金总金额（单位时间）
     * @param dateLimit 时间参数
     * @return BigDecimal
     */
    BigDecimal getTotalSpreadPriceBydateLimit(String dateLimit);

    /**
     * 单位时间消耗的佣金
     * @param dateLimit 时间参数
     * @return BigDecimal
     */
    BigDecimal getSubSpreadPriceByDateLimit(String dateLimit);

    /**
     * 获取冻结期佣金
     * @param uid uid
     * @return BigDecimal
     */
    BigDecimal getFreezePrice(Integer uid);

    /**
     * 佣金记录列表
     * @param request 筛选条件
     * @return PageInfo
     */
    PageInfo<UserBrokerageRecord> getAdminList(BrokerageRecordRequest request);

    /**
     * 团队奖资金记录（后台）
     */
    PageInfo<UserBrokerageRecord> getTeamBrokerageAdminList(TeamBrokerageRecordRequest request, PageParamRequest pageParamRequest);

    /**
     * 团队奖资金明细（用户端，按月分组）
     */
    PageInfo<SpreadCommissionDetailResponse> findTeamDetailListByUid(Integer uid, PageParamRequest pageParamRequest);

    /**
     * 用户累计团队奖金额（已完成）
     */
    BigDecimal getTeamBrokerageTotalByUid(Integer uid);

    /**
     * 根据日期获取支付佣金金额（确认到账佣金）
     * @param date 日期，yyyy-MM-dd格式
     * @return BigDecimal
     */
    BigDecimal getBrokerageAmountByDate(String date);

    /**
     * 获取累计佣金转余额金额
     * @return BigDecimal
     */
    BigDecimal getTotalYuePrice();

    /**
     * 订单佣金冻结
     *
     * @param orderNo 订单号
     * @param freezeDay 冻结天数
     * @return
     */
    Boolean brokerageFrozen(String orderNo, Integer freezeDay);
}
