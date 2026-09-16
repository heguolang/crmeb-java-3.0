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

    /** 级差月结：按月重算指定月份全部代理级差奖励 */
    Boolean monthlySettle(String month);

    // ==================== 会员端 ====================

    /** 我的奖金中心（余额/累计/待审核提现/各类明细统计） */
    HashMap<String, Object> getMyBonus(Integer uid);

    /** 我的奖金明细 */
    CommonPage<StockReward> getMyRewardList(Integer uid, Integer type, PageParamRequest page);

    /** 我的业绩（个人/团队，时间范围） */
    HashMap<String, Object> getMyPerformance(Integer uid, String dateLimit);

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

    // ==================== 后台 ====================

    /** 奖金明细列表 */
    CommonPage<StockReward> getAdminRewardList(Integer uid, Integer type, String orderNo, PageParamRequest page);

    /** 提现列表 */
    CommonPage<StockWithdraw> getAdminWithdrawList(Integer status, PageParamRequest page);

    /** 提现审核：1=打款 -1=驳回 */
    Boolean auditWithdraw(Integer withdrawId, StockRequests.StockWithdrawAuditRequest request);

    /** 订货报表汇总 */
    HashMap<String, Object> getReport(String dateLimit, PageParamRequest page);

    /** 发送站内消息 */
    void sendNotice(Integer uid, Integer type, String title, String content);
}
