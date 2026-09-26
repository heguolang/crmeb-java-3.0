package com.qxkj.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qxkj.common.model.order.StoreOrder;
import com.qxkj.common.model.system.DistributorLevel;
import com.qxkj.common.model.user.User;
import com.qxkj.common.request.DistributorLevelRequest;

import java.util.List;

/**
 * 分销商等级 Service
 */
public interface DistributorLevelService extends IService<DistributorLevel> {

    /**
     * 分销商等级列表（按等级权重升序）
     */
    List<DistributorLevel> getList();

    /**
     * 分销商等级详情
     *
     * @param id 等级id
     */
    DistributorLevel getLevelInfo(Integer id);

    /**
     * 新增分销商等级
     */
    Boolean saveLevel(DistributorLevelRequest request);

    /**
     * 更新分销商等级
     *
     * @param id 等级id
     */
    Boolean updateLevel(Integer id, DistributorLevelRequest request);

    /**
     * 删除分销商等级（逻辑删除）
     *
     * @param id 等级id
     */
    Boolean deleteLevel(Integer id);

    /**
     * 启用/隐藏分销商等级
     *
     * @param id     等级id
     * @param isShow 是否显示
     */
    Boolean updateShow(Integer id, Boolean isShow);

    // ==================== 统计与自动升级 ====================

    /**
     * 订单支付成功：累加分销商等级统计（支付口径）并触发升级判定
     */
    Boolean processOnOrderPaid(StoreOrder storeOrder);

    /**
     * 订单完成：不重复累加金额，仅重试一次升级判定（人数类条件可能已变化）
     */
    Boolean processOnOrderComplete(StoreOrder storeOrder);

    /**
     * 订单退款：回退统计金额（只升不降，等级不回退）
     */
    Boolean rollbackOnRefund(StoreOrder storeOrder);

    /**
     * 计算用户当前满足的最高分销商等级，都不满足返回 null
     */
    DistributorLevel resolveMatchedLevel(User user);

    /**
     * 批量同步分销商等级（只升不降）
     */
    Boolean syncDistributorLevels(List<Integer> uids);

    /**
     * 全量重算指定用户的统计与等级（后台修复历史数据用）
     */
    Boolean recalcUser(Integer uid);

    /**
     * 按分销商等级取返佣比例（%），未启用或无等级返回 null
     *
     * @param uid            用户id
     * @param brokerageLevel 0=自购 1=一级 2=二级
     */
    Integer getBrokerageRate(Integer uid, Integer brokerageLevel);

    /**
     * 参与升级判定的等级列表（未删除，按权重升序）
     */
    List<DistributorLevel> getUsableList();
}
