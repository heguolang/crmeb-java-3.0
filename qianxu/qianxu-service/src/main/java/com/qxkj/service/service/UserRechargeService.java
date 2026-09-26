package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.finance.UserRecharge;
import com.qxkj.common.request.UserRechargeSearchRequest;
import com.qxkj.common.response.UserRechargeResponse;

import java.math.BigDecimal;
import java.util.HashMap;
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
public interface UserRechargeService extends IService<UserRecharge> {

    /**
     * 充值记录列表
     * @param request 请求参数
     * @return PageInfo
     */
    PageInfo<UserRechargeResponse> getList(UserRechargeSearchRequest request);

    /**
     * 充值统计
     * @return HashMap
     */
    HashMap<String, BigDecimal> getBalanceList();

    UserRecharge getInfoByEntity(UserRecharge userRecharge);

    /**
     * 根据日期获取充值订单数量
     * @param date 日期，yyyy-MM-dd格式
     * @return Integer
     */
    Integer getRechargeOrderNumByDate(String date);

    /**
     * 根据日期获取充值订单金额
     * @param date 日期，yyyy-MM-dd格式
     * @return BigDecimal
     */
    BigDecimal getRechargeOrderAmountByDate(String date);

    /**
     * 获取总人数
     * @return Integer
     */
    Integer getTotalPeople();

    /**
     * 获取总金额
     * @return BigDecimal
     */
    BigDecimal getTotalPrice();

    /**
     * 根据时间获取充值用户数量
     * @param date 日期
     * @return Integer
     */
    Integer getRechargeUserNumByDate(String date);

    /**
     * 根据时间获取充值用户数量
     * @param startDate 日期
     * @param endDate 日期
     * @return Integer
     */
    Integer getRechargeUserNumByPeriod(String startDate, String endDate);

    /**
     * 获取待上传微信发货管理订单
     */
    List<UserRecharge> findAwaitUploadWechatList();

    /**
     * 获取订单
     * @param outTradeNo 商户系统内部的订单号
     */
    UserRecharge getByOutTradeNo(String outTradeNo);
}
