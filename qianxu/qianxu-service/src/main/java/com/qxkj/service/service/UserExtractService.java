package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.UserExtractRecordResponse;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.finance.UserExtract;
import com.qxkj.common.request.UserExtractRequest;
import com.qxkj.common.request.UserExtractSearchRequest;
import com.qxkj.common.response.BalanceResponse;
import com.qxkj.common.response.UserExtractResponse;

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
public interface UserExtractService extends IService<UserExtract> {

    List<UserExtract> getList(UserExtractSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 提现总金额
     */
    BalanceResponse getBalance(String dateLimit);

    /**
     * 提现总金额
     * @author Mr.Zhang
     * @since 2020-05-11
     * @return BalanceResponse
     */
    BigDecimal getWithdrawn(String startTime,String endTime);

    UserExtractResponse getUserExtractByUserId(Integer userId);

    /**
     * 提现审核
     * @param id    提现申请id
     * @param status 审核状态 -1 未通过 0 审核中 1 已提现
     * @param backMessage   驳回原因
     * @return  审核结果
     */
    Boolean updateStatus(Integer id,Integer status,String backMessage);

    /**
     * 获取提现记录列表
     * @param userId 用户uid
     * @param pageParamRequest 分页参数
     * @return PageInfo
     */
    PageInfo<UserExtractRecordResponse> getExtractRecord(Integer userId, PageParamRequest pageParamRequest);

    BigDecimal getExtractTotalMoney(Integer userId);

    /**
     * 提现申请
     * @return Boolean
     */
    Boolean extractApply(UserExtractRequest request);

    /**
     * 当前是否在可提现时间窗口（佣金）
     * @param throwEx true 时不在窗口抛异常
     */
    boolean checkExtractTimeAllowed(boolean throwEx);

    /**
     * 当前是否在可提现时间窗口
     * @param throwEx true 时不在窗口抛异常
     * @param category brokerage / balance
     */
    boolean checkExtractTimeAllowed(boolean throwEx, String category);

    /**
     * 支持银行列表
     */
    List<String> getSupportBankList();

    /**
     * 可提现星期文案
     */
    String formatWeekdaysTip(String weekdays);

    /**
     * 可提现时段文案
     */
    String formatTimeTip(int startHour, int endHour);

    /**
     * 修改提现申请
     * @param id 申请id
     * @param userExtractRequest 具体参数
     */
    Boolean updateExtract(Integer id, UserExtractRequest userExtractRequest);

    /**
     * 提现申请待审核数量
     * @return Integer
     */
    Integer getNotAuditNum();
}
