package com.qxkj.service.service;

import com.qxkj.common.model.chain.ChainAuditLog;
import com.qxkj.common.model.chain.ChainCommission;
import com.qxkj.common.model.chain.ChainDividendRecord;
import com.qxkj.common.model.chain.ChainNotifyLog;
import com.qxkj.common.model.chain.ChainProduct;
import com.qxkj.common.model.chain.ChainRewardRule;
import com.qxkj.common.model.chain.ChainSlideLog;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.user.User;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.ChainRelationUpdateRequest;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.response.ChainIdentityResponse;
import com.qxkj.common.response.ChainTeamNodeResponse;

import java.util.List;
import java.util.Map;

/**
 * 2+1 链动核心服务
 */
public interface ChainService {

    boolean isEnabled();

    /** 订单是否包含代理礼包商品 */
    boolean isChainGiftOrder(StoreOrder storeOrder);

    /** 礼包订单是否跳过普通分销/团队奖 */
    boolean shouldSkipNormalBrokerage(StoreOrder storeOrder);

    /**
     * 绑定链动上级（走人留人安置）。inviteUid 为分享/邀请人。
     * @return true 绑定成功或已绑定
     */
    boolean bindParent(Integer userId, Integer inviteUid);

    /** 支付成功：售后保护期为 0 时直接结算，否则仅标记等待定时任务结算 */
    List<UserBrokerageRecord> onOrderPaid(StoreOrder storeOrder);

    /** 结算链动礼包订单：门槛校验 + 升代理 + 升老板 + 分润 + 帮扶推进（幂等） */
    List<UserBrokerageRecord> settleChainOrder(StoreOrder storeOrder);

    /** 售后保护期到期后的补偿结算（定时任务）：返回本次结算单数 */
    int processAfterSaleOrders();

    /** 订单完成：有效时机/到账时机=完成时处理 */
    void onOrderComplete(StoreOrder storeOrder);

    /** 退款：佣金流水已由主流程失效；此处处理身份/有效性/链动流水状态 */
    void onOrderRefund(StoreOrder storeOrder);

    ChainIdentityResponse myIdentity(Integer uid);

    ChainTeamNodeResponse myTeam(Integer uid, Integer depth);

    Map<String, Object> commissionSummary(Integer uid);

    CommonPage<ChainCommission> commissionPage(Integer uid, String type, PageParamRequest pageParamRequest);

    /** 推广海报（含链动邀请链接） */
    Map<String, Object> promotePoster(Integer uid);

    /** 规则说明 */
    String rulesContent();

    /** 规则页元信息：级别名称 / 激活门槛 / 关键配置（会员端展示用） */
    Map<String, Object> rulesMeta();

    /** 管理端 */
    Map<String, Object> getConfig();

    void saveConfig(Map<String, String> config);

    List<ChainRewardRule> listRules();

    void saveRules(List<ChainRewardRule> rules);

    /** 链动产品分页 */
    CommonPage<ChainProduct> chainProductPage(String keywords, Integer status, PageParamRequest page);

    /** 新增/更新链动产品（按商品ID唯一） */
    void saveChainProduct(ChainProduct product);

    void deleteChainProduct(Integer id);

    CommonPage<Map<String, Object>> adminUserPage(String keywords, Integer identity, Integer inviterUid, PageParamRequest page);

    void adminUpdateRelation(ChainRelationUpdateRequest request, String operator);

    CommonPage<ChainSlideLog> slideLogPage(PageParamRequest page);

    CommonPage<ChainCommission> adminCommissionPage(String orderId, String type, PageParamRequest page);

    /** 导出佣金 Excel，返回文件相对路径 */
    String exportCommission(String orderId, String type);

    CommonPage<ChainAuditLog> auditLogPage(PageParamRequest page);

    /** 后台用户链动详情 */
    Map<String, Object> userChainDetail(Integer uid);

    Map<String, Object> dashboard();

    CommonPage<ChainNotifyLog> notifyPage(Integer uid, PageParamRequest page);

    void markNotifyRead(Integer uid, Integer id);

    int unreadNotifyCount(Integer uid);

    /** 执行指定账期团队分红，period=yyyy-MM，空则上月 */
    int runDividend(String period);

    CommonPage<ChainDividendRecord> dividendPage(String period, PageParamRequest page);
}
