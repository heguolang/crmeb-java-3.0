package com.zbkj.service.service;

import com.zbkj.common.model.stock.StockOrder;
import com.zbkj.common.model.stock.StockReward;
import com.zbkj.common.model.stock.StockWithdraw;
import com.zbkj.common.page.CommonPage;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.request.StockRequests;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * 订货系统-奖励/奖金/提现/业绩/消息服务
 */
public interface StockRewardService {

    // ==================== 奖励核算 ====================

    /**
     * 订单完成奖励核算：差价 + 级差 + 平级
     * 差价：直接上级赚取（上级拿价-下级拿价）×数量
     * 级差：沿上级链，按团队业绩阶梯差额比例×本单金额
     * 平级：上级与其上级同层级时，向上拿平级比例×本单金额
     */
    void settleOrderReward(StockOrder order);

    /** 换货差价奖励：换货人补付的差价按 stock_exchange_diff_parent_rate 比例奖励给其直接上级（幂等） */
    void settleExchangeDiffReward(com.zbkj.common.model.stock.StockExchange exchange);

    /**
     * 阶梯业绩奖励结算：按周期（1=月度 2=季度 3=年度）重算指定周期内全部代理的阶梯奖励。
     * month 为该周期内任一月份（yyyy-MM），后端自动归集周期起止；幂等可重复执行。
     */
    Boolean settleLadderReward(Integer type, String month);

    // ==================== 会员端 ====================

    /** 我的奖金中心（余额/累计/待审核提现/各类明细统计） */
    HashMap<String, Object> getMyBonus(Integer uid);

    /** 我的奖金明细 */
    CommonPage<StockReward> getMyRewardList(Integer uid, Integer type, PageParamRequest page);

    /** 我的业绩（个人/团队，时间范围） */
    HashMap<String, Object> getMyPerformance(Integer uid, String dateLimit);

    /**
     * 我的业绩订单明细（来源可在列表上区分个人业绩/团队业绩）
     *
     * @param uid       当前用户
     * @param dateLimit 统计范围 yyyy-MM
     * @param source    业绩来源：1=个人业绩 2=团队业绩 null/其它=两者都返回
     * @param page      分页
     */
    CommonPage<StockOrder> getMyPerformanceOrderList(Integer uid, String dateLimit, Integer source, PageParamRequest page);

    /** 申请提现 */
    Boolean applyWithdraw(Integer uid, BigDecimal price, String mark);

    /** 我的提现记录 */
    CommonPage<StockWithdraw> getMyWithdrawList(Integer uid, PageParamRequest page);

    /** 我的消息列表 */
    CommonPage<com.zbkj.common.model.stock.StockNotice> getMyNoticeList(Integer uid, PageParamRequest page);

    /** 标记消息已读 */
    Boolean readNotice(Integer uid, Integer noticeId);

    /** 未读消息数 */
    Long unreadNoticeCount(Integer uid);

    /** 一键全部已读，返回处理条数 */
    Integer readAllNotices(Integer uid);

    // ==================== 后台 ====================

    /** 奖金明细列表 */
    CommonPage<StockReward> getAdminRewardList(Integer uid, Integer type, String orderNo, PageParamRequest page);

    /** 提现列表 */
    CommonPage<StockWithdraw> getAdminWithdrawList(Integer status, PageParamRequest page);

    /** 提现审核：1=打款 -1=驳回 */
    Boolean auditWithdraw(Integer withdrawId, StockRequests.StockWithdrawAuditRequest request);

    /** 订货报表汇总 */
    HashMap<String, Object> getReport(Integer uid, String dateLimit, PageParamRequest page);

    /** 发送站内消息 */
    void sendNotice(Integer uid, Integer type, String title, String content);
}
