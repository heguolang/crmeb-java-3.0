package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.request.FundsMonitorRequest;
import com.qxkj.common.request.FundsMonitorSearchRequest;
import com.qxkj.common.response.MonitorResponse;
import com.qxkj.common.request.StoreOrderRefundRequest;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBill;

import java.math.BigDecimal;
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
public interface UserBillService extends IService<UserBill> {

    /**
     * 列表
     *
     * @param request          请求参数
     * @param pageParamRequest 分页类参数
     * @return List<UserBill>
     */
    List<UserBill> getList(FundsMonitorSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增/消耗  总金额
     *
     * @param pm       Integer 0 = 支出 1 = 获得
     * @param userId   Integer 用户uid
     * @param category String 类型
     * @param date     String 时间范围
     * @param type     String 小类型
     * @return UserBill
     */
    BigDecimal getSumBigDecimal(Integer pm, Integer userId, String category, String date, String type);

    /**
     * 保存退款日志
     *
     * @return boolean
     */
    Boolean saveRefundBill(StoreOrderRefundRequest request, User user);

    /**
     * 资金监控
     *
     * @param request          查询参数
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoring(FundsMonitorRequest request);

    /**
     * 资金监控 - 不限（UNION 三表：余额 + 佣金 + 积分）
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoringAll(FundsMonitorRequest request);

    /**
     * 积分资金流水监控
     *
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoringIntegral(FundsMonitorRequest request);

    /**
     * 佣金资金流水监控（eb_user_brokerage_record 全量 + eb_user_bill 后台充减）
     *
     * @param request 查询参数
     * @return PageInfo
     */
    PageInfo<MonitorResponse> fundMonitoringBrokerage(FundsMonitorRequest request);

    /**
     * 用户账单记录（现金）
     *
     * @param uid  用户uid
     * @param type 记录类型：all-全部，expenditure-支出，income-收入
     * @return PageInfo
     */
    PageInfo<UserBill> nowMoneyBillRecord(Integer uid, String type, PageParamRequest pageRequest);
}
