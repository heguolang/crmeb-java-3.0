package com.qxkj.service.service;

import com.qxkj.common.model.agent.Agent;
import com.qxkj.common.model.agent.AgentChangeLog;
import com.qxkj.common.model.agent.AgentReward;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.user.UserBrokerageRecord;
import com.qxkj.common.page.CommonPage;
import com.qxkj.common.request.AgentAdminRequest;
import com.qxkj.common.request.AgentApplyRequest;
import com.qxkj.common.request.PageParamRequest;

import java.util.HashMap;
import java.util.List;

/**
 * 区域代理服务接口
 */
public interface AgentService {

    // ==================== 后台 ====================

    /** 代理列表 */
    CommonPage<Agent> getAdminList(String keywords, Integer level, Integer status, PageParamRequest pageParamRequest);

    /** 后台直接设置代理 */
    Boolean saveAdminAgent(AgentAdminRequest request);

    /** 后台修改代理（比例/区域/状态等） */
    Boolean updateAdminAgent(AgentAdminRequest request);

    /** 审核代理：status=1通过 2拒绝 */
    Boolean auditAgent(Integer id, Integer status);

    /** 删除代理 */
    Boolean deleteAgent(Integer id);

    /** 代理设置 */
    HashMap<String, Object> getSetting();

    /** 保存代理设置 */
    Boolean updateSetting(HashMap<String, Object> settingMap);

    /** 奖励明细列表（后台） */
    CommonPage<AgentReward> getRewardList(Integer uid, String orderId, Integer status, PageParamRequest pageParamRequest);

    /** 代理商变更记录列表（后台） */
    CommonPage<AgentChangeLog> getChangeLogList(Integer uid, Integer type, PageParamRequest pageParamRequest);

    // ==================== 会员端 ====================

    /** 申请成为区域代理 */
    Boolean apply(Integer uid, AgentApplyRequest request);

    /** 我的代理信息（含统计） */
    HashMap<String, Object> getMyAgentInfo(Integer uid);

    /** 我的奖励明细（会员端） */
    CommonPage<AgentReward> getMyRewardList(Integer uid, PageParamRequest pageParamRequest);

    // ==================== 结算 ====================

    /**
     * 订单支付成功时计算区域代理奖励（生成待入账佣金记录）
     * @return 待保存的佣金记录列表（可能为空）
     */
    List<UserBrokerageRecord> assignAgentBrokerage(StoreOrder storeOrder);

    /**
     * 按佣金记录状态同步订单的代理奖励明细状态
     * （支付到账/订单完成入账后调用）
     */
    Boolean syncRewardStatus(String orderId);
}
