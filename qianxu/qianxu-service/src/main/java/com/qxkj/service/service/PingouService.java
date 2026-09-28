package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.pingou.PingouActivity;
import com.qxkj.common.model.pingou.PingouJoin;
import com.qxkj.common.request.PageParamRequest;
import com.qxkj.common.request.PingouActivityRequest;
import com.qxkj.common.request.PingouSearchRequest;
import com.qxkj.common.request.PingouTeamSearchRequest;
import com.qxkj.common.response.PingouFrontDetailResponse;
import com.qxkj.common.response.PingouTeamListResponse;

import java.util.List;

/**
 * 拼购活动 Service
 */
public interface PingouService extends IService<PingouActivity> {

    // ---------------- 后台：活动管理 ----------------

    /**
     * 后台活动分页列表
     */
    List<PingouActivity> getAdminList(PingouSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 新增拼购活动
     */
    boolean saveActivity(PingouActivityRequest request);

    /**
     * 修改拼购活动
     */
    boolean updateActivity(PingouActivityRequest request);

    /**
     * 删除拼购活动（逻辑删除）
     */
    boolean deleteActivity(Integer id);

    /**
     * 启用/禁用活动
     */
    boolean updateActivityStatus(Integer id, boolean status);

    // ---------------- 后台：团记录 ----------------

    /**
     * 后台团记录分页列表
     */
    List<PingouTeamListResponse> getAdminTeamList(PingouTeamSearchRequest request, PageParamRequest pageParamRequest);

    /**
     * 团成员明细（含中奖情况）
     */
    List<PingouTeamListResponse> getTeamDetail(Integer teamId);

    // ---------------- 会员端 ----------------

    /**
     * 前端活动列表（展示中）
     */
    List<PingouActivity> getFrontList(PageParamRequest pageParamRequest);

    /**
     * 前端活动详情（含当前团进度、累计开团次数、最近开奖团）
     */
    PingouFrontDetailResponse getFrontDetail(Integer id);

    /**
     * 前端团列表（按活动）
     */
    List<PingouTeamListResponse> getFrontTeamList(Integer activityId, PageParamRequest pageParamRequest);

    /**
     * 我的拼购参与记录
     */
    PageInfo<PingouJoin> getMyJoins(Integer uid, PageParamRequest pageParamRequest);

    // ---------------- 核心：支付成功参团 / 开奖 ----------------

    /**
     * 订单支付成功后的拼购参团处理（在调用方事务内执行；非拼购商品订单静默跳过，失败抛异常回滚支付事务）
     */
    void onPaySuccess(StoreOrder storeOrder);

    /**
     * 兜底：扫描应开奖未开奖的团并开奖、补发未发奖励
     */
    void checkAndDrawTeams();
}
